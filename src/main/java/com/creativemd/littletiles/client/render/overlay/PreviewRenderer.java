package com.creativemd.littletiles.client.render.overlay;

import java.util.List;

import com.creativemd.creativecore.common.gui.mc.GuiContainerSub;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleEditor;
import com.creativemd.littletiles.common.api.ILittlePlacer;
import com.creativemd.littletiles.common.api.ILittleTool;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.IMarkMode;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementMode.PreviewMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.place.PlacementPreview;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
    
    public static final ResourceLocation WHITE_TEXTURE = new ResourceLocation(LittleTiles.modid, "textures/preview.png");
    
    public static Minecraft mc = Minecraft.getMinecraft();
    
    public static IMarkMode marked;
    
    public static boolean isCentered(EntityPlayer player, ItemStack stack, ILittlePlacer iTile) {
        if (iTile.snapToGridByDefault(stack))
            return LittleAction.isUsingSecondMode(player) && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid == LittleAction.isUsingSecondMode(player) || marked != null;
    }
    
    public static boolean isFixed(EntityPlayer player, ItemStack stack, ILittlePlacer iTile) {
        if (iTile.snapToGridByDefault(stack))
            return !LittleAction.isUsingSecondMode(player) && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid != LittleAction.isUsingSecondMode(player) && marked == null;
    }
    
    public static void handleUndoAndRedo(EntityPlayer player) {
        while (LittleTilesClient.undo.isPressed()) {
            try {
                if (LittleAction.canUseUndoOrRedo(player))
                    LittleAction.undo();
            } catch (LittleActionException e) {
                LittleAction.handleExceptionClient(e);
            }
        }
        
        while (LittleTilesClient.redo.isPressed()) {
            try {
                if (LittleAction.canUseUndoOrRedo(player))
                    LittleAction.redo();
            } catch (LittleActionException e) {
                LittleAction.handleExceptionClient(e);
            }
        }
    }
    
    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote)
            LittleAction.unloadWorld();
    }
    
    @SubscribeEvent
    public void tick(RenderWorldLastEvent event) {
        if (mc.player != null && mc.inGameHasFocus && !mc.gameSettings.hideGUI) {
            World world = mc.world;
            EntityPlayer player = mc.player;
            ItemStack stack = mc.player.getHeldItemMainhand();
            
            if (!LittleAction.canPlace(player))
                return;
            
            handleUndoAndRedo(player);
            
            if (stack
                .getItem() instanceof ILittleTool && (marked != null || (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK && mc.objectMouseOver.sideHit != null))) {
                PlacementPosition position = marked != null ? marked.getPosition() : PlacementHelper
                    .getPosition(world, mc.objectMouseOver, ((ILittleTool) stack.getItem()).getPositionContext(stack), (ILittleTool) stack.getItem(), stack);
                
                double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
                double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
                double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
                
                processRotateKeys(stack, ((ILittleTool) stack.getItem()).getPositionContext(stack));
                
                ((ILittleTool) stack.getItem()).tick(player, stack, position, mc.objectMouseOver);
                
                if (PlacementHelper.isLittleBlock(stack)) {
                    
                    ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
                    
                    PlacementMode mode = iTile.getPlacementMode(stack);
                    
                    if (mode.getPreviewMode() == PreviewMode.PREVIEWS) {
                        GlStateManager.enableBlend();
                        GlStateManager
                            .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
                        GlStateManager.enableTexture2D();
                        mc.renderEngine.bindTexture(WHITE_TEXTURE);
                        GlStateManager.depthMask(false);
                        
                        boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
                        PlacementPreview result = PlacementHelper
                            .getPreviews(world, stack, position, isCentered(player, stack, iTile), isFixed(player, stack, iTile), allowLowResolution, mode);
                        
                        if (result != null) {
                            processMarkKey(player, iTile, stack, result);
                            List<PlacePreview> placePreviews = result.getPreviews();
                            
                            double posX = result.pos.getX() - TileEntityRendererDispatcher.staticPlayerX;
                            double posY = result.pos.getY() - TileEntityRendererDispatcher.staticPlayerY;
                            double posZ = result.pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
                            
                            float alpha = (float) (Math.sin(System.nanoTime() / 200000000F) * 0.2F + 0.5F);
                            
                            for (int i = 0; i < placePreviews.size(); i++) {
                                PlacePreview preview = placePreviews.get(i);
                                List<LittleRenderBox> cubes = preview.getPreviews(result.context);
                                for (LittleRenderBox cube : cubes)
                                    cube.renderPreview(posX, posY, posZ, (int) (alpha * iTile.getPreviewAlphaFactor() * 255));
                            }
                            
                            if (position.positingCubes != null)
                                for (LittleRenderBox cube : position.positingCubes)
                                    cube.renderPreview(posX, posY, posZ, (int) (alpha * ColorUtils.getAlphaDecimal(cube.color) * iTile.getPreviewAlphaFactor() * 255));
                        }
                        
                        GlStateManager.depthMask(true);
                        GlStateManager.enableTexture2D();
                        GlStateManager.disableBlend();
                    }
                    
                } else
                    processMarkKey(player, (ILittleTool) stack.getItem(), stack, null);
                
                ((ILittleTool) stack.getItem()).render(player, stack, x, y, z);
                if (marked != null)
                    marked.render(((ILittleTool) stack.getItem()).getPositionContext(stack), x, y, z);
            } else
                marked = null;
        }
    }
    
    public void processMarkKey(EntityPlayer player, ILittleTool iTile, ItemStack stack, PlacementPreview preview) {
        while (LittleTilesClient.mark.isPressed()) {
            if (marked == null) {
                marked = iTile
                    .onMark(player, stack, PlacementHelper.getPosition(player.world, mc.objectMouseOver, iTile.getPositionContext(stack), iTile, stack), mc.objectMouseOver, preview);
                if (GuiScreen.isCtrlKeyDown())
                    FMLClientHandler.instance().displayGuiScreen(player, new GuiContainerSub(player, marked.getConfigurationGui(), new SubContainerEmpty(player)));
            } else {
                if (GuiScreen.isCtrlKeyDown())
                    FMLClientHandler.instance().displayGuiScreen(player, new GuiContainerSub(player, marked.getConfigurationGui(), new SubContainerEmpty(player)));
                else {
                    marked.done();
                    marked = null;
                }
            }
        }
    }
    
    public static void processRotateKey(EntityPlayer player, Rotation rotation, ItemStack stack) {
        LittleRotatePacket packet = new LittleRotatePacket(rotation);
        packet.executeClient(player);
        
        if (stack.getItem() instanceof ILittleTool && !((ILittleTool) stack.getItem()).sendTransformationUpdate())
            return;
        
        PacketHandler.sendPacketToServer(packet);
    }
    
    public void processRotateKeys(ItemStack stack, LittleGridContext context) {
        while (LittleTilesClient.flip.isPressed())
            processFlipKey(mc.player, stack);
        
        boolean repeated = marked != null;
        
        // Rotate Block
        while (LittleTilesClient.up.isPressed(repeated)) {
            if (marked != null)
                marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.UP : EnumFacing.EAST);
            else
                processRotateKey(mc.player, Rotation.Z_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.down.isPressed(repeated)) {
            if (marked != null)
                marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.DOWN : EnumFacing.WEST);
            else
                processRotateKey(mc.player, Rotation.Z_COUNTER_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.right.isPressed(repeated)) {
            if (marked != null)
                marked.move(context, EnumFacing.SOUTH);
            else
                processRotateKey(mc.player, Rotation.Y_COUNTER_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.left.isPressed(repeated)) {
            if (marked != null)
                marked.move(context, EnumFacing.NORTH);
            else
                processRotateKey(mc.player, Rotation.Y_CLOCKWISE, stack);
        }
    }
    
    public static void processFlipKey(EntityPlayer player, ItemStack stack) {
        int i4 = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        EnumFacing direction = null;
        switch (i4) {
        case 0:
            direction = EnumFacing.SOUTH;
            break;
        case 1:
            direction = EnumFacing.WEST;
            break;
        case 2:
            direction = EnumFacing.NORTH;
            break;
        case 3:
            direction = EnumFacing.EAST;
            break;
        }
        if (player.rotationPitch > 45)
            direction = EnumFacing.DOWN;
        if (player.rotationPitch < -45)
            direction = EnumFacing.UP;
        
        LittleFlipPacket packet = new LittleFlipPacket(direction.getAxis());
        packet.executeClient(player);
        
        if (stack.getItem() instanceof ILittleTool && !((ILittleTool) stack.getItem()).sendTransformationUpdate())
            return;
        
        PacketHandler.sendPacketToServer(packet);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void drawHighlight(DrawBlockHighlightEvent event) {
        EntityPlayer player = event.getPlayer();
        World world = player.world;
        ItemStack stack = player.getHeldItemMainhand();
        
        if (!LittleAction.canPlace(player))
            return;
        
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
        
        if ((event.getTarget().typeOfHit == Type.BLOCK || marked != null) && stack.getItem() instanceof ILittleTool) {
            BlockPos pos = marked != null ? marked.getPosition().getPos() : event.getTarget().getBlockPos();
            IBlockState state = world.getBlockState(pos);
            if (stack.getItem() instanceof ILittleEditor) {
                ILittleEditor selector = (ILittleEditor) stack.getItem();
                
                processMarkKey(player, selector, stack, null);
                PlacementPosition result = new PlacementPosition(event.getTarget(), selector.getPositionContext(stack));
                if (selector.hasCustomBoxes(world, stack, player, state, result, event.getTarget()) || marked != null) {
                    LittleBoxes boxes = ((ILittleEditor) stack.getItem()).getBoxes(world, stack, player, result, event.getTarget());
                    GlStateManager.enableBlend();
                    GlStateManager
                        .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    
                    GlStateManager.enableTexture2D();
                    Minecraft.getMinecraft().renderEngine.bindTexture(PreviewRenderer.WHITE_TEXTURE);
                    GlStateManager.depthMask(false);
                    
                    double posX = x - boxes.pos.getX();
                    double posY = y - boxes.pos.getY();
                    double posZ = z - boxes.pos.getZ();
                    
                    GlStateManager.glLineWidth(4.0F);
                    for (LittleBox box : boxes.all()) {
                        LittleRenderBox cube = box.getRenderingCube(boxes.getContext(), null, 0);
                        
                        if (cube != null) {
                            cube.color = 0;
                            cube.renderLines(-posX, -posY, -posZ, 102, cube.getCenter(), 0.002);
                        }
                    }
                    
                    if (state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos)) {
                        GlStateManager.glLineWidth(1.0F);
                        RenderGlobal.drawSelectionBoundingBox(state.getSelectedBoundingBox(world, pos).grow(0.002).offset(-x, -y, -z), 0.0F, 0.0F, 0.0F, 0.4F);
                    }
                    
                    GlStateManager.depthFunc(515);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    
                    event.setCanceled(true);
                }
            } else if (stack.getItem() instanceof ILittlePlacer) {
                
                ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
                PlacementMode mode = iTile.getPlacementMode(stack);
                if (mode.getPreviewMode() == PreviewMode.LINES) {
                    
                    PlacementPosition position = marked != null ? marked.getPosition() : PlacementHelper
                        .getPosition(world, mc.objectMouseOver, iTile.getPositionContext(stack), iTile, stack);
                    
                    boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
                    
                    PlacementPreview result = PlacementHelper
                        .getPreviews(world, stack, position, isCentered(player, stack, iTile), isFixed(player, stack, iTile), allowLowResolution, mode);
                    
                    if (result != null) {
                        processMarkKey(player, iTile, stack, result);
                        
                        GlStateManager.enableBlend();
                        GlStateManager
                            .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.glLineWidth((float) LittleTiles.CONFIG.rendering.previewLineThickness);
                        GlStateManager.enableTexture2D();
                        mc.renderEngine.bindTexture(WHITE_TEXTURE);
                        GlStateManager.depthMask(false);
                        
                        double posX = x - result.pos.getX();
                        double posY = y - result.pos.getY();
                        double posZ = z - result.pos.getZ();
                        
                        List<PlacePreview> placePreviews = result.getPreviews();
                        for (int i = 0; i < placePreviews.size(); i++)
                            for (LittleRenderBox cube : placePreviews.get(i).getPreviews(result.context))
                                cube.renderLines(-posX, -posY, -posZ, 102, cube.getCenter(), 0.002);
                            
                        if (position.positingCubes != null)
                            for (LittleRenderBox cube : position.positingCubes)
                                cube.renderLines(-posX, -posY, -posZ, 102, cube.getCenter(), 0.002);
                            
                        GlStateManager.depthMask(true);
                        GlStateManager.enableTexture2D();
                        GlStateManager.disableBlend();
                    }
                }
            }
        }
    }
}
