package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.IngredientUtils;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;

public class ItemBlockIngredient extends Item implements ILittleIngredientInventory {
    
    public ItemBlockIngredient() {
        super(new Item.Properties().stacksTo(1));
    }
    
    public static BlockIngredientEntry loadIngredient(ItemStack stack) {
        if (stack.hasTag())
            return IngredientUtils.loadBlockIngredient(stack.getTag());
        return null;
    }
    
    public static void saveIngredient(ItemStack stack, BlockIngredientEntry entry) {
        entry.save(stack.getTag());
    }
    
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return false;
    }
    
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack me, ItemStack other, Slot p_150894_, ClickAction action, Player player, SlotAccess slot) {
        return false;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        BlockIngredientEntry entry = loadIngredient(stack);
        if (entry != null) {
            return entry.getBlockStack().getDisplayName();
        } else
            return super.getName(stack);
    }
    
    @Override
    public boolean shouldOverrideMultiplayerNbt() {
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        BlockIngredientEntry entry = loadIngredient(stack);
        if (entry != null)
            tooltip.add(BlockIngredient.printVolume(entry.value, false));
    }
    
    @Override
    public LittleIngredients getInventory(ItemStack stack) {
        BlockIngredientEntry entry = loadIngredient(stack);
        if (entry != null) {
            BlockIngredient ingredient = new BlockIngredient();
            ingredient.add(entry);
            return new LittleIngredients(ingredient.setLimits(1, 64)) {
                @Override
                protected boolean canAddNewIngredients() {
                    return false;
                }
                
                @Override
                protected boolean removeEmptyIngredients() {
                    return false;
                }
            };
        }
        return null;
    }
    
    @Override
    public void setInventory(ItemStack stack, LittleIngredients ingredients, LittleInventory inventory) {
        BlockIngredient blocks = ingredients.get(BlockIngredient.class);
        if (blocks != null && !blocks.isEmpty())
            for (BlockIngredientEntry entry : blocks)
                if (!entry.isEmpty() || entry.block instanceof AirBlock) {
                    if (inventory != null && entry.value >= 1) {
                        ItemStack overflow = entry.getBlockStack();
                        overflow.setCount((int) entry.value);
                        entry.value -= overflow.getCount();
                        inventory.addStack(overflow);
                    }
                    
                    if (entry.value > 0) {
                        saveIngredient(stack, entry);
                        return;
                    }
                }
            
        stack.setTag(null);
        stack.setCount(0);
    }
    
    @Override
    public boolean shouldBeMerged() {
        return true;
    }
    
}
