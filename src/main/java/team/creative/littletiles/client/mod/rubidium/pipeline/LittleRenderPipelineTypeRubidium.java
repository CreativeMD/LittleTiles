package team.creative.littletiles.client.mod.rubidium.pipeline;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15C;

import com.mojang.blaze3d.platform.MemoryTracker;

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
    public ByteBuffer downloadUploadedData(VertexBufferExtender buffer, long offset, int size) {
        RenderDevice.INSTANCE.createCommandList().bindBuffer(GlBufferTarget.ARRAY_BUFFER, (GlBuffer) buffer);
        try {
            ByteBuffer result = MemoryTracker.create(size);
            GL15C.glGetBufferSubData(GlBufferTarget.ARRAY_BUFFER.getTargetParameter(), offset, result);
            return result;
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (!(e instanceof IllegalStateException))
                e.printStackTrace();
            return null;
        } finally {
            
        }
    }
    
    @Override
    public boolean canBeUploadedDirectly() {
        return false;
    }
}
