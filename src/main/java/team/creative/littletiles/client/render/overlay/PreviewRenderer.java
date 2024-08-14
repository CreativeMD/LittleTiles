package team.creative.littletiles.client.render.overlay;

import java.util.List;

import org.lwjgl.opengl.GL14;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleEditor;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.mc.BlockTile;
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
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

public class PreviewRenderer implements LevelAwareHandler {
    
    public static final ResourceLocation WHITE_TEXTURE = ResourceLocation.tryBuild(LittleTiles.MODID, "textures/preview.png");
    public static Minecraft mc = Minecraft.getInstance();
    
    private boolean lastLowResolution;
    private CompoundTag lastCached;
    private PlacementPreview lastPreviews;
    private PlacementMode lastMode;
    private IMarkMode marked;
    
    public PreviewRenderer() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    public PlacementPosition getPosition(Player player, Level level, ItemStack stack, BlockHitResult result) {
        ILittleTool iTile = (ILittleTool) stack.getItem();
        return marked != null ? marked.getPosition() : PlacementHelper.getPosition(level, result, iTile.getPositionGrid(player, stack), iTile, stack);
    }
    
    /** @param centered
     *            if the previews should be centered
     * @param facing
     *            if centered is true it will be used to apply the offset
     * @param fixed
     *            if the previews should keep it's original boxes */
    public PlacementPreview getPreviews(Entity entity, Level level, ItemStack stack, PlacementPosition position, boolean centered, boolean fixed, boolean allowLowResolution) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        
        var tag = ILittleTool.getData(stack);
        PlacementMode mode = iTile.getPlacementMode(stack);
        PlacementPreview preview = allowLowResolution == lastLowResolution && iTile.shouldCache() && lastCached != null && lastCached.equals(tag) && lastMode == mode ? lastPreviews
                .copy() : null;
        if (preview != null)
            try {
                preview.moveRelative(entity, stack, position, centered, fixed);
            } catch (MissingAnimationException e) {
                preview = null;
            }
        
        if (preview == null && iTile != null)
            preview = iTile.getPlacement((Player) entity, level, stack, position, allowLowResolution);
        
        if (preview != null)
            if (tag.isEmpty()) {
                lastCached = null;
                lastPreviews = null;
                lastMode = null;
            } else {
                lastLowResolution = allowLowResolution;
                lastCached = tag.copy();
                lastPreviews = preview.copy();
                lastMode = mode;
            }
        return preview;
    }
    
    public void removeMarked() {
        marked = null;
    }
    
    public void removeCache() {
        lastCached = null;
        lastPreviews = null;
        lastLowResolution = false;
        lastMode = null;
    }
    
    public boolean isCentered(ItemStack stack, ILittlePlacer iTile) {
        if (!iTile.canSnapToGrid(stack))
            return marked == null;
        if (iTile.snapToGridByDefault(stack))
            return LittleActionHandlerClient.isUsingSecondMode() && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid == LittleActionHandlerClient.isUsingSecondMode() || marked != null;
    }
    
    public boolean isFixed(ItemStack stack, ILittlePlacer iTile) {
        if (!iTile.canSnapToGrid(stack))
            return marked != null;
        if (iTile.snapToGridByDefault(stack))
            return !LittleActionHandlerClient.isUsingSecondMode() && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid != LittleActionHandlerClient.isUsingSecondMode() && marked == null;
    }
    
    @Override
    public void unload() {
        removeMarked();
        removeCache();
    }
    
    private void handleUndoAndRedo(boolean execute) {
        while (LittleTilesClient.undo.consumeClick()) {
            try {
                if (execute && LittleActionHandlerClient.canUseUndoOrRedo())
                    LittleTilesClient.ACTION_HANDLER.undo();
            } catch (LittleActionException e) {
                LittleActionHandlerClient.handleException(e);
            }
        }
        
        while (LittleTilesClient.redo.consumeClick()) {
            try {
                if (execute && LittleActionHandlerClient.canUseUndoOrRedo())
                    LittleTilesClient.ACTION_HANDLER.redo();
            } catch (LittleActionException e) {
                LittleActionHandlerClient.handleException(e);
            }
        }
    }
    
    @SubscribeEvent
    public void tick(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_WEATHER)
            return;
        if (mc.player != null) {
            Level level = mc.level;
            Player player = mc.player;
            ItemStack stack = mc.player.getMainHandItem();
            PoseStack pose = new PoseStack();
            
            if (!LittleAction.canPlace(player)) {
                handleUndoAndRedo(false);
                return;
            }
            
            handleUndoAndRedo(true);
            
            if (mc.options.hideGui)
                return;
            
            Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
            
            if (stack.getItem() instanceof ILittleTool tool && (marked != null || mc.hitResult.getType() == Type.BLOCK)) {
                BlockHitResult blockHit = mc.hitResult instanceof BlockHitResult ? (BlockHitResult) mc.hitResult : null;
                
                PlacementPosition position = marked != null ? marked.getPosition() : PlacementHelper.getPosition(level, blockHit, tool.getPositionGrid(player, stack), tool, stack);
                
                processKeys(stack, tool.getPositionGrid(player, stack));
                
                tool.tick(player, stack, position, blockHit);
                
                if (PlacementHelper.isLittleBlock(stack)) {
                    
                    ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
                    
                    PlacementMode mode = iTile.getPlacementMode(stack);
                    
                    if (mode.getPreviewMode() == PreviewMode.PREVIEWS) {
                        RenderSystem.enableBlend();
                        
                        if (LittleTiles.CONFIG.rendering.darkerPreviewBoxShading) {
                            GL14.glBlendColor(0.25F, 0.25F, 0.25F, 0.25F);
                            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.CONSTANT_COLOR, GlStateManager.DestFactor.ONE_MINUS_DST_COLOR,
                                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        } else
                            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                                GlStateManager.DestFactor.ZERO);
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                        
                        RenderSystem.setShaderTexture(0, WHITE_TEXTURE);
                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.depthMask(Minecraft.useShaderTransparency());
                        
                        boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
                        PlacementPreview result = getPreviews(player, level, stack, position, isCentered(stack, iTile), isFixed(stack, iTile), allowLowResolution);
                        
                        if (result != null) {
                            BlockPos pos = result.position.getPos();
                            
                            pose.pushPose();
                            pose.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
                            processMarkKey(player, iTile, stack, result);
                            
                            double alpha = (float) (Math.sin(System.nanoTime() / 200000000D) * 0.2 + 0.5);
                            int colorAlpha = (int) (alpha * iTile.getPreviewAlphaFactor() * 255);
                            
                            for (RenderBox box : result.previews.getPlaceBoxes(result.position.getVec()))
                                box.renderPreview(pose, colorAlpha);
                            
                            if (LittleActionHandlerClient.isUsingSecondMode() != iTile.snapToGridByDefault(stack)) {
                                List<RenderBox> cubes = iTile.getPositingCubes(level, pos, stack);
                                if (cubes != null)
                                    for (RenderBox cube : cubes)
                                        cube.renderPreview(pose, colorAlpha);
                            }
                            
                            pose.popPose();
                        }
                        
                        RenderSystem.depthMask(true);
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
                    
                } else
                    processMarkKey(player, (ILittleTool) stack.getItem(), stack, null);
                
                pose.pushPose();
                pose.translate(-cam.x, -cam.y, -cam.z);
                tool.render(player, stack, pose);
                if (marked != null)
                    marked.render(tool.getPositionGrid(player, stack), pose);
                pose.popPose();
            }
        } else
            marked = null;
    }
    
    public void processMarkKey(Player player, ILittleTool iTile, ItemStack stack, PlacementPreview preview) {
        while (LittleTilesClient.mark.consumeClick()) {
            if (marked == null) {
                marked = iTile.onMark(player, stack, getPosition(player, player.level(), stack, (BlockHitResult) mc.hitResult), (BlockHitResult) mc.hitResult, preview);
                if (Screen.hasControlDown())
                    GuiCreator.openClientSide(marked.getConfigurationGui());
            } else {
                if (Screen.hasControlDown())
                    GuiCreator.openClientSide(marked.getConfigurationGui());
                else {
                    marked.done();
                    marked = null;
                }
            }
        }
    }
    
    public void processRotateKey(Player player, Rotation rotation, ItemStack stack) {
        RotatePacket packet = new RotatePacket(rotation);
        packet.executeClient(player);
        
        if (stack.getItem() instanceof ILittleTool && !((ILittleTool) stack.getItem()).sendTransformationUpdate())
            return;
        
        LittleTiles.NETWORK.sendToServer(packet);
    }
    
    public void processKeys(ItemStack stack, LittleGrid grid) {
        while (LittleTilesClient.mirror.consumeClick())
            processMirrorKey(mc.player, stack);
        
        // Rotate Block
        while (LittleTilesClient.up.consumeClick()) {
            if (marked != null)
                marked.move(grid, LittleActionHandlerClient.isUsingSecondMode() ? Facing.UP : Facing.EAST);
            else
                processRotateKey(mc.player, Rotation.Z_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.down.consumeClick()) {
            if (marked != null)
                marked.move(grid, LittleActionHandlerClient.isUsingSecondMode() ? Facing.DOWN : Facing.WEST);
            else
                processRotateKey(mc.player, Rotation.Z_COUNTER_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.right.consumeClick()) {
            if (marked != null)
                marked.move(grid, Facing.SOUTH);
            else
                processRotateKey(mc.player, Rotation.Y_COUNTER_CLOCKWISE, stack);
        }
        
        while (LittleTilesClient.left.consumeClick()) {
            if (marked != null)
                marked.move(grid, Facing.NORTH);
            else
                processRotateKey(mc.player, Rotation.Y_CLOCKWISE, stack);
        }
    }
    
    public void processMirrorKey(Player player, ItemStack stack) {
        Facing direction = Facing.get(player.getDirection());
        if (player.getXRot() > 45)
            direction = Facing.DOWN;
        if (player.getXRot() < -45)
            direction = Facing.UP;
        
        MirrorPacket packet = new MirrorPacket(direction.axis);
        packet.executeClient(player);
        
        if (stack.getItem() instanceof ILittleTool && !((ILittleTool) stack.getItem()).sendTransformationUpdate())
            return;
        
        LittleTiles.NETWORK.sendToServer(packet);
    }
    
    @SubscribeEvent
    public void drawHighlight(RenderHighlightEvent.Block event) {
        Player player = mc.player;
        Level level = player.level();
        ItemStack stack = player.getMainHandItem();
        
        if (!LittleAction.canPlace(player))
            return;
        
        Vec3 vec = mc.gameRenderer.getMainCamera().getPosition();
        
        PoseStack pose = event.getPoseStack();
        
        Tesselator tesselator = Tesselator.getInstance();
        
        if ((event.getTarget().getType() == Type.BLOCK || marked != null) && stack.getItem() instanceof ILittleTool) {
            
            BlockHitResult blockHit = event.getTarget().getType() == Type.BLOCK ? (BlockHitResult) event.getTarget() : null;
            BlockPos pos = marked != null ? marked.getPosition().getPos() : blockHit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            
            Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
            
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            
            if (stack.getItem() instanceof ILittleEditor selector) {
                processMarkKey(player, selector, stack, null);
                PlacementPosition result = getPosition(player, level, stack, blockHit);
                
                if (selector.hasCustomBoxes(level, stack, player, state, result, blockHit) || marked != null) {
                    LittleBoxes boxes = selector.getBoxes(level, stack, player, result, blockHit);
                    
                    RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
                    
                    BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                    
                    pose.pushPose();
                    pose.translate(boxes.pos.getX() - cam.x, boxes.pos.getY() - cam.y, boxes.pos.getZ() - cam.z);
                    RenderSystem.lineWidth(2.0F);
                    for (LittleBox box : boxes.all()) {
                        LittleRenderBox cube = box.getRenderingBox(boxes.getGrid());
                        if (cube != null) {
                            cube.color = 0;
                            cube.renderLines(pose, bufferbuilder, 255, cube.getCenter(), 0.002);
                        }
                    }
                    BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
                    pose.popPose();
                    
                    bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                    
                    RenderSystem.lineWidth(1.0F);
                    renderHitOutline(pose, level, bufferbuilder, player, vec.x, vec.y, vec.z, pos);
                    BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
                    
                    RenderSystem.lineWidth(2.0F);
                    event.setCanceled(true);
                }
            } else if (stack.getItem() instanceof ILittlePlacer iTile) {
                PlacementMode mode = iTile.getPlacementMode(stack);
                if (mode.getPreviewMode() == PreviewMode.LINES) {
                    
                    PlacementPosition position = getPosition(player, level, stack, blockHit);
                    boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
                    
                    PlacementPreview result = getPreviews(player, level, stack, position, isCentered(stack, iTile), isFixed(stack, iTile), allowLowResolution);
                    
                    if (result != null) {
                        processMarkKey(player, iTile, stack, result);
                        
                        pose.pushPose();
                        BlockPos renderCenter = result.position.getPos();
                        pose.translate(renderCenter.getX() - cam.x, renderCenter.getY() - cam.y, renderCenter.getZ() - cam.z);
                        
                        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
                        RenderSystem.lineWidth((float) LittleTiles.CONFIG.rendering.previewLineThickness);
                        
                        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                        
                        int colorAlpha = 255;
                        for (RenderBox box : result.previews.getPlaceBoxes(result.position.getVec()))
                            box.renderLines(pose, bufferbuilder, colorAlpha, box.getCenter(), 0.002);
                        
                        if (LittleActionHandlerClient.isUsingSecondMode() != iTile.snapToGridByDefault(stack)) {
                            List<RenderBox> cubes = iTile.getPositingCubes(level, pos, stack);
                            if (cubes != null)
                                for (RenderBox cube : cubes)
                                    cube.renderLines(pose, bufferbuilder, colorAlpha, cube.getCenter(), 0.002);
                        }
                        
                        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
                        pose.popPose();
                    }
                }
            }
        }
        
        if (!event.isCanceled() && level.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof BlockTile && level.getWorldBorder().isWithinBounds(event.getTarget()
                .getBlockPos())) {
            renderHitOutline(pose, level, event.getMultiBufferSource().getBuffer(RenderType.lines()), player, vec.x, vec.y, vec.z, event.getTarget().getBlockPos());
            event.setCanceled(true);
        }
        
        RenderSystem.enableCull();
    }
    
    private void renderHitOutline(PoseStack pose, Level level, VertexConsumer consumer, Entity entity, double x, double y, double z, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        VoxelShape shape;
        if (state.getBlock() instanceof BlockTile block)
            shape = block.getSelectionShape(level, pos);
        else
            shape = state.getShape(level, pos, CollisionContext.of(entity));
        renderShape(pose, consumer, shape, pos.getX() - x, pos.getY() - y, pos.getZ() - z, 0.0F, 0.0F, 0.0F, 0.4F);
    }
    
    public static void renderShape(PoseStack pose, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        PoseStack.Pose posestack$pose = pose.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float f = (float) (x2 - x1);
            float f1 = (float) (y2 - y1);
            float f2 = (float) (z2 - z1);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            consumer.addVertex(posestack$pose.pose(), (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).setColor(red, green, blue, alpha).setNormal(posestack$pose, f, f1, f2);
            consumer.addVertex(posestack$pose.pose(), (float) (x2 + x), (float) (y2 + y), (float) (z2 + z)).setColor(red, green, blue, alpha).setNormal(posestack$pose, f, f1, f2);
        });
    }
}
