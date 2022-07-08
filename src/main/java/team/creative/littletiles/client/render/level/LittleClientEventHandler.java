package team.creative.littletiles.client.render.level;

import java.lang.reflect.Field;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;

public class LittleClientEventHandler {
    
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    private static final Field xTransparentOldField = ObfuscationReflectionHelper.findField(LevelRenderer.class, "f_109445_");
    private static final Field yTransparentOldField = ObfuscationReflectionHelper.findField(LevelRenderer.class, "f_109446_");
    private static final Field zTransparentOldField = ObfuscationReflectionHelper.findField(LevelRenderer.class, "f_109447_");
    
    public static int transparencySortingIndex;
    
    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase == Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            
            if (mc.player != null && mc.levelRenderer != null) {
                try {
                    Vec3 vec3 = mc.gameRenderer.getMainCamera().getPosition();
                    double d0 = vec3.x() - xTransparentOldField.getDouble(mc.levelRenderer);
                    double d1 = vec3.y() - yTransparentOldField.getDouble(mc.levelRenderer);
                    double d2 = vec3.z() - zTransparentOldField.getDouble(mc.levelRenderer);
                    if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)
                        transparencySortingIndex++;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    public void reloadRenderer(EntityRenderersEvent.AddLayers event) {
        
    }
    
    @SubscribeEvent
    public synchronized void worldUnload(Unload event) {
        if (event.getWorld().isClientSide())
            RenderingThread.unload();
    }
    
    @SubscribeEvent
    public void renderOverlay(RenderBlockOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getOverlayType() == OverlayType.WATER) {
            PoseStack pose = new PoseStack();
            Player player = event.getPlayer();
            BlockPos blockpos = new BlockPos(player.getEyePosition(TickUtils.getFrameTime(player.level)));
            BlockEntity blockEntity = player.level.getBlockEntity(blockpos);
            if (blockEntity instanceof BETiles be) {
                AABB bb = player.getBoundingBox();
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
                    LittleTile tile = pair.value;
                    if (tile.isMaterial(Material.WATER) && tile.intersectsWith(bb, pair.key)) {
                        
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.enableTexture();
                        RenderSystem.setShaderTexture(0, RES_UNDERWATER_OVERLAY);
                        
                        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
                        float f = LightTexture.getBrightness(player.level.dimensionType(), player.level.getMaxLocalRawBrightness(blockpos));
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(f, f, f, 0.1F);
                        Vector3d color = ColorUtils.toVec(tile.color);
                        RenderSystem.setShaderColor(f * (float) color.x, f * (float) color.y, f * (float) color.z, 0.5F);
                        float f7 = -mc.player.getYRot() / 64.0F;
                        float f8 = mc.player.getXRot() / 64.0F;
                        Matrix4f matrix4f = pose.last().pose();
                        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        bufferbuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(4.0F + f7, 4.0F + f8).endVertex();
                        bufferbuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F + f7, 4.0F + f8).endVertex();
                        bufferbuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F + f7, 0.0F + f8).endVertex();
                        bufferbuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(4.0F + f7, 0.0F + f8).endVertex();
                        BufferUploader.drawWithShader(bufferbuilder.end());
                        RenderSystem.disableBlend();
                        
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }
    
}
