package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

@Mixin(BufferBuilder.class)
public interface BufferBuilderFormatAccessor {
    
    @Accessor("format")
    VertexFormat getFormat();
    
}
