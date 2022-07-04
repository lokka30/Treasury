package me.lokka30.treasury.api.common.misc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class to help with {@link CompletableFuture} stuff
 *
 * @author MrIvanPlays
 * @since 1.2.1
 */
public class FutureHelper {

    /**
     * Map, join, filter - this is what the name of the method is.
     * Maps all the futures holding data type A to return a collection of data type B
     * via the specified {@code mapper} {@link Function} and filters them via the specified
     * filter function.
     * <br><b>WARNING: This is a thread unsafe method. ASYNCHRONOUS CALL MANDATORY!!!!</b>
     *
     * @param mapper  mapper
     * @param futures futures of data type A
     * @param <A>     type parameter A
     * @param <B>     type parameter B
     * @return future with collection of data type B
     */
    @NotNull
    public static <A, B> CompletableFuture<Collection<B>> mjf(
            @Nullable Function<A, CompletableFuture<Boolean>> filter,
            @NotNull Function<A, B> mapper,
            @NotNull Collection<CompletableFuture<A>> futures
    ) {
        Objects.requireNonNull(mapper, "mapper");
        Objects.requireNonNull(futures, "futures");
        Collection<B> ret = new HashSet<>();
        for (CompletableFuture<A> future : futures) {
            if (filter != null) {
                A a = future.join();
                if (filter.apply(a).join()) {
                    ret.add(mapper.apply(a));
                }
            } else {
                ret.add(mapper.apply(future.join()));
            }
        }
        return CompletableFuture.completedFuture(ret);
    }

}
