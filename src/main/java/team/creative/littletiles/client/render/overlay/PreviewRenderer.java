package team.creative.littletiles.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.math.vec.LittleHitResult;

public class PreviewRenderer implements LevelAwareHandler {
    
    public static final ResourceLocation WHITE_TEXTURE = ResourceLocation.tryBuild(LittleTiles.MODID, "textures/preview.png");
    public static final Minecraft mc = Minecraft.getInstance();
    
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
    
    private ItemStack lastHeld = ItemStack.EMPTY;
    private Iterable<LittleTool> tools;
    
    public PreviewRenderer() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    public Iterable<LittleTool> tools() {
        return tools;
    }
    
    public void clearToolPreviews() {
        lastHeld = ItemStack.EMPTY;
        if (tools != null)
            for (LittleTool p : tools)
                p.removed();
        tools = null;
    }
    
    @Override
    public void unload() {
        clearToolPreviews();
    }
    
    @SubscribeEvent
    public void tick(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_WEATHER)
            return;
        if (mc.player == null || mc.screen instanceof IGuiParent)
            return;
        
        Level level = mc.level;
        Player player = mc.player;
        ItemStack stack = player.getMainHandItem();
        PoseStack pose = new PoseStack();
        
        if (!ItemStack.isSameItem(stack, lastHeld)) {
            clearToolPreviews();
            if (stack.getItem() instanceof ILittleTool tool)
                tools = tool.tools(stack);
            else
                tools = null;
            lastHeld = stack.copy();
        }
        
        if (!LittleAction.canPlace(player) || mc.options.hideGui) {
            processKeys(level, player, stack, false); // Make sure the actions are not queued and performed once things can be placed
            return;
        }
        
        if (tools != null) {
            BlockHitResult blockHit = blockHit();
            for (LittleTool t : tools) {
                t.stack = stack;
                t.tick(level, player, blockHit);
            }
        }
        
        processKeys(level, player, stack, true);
        
        if (tools != null) {
            Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
            RenderSystem.enableBlend();
            
            for (LittleTool t : tools)
                t.render(level, player, pose, cam, false);
            
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }
    
    private void processKeys(Level level, Player player, ItemStack stack, boolean execute) {
        while (LittleTilesClient.KEY_UNDO.consumeClick()) {
            try {
                if (execute && LittleActionHandlerClient.canUseUndoOrRedo())
                    LittleTilesClient.ACTION_HANDLER.undo();
            } catch (LittleActionException e) {
                LittleActionHandlerClient.handleException(e);
            }
        }
        
        while (LittleTilesClient.KEY_REDO.consumeClick()) {
            try {
                if (execute && LittleActionHandlerClient.canUseUndoOrRedo())
                    LittleTilesClient.ACTION_HANDLER.redo();
            } catch (LittleActionException e) {
                LittleActionHandlerClient.handleException(e);
            }
        }
        
        while (LittleTilesClient.KEY_CONFIGURE.consumeClick())
            if (stack.getItem() instanceof ILittleTool t) {
                GuiConfigure gui = t.getConfigure(mc.player, ContainerSlotView.mainHand(mc.player), false);
                if (gui != null)
                    LittleTilesGuiRegistry.OPEN_CONFIG.open(mc.player);
            }
        
        while (LittleTilesClient.KEY_CONFIGURE_SECONDARY.consumeClick())
            if (stack.getItem() instanceof ILittleTool t) {
                GuiConfigure gui = t.getConfigure(mc.player, ContainerSlotView.mainHand(mc.player), true);
                if (gui != null) {
                    CompoundTag nbt = new CompoundTag();
                    nbt.putBoolean("second", true);
                    LittleTilesGuiRegistry.OPEN_CONFIG.open(nbt, mc.player);
                }
            }
        
        for (KeyMapping key : LittleTilesClient.TOOL_KEYS)
            while (key.consumeClick())
                if (execute && tools != null)
                    for (LittleTool p : tools)
                        if (p.keyPressed(level, player, key))
                            break;
    }
    
    @SubscribeEvent
    public void drawNonHighlight(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_BLOCK_ENTITIES)
            return;
        if (mc.getCameraEntity() instanceof Player && !mc.options.hideGui && mc.hitResult != null && mc.hitResult.getType() == Type.MISS && tools != null) {
            Player player = mc.player;
            Level level = player.level();
            Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
            
            for (LittleTool t : tools)
                t.render(level, player, event.getPoseStack(), cam, true);
        }
    }
    
    @SubscribeEvent
    public void drawHighlight(RenderHighlightEvent.Block event) {
        Player player = mc.player;
        Level level = player.level();
        
        if (!LittleAction.canPlace(player))
            return;
        
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        
        PoseStack pose = event.getPoseStack();
        if (tools != null)
            for (LittleTool t : tools)
                t.render(level, player, pose, cam, true);
            
        if (!event.isCanceled() && level.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof BlockTile && level.getWorldBorder().isWithinBounds(event.getTarget()
                .getBlockPos())) {
            BlockPos pos = event.getTarget().getBlockPos();
            BlockState state = level.getBlockState(pos);
            VoxelShape shape;
            if (state.getBlock() instanceof BlockTile block)
                shape = block.getSelectionShape(level, pos);
            else
                shape = state.getShape(level, pos, CollisionContext.of(player));
            renderShape(pose, event.getMultiBufferSource().getBuffer(RenderType.lines()), shape, pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, 0.0F, 0.0F, 0.0F,
                0.4F);
            event.setCanceled(true);
        }
        
        RenderSystem.enableCull();
    }
    
    public BlockHitResult blockHit() {
        if (mc.hitResult instanceof BlockHitResult b)
            return b;
        if (mc.hitResult instanceof LittleHitResult result && result.isBlock())
            return result.asBlockHit();
        return null;
    }
    
    @SubscribeEvent
    public void onMouseWheelClick(InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock() || tools == null)
            return;
        BlockHitResult hit = blockHit();
        
        for (LittleTool t : tools)
            if (t.onMouseWheelClickBlock(mc.level, mc.player, hit)) {
                event.setCanceled(true);
                return;
            }
    }
    
    @SubscribeEvent
    public void onLeftClickAir(LeftClickEmpty event) {
        if (!event.getLevel().isClientSide || tools == null)
            return;
        
        var hit = blockHit();
        for (LittleTool t : tools)
            t.onLeftClick(event.getLevel(), event.getEntity(), hit);
    }
    
    @SubscribeEvent
    public void onLeftClickBlock(LeftClickBlock event) {
        if (!event.getLevel().isClientSide || tools == null || event.getAction() != Action.START)
            return;
        
        var hit = blockHit();
        for (LittleTool t : tools)
            if (t.onLeftClick(event.getLevel(), event.getEntity(), hit))
                event.setUseItem(TriState.TRUE);
    }
    
    @SubscribeEvent
    public void onRightClickAir(RightClickEmpty event) {
        if (!event.getLevel().isClientSide || tools == null)
            return;
        var hit = blockHit();
        for (LittleTool t : tools)
            t.onRightClick(event.getLevel(), event.getEntity(), hit);
    }
    
    @SubscribeEvent
    public void onRightClickBlock(RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getLevel().isClientSide || tools == null)
            return;
        
        var hit = blockHit();
        for (LittleTool t : tools)
            if (t.onRightClick(mc.level, mc.player, hit)) {
                event.setCancellationResult(InteractionResult.CONSUME);
                event.setCanceled(true);
            }
    }
    
}
