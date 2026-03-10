package net.neoforged.neoforge.resource;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A reload listener that provides access to the condition context (tags, etc.).
 * Extends the vanilla PreparableReloadListener with condition-aware reload support.
 */
public abstract class ContextAwareReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager,
                                           ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                           Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> prepare(resourceManager, preparationsProfiler), backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(result -> apply(result, resourceManager, reloadProfiler), gameExecutor);
    }

    /**
     * Perform preparation work (off-thread).
     */
    protected abstract Object prepare(ResourceManager resourceManager, ProfilerFiller profiler);

    /**
     * Apply the prepared data (on game thread).
     */
    protected abstract void apply(Object prepared, ResourceManager resourceManager, ProfilerFiller profiler);
}
