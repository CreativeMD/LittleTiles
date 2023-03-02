package team.creative.littletiles.client.render.entity;

import org.joml.Matrix4f;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationRenderManager extends LittleEntityRenderManager<LittleAnimationEntity> {
    
    public LittleAnimationRenderManager(LittleAnimationEntity entity) {
        super(entity);
    }
    
    @Override
    public void compileChunks() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void setSectionDirty(int x, int y, int z, boolean playerChanged) {
        // TODO Auto-generated method stub
        
    }
    
}
