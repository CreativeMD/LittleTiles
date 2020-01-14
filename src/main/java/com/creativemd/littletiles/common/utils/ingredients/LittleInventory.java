package com.creativemd.littletiles.common.utils.ingredients;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.api.ILittleInventory;
import com.creativemd.littletiles.common.utils.ingredients.NotEnoughIngredientsException.NotEnoughSpaceException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class LittleInventory implements Iterable<ItemStack> {
	
	private boolean simulate;
	protected EntityPlayer player;
	protected IInventory inventory;
	protected List<LittleIngredients> inventories;
	protected List<Integer> inventoriesId;
	protected List<ItemStack> cachedInventory;
	
	public boolean allowDrop = true;
	
	public LittleInventory(EntityPlayer player) {
		this(player, player.inventory);
	}
	
	public LittleInventory(IInventory inventory) {
		this(null, inventory);
	}
	
	public LittleInventory(EntityPlayer player, IInventory inventory) {
		this.player = player;
		this.inventory = inventory;
		this.inventories = new ArrayList<>();
		this.inventoriesId = new ArrayList<>();
		reloadInventories();
	}
	
	public void reloadInventories() {
		inventories.clear();
		inventoriesId.clear();
		
		for (int i = 0; i < size(); i++) {
			ItemStack stack = get(i);
			if (stack.getItem() instanceof ILittleInventory) {
				LittleIngredients ingredient = ((ILittleInventory) stack.getItem()).getInventory(stack);
				if (ingredient != null) {
					inventories.add(ingredient);
					inventoriesId.add(i);
				}
			}
		}
	}
	
	public boolean isSimulation() {
		return simulate;
	}
	
	public void startSimulation() {
		cachedInventory = new ArrayList<ItemStack>();
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			cachedInventory.add(inventory.getStackInSlot(i).copy());
		
		simulate = true;
		reloadInventories();
	}
	
	public void stopSimulation() {
		simulate = false;
		cachedInventory = null;
		
		reloadInventories();
	}
	
	public boolean addStack(ItemStack stack) {
		return addStack(stack, false);
	}
	
	public boolean addStack(ItemStack stack, boolean onlyMerge) {
		for (int i = 0; i < size(); i++) {
			ItemStack inventoryStack = get(i);
			if (InventoryUtils.isItemStackEqual(inventoryStack, stack)) {
				int amount = Math.min(stack.getMaxStackSize() - inventoryStack.getCount(), stack.getCount());
				if (amount > 0) {
					ItemStack newStack = stack.copy();
					newStack.setCount(inventoryStack.getCount() + amount);
					set(i, newStack);
					
					stack.shrink(amount);
					if (stack.isEmpty())
						return true;
				}
			}
		}
		
		if (onlyMerge)
			return false;
		
		for (int i = 0; i < size(); i++) {
			if (get(i).isEmpty()) {
				set(i, stack);
				return true;
			}
		}
		return false;
	}
	
	public void addOrDropStacks(List<ItemStack> stacks) throws NotEnoughSpaceException {
		List<ItemStack> toDrop = null;
		for (ItemStack stack : stacks) {
			if (!addStack(stack)) {
				if (toDrop == null)
					toDrop = new ArrayList<>();
				toDrop.add(stack);
				
			}
		}
		
		if (toDrop != null) {
			if (player == null || !allowDrop)
				throw new NotEnoughSpaceException(new StackIngredient(toDrop));
			
			if (!simulate && !player.world.isRemote)
				WorldUtils.dropItem(player, toDrop);
		}
	}
	
	public void set(int index, ItemStack stack) {
		if (simulate)
			cachedInventory.set(index, stack);
		else
			inventory.setInventorySlotContents(index, stack);
	}
	
	public ItemStack get(int index) {
		return simulate ? cachedInventory.get(index) : inventory.getStackInSlot(index);
	}
	
	public int size() {
		if (inventory instanceof InventoryPlayer)
			return 36;
		return simulate ? cachedInventory.size() : inventory.getSizeInventory();
	}
	
	@Override
	public Iterator<ItemStack> iterator() {
		if (simulate)
			return cachedInventory.iterator();
		return new Iterator<ItemStack>() {
			
			public int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < inventory.getSizeInventory();
			}
			
			@Override
			public ItemStack next() {
				return inventory.getStackInSlot(index++);
			}
			
		};
	}
	
	protected LittleIngredient take(LittleIngredient ingredient) throws NotEnoughIngredientsException {
		for (LittleIngredients ingredients : inventories) {
			ingredient = ingredients.sub(ingredient);
			if (ingredient == null)
				return null;
		}
		return ingredient;
	}
	
	public void take(LittleIngredients ingredients) throws NotEnoughIngredientsException {
		for (LittleIngredient ingredient : ingredients.getContent())
			if (ingredient != null)
				ingredients.set(ingredient.getClass(), take(ingredient));
			
		if (!ingredients.isEmpty()) { // Try to drain remaining ingredients from inventory
			LittleIngredients overflow = new LittleIngredients();
			for (int i = 0; i < size(); i++) {
				ItemStack stack = get(i);
				LittleIngredients stackIngredients = LittleIngredient.extractWithoutCount(stack, false);
				if (stackIngredients == null)
					continue;
				
				int amount = ingredients.getMinimumCount(stackIngredients, stack.getCount());
				if (amount > -1) {
					stackIngredients.scale(amount);
					overflow.add(ingredients.sub(stackIngredients));
					stack.shrink(amount);
					if (ingredients.isEmpty())
						break;
				}
			}
			
			if (!ingredients.isEmpty())
				throw new NotEnoughIngredientsException(ingredients);
			
			if (!overflow.isEmpty()) {
				try {
					give(overflow);
				} catch (NotEnoughSpaceException e) {
					List<ItemStack> stacks = overflow.handleOverflow();
					if (stacks != null && !stacks.isEmpty())
						addOrDropStacks(stacks);
				}
			}
		}
		
		saveInventories();
	}
	
	protected LittleIngredient give(LittleIngredient ingredient) throws NotEnoughSpaceException {
		for (LittleIngredients ingredients : inventories) {
			ingredient = ingredients.add(ingredient);
			if (ingredient == null)
				return null;
		}
		
		try {
			List<ItemStack> stacks = LittleIngredient.handleOverflow(ingredient);
			if (stacks != null)
				addOrDropStacks(stacks);
			return null;
		} catch (NotEnoughSpaceException e) {
			return ingredient;
		}
	}
	
	public void give(LittleIngredients ingredients) throws NotEnoughSpaceException {
		LittleIngredients remainings = new LittleIngredients();
		for (LittleIngredient ingredient : ingredients.getContent())
			if (ingredient != null)
				remainings.set(ingredient.getClass(), give(ingredient));
			
		if (!remainings.isEmpty())
			throw new NotEnoughSpaceException(remainings);
		
		saveInventories();
	}
	
	public void saveInventories() {
		for (int i = 0; i < inventoriesId.size(); i++) {
			int index = inventoriesId.get(i);
			ItemStack stack = get(index);
			((ILittleInventory) stack.getItem()).setInventory(stack, inventories.get(i), this);
		}
		
		reloadInventories();
	}
	
}
