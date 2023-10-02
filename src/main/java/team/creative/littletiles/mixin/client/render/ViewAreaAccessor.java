package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;

@Mixin(ViewArea.class)
public interface ViewAreaAccessor {
    
    @Invoker("getRenderSectionAt")
    public SectionRenderDispatcher.RenderSection getChunkAt(BlockPos pos);
    
}
