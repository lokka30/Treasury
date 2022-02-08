/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/lokka30/Treasury>.
 */

package me.lokka30.treasury.api.common.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Completion {

    public static Completion completed() {
        return new Completion(true);
    }

    public static Completion completedExceptionally(@NotNull Throwable error) {
        return new Completion(error);
    }

    public static Completion completedExceptionally(@NotNull Collection<@NotNull Throwable> errors) {
        return new Completion(errors);
    }

    public static Completion join(Completion... other) {
        List<Throwable> errors = new ArrayList<>();
        for (Completion completion : other) {
            completion.waitCompletion();
            if (!completion.getErrors().isEmpty()) {
                errors.addAll(completion.getErrors());
            }
        }
        return errors.isEmpty()
                ? Completion.completed()
                : Completion.completedExceptionally(errors);
    }

    private CountDownLatch latch = new CountDownLatch(1);
    private Executor async;
    private Collection<Throwable> errors;

    Completion(Executor async) {
        this.async = async;
    }

    private Completion(boolean completed) {
        if (completed) {
            this.latch.countDown();
        }
    }

    private Completion(@NotNull Collection<@NotNull Throwable> errors) {
        this.errors = Objects.requireNonNull(errors, "errors");
        this.latch.countDown();
    }

    private Completion(@NotNull Throwable error) {
        this(Collections.singletonList(Objects.requireNonNull(error, "error")));
    }

    public void complete() {
        if (latch.getCount() == 0) {
            throw new IllegalStateException("Completion already completed");
        }
        latch.countDown();
    }

    public void completeExceptionally(@NotNull Throwable error) {
        if (latch.getCount() == 0) {
            throw new IllegalStateException("Completion already completed");
        }
        Objects.requireNonNull(error, "error");
        this.errors = Collections.singletonList(error);
        latch.countDown();
    }

    public void completeExceptionally(@NotNull Collection<@NotNull Throwable> errors) {
        if (latch.getCount() == 0) {
            throw new IllegalStateException("Completion already completed");
        }
        Objects.requireNonNull(errors, "errors");
        this.errors = errors;
        latch.countDown();
    }

    public void waitCompletion() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @NotNull
    public Collection<@NotNull Throwable> getErrors() {
        return errors == null ? Collections.emptyList() : errors;
    }

    public void whenCompleteBlocking(@Nullable Consumer<@NotNull Collection<@NotNull Throwable>> completedTask) {
        if (completedTask != null) {
            if (latch.getCount() == 0) {
                completedTask.accept(getErrors());
            } else {
                try {
                    latch.await();
                    completedTask.accept(getErrors());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void whenCompleteAsync(@Nullable Consumer<@NotNull Collection<@NotNull Throwable>> completedTask) {
        if (completedTask != null) {
            async.execute(() -> {
                if (latch.getCount() == 0) {
                    completedTask.accept(getErrors());
                } else {
                    try {
                        latch.await();
                        completedTask.accept(getErrors());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

}
