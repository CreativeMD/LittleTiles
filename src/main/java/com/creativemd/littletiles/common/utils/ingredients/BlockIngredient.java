package com.creativemd.littletiles.common.utils.ingredients;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.type.LinkedHashMapDouble;

import net.minecraft.item.ItemStack;

public class BlockIngredient extends LittleIngredient<BlockIngredient> {
	
	private int maxEntries = -1;
	private double maxVolume = -1;
	
	private List<BlockIngredientEntry> content;
	
	public BlockIngredient() {
		this.content = new ArrayList<>();
	}
	
	public BlockIngredient setLimits(int maxEntries, double maxVolume) {
		this.maxEntries = maxEntries;
		this.maxVolume = maxVolume;
		return this;
	}
	
	@Override
	public BlockIngredient add(BlockIngredient ingredient) {
		BlockIngredient remaings = null;
		for (BlockIngredientEntry entry : ingredient.content) {
			BlockIngredientEntry remaing = add(entry);
			if (remaing != null) {
				if (remaings == null)
					remaings = new BlockIngredient();
				remaings.add(remaing);
			}
		}
		return remaings;
	}
	
	@Override
	public BlockIngredient sub(BlockIngredient ingredient) {
		BlockIngredient remaings = null;
		for (BlockIngredientEntry entry : ingredient.content) {
			BlockIngredientEntry remaing = sub(entry);
			if (remaing != null) {
				if (remaings == null)
					remaings = new BlockIngredient();
				remaings.add(remaing);
			}
		}
		return remaings;
	}
	
	public BlockIngredientEntry add(BlockIngredientEntry ingredient) {
		if (ingredient == null || ingredient.isEmpty())
			return null;
		
		for (int i = 0; i < content.size(); i++) {
			BlockIngredientEntry entry = content.get(i);
			if (entry.equals(ingredient)) {
				entry.value += ingredient.value;
				if (maxVolume != -1 && entry.value > maxVolume) {
					BlockIngredientEntry remaining = entry.copy();
					remaining.value = entry.value - maxVolume;
					entry.value = maxVolume;
					ingredient = remaining;
				} else
					return null;
			}
		}
		
		if (maxEntries != -1 && content.size() < maxEntries) {
			content.add(ingredient.copy());
			return null;
		}
		return ingredient;
	}
	
	public BlockIngredientEntry sub(BlockIngredientEntry ingredient) {
		if (ingredient == null || ingredient.isEmpty())
			return null;
		
		for (int i = content.size() - 1; i >= 0; i--) {
			BlockIngredientEntry entry = content.get(i);
			if (entry.equals(ingredient)) {
				entry.value -= ingredient.value;
				if (entry.value <= 0) {
					content.remove(i);
					if (entry.value < 0) {
						ingredient = entry;
						ingredient.value = -ingredient.value;
						continue;
					}
				}
				
				return null;
			}
		}
		
		return ingredient;
	}
	
	public List<BlockIngredientEntry> getContent() {
		return content;
	}
	
	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}
	
	@Override
	public BlockIngredient copy() {
		BlockIngredient copy = new BlockIngredient();
		copy.maxEntries = maxEntries;
		copy.maxVolume = maxVolume;
		content.forEach((x) -> copy.add(x.copy()));
		return copy;
	}
	
	@Override
	public void print(List<String> lines, List<ItemStack> stacks) {
		for (BlockIngredientEntry entry : content) {
			ItemStack stack = entry.getItemStack();
			lines.add(stack.getDisplayName());
			stacks.add(stack);
		}
	}
	
	@Override
	public void scale(int count) {
		for (BlockIngredientEntry entry : content)
			entry.scale(count);
	}
	
	protected LinkedHashMapDouble<BlockIngredientEntry> getCombinedEntries() {
		LinkedHashMapDouble<BlockIngredientEntry> map = new LinkedHashMapDouble<>();
		for (BlockIngredientEntry entry : content)
			map.put(entry, entry.value);
		return map;
	}
	
	public boolean isVolumeLimited() {
		return maxVolume > 0;
	}
	
	@Override
	public int getMinimumCount(BlockIngredient other, int availableCount) {
		int count = -1;
		if (this.isVolumeLimited() || other.isVolumeLimited()) {
			LinkedHashMapDouble<BlockIngredientEntry> thisEntries = getCombinedEntries();
			LinkedHashMapDouble<BlockIngredientEntry> otherEntries = other.getCombinedEntries();
			
			for (Entry<BlockIngredientEntry, Double> entry : thisEntries.entrySet()) {
				Double value = otherEntries.get(entry.getKey());
				if (value != null)
					count = (int) Math.ceil(Math.max(count, entry.getValue() / value));
			}
		}
		return Math.min(count, availableCount);
	}
}
