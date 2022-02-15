package team.creative.littletiles.client.render.level;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.event.AxisAlignedBB;
import com.creativemd.littletiles.common.event.EntityPlayer;
import com.creativemd.littletiles.common.event.IParentTileList;
import com.creativemd.littletiles.common.event.IntHashMap;
import com.creativemd.littletiles.common.event.LittleTileColored;
import com.creativemd.littletiles.common.event.RenderGlobal;
import com.creativemd.littletiles.common.event.Tessellator;
import com.creativemd.littletiles.common.event.TileEntity;
import com.creativemd.littletiles.common.event.TileEntityLittleTiles;
import com.creativemd.littletiles.common.event.World;
import com.creativemd.littletiles.common.event.WorldClient;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.cache.RenderingThread;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.little.tile.LittleTile;

public class LittleClientEventHandler {
    
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    
    private static Field setTileEntitiesField;
    
    public static int transparencySortingIndex;
    
    private static Field prevRenderSortX;
    private static Field prevRenderSortY;
    private static Field prevRenderSortZ;
    
    private static List<RenderChunk> queuedRenderChunksUpdate = new ArrayList<>();
    
    private static Field entitiesById = ReflectionHelper.findField(World.class, new String[] { "entitiesById", "field_175729_l" });
    
    public static boolean cancelEntitySpawn(WorldClient world, int entityID, Entity entity) {
        if (entity instanceof EntityAnimation) {
            ((EntityAnimation) entity).addDoor();
            
            if (((EntityAnimation) entity).spawnedInWorld) {
                entity.setEntityId(entityID);
                try {
                    ((IntHashMap<Entity>) entitiesById.get(world)).addKey(entityID, entity);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return true;
            }
            ((EntityAnimation) entity).spawnedInWorld = true;
            return false;
        }
        return false;
    }
    
    public static void queueChunkUpdate(RenderChunk chunk) {
        synchronized (queuedRenderChunksUpdate) {
            if (!queuedRenderChunksUpdate.contains(chunk))
                queuedRenderChunksUpdate.add(chunk);
        }
    }
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            synchronized (queuedRenderChunksUpdate) {
                if (!queuedRenderChunksUpdate.isEmpty()) {
                    for (Iterator iterator = queuedRenderChunksUpdate.iterator(); iterator.hasNext();) {
                        RenderChunk chunk = (RenderChunk) iterator.next();
                        if (!chunk.isDirty()) {
                            chunk.setDirty(true);
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase == Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            
            if (mc.player != null && mc.renderGlobal != null) {
                if (prevRenderSortX == null) {
                    prevRenderSortX = ReflectionHelper.findField(RenderGlobal.class, new String[] { "prevRenderSortX", "field_147596_f" });
                    prevRenderSortY = ReflectionHelper.findField(RenderGlobal.class, new String[] { "prevRenderSortY", "field_147597_g" });
                    prevRenderSortZ = ReflectionHelper.findField(RenderGlobal.class, new String[] { "prevRenderSortZ", "field_147602_h" });
                }
                
                Entity entityIn = mc.getRenderViewEntity();
                if (entityIn == null)
                    return;
                try {
                    double d0 = entityIn.posX - prevRenderSortX.getDouble(mc.renderGlobal);
                    double d1 = entityIn.posY - prevRenderSortY.getDouble(mc.renderGlobal);
                    double d2 = entityIn.posZ - prevRenderSortZ.getDouble(mc.renderGlobal);
                    if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)
                        transparencySortingIndex++;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    @SubscribeEvent
    public synchronized void worldUnload(Unload event) {
        if (event.getWorld().isClientSide()) {
            
            for (int i = 0; i < RenderingThread.threads.size(); i++) {
                RenderingThread thread = RenderingThread.threads.get(i);
                if (thread == null)
                    continue;
                thread.interrupt();
                thread.updateCoords.clear();
                RenderingThread.threads.set(i, null);
            }
            RenderingThread.chunks.clear();
            
            if (setTileEntitiesField == null)
                setTileEntitiesField = ReflectionHelper.findField(RenderGlobal.class, new String[] { "setTileEntities", "field_181024_n" });
            
            try {
                Set<TileEntity> set = (Set<TileEntity>) setTileEntitiesField.get(Minecraft.getMinecraft().renderGlobal);
                synchronized (set) {
                    set.clear();
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    @SubscribeEvent
    public void renderOverlay(RenderBlockOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getOverlayType() == OverlayType.WATER) {
            EntityPlayer player = event.getPlayer();
            double d0 = player.posY + player.getEyeHeight();
            BlockPos blockpos = new BlockPos(player.posX, d0, player.posZ);
            TileEntity te = player.world.getTileEntity(blockpos);
            if (te instanceof TileEntityLittleTiles) {
                AxisAlignedBB bb = player.getEntityBoundingBox();
                for (Pair<IParentTileList, LittleTile> pair : ((TileEntityLittleTiles) te).allTiles()) {
                    LittleTile tile = pair.value;
                    if (tile instanceof LittleTileColored && tile.isMaterial(Material.WATER) && tile.getBox().getBox(pair.key.getContext(), blockpos).intersects(bb)) {
                        
                        mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferbuilder = tessellator.getBuffer();
                        float f = mc.player.getBrightness();
                        Vec3d color = ColorUtils.IntToVec(((LittleTileColored) tile).color);
                        GlStateManager.color(f * (float) color.x, f * (float) color.y, f * (float) color.z, 0.5F);
                        GlStateManager.enableBlend();
                        GlStateManager
                                .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.pushMatrix();
                        float f1 = 4.0F;
                        float f2 = -1.0F;
                        float f3 = 1.0F;
                        float f4 = -1.0F;
                        float f5 = 1.0F;
                        float f6 = -0.5F;
                        float f7 = -mc.player.rotationYaw / 64.0F;
                        float f8 = mc.player.rotationPitch / 64.0F;
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                        bufferbuilder.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + f7, 4.0F + f8).endVertex();
                        bufferbuilder.pos(1.0D, -1.0D, -0.5D).tex(0.0F + f7, 4.0F + f8).endVertex();
                        bufferbuilder.pos(1.0D, 1.0D, -0.5D).tex(0.0F + f7, 0.0F + f8).endVertex();
                        bufferbuilder.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + f7, 0.0F + f8).endVertex();
                        tessellator.draw();
                        GlStateManager.popMatrix();
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.disableBlend();
                        
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }
    
}