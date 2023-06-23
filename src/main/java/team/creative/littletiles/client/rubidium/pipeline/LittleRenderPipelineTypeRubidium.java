package team.creative.littletiles.client.rubidium.pipeline;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15C;

import me.jellysquid.mods.sodium.client.gl.buffer.GlBuffer;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBufferTarget;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

public class LittleRenderPipelineTypeRubidium extends LittleRenderPipelineType {
    
    public LittleRenderPipelineTypeRubidium() {
        super(LittleRenderPipelineRubidium::new);
    }
    
    @Override
    public void bindBuffer(VertexBufferExtender extender) {
        RenderDevice.INSTANCE.createCommandList().bindBuffer(GlBufferTarget.ARRAY_BUFFER, (GlBuffer) extender);
    }
    
    @Override
    public void getBufferSubData(long offset, ByteBuffer buffer) {
        GL15C.glGetBufferSubData(GlBufferTarget.ARRAY_BUFFER.getTargetParameter(), offset, buffer);
    }
    
    @Override
    public void unbindBuffer() {
        
    }
    
    @Override
    public boolean canBeUploadedDirectly() {
        return false;
    }
}
