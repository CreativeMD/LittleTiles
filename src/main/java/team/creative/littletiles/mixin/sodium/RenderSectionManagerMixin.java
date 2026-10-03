package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.caffeinemc.mods.sodium.client.render.chunk.ChunkUpdateTypes;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.estimation.UploadResourceBudget;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkJobCollector;
import net.minecraft.core.SectionPos;
import team.creative.littletiles.client.render.block.NeighbourRenderTransition;

@Mixin(RenderSectionManager.class)
public abstract class RenderSectionManagerMixin {
    @Inject(method = "submitSectionTask", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void deferNeighbourBuild(ChunkJobCollector collector, RenderSection section, int updateType,
            UploadResourceBudget budget, boolean blocking, CallbackInfo ci) {
        if (ChunkUpdateTypes.isRebuild(updateType) && NeighbourRenderTransition.defer(
                SectionPos.asLong(section.getChunkX(), section.getChunkY(), section.getChunkZ())))
            ci.cancel(); // Retain pending work; the transition requests it again when ready.
    }
}
