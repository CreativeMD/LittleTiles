package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

public interface RenderChunkExtender {
    
    public boolean dynamicLightUpdate();
    
    public void dynamicLightUpdate(boolean value);
    
    public void begin(BufferBuilder builder);
    
    public VertexBuffer getVertexBuffer(RenderType layer);
    
    public void markReadyForUpdate(boolean playerChanged);
    
    public default void prepareBlockTranslation(PoseStack posestack, BlockPos pos) {
        posestack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }
    
}
