package com.kong.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kong
 */
@Slf4j
public final class AsyncUtil {

    public static <R> CompletableFuture<R> newFailedFuture(Throwable ex) {
        CompletableFuture<R> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

    public static <T> T getValue(CompletableFuture<T> future) {
        try {
            return future.get(10, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            log.error("getValue for async result failed", ex);
        }
        return null;
    }

    private AsyncUtil() {}
}
