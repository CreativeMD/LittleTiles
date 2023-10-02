package team.creative.littletiles.mixin.client.render;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.CompiledSection;
import net.minecraft.client.renderer.chunk.VisibilitySet;

@Mixin(CompiledSection.class)
public interface CompiledChunkAccessor {
    
    @Accessor
    public BufferBuilder.SortState getTransparencyState();
    
    @Accessor
    public void setTransparencyState(BufferBuilder.SortState state);
    
    @Accessor
    public Set<RenderType> getHasBlocks();
    
    @Accessor
    public VisibilitySet getVisibilitySet();
    
    @Accessor
    public void setVisibilitySet(VisibilitySet visibilitySet);
    
}
