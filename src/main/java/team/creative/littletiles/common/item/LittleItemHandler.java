package team.creative.littletiles.common.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;

public class LittleItemHandler {
    
    @SubscribeEvent
    public void breakSpeed(BreakSpeed event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        if (stack.getItem() instanceof ILittleTool)
            event.setNewSpeed(0);
    }
    
    @SubscribeEvent
    public void onLeftClick(LeftClickBlock event) {
        if (!event.getLevel().isClientSide && event.getItemStack().getItem() instanceof ILittleTool) {
            event.setCanceled(true);
            return;
        }
    }
    
    @SubscribeEvent
    public void onPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemEntity entityItem = event.getItemEntity();
        ItemStack stack = entityItem.getItem();
        
        if (!entityItem.hasPickUpDelay() && stack.getItem() instanceof ILittleIngredientInventory inv && inv.shouldBeMerged()) {
            LittleIngredients ingredients = inv.getInventory(stack);
            LittleInventory inventory = new LittleInventory(player);
            inventory.allowDrop = false;
            
            if (ingredients == null) {
                entityItem.kill();
                event.setCanPickup(TriState.FALSE);
                return;
            }
            
            try {
                if (LittleAction.canGive(player, inventory, ingredients)) {
                    LittleAction.give(player, inventory, ingredients);
                    
                    player.onItemPickup(entityItem);
                    entityItem.kill();
                    
                    event.setCanPickup(TriState.FALSE);
                    player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.level().random
                            .nextFloat() - player.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            } catch (NotEnoughIngredientsException e1) {}
        }
    }
}
