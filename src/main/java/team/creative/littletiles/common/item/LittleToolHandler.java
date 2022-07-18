package team.creative.littletiles.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator.GuiCreatorBasic;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.event.PickBlockEvent;
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
import team.creative.littletiles.common.placement.PlacementPosition;

public class LittleToolHandler {
    
    public static ItemStack lastSelectedItem = null;
    public static ILittleTool tool = null;
    
    public static final GuiCreatorBasic OPEN_CONFIG = GuiCreator.register("configure", new GuiCreatorBasic((nbt, player) -> {
        if (player.getMainHandItem().getItem() instanceof ILittleTool)
            return ((ILittleTool) player.getMainHandItem().getItem()).getConfigure(player, ContainerSlotView.mainHand(player));
        return null;
    }));
    
    public static final GuiCreatorBasic OPEN_CONFIG_ADVANCED = GuiCreator.register("configureadvanced", new GuiCreatorBasic((nbt, player) -> {
        if (player.getMainHandItem().getItem() instanceof ILittleTool)
            return ((ILittleTool) player.getMainHandItem().getItem()).getConfigureAdvanced(player, ContainerSlotView.mainHand(player));
        return null;
    }));
    
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
        if (event.getLevel().isClientSide) {
            ItemStack stack = event.getItemStack();
            
            if (stack.getItem() instanceof ILittleTool)
                ((ILittleTool) stack.getItem()).onClickAir(event.getEntity(), stack);
        }
    }
    
    @SubscribeEvent
    public void onLeftClick(LeftClickBlock event) {
        if (event.getLevel().isClientSide) {
            if (!leftClicked) {
                ItemStack stack = event.getItemStack();
                
                if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult))
                    return;
                
                BlockHitResult ray = (BlockHitResult) Minecraft.getInstance().hitResult;
                if (lastSelectedItem != null && lastSelectedItem.getItem() != stack.getItem()) {
                    tool.onClickAir(event.getEntity(), lastSelectedItem);
                    lastSelectedItem = null;
                }
                
                if (stack.getItem() instanceof ILittleTool) {
                    PlacementPosition position = LittleTilesClient.PREVIEW_RENDERER.getPosition(event.getLevel(), stack, ray);
                    if (((ILittleTool) stack.getItem()).onClickBlock(event.getLevel(), event.getEntity(), stack, position, ray))
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
        ItemStack stack = event.getEntity().getMainHandItem();
        if (stack.getItem() instanceof ILittleTool)
            event.setNewSpeed(0);
    }
    
    @SubscribeEvent
    public void onInteract(RightClickBlock event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        
        if (stack.getItem() instanceof ILittleTool) {
            if (event.getHand() == InteractionHand.MAIN_HAND && event.getLevel().isClientSide)
                if (onRightInteractClient((ILittleTool) stack.getItem(), event.getEntity(), event.getHand(), event.getLevel(), stack, event.getPos(), Facing.get(event.getFace())))
                    event.setCanceled(true);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean onRightInteractClient(ILittleTool iTile, Player player, InteractionHand hand, Level level, ItemStack stack, BlockPos pos, Facing facing) {
        HitResult result = Minecraft.getInstance().hitResult;
        if (!(result instanceof BlockHitResult))
            return false;
        PlacementPosition position = LittleTilesClient.PREVIEW_RENDERER.getPosition(level, stack, (BlockHitResult) result);
        if (iTile.onRightClick(level, player, stack, position.copy(), (BlockHitResult) result) && iTile instanceof ILittlePlacer placer && placer.hasTiles(stack)) {
            LittleTilesClient.ACTION_HANDLER.execute(new LittleActionPlace(PlaceAction.CURRENT_ITEM, placer.getPlacement(level, stack, position, false)));
            LittleTilesClient.PREVIEW_RENDERER.removeMarked();
            iTile.onDeselect(level, stack, player);
            return true;
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
                            OPEN_CONFIG.open(mc.player);
                    }
                
                while (LittleTilesClient.configureAdvanced.consumeClick())
                    if (stack.getItem() instanceof ILittleTool) {
                        GuiConfigure gui = ((ILittleTool) stack.getItem()).getConfigureAdvanced(mc.player, ContainerSlotView.mainHand(mc.player));
                        if (gui != null)
                            OPEN_CONFIG_ADVANCED.open(mc.player);
                    }
            }
        }
    }
    
    @SubscribeEvent
    public void onPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
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
