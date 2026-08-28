package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.mod.sodium.entity.LittleAnimationRenderManagerSodium;
import team.creative.littletiles.client.mod.sodium.renderer.DefaultChunkRendererExtender;
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
    
    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/ShaderChunkRenderer;end(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V",
            remap = false), method = "render", remap = false, require = 1)
    public void render(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera,
            boolean indexedRenderingEnabled, CallbackInfo info) {
        var bindings = vertexFormat.getShaderBindings();
        PoseStack pose = new PoseStack();
        pose.last().pose().set(matrices.modelView());
        
        Minecraft mc = Minecraft.getInstance();
        ChunkShaderInterface shader = null;
        if (shader == null)
            shader = this.activeProgram.getInterface();
        float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(false);
        Vec3 defaultCamera = new Vec3(camera.x, camera.y, camera.z);
        for (LittleEntity animation : LittleTilesClient.ANIMATION_HANDLER) {
            if (animation.getRenderManager().shouldRender(true) && animation.getRenderManager() instanceof LittleAnimationRenderManagerSodium r) {
                
                r.prepare(bindings, vertexFormat);
                
                pose.pushPose();
                var cam = animation.getOrigin().pose(partialTicks).setup(pose, defaultCamera);
                shader.setModelViewMatrix(pose.last().pose());
                r.renderChunkLayerSodium(((TerrainRenderPassAccessor) renderPass).getRenderType(), pose, cam.x, cam.y, cam.z, matrices.projection(), shader, camera);
                pose.popPose();
                
            }
        }
    }
}
