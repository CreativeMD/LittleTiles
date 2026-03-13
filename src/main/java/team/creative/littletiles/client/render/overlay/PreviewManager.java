package team.creative.littletiles.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.math.vec.LittleHitResult;

public class PreviewManager implements LevelAwareHandler {
    
    public static final ResourceLocation WHITE_TEXTURE = ResourceLocation.tryBuild(LittleTiles.MODID, "textures/preview.png");
    public static final Minecraft MC = Minecraft.getInstance();
    
    public final PreviewRenderer renderer = new PreviewRenderer(this);
    private ItemStack lastHeld = ItemStack.EMPTY;
    private LittleTool tool;
    
    public PreviewManager() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    public LittleTool tool() {
        return tool;
    }
    
    public void clearToolPreviews() {
        lastHeld = ItemStack.EMPTY;
        if (tool != null)
            tool.remove();
        tool = null;
    }
    
    @Override
    public void unload() {
        clearToolPreviews();
        for (BuildingModeFeature feature : BuildingModeFeatures.REGISTRY.values())
            feature.unloadLevel();
    }
    
    @SubscribeEvent
    protected void tick(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_WEATHER)
            return;
        if (MC.player == null || MC.screen instanceof IGuiParent)
            return;
        
        Player player = MC.player;
        ItemStack stack = player.getMainHandItem();
        PoseStack pose = new PoseStack();
        
        if (!ItemStack.isSameItem(stack, lastHeld) || (stack.getItem() instanceof ILittleTool tool && !tool.isCorrectTool(stack, this.tool))) {
            boolean buildingMode = tool.buildingMode();
            clearToolPreviews();
            if (stack.getItem() instanceof ILittleTool tool)
                this.tool = tool.tool(stack);
            else
                this.tool = null;
            if (tool != null && buildingMode)
                tool.startBuildingMode();
            lastHeld = stack.copy();
        }
        
        if (!LittleAction.canPlace(player) || MC.options.hideGui) {
            processKeys(stack, false); // Make sure the actions are not queued and performed once things can be placed
            return;
        }
        
        if (tool != null) {
            tool.stack = stack;
            tool.tick(renderer);
        }
        
        processKeys(stack, true);
        
        if (tool != null) {
            Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
            RenderSystem.enableBlend();
            
            tool.render(renderer, pose, cam, false);
            
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }
    
    private void processKeys(ItemStack stack, boolean execute) {
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
                GuiConfigure gui = t.getConfigure(MC.player, ContainerSlotView.mainHand(MC.player), false);
                if (gui != null)
                    LittleTilesGuiRegistry.OPEN_CONFIG.open(MC.player);
            }
        
        while (LittleTilesClient.KEY_CONFIGURE_SECONDARY.consumeClick())
            if (stack.getItem() instanceof ILittleTool t) {
                GuiConfigure gui = t.getConfigure(MC.player, ContainerSlotView.mainHand(MC.player), true);
                if (gui != null) {
                    CompoundTag nbt = new CompoundTag();
                    nbt.putBoolean("second", true);
                    LittleTilesGuiRegistry.OPEN_CONFIG.open(nbt, MC.player);
                }
            }
        
        for (KeyMapping key : LittleTilesClient.TOOL_KEYS)
            while (key.consumeClick())
                if (execute && tool != null)
                    if (tool.toolKeyPressed(renderer, key))
                        break;
    }
    
    @SubscribeEvent
    protected void drawNonHighlight(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_BLOCK_ENTITIES)
            return;
        if (MC.getCameraEntity() instanceof Player && !MC.options.hideGui && MC.hitResult != null && MC.hitResult.getType() == Type.MISS && tool != null)
            tool.render(renderer, event.getPoseStack(), MC.gameRenderer.getMainCamera().getPosition(), true);
    }
    
    @SubscribeEvent
    protected void drawHighlight(RenderHighlightEvent.Block event) {
        Player player = MC.player;
        Level level = player.level();
        
        if (!LittleAction.canPlace(player))
            return;
        
        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        
        PoseStack pose = event.getPoseStack();
        if (tool != null)
            tool.render(renderer, pose, cam, true);
        
        if (!event.isCanceled() && level.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof BlockTile && level.getWorldBorder().isWithinBounds(event.getTarget()
                .getBlockPos())) {
            BlockPos pos = event.getTarget().getBlockPos();
            BlockState state = level.getBlockState(pos);
            VoxelShape shape;
            if (state.getBlock() instanceof BlockTile block)
                shape = block.getSelectionShape(level, pos);
            else
                shape = state.getShape(level, pos, CollisionContext.of(player));
            PreviewRenderer.renderShape(pose, event.getMultiBufferSource().getBuffer(RenderType.lines()), shape, pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, 0.0F,
                0.0F, 0.0F, 0.4F);
            event.setCanceled(true);
        }
        
        RenderSystem.enableCull();
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        if (tool != null)
            return tool.keyPressed(renderer, keyCode, scanCode, action, modifiers);
        return false;
    }
    
    public Player player() {
        return MC.player;
    }
    
    public Level level() {
        return MC.level;
    }
    
    public BlockHitResult blockHit() {
        if (MC.hitResult instanceof BlockHitResult b)
            return b;
        if (MC.hitResult instanceof LittleHitResult result && result.isBlock())
            return result.asBlockHit();
        return null;
    }
    
    @SubscribeEvent
    protected void onMouseWheelClick(InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock() || tool == null)
            return;
        BlockHitResult hit = blockHit();
        
        if (tool.onMouseWheelClickBlock(renderer, hit)) {
            event.setCanceled(true);
            return;
        }
    }
    
    @SubscribeEvent
    protected void onLeftClickAir(LeftClickEmpty event) {
        if (!event.getLevel().isClientSide || tool == null)
            return;
        
        var hit = blockHit();
        tool.onLeftClick(renderer, hit);
    }
    
    @SubscribeEvent
    protected void onLeftClickBlock(LeftClickBlock event) {
        if (!event.getLevel().isClientSide || tool == null || event.getAction() != Action.START)
            return;
        
        var hit = blockHit();
        if (tool.onLeftClick(renderer, hit))
            event.setUseItem(TriState.TRUE);
    }
    
    @SubscribeEvent
    protected void onRightClickAir(RightClickEmpty event) {
        if (!event.getLevel().isClientSide || tool == null)
            return;
        var hit = blockHit();
        tool.onRightClick(renderer, hit);
    }
    
    @SubscribeEvent
    protected void onRightClickBlock(RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getLevel().isClientSide || tool == null)
            return;
        
        var hit = blockHit();
        if (tool.onRightClick(renderer, hit)) {
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }
    
}
