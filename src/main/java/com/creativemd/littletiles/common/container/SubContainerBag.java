package com.creativemd.littletiles.common.container;

import java.util.List;

import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.common.gui.premade.SubContainerHeldItem;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.client.gui.controls.SlotControlBlockIngredient;
import com.creativemd.littletiles.common.items.ItemBag;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;
import com.creativemd.littletiles.common.utils.ingredients.ColorIngredient;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredient;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerBag extends SubContainerHeldItem {
	
	public LittleIngredients bag;
	
	public SubContainerBag(EntityPlayer player, ItemStack stack, int index) {
		super(player, stack, index);
	}
	
	@CustomEventSubscribe
	public void onSlotChange(SlotChangeEvent event) {
		if (event.source instanceof SlotControl) {
			if (event.source instanceof SlotControlBlockIngredient) {
				SlotControlBlockIngredient slot = (SlotControlBlockIngredient) event.source;
				
				if (slot.slot.getStack().isEmpty())
					slot.ingredient = null;
				else if (slot.ingredient != null)
					slot.ingredient.value = slot.slot.getStack().getCount() / (double) LittleGridContext.get().maxTilesPerBlock;
				
				BlockIngredient blocks = new BlockIngredient().setLimits(ItemBag.inventorySize, ItemBag.maxStackSize);
				for (int y = 0; y < ItemBag.inventoryHeight; y++) {
					for (int x = 0; x < ItemBag.inventoryWidth; x++) {
						int index = x + y * ItemBag.inventoryWidth;
						BlockIngredientEntry ingredient = ((SlotControlBlockIngredient) get("item" + index)).ingredient;
						if (ingredient != null)
							blocks.add(ingredient);
					}
				}
				
				bag.set(blocks.getClass(), blocks);
				((ItemBag) stack.getItem()).setInventory(stack, bag);
				
				reloadControls();
			} else if (event.source.name.startsWith("input")) {
				
				ItemStack input = ((SlotControl) event.source).slot.getStack();
				
				LittleIngredients ingredients = LittleIngredient.extractWithoutCount(input, true);
				ingredients.scale(input.getCount());
				
				if (bag.add(ingredients) == null) {
					((ItemBag) stack.getItem()).setInventory(stack, bag);
					if (ingredients.contains(BlockIngredient.class)) {
						updateSlots();
						player.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
					}
					
					if (ingredients.contains(ColorIngredient.class)) {
						reloadControls();
						player.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
					}
					
				} else
					bag = ((ItemBag) stack.getItem()).getInventory(stack);
			}
			
		}
	}
	
	public void reloadControls() {
		controls.clear();
		createControls();
		refreshControls();
		NBTTagCompound nbt = stack.getTagCompound().copy();
		nbt.setBoolean("reload", true);
		sendNBTToGui(nbt);
	}
	
	public InventoryBasic bagInventory;
	
	public void updateSlots() {
		List<BlockIngredientEntry> inventory = bag.get(BlockIngredient.class).getContent();
		for (int y = 0; y < ItemBag.inventoryHeight; y++) {
			for (int x = 0; x < ItemBag.inventoryWidth; x++) {
				int index = x + y * ItemBag.inventoryWidth;
				bagInventory.setInventorySlotContents(index, index < inventory.size() ? inventory.get(index).getTileItemStack() : ItemStack.EMPTY);
				((SlotControlBlockIngredient) get("item" + index)).ingredient = index < inventory.size() ? inventory.get(index) : null;
			}
		}
	}
	
	@Override
	public void createControls() {
		bag = ((ItemBag) stack.getItem()).getInventory(stack);
		List<BlockIngredientEntry> inventory = bag.get(BlockIngredient.class).getContent();
		
		bagInventory = new InventoryBasic("item", false, ItemBag.inventorySize) {
			@Override
			public int getInventoryStackLimit() {
				return ItemBag.maxStackSizeOfTiles;
			}
		};
		for (int y = 0; y < ItemBag.inventoryHeight; y++) {
			for (int x = 0; x < ItemBag.inventoryWidth; x++) {
				int index = x + y * ItemBag.inventoryWidth;
				bagInventory.setInventorySlotContents(index, index < inventory.size() ? inventory.get(index).getTileItemStack() : ItemStack.EMPTY);
				controls.add(new SlotControlBlockIngredient(new Slot(bagInventory, index, 5 + x * 18, 5 + y * 18) {
					@Override
					public boolean isItemValid(ItemStack stack) {
						return false;
					}
				}, index < inventory.size() ? inventory.get(index) : null));
			}
		}
		
		InventoryBasic input = new InventoryBasic("input", false, 1);
		addSlotToContainer(new Slot(input, 0, 120, 5));
		
		addPlayerSlotsToContainer(player);
		
	}
	
	@Override
	public void onClosed() {
		ItemStack stack = ((SlotControl) get("input0")).slot.getStack();
		if (!stack.isEmpty())
			WorldUtils.dropItem(player, stack);
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}
	
}
