package com.creativemd.littletiles.common.utils.ingredients;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.common.utils.ingredients.NotEnoughIngredientsException.NotEnoughSpaceException;

import net.minecraft.item.ItemStack;

public class LittleIngredients extends LittleIngredientBase<LittleIngredients> implements Iterable<LittleIngredient> {
	
	protected LittleIngredient[] content = new LittleIngredient[LittleIngredient.getSize()];
	
	public LittleIngredients(LittleIngredient... ingredients) {
		for (int i = 0; i < ingredients.length; i++) {
			set(ingredients[i]);
		}
	}
	
	public LittleIngredients() {
		
	}
	
	public boolean contains(Class<? extends LittleIngredient> type) {
		LittleIngredient ingredient = content[LittleIngredient.indexOf(type)];
		return ingredient != null && !ingredient.isEmpty();
	}
	
	public LittleIngredient[] getContent() {
		return this.content;
	}
	
	public <T extends LittleIngredient> T get(Class<T> type) {
		return (T) content[LittleIngredient.indexOf(type)];
	}
	
	public void set(LittleIngredient ingredient) {
		content[LittleIngredient.indexOf(ingredient)] = ingredient;
	}
	
	public LittleIngredient add(LittleIngredient ingredient) {
		if (ingredient == null || ingredient.isEmpty())
			return null;
		
		int index = LittleIngredient.indexOf(ingredient);
		LittleIngredient item = content[index];
		if (item != null)
			return item.add(ingredient);
		
		if (!canAddNewIngredients())
			return ingredient;
		
		content[index] = ingredient;
		return null;
	}
	
	public LittleIngredient sub(LittleIngredient ingredient) {
		if (ingredient == null || ingredient.isEmpty())
			return null;
		
		int index = LittleIngredient.indexOf(ingredient);
		LittleIngredient item = content[index];
		if (item != null)
			return item.sub(ingredient);
		
		return ingredient;
	}
	
	protected void assignContent(LittleIngredients toAssign) {
		for (int i = 0; i < this.content.length; i++)
			this.content[i] = toAssign.content[i] != null ? toAssign.content[i].copy() : null;
	}
	
	@Override
	public LittleIngredients copy() {
		LittleIngredients ingredients = new LittleIngredients();
		ingredients.assignContent(this);
		return ingredients;
	}
	
	@Override
	public LittleIngredients add(LittleIngredients ingredient) {
		if (ingredient == null)
			return null;
		LittleIngredients remains = null;
		for (int i = 0; i < content.length; i++) {
			LittleIngredient existing = content[i];
			LittleIngredient toAdd = ingredient.content[i];
			if (existing == null) {
				if (canAddNewIngredients())
					content[i] = toAdd;
				else {
					if (remains == null)
						remains = new LittleIngredients();
					remains.content[i] = toAdd;
				}
			} else {
				LittleIngredient remain = existing.add(toAdd);
				if (remain != null) {
					if (remains == null)
						remains = new LittleIngredients();
					remains.content[i] = remain;
				}
			}
		}
		return remains;
	}
	
	@Override
	public LittleIngredients sub(LittleIngredients ingredient) {
		if (ingredient == null)
			return null;
		
		LittleIngredients remains = null;
		for (int i = 0; i < content.length; i++) {
			LittleIngredient existing = content[i];
			LittleIngredient toSub = ingredient.content[i];
			if (existing == null) {
				if (remains == null)
					remains = new LittleIngredients();
				remains.content[i] = toSub;
			} else {
				LittleIngredient remain = existing.sub(toSub);
				if (remain != null) {
					if (remains == null)
						remains = new LittleIngredients();
					remains.content[i] = remain;
				}
			}
		}
		return remains;
	}
	
	@Override
	public boolean isEmpty() {
		for (int i = 0; i < content.length; i++)
			if (content[i] != null && !content[i].isEmpty())
				return false;
		return true;
	}
	
	public void scale(int count) {
		for (int i = 0; i < content.length; i++)
			if (content[i] != null && !content[i].isEmpty())
				content[i].scale(count);
	}
	
	public int getMinimumCount(LittleIngredients other, int availableCount) {
		int count = -1;
		for (int i = 0; i < content.length; i++) {
			LittleIngredient existing = content[i];
			LittleIngredient toSub = other.content[i];
			if (existing != null && !existing.isEmpty() && toSub != null && !toSub.isEmpty())
				count = Math.max(count, existing.getMinimumCount(toSub, availableCount));
		}
		return count;
	}
	
	public List<ItemStack> handleOverflow() throws NotEnoughSpaceException {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < content.length; i++)
			if (content[i] != null && !content[i].isEmpty())
				stacks.addAll(LittleIngredient.handleOverflow(content[i]));
		return stacks;
	}
	
	protected boolean canAddNewIngredients() {
		return true;
	}
	
	protected boolean removeEmptyIngredients() {
		return true;
	}
	
	@Override
	public Iterator<LittleIngredient> iterator() {
		return new Iterator<LittleIngredient>() {
			int index = -1;
			
			@Override
			public boolean hasNext() {
				int nextIndex = index + 1;
				while (nextIndex < content.length) {
					if (content[nextIndex] != null && !content[nextIndex].isEmpty())
						return true;
					nextIndex++;
				}
				return false;
			}
			
			@Override
			public LittleIngredient next() {
				index++;
				while (index < content.length) {
					if (content[index] != null && !content[index].isEmpty())
						return content[index];
					index++;
				}
				throw new RuntimeException("Iterator reached end of ingredients. Something has gone wrong!");
			}
		};
	}
	
}
