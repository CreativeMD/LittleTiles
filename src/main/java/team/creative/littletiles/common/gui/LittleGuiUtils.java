package team.creative.littletiles.common.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
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
                    @Nullable
                    IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
                    if (handler != null)
                        collect(handler, ingredients);
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
                    IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
                    if (handler != null)
                        collect(handler, ingredients);
                }
            }
            
            List<ItemStack> newStacks = new ArrayList<>();
            for (BlockIngredientEntry ingredient : ingredients) {
                ItemStack stack = ingredient.getBlockStack();
                
                stack.setCount(Math.max(1, (int) ingredient.value));
                
                int blocks = (int) ingredient.value;
                double pixel = (ingredient.value - blocks) * LittleGrid.OVERALL_DEFAULT_COUNT3D;
                stack.set(DataComponents.LORE, new ItemLore(Arrays.asList(Component.literal((blocks > 0 ? blocks + " blocks " : "") + (pixel > 0 ? (Math.round(
                    pixel * 100) / 100) + " pixel" : "")))));
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
