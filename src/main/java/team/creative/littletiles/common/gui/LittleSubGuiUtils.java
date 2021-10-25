package team.creative.littletiles.common.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector.StackCollector;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector.StackSelector;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientSupplier;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.LittleIngredients;

public class LittleSubGuiUtils {
    
    public static class LittleBlockSelector extends GuiStackSelectorAll.GuiBlockSelector {
        
        @Override
        public boolean allow(ItemStack stack) {
            if (super.allow(stack))
                return LittleAction.isBlockValid(BlockUtils.getState(stack));
            return false;
        }
        
    }
    
    public static class LittleBlockCollector extends GuiStackSelectorAll.InventoryCollector {
        
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
                } else if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
                    collect(stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), ingredients);
                
            }
            
        }
        
        @Override
        public HashMapList<String, ItemStack> collect(EntityPlayer player) {
            HashMapList<String, ItemStack> stacks = super.collect(player);
            
            BlockIngredient ingredients = new BlockIngredient();
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack.getItem() instanceof ILittleIngredientSupplier)
                    ((ILittleIngredientSupplier) stack.getItem()).collect(stacks, stack, player);
                else if (stack.getItem() instanceof ILittleIngredientInventory) {
                    LittleIngredients inventory = ((ILittleIngredientInventory) stack.getItem()).getInventory(stack);
                    if (inventory != null && inventory.contains(BlockIngredient.class))
                        ingredients.add(inventory.get(BlockIngredient.class));
                } else if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
                    collect(stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), ingredients);
            }
            
            List<ItemStack> newStacks = new ArrayList<>();
            for (BlockIngredientEntry ingredient : ingredients) {
                ItemStack stack = ingredient.getItemStack();
                
                stack.setCount(Math.max(1, (int) ingredient.value));
                
                NBTTagCompound display = new NBTTagCompound();
                NBTTagList list = new NBTTagList();
                int blocks = (int) ingredient.value;
                double pixel = (ingredient.value - blocks) * LittleGridContext.get().maxTilesPerBlock;
                list.appendTag(new NBTTagString((blocks > 0 ? blocks + " blocks " : "") + (pixel > 0 ? (Math.round(pixel * 100) / 100) + " pixel" : "")));
                display.setTag("Lore", list);
                stack.setTagInfo("display", display);
                newStacks.add(stack);
            }
            stacks.add("selector.ingredients", newStacks);
            return stacks;
        }
        
    }
    
    public static StackCollector getCollector(Player player) {
        if (player.isCreative())
            return new GuiStackSelectorAll.CreativeCollector(new LittleBlockSelector());
        return new LittleBlockCollector(new LittleBlockSelector());
    }
    
}
