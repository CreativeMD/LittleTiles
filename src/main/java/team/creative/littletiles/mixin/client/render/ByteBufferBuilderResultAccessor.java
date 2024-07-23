package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

@Mixin(ByteBufferBuilder.Result.class)
public interface ByteBufferBuilderResultAccessor {
    
    @Accessor
    public int getOffset();
    
    @Accessor
    public int getCapacity();
    
}
