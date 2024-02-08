package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.mod.oculus.OculusInteractor;
import team.creative.littletiles.client.mod.oculus.OculusManager;
import team.creative.littletiles.client.mod.rubidium.entity.LittleAnimationRenderManagerRubidium;
import team.creative.littletiles.client.mod.rubidium.renderer.DefaultChunkRendererExtender;
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
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ShaderChunkRenderer;end(Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V",
            remap = false), method = "render", remap = false)
    public void render(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera, CallbackInfo info) {
        var bindings = vertexAttributeBindings;
        if (bindings == null) {
            bindings = new GlVertexAttributeBinding[] { new GlVertexAttributeBinding(1, vertexFormat.getAttribute(
                ChunkMeshAttribute.POSITION_MATERIAL_MESH)), new GlVertexAttributeBinding(2, vertexFormat.getAttribute(
                    ChunkMeshAttribute.COLOR_SHADE)), new GlVertexAttributeBinding(3, vertexFormat.getAttribute(
                        ChunkMeshAttribute.BLOCK_TEXTURE)), new GlVertexAttributeBinding(4, vertexFormat.getAttribute(
                            ChunkMeshAttribute.LIGHT_TEXTURE)), new GlVertexAttributeBinding(14, vertexFormat.getAttribute(
                                IrisChunkMeshAttributes.MID_BLOCK)), new GlVertexAttributeBinding(11, vertexFormat.getAttribute(
                                    IrisChunkMeshAttributes.BLOCK_ID)), new GlVertexAttributeBinding(12, vertexFormat.getAttribute(
                                        IrisChunkMeshAttributes.MID_TEX_COORD)), new GlVertexAttributeBinding(13, vertexFormat.getAttribute(
                                            IrisChunkMeshAttributes.TANGENT)), new GlVertexAttributeBinding(10, vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL)) };
        }
        
        PoseStack pose = new PoseStack();
        pose.last().pose().set(matrices.modelView());
        
        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        ChunkShaderInterface shader = null;
        if (OculusManager.installed())
            shader = (ChunkShaderInterface) OculusInteractor.getShader(this);
        if (shader == null)
            shader = this.activeProgram.getInterface();
        float partialTicks = mc.getPartialTick();
        for (LittleEntity animation : LittleTilesClient.ANIMATION_HANDLER) {
            if (animation.getRenderManager() instanceof LittleAnimationRenderManagerRubidium r) {
                
                r.prepare(bindings, vertexFormat);
                
                pose.pushPose();
                animation.getOrigin().setupRendering(pose, cam.x, cam.y, cam.z, partialTicks);
                shader.setModelViewMatrix(pose.last().pose());
                r.renderChunkLayerRubidium(((TerrainRenderPassAccessor) renderPass).getLayer(), pose, cam.x, cam.y, cam.z, RenderSystem.getProjectionMatrix(), shader, camera);
                pose.popPose();
                
            }
        }
    }
}
