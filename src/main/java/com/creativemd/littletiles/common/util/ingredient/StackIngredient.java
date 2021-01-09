package com.creativemd.littletiles.common.util.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.type.LinkedHashMapInteger;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class StackIngredient extends LittleIngredient<StackIngredient> implements Iterable<StackIngredientEntry> {
    
    private List<StackIngredientEntry> content = new ArrayList<>();
    
    private int stackLimit = -1;
    private int maxEntries = -1;
    
    public StackIngredient() {
        
    }
    
    public StackIngredient(List<ItemStack> stacks) {
        stacks.forEach((x) -> content.add(new StackIngredientEntry(x, x.getCount())));
    }
    
    public StackIngredient(IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty())
                add(new StackIngredientEntry(stack, stack.getCount())); // Might be bad for performance (for huge inventories)
        }
    }
    
    public StackIngredient setLimits(int maxEntries, int stackLimit) {
        this.maxEntries = maxEntries;
        this.stackLimit = stackLimit;
        return this;
    }
    
    @Override
    public Iterator<StackIngredientEntry> iterator() {
        return content.iterator();
    }
    
    @Override
    public StackIngredient copy() {
        StackIngredient copy = new StackIngredient();
        copy.stackLimit = stackLimit;
        copy.maxEntries = maxEntries;
        content.forEach((x) -> copy.content.add(x.copy()));
        return copy;
    }
    
    @Override
    public void print(List<String> lines, List<ItemStack> stacks) {
        for (StackIngredientEntry entry : content) {
            lines.add(entry.stack.getDisplayName());
            stacks.add(entry.stack);
        }
    }
    
    public StackIngredientEntry add(StackIngredientEntry ingredient) {
        if (ingredient.isEmpty())
            return null;
        
        for (int i = 0; i < content.size(); i++) {
            StackIngredientEntry entry = content.get(i);
            if (entry.equals(ingredient)) {
                entry.count += ingredient.count;
                if (stackLimit != -1 && entry.count > stackLimit) {
                    StackIngredientEntry remaining = entry.copy();
                    remaining.count = entry.count - stackLimit;
                    entry.count = stackLimit;
                    ingredient = remaining;
                } else
                    return null;
            }
        }
        
        if (maxEntries == -1 || content.size() < maxEntries) {
            content.add(ingredient.copy());
            return null;
        }
        return ingredient;
    }
    
    @Override
    public StackIngredient add(StackIngredient ingredient) {
        StackIngredient remaings = null;
        for (StackIngredientEntry entry : ingredient.content) {
            StackIngredientEntry remaing = add(entry);
            if (remaing != null) {
                if (remaings == null)
                    remaings = new StackIngredient();
                remaings.add(remaing);
            }
        }
        return remaings;
    }
    
    public StackIngredientEntry sub(StackIngredientEntry ingredient) {
        if (ingredient.isEmpty())
            return null;
        
        for (int i = content.size() - 1; i >= 0; i--) {
            StackIngredientEntry entry = content.get(i);
            if (entry.equals(ingredient)) {
                entry.count -= ingredient.count;
                if (entry.count <= 0) {
                    content.remove(i);
                    if (entry.count < 0) {
                        ingredient = entry;
                        ingredient.count = -ingredient.count;
                        continue;
                    }
                }
                
                return null;
            }
        }
        
        return ingredient;
    }
    
    @Override
    public StackIngredient sub(StackIngredient ingredient) {
        StackIngredient remaings = null;
        for (StackIngredientEntry entry : ingredient.content) {
            StackIngredientEntry remaing = sub(entry);
            if (remaing != null) {
                if (remaings == null)
                    remaings = new StackIngredient();
                remaings.add(remaing);
            }
        }
        return remaings;
    }
    
    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    @Override
    public void scale(int count) {
        for (StackIngredientEntry entry : content)
            entry.scale(count);
    }
    
    protected LinkedHashMapInteger<StackIngredientEntry> getCombinedEntries() {
        LinkedHashMapInteger<StackIngredientEntry> map = new LinkedHashMapInteger<>();
        for (StackIngredientEntry entry : content)
            map.put(entry, entry.count);
        return map;
    }
    
    public boolean isStackLimited() {
        return stackLimit > 0;
    }
    
    @Override
    public int getMinimumCount(StackIngredient other, int availableCount) {
        int count = -1;
        LinkedHashMapInteger<StackIngredientEntry> thisEntries = getCombinedEntries();
        LinkedHashMapInteger<StackIngredientEntry> otherEntries = other.getCombinedEntries();
        
        for (Entry<StackIngredientEntry, Integer> entry : thisEntries.entrySet()) {
            Integer value = otherEntries.get(entry.getKey());
            if (value != null)
                count = (int) Math.ceil(Math.max(count, entry.getValue() / value));
        }
        return Math.min(count, availableCount);
    }
    
    public List<StackIngredientEntry> getContent() {
        return content;
    }
    
    @Override
    public String print(List<Object> objects) {
        if (content.size() <= 4) {
            String message = "";
            for (StackIngredientEntry entry : content) {
                message += "{" + objects.size() + "} " + entry.count + " " + entry.stack.getDisplayName() + "\n";
                objects.add(entry.stack);
            }
            return message;
        }
        
        String message = "";
        for (StackIngredientEntry entry : content) {
            message += "{" + objects.size() + "} " + entry.count + " ";
            objects.add(entry.stack);
        }
        return message;
    }
    
    @Override
    public String toString() {
        return content.toString();
    }
}
