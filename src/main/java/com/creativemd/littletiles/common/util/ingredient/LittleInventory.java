package com.creativemd.littletiles.common.util.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.api.ILittleIngredientInventory;
import com.creativemd.littletiles.common.api.ILittleIngredientSupplier;
import com.creativemd.littletiles.common.util.ingredient.NotEnoughIngredientsException.NotEnoughSpaceException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class LittleInventory implements Iterable<ItemStack> {
	
	protected EntityPlayer player;
	protected IItemHandler inventory;
	
	private boolean simulate;
	
	protected List<LittleInventory> subInventories;
	protected List<LittleIngredients> inventories;
	protected List<Integer> inventoriesId;
	protected List<ItemStack> cachedInventory;
	
	public boolean allowDrop = true;
	
	public LittleInventory(EntityPlayer player) {
		this(player, player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
	}
	
	public LittleInventory(IItemHandler inventory) {
		this(null, inventory);
	}
	
	public LittleInventory(EntityPlayer player, IItemHandler inventory) {
		this.player = player;
		this.inventory = inventory;
		this.inventories = new ArrayList<>();
		this.inventoriesId = new ArrayList<>();
		this.subInventories = new ArrayList<>();
		reloadInventories(false);
	}
	
	public void reloadInventories(boolean onlyIngredientInventories) {
		inventories.clear();
		inventoriesId.clear();
		if (!onlyIngredientInventories)
			subInventories.clear();
		
		for (int i = 0; i < size(); i++) {
			ItemStack stack = get(i);
			if (stack.getItem() instanceof ILittleIngredientInventory) {
				LittleIngredients ingredient = ((ILittleIngredientInventory) stack.getItem()).getInventory(stack);
				if (ingredient != null) {
					inventories.add(ingredient);
					inventoriesId.add(i);
				}
			} else if (!onlyIngredientInventories && stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
				subInventories.add(new LittleInventory(player, stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)));
		}
	}
	
	public boolean isSimulation() {
		return simulate;
	}
	
	public void startSimulation() {
		cachedInventory = new ArrayList<ItemStack>();
		for (int i = 0; i < inventory.getSlots(); i++)
			cachedInventory.add(inventory.getStackInSlot(i).copy());
		
		simulate = true;
		reloadInventories(false);
	}
	
	public void stopSimulation() {
		simulate = false;
		cachedInventory = null;
		
		reloadInventories(false);
	}
	
	public boolean addStack(ItemStack stack) {
		return addStack(stack, false);
	}
	
	public boolean addStack(ItemStack stack, boolean onlyMerge) {
		for (int i = 0; i < size(); i++) {
			ItemStack inventoryStack = get(i);
			if (!(inventoryStack.getItem() instanceof ILittleIngredientInventory) && InventoryUtils.isItemStackEqual(inventoryStack, stack)) {
				int amount = Math.min(stack.getMaxStackSize() - inventoryStack.getCount(), stack.getCount());
				if (amount > 0) {
					inventoryStack.setCount(inventoryStack.getCount() + amount);
					
					stack.shrink(amount);
					if (stack.isEmpty())
						return true;
				}
			}
		}
		
		for (int i = 0; i < subInventories.size(); i++)
			if (subInventories.get(i).addStack(stack, true))
				return true;
			
		if (onlyMerge)
			return false;
		
		for (int i = 0; i < size(); i++) {
			if (get(i).isEmpty()) {
				if (simulate)
					cachedInventory.set(i, stack);
				else
					inventory.insertItem(i, stack, false);
				return true;
			}
		}
		
		for (int i = 0; i < subInventories.size(); i++)
			if (subInventories.get(i).addStack(stack, false))
				return true;
			
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
	
	public ItemStack get(int index) {
		return simulate ? cachedInventory.get(index) : inventory.getStackInSlot(index);
	}
	
	public int size() {
		if (inventory instanceof InventoryPlayer)
			return 36;
		return simulate ? cachedInventory.size() : inventory.getSlots();
	}
	
	@Override
	public Iterator<ItemStack> iterator() {
		if (simulate)
			return cachedInventory.iterator();
		return new Iterator<ItemStack>() {
			
			public int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < inventory.getSlots();
			}
			
			@Override
			public ItemStack next() {
				return inventory.getStackInSlot(index++);
			}
			
		};
	}
	
	protected LittleIngredient take(LittleIngredient ingredient) {
		for (LittleIngredients ingredients : inventories) {
			ingredient = ingredients.sub(ingredient);
			if (ingredient == null)
				return null;
		}
		
		for (int i = 0; i < subInventories.size(); i++) {
			ingredient = subInventories.get(i).take(ingredient);
			if (ingredient == null)
				return null;
		}
		
		return ingredient;
	}
	
	protected boolean takeFromStacks(LittleIngredients ingredients, LittleIngredients overflow) {
		for (int i = 0; i < size(); i++) {
			ItemStack stack = get(i);
			
			if (stack.isEmpty())
				continue;
			
			LittleIngredients stackIngredients = LittleIngredient.extractWithoutCount(stack, false);
			if (stackIngredients != null) {
				
				int amount = ingredients.getMinimumCount(stackIngredients, stack.getCount());
				if (amount > -1) {
					stackIngredients.scale(amount);
					overflow.add(ingredients.sub(stackIngredients));
					stack.shrink(amount);
					continue;
				}
			}
			
			LittleIngredient[] content = ingredients.getContent();
			for (int j = 0; j < content.length; j++)
				if (content[j] != null)
					if (LittleIngredient.handleExtra(content[j], stack, overflow))
						content[j] = null;
					
			if (ingredients.isEmpty())
				return true;
			
		}
		
		for (int i = 0; i < subInventories.size(); i++)
			if (subInventories.get(i).takeFromStacks(ingredients, overflow))
				return true;
			
		for (int i = 0; i < size(); i++) {
			ItemStack stack = get(i);
			if (stack.getItem() instanceof ILittleIngredientSupplier)
				((ILittleIngredientSupplier) stack.getItem()).requestIngredients(stack, ingredients, overflow, this);
		}
		
		return ingredients.isEmpty();
	}
	
	public void take(LittleIngredients ingredients) throws NotEnoughIngredientsException {
		for (LittleIngredient ingredient : ingredients.getContent())
			if (ingredient != null)
				ingredients.set(ingredient.getClass(), take(ingredient));
			
		if (!ingredients.isEmpty()) { // Try to drain remaining ingredients from inventory
			LittleIngredients overflow = new LittleIngredients();
			takeFromStacks(ingredients, overflow);
			
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
	
	protected LittleIngredient tryGive(LittleIngredient ingredient) {
		for (LittleIngredients ingredients : inventories) {
			ingredient = ingredients.add(ingredient);
			if (ingredient == null)
				return null;
		}
		
		for (int i = 0; i < subInventories.size(); i++) {
			ingredient = subInventories.get(i).tryGive(ingredient);
			if (ingredient == null)
				return null;
		}
		
		return ingredient;
	}
	
	protected LittleIngredient give(LittleIngredient ingredient) throws NotEnoughSpaceException {
		ingredient = tryGive(ingredient);
		if (ingredient == null)
			return null;
		
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
			((ILittleIngredientInventory) stack.getItem()).setInventory(stack, inventories.get(i), this);
		}
		
		//for (int i = 0; i < subInventories.size(); i++)
		//subInventories.get(i).save();
		
		reloadInventories(true);
	}
	
	@Nullable
	public EntityPlayer getPlayer() {
		return player;
	}
	
}
