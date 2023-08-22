package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;

import me.jellysquid.mods.sodium.client.gl.buffer.GlBuffer;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(GlBuffer.class)
public class GlBufferMixin implements VertexBufferExtender {
    
    @Override
    public int getLastUploadedLength() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getVertexBufferId() {
        return ((GlBuffer) (Object) this).handle();
    }
    
}
