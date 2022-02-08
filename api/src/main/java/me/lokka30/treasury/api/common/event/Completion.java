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
    private Collection<Throwable> errors;
    private Consumer<@NotNull Collection<@NotNull Throwable>> completedTask;

    public Completion() {

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
        if (completedTask != null) {
            completedTask.accept(Collections.emptyList());
        }
        latch.countDown();
    }

    public void completeExceptionally(@NotNull Throwable error) {
        if (latch.getCount() == 0) {
            throw new IllegalStateException("Completion already completed");
        }
        Objects.requireNonNull(error, "error");
        if (completedTask != null) {
            completedTask.accept(Collections.singletonList(error));
        } else {
            this.errors = Collections.singletonList(error);
        }
        latch.countDown();
    }

    public void completeExceptionally(@NotNull Collection<@NotNull Throwable> errors) {
        if (latch.getCount() == 0) {
            throw new IllegalStateException("Completion already completed");
        }
        Objects.requireNonNull(errors, "errors");
        if (completedTask != null) {
            completedTask.accept(errors);
        } else {
            this.errors = errors;
        }
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

    public void whenComplete(@Nullable Consumer<@NotNull Collection<@NotNull Throwable>> completedTask) {
        this.completedTask = completedTask;
        if (completedTask != null) {
            if (latch.getCount() == 0) {
                completedTask.accept(getErrors());
            } else {
                try {
                    // todo: this blocks the thread this is called on. shall fix this!!!
                    latch.await();
                    completedTask.accept(getErrors());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
