package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeBinding;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.embeddedt.embeddium.impl.render.chunk.DefaultChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.ShaderChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.lists.ChunkRenderListIterable;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.DefaultMaterials;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.mod.embeddium.entity.LittleAnimationRenderManagerEmbeddium;
import team.creative.littletiles.client.mod.embeddium.renderer.DefaultChunkRendererExtender;
import team.creative.littletiles.common.entity.LittleEntity;

@Mixin(DefaultChunkRenderer.class)
public abstract class DefaultChunkRendererMixin extends ShaderChunkRenderer implements DefaultChunkRendererExtender {
    
    public DefaultChunkRendererMixin(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);
    }
    
    @Override
    public void begin(RenderType layer) {
        super.begin(DefaultMaterials.forRenderLayer(layer).pass);
    }
    
    @Override
    public void end(RenderType layer) {
        super.end(DefaultMaterials.forRenderLayer(layer).pass);
    }
    
    @Shadow
    @Final
    private GlVertexAttributeBinding[] vertexAttributeBindings;
    
    @Inject(at = @At(value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/chunk/ShaderChunkRenderer;end(Lorg/embeddedt/embeddium/impl/render/chunk/terrain/TerrainRenderPass;)V", remap = false),
            method = "render", remap = false)
    public void render(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera,
            CallbackInfo info) {
        var bindings = vertexAttributeBindings;
        //if (bindings == null && OculusManager.installed()) {
        //    bindings = (GlVertexAttributeBinding[]) OculusManager.createVertexFormat(vertexFormat);
        //}
        
        PoseStack pose = new PoseStack();
        pose.last().pose().set(matrices.modelView());
        
        Minecraft mc = Minecraft.getInstance();
        ChunkShaderInterface shader = null;
        //if (OculusManager.installed())
        //    shader = (ChunkShaderInterface) OculusInteractor.getShader(this);
        if (shader == null)
            shader = this.activeProgram.getInterface();
        float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(false);
        for (LittleEntity animation : LittleTilesClient.ANIMATION_HANDLER) {
            if (animation.getRenderManager() instanceof LittleAnimationRenderManagerEmbeddium r) {
                
                r.prepare(bindings, vertexFormat);
                
                pose.pushPose();
                animation.getOrigin().setupRendering(pose, camera.x, camera.y, camera.z, partialTicks);
                shader.setModelViewMatrix(pose.last().pose());
                r.renderChunkLayerEmbeddium(((TerrainRenderPassAccessor) renderPass).getLayer(), pose, camera.x, camera.y, camera.z, matrices.projection(), shader, camera);
                pose.popPose();
                
            }
        }
    }
}
