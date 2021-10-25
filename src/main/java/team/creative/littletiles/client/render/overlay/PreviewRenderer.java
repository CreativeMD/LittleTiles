package team.creative.littletiles.client.render.overlay;

import java.util.List;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.gui.mc.GuiContainerSub;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.api.tool.ILittleEditor;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.packet.item.MirrorPacket;
import team.creative.littletiles.common.packet.item.RotatePacket;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.mode.PlacementMode.PreviewMode;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
    
    public static final ResourceLocation WHITE_TEXTURE = new ResourceLocation(LittleTiles.MODID, "textures/preview.png");
    
    public static Minecraft mc = Minecraft.getInstance();
    
    public static IMarkMode marked;
    
    public static boolean isCentered(Player player, ItemStack stack, ILittlePlacer iTile) {
        if (iTile.snapToGridByDefault(stack))
            return LittleAction.isUsingSecondMode(player) && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid == LittleAction.isUsingSecondMode(player) || marked != null;
    }
    
    public static boolean isFixed(Player player, ItemStack stack, ILittlePlacer iTile) {
        if (iTile.snapToGridByDefault(stack))
            return !LittleAction.isUsingSecondMode(player) && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid != LittleAction.isUsingSecondMode(player) && marked == null;
    }
    
    public static void handleUndoAndRedo(Player player) {
        while (LittleTilesClient.undo.consumeClick()) {
            try {
                if (LittleAction.canUseUndoOrRedo(player))
                    LittleAction.undo();
            } catch (LittleActionException e) {
                LittleAction.handleExceptionClient(e);
            }
        }
        
        while (LittleTilesClient.redo.consumeClick()) {
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
        if (event.getWorld().isClientSide())
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
                
                processRotateKeys(stack, position.getContext());
                
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
                    marked.render(x, y, z);
            } else
                marked = null;
        }
    }
    
    public void processMarkKey(Player player, ILittleTool iTile, ItemStack stack, PlacementPreview preview) {
        while (LittleTilesClient.mark.isPressed()) {
            if (marked == null) {
                marked = iTile.onMark(player, stack, PlacementHelper
                        .getPosition(player.world, mc.objectMouseOver, iTile.getPositionContext(stack), iTile, stack), mc.objectMouseOver, preview);
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
    
    public static void processRotateKey(Player player, Rotation rotation, ItemStack stack) {
        RotatePacket packet = new RotatePacket(rotation);
        packet.executeClient(player);
        
        if (stack.getItem() instanceof ILittleTool && !((ILittleTool) stack.getItem()).sendTransformationUpdate())
            return;
        
        LittleTiles.NETWORK.sendToServer(packet);
    }
    
    public void processRotateKeys(ItemStack stack, LittleGrid context) {
        while (LittleTilesClient.flip.consumeClick())
            processFlipKey(mc.player, stack);
        
        boolean repeated = marked != null;
        
        // Rotate Block
        while (LittleTilesClient.up.consumeClick(repeated)) {
            if (marked != null)
                marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? Facing.UP : Facing.EAST);
            else
                processRotateKey(mc.player, Rotation.Z_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.down.consumeClick(repeated)) {
            if (marked != null)
                marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? Facing.DOWN : Facing.WEST);
            else
                processRotateKey(mc.player, Rotation.Z_COUNTER_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.right.consumeClick(repeated)) {
            if (marked != null)
                marked.move(context, Facing.SOUTH);
            else
                processRotateKey(mc.player, Rotation.Y_COUNTER_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.left.consumeClick(repeated)) {
            if (marked != null)
                marked.move(context, Facing.NORTH);
            else
                processRotateKey(mc.player, Rotation.Y_CLOCKWISE, stack);
        }
    }
    
    public static void processFlipKey(Player player, ItemStack stack) {
        int i4 = Mth.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        Facing direction = null;
        switch (i4) {
        case 0:
            direction = Facing.SOUTH;
            break;
        case 1:
            direction = Facing.WEST;
            break;
        case 2:
            direction = Facing.NORTH;
            break;
        case 3:
            direction = Facing.EAST;
            break;
        }
        if (player.rotationPitch > 45)
            direction = Facing.DOWN;
        if (player.rotationPitch < -45)
            direction = Facing.UP;
        
        MirrorPacket packet = new MirrorPacket(direction.axis);
        packet.executeClient(player);
        
        if (stack.getItem() instanceof ILittleTool && !((ILittleTool) stack.getItem()).sendTransformationUpdate())
            return;
        
        LittleTiles.NETWORK.sendToServer(packet);
    }
    
    @SubscribeEvent
    public void drawHighlight(DrawBlockHighlightEvent event) {
        Player player = event.getPlayer();
        Level world = player.world;
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
