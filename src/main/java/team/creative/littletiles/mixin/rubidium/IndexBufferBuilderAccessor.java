package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.model.IndexBufferBuilder;

@Mixin(IndexBufferBuilder.class)
public interface IndexBufferBuilderAccessor {
    
    @Accessor(remap = false)
    public IntArrayList getIndices();
    
}
