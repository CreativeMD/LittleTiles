package team.creative.littletiles.mixin.client.render;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.creativecore.mixin.BufferBuilderAccessor;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements ChunkBufferUploader {
    
    @Override
    public int uploadIndex() {
        return ((BufferBuilderAccessor) this).getNextElementByte();
    }
    
    @Override
    public void upload(ByteBuffer buffer) {
        ((BufferBuilder) (Object) this).putBulkData(buffer);
    }
    
    @Override
    public boolean hasFacingSupport() {
        return false;
    }
    
    @Override
    public int uploadIndex(int facing) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void upload(int facing, ByteBuffer buffer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addSprite(TextureAtlasSprite texture) {}
}
