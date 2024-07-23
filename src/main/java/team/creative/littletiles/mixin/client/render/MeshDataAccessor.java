package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

@Mixin(MeshData.class)
public interface MeshDataAccessor {
    
    @Accessor
    public ByteBufferBuilder.Result getVertexBuffer();
}
