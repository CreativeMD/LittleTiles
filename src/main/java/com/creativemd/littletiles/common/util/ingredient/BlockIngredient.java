package com.creativemd.littletiles.common.util.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.tooltip.TooltipUtils;
import com.creativemd.creativecore.common.utils.type.LinkedHashMapDouble;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class BlockIngredient extends LittleIngredient<BlockIngredient> implements Iterable<BlockIngredientEntry> {
	
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
		
		if (maxEntries == -1 || content.size() < maxEntries) {
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
		LinkedHashMapDouble<BlockIngredientEntry> thisEntries = getCombinedEntries();
		LinkedHashMapDouble<BlockIngredientEntry> otherEntries = other.getCombinedEntries();
		
		for (Entry<BlockIngredientEntry, Double> entry : thisEntries.entrySet()) {
			Double value = otherEntries.get(entry.getKey());
			if (value != null)
				count = (int) Math.ceil(Math.max(count, entry.getValue() / value));
		}
		return Math.min(count, availableCount);
	}
	
	@Override
	public Iterator<BlockIngredientEntry> iterator() {
		return content.iterator();
	}
	
	public List<BlockIngredientEntry> getContent() {
		return content;
	}
	
	@Override
	public String print(List<Object> objects) {
		if (content.size() <= 4) {
			String message = "";
			for (BlockIngredientEntry entry : content) {
				ItemStack stack = entry.getItemStack();
				message += "{" + objects.size() + "} " + printVolume(entry.value, false) + " " + stack.getDisplayName() + "\n";
				objects.add(stack);
			}
			return message;
		}
		
		String message = "";
		for (BlockIngredientEntry entry : content) {
			ItemStack stack = entry.getItemStack();
			message += "{" + objects.size() + "} " + printVolume(entry.value, false) + " ";
			objects.add(stack);
		}
		return message;
	}
	
	@Override
	public String toString() {
		return content.toString();
	}
	
	public static String printVolume(double volume, boolean extended) {
		String text = "";
		int fullBlocks = (int) volume;
		int pixels = (int) Math.ceil(((volume - fullBlocks) * LittleGridContext.get().maxTilesPerBlock));
		
		if (fullBlocks > 0)
			text += TooltipUtils.printNumber(fullBlocks) + (extended ? " " + (fullBlocks == 1 ? I18n.translateToLocal("volume.unit.big.single") : I18n.translateToLocal("volume.unit.big.multiple")) : I18n.translateToLocal("volume.unit.big.short"));
		if (pixels > 0) {
			if (fullBlocks > 0)
				text += " ";
			text += TooltipUtils.printNumber(pixels) + (extended ? " " + (pixels == 1 ? I18n.translateToLocal("volume.unit.small.single") : I18n.translateToLocal("volume.unit.small.multiple")) : I18n.translateToLocal("volume.unit.small.short"));
		}
		
		return text;
	}
}
