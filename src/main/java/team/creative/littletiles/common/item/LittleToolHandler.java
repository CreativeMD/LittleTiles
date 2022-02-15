package team.creative.littletiles.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.event.PickBlockEvent;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;

public class LittleToolHandler {
    
    public static ItemStack lastSelectedItem = null;
    public static ILittleTool tool = null;
    
    private boolean leftClicked;
    
    @SubscribeEvent
    public void onMouseWheelClick(PickBlockEvent event) {
        if (event.result != null) {
            ItemStack stack = event.player.getMainHandItem();
            if (stack.getItem() instanceof ILittleTool && ((ILittleTool) stack.getItem())
                    .onMouseWheelClickBlock(event.level, event.player, stack, new PlacementPosition(event.result, ((ILittleTool) stack.getItem())
                            .getPositionGrid(stack)), event.result))
                event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onLeftClickAir(LeftClickEmpty event) {
        if (event.getWorld().isClientSide) {
            ItemStack stack = event.getItemStack();
            
            if (stack.getItem() instanceof ILittleTool)
                ((ILittleTool) stack.getItem()).onClickAir(event.getPlayer(), stack);
        }
    }
    
    @SubscribeEvent
    public void onLeftClick(LeftClickBlock event) {
        if (event.getWorld().isClientSide) {
            if (!leftClicked) {
                ItemStack stack = event.getItemStack();
                
                if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult))
                    return;
                
                BlockHitResult ray = (BlockHitResult) Minecraft.getInstance().hitResult;
                if (lastSelectedItem != null && lastSelectedItem.getItem() != stack.getItem()) {
                    tool.onClickAir(event.getPlayer(), lastSelectedItem);
                    lastSelectedItem = null;
                }
                
                if (stack.getItem() instanceof ILittleTool) {
                    if (((ILittleTool) stack.getItem())
                            .onClickBlock(event.getWorld(), event.getPlayer(), stack, new PlacementPosition(ray, ((ILittleTool) stack.getItem()).getPositionGrid(stack)), ray))
                        event.setCanceled(true);
                    tool = (ILittleTool) stack.getItem();
                    lastSelectedItem = stack;
                }
                
                leftClicked = true;
            }
        } else if (event.getItemStack().getItem() instanceof ILittleTool)
            event.setCanceled(true);
    }
    
    @SubscribeEvent
    public void breakSpeed(BreakSpeed event) {
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (stack.getItem() instanceof ILittleTool)
            event.setNewSpeed(0);
    }
    
    @SubscribeEvent
    public void onInteract(RightClickBlock event) {
        ItemStack stack = event.getPlayer().getMainHandItem();
        
        if (stack.getItem() instanceof ILittleTool) {
            if (event.getHand() == InteractionHand.MAIN_HAND && event.getWorld().isClientSide)
                if (onRightInteractClient((ILittleTool) stack.getItem(), event.getPlayer(), event.getHand(), event.getWorld(), stack, event.getPos(), Facing.get(event.getFace())))
                    event.setCanceled(true);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPosition getPosition(Level level, ILittleTool iTile, ItemStack stack, BlockHitResult result) {
        return PreviewRenderer.marked != null ? PreviewRenderer.marked.getPosition() : PlacementHelper.getPosition(level, result, iTile.getPositionGrid(stack), iTile, stack);
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean onRightInteractClient(ILittleTool iTile, Player player, InteractionHand hand, Level level, ItemStack stack, BlockPos pos, Facing facing) {
        if (iTile instanceof ILittlePlacer) {
            HitResult result = Minecraft.getInstance().hitResult;
            if (!(result instanceof BlockHitResult))
                return false;
            PlacementPosition position = getPosition(level, iTile, stack, (BlockHitResult) result);
            if (iTile.onRightClick(level, player, stack, position.copy(), (BlockHitResult) result) && ((ILittlePlacer) iTile).hasTiles(stack)) {
                if (!stack.isEmpty()) {
                    LittleTilesClient.ACTION_HANDLER.execute(new LittleActionPlace(PlaceAction.CURRENT_ITEM, PlacementPreview.relative(level, stack, position, false)));
                    PreviewRenderer.marked = null;
                }
                iTile.onDeselect(level, stack, player);
                return true;
            }
        }
        return false;
    }
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (leftClicked && !mc.options.keyAttack.isDown()) {
                leftClicked = false;
            }
            
            if (mc.player != null) {
                ItemStack stack = mc.player.getMainHandItem();
                
                if (lastSelectedItem != null && lastSelectedItem.getItem() != stack.getItem()) {
                    tool.onDeselect(mc.level, lastSelectedItem, mc.player);
                    lastSelectedItem = null;
                }
                
                while (LittleTilesClient.configure.consumeClick())
                    if (stack.getItem() instanceof ILittleTool) {
                        GuiConfigure gui = ((ILittleTool) stack.getItem()).getConfigure(mc.player, ContainerSlotView.mainHand(mc.player));
                        if (gui != null)
                            GuiHandler.openGui("configure", new CompoundTag(), mc.player);
                    }
                
                while (LittleTilesClient.configureAdvanced.consumeClick())
                    if (stack.getItem() instanceof ILittleTool) {
                        GuiConfigure gui = ((ILittleTool) stack.getItem()).getConfigureAdvanced(mc.player, ContainerSlotView.mainHand(mc.player));
                        if (gui != null)
                            GuiHandler.openGui("configureadvanced", new CompoundTag(), mc.player);
                    }
            }
        }
    }
    
    @SubscribeEvent
    public void onPickup(EntityItemPickupEvent event) {
        Player player = event.getPlayer();
        ItemEntity entityItem = event.getItem();
        ItemStack stack = entityItem.getItem();
        
        if (stack.getItem() instanceof ILittleIngredientInventory && ((ILittleIngredientInventory) stack.getItem()).shouldBeMerged()) {
            LittleIngredients ingredients = ((ILittleIngredientInventory) stack.getItem()).getInventory(stack);
            LittleInventory inventory = new LittleInventory(player);
            inventory.allowDrop = false;
            
            if (ingredients == null) {
                entityItem.kill();
                event.setCanceled(true);
                event.setResult(Result.DENY);
                return;
            }
            
            try {
                if (LittleAction.canGive(player, inventory, ingredients)) {
                    LittleAction.give(player, inventory, ingredients);
                    
                    player.onItemPickup(entityItem);
                    entityItem.kill();
                    
                    event.setCanceled(true);
                    event.setResult(Result.DENY);
                }
            } catch (NotEnoughIngredientsException e1) {
                
            }
        }
    }
}