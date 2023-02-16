package team.creative.littletiles.common.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector.StackCollector;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector.StackSelector;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientSupplier;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.LittleIngredients;

public class LittleGuiUtils {
    
    public static class LittleBlockSelector extends GuiStackSelector.GuiBlockSelector {
        
        @Override
        public boolean allow(ItemStack stack) {
            if (super.allow(stack))
                return LittleAction.isBlockValid(Block.byItem(stack.getItem()).defaultBlockState());
            return false;
        }
        
    }
    
    public static class LittleBlockCollector extends GuiStackSelector.InventoryCollector {
        
        public LittleBlockCollector(StackSelector selector) {
            super(selector);
        }
        
        protected void collect(IItemHandler inventory, BlockIngredient ingredients) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.getItem() instanceof ILittleIngredientInventory) {
                    LittleIngredients ingredientsInventory = ((ILittleIngredientInventory) stack.getItem()).getInventory(stack);
                    if (ingredientsInventory != null && ingredientsInventory.contains(BlockIngredient.class))
                        ingredients.add(ingredientsInventory.get(BlockIngredient.class));
                } else {
                    LazyOptional<IItemHandler> optional = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if (optional.isPresent())
                        collect(optional.orElseThrow(RuntimeException::new), ingredients);
                }
                
            }
            
        }
        
        @Override
        public HashMapList<String, ItemStack> collect(Player player) {
            HashMapList<String, ItemStack> stacks = super.collect(player);
            
            BlockIngredient ingredients = new BlockIngredient();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof ILittleIngredientSupplier)
                    ((ILittleIngredientSupplier) stack.getItem()).collect(stacks, stack, player);
                else if (stack.getItem() instanceof ILittleIngredientInventory) {
                    LittleIngredients inventory = ((ILittleIngredientInventory) stack.getItem()).getInventory(stack);
                    if (inventory != null && inventory.contains(BlockIngredient.class))
                        ingredients.add(inventory.get(BlockIngredient.class));
                } else {
                    LazyOptional<IItemHandler> optional = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    if (optional.isPresent())
                        collect(optional.orElseThrow(RuntimeException::new), ingredients);
                }
            }
            
            List<ItemStack> newStacks = new ArrayList<>();
            for (BlockIngredientEntry ingredient : ingredients) {
                ItemStack stack = ingredient.getBlockStack();
                
                stack.setCount(Math.max(1, (int) ingredient.value));
                
                CompoundTag display = new CompoundTag();
                ListTag list = new ListTag();
                int blocks = (int) ingredient.value;
                double pixel = (ingredient.value - blocks) * LittleGrid.defaultGrid().count3d;
                list.add(StringTag.valueOf((blocks > 0 ? blocks + " blocks " : "") + (pixel > 0 ? (Math.round(pixel * 100) / 100) + " pixel" : "")));
                display.put(ItemStack.TAG_LORE, list);
                stack.addTagElement(ItemStack.TAG_DISPLAY, display);
                newStacks.add(stack);
            }
            stacks.add("selector.ingredients", newStacks);
            return stacks;
        }
        
    }
    
    public static StackCollector getCollector(Player player) {
        if (player.isCreative())
            return new GuiStackSelector.CreativeCollector(new LittleBlockSelector());
        return new LittleBlockCollector(new LittleBlockSelector());
    }
    
}
