package com.creativemd.littletiles.common.container;

import java.util.List;

import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.common.gui.premade.SubContainerHeldItem;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.controls.SlotControlBlockIngredient;
import com.creativemd.littletiles.common.api.ILittleInventory;
import com.creativemd.littletiles.common.item.ItemBag;
import com.creativemd.littletiles.common.item.ItemBlockIngredient;
import com.creativemd.littletiles.common.item.ItemColorIngredient;
import com.creativemd.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredient;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.ColorIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerBag extends SubContainerHeldItem {
	
	public LittleIngredients bag;
	public InventoryBasic input = new InventoryBasic("input", false, 1);
	
	public SubContainerBag(EntityPlayer player, ItemStack stack, int index) {
		super(player, stack, index);
		
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = stack.getTagCompound().copy();
		nbt.setBoolean("reload", true);
		sendNBTToGui(nbt);
	}
	
	@CustomEventSubscribe
	public void onSlotChange(SlotChangeEvent event) {
		if (event.source instanceof SlotControl) {
			if (event.source instanceof SlotControlBlockIngredient) {
				SlotControlBlockIngredient slot = (SlotControlBlockIngredient) event.source;
				
				BlockIngredient blocks = new BlockIngredient().setLimits(ItemBag.inventorySize, ItemBag.maxStackSize);
				for (int y = 0; y < ItemBag.inventoryHeight; y++) {
					for (int x = 0; x < ItemBag.inventoryWidth; x++) {
						int index = x + y * ItemBag.inventoryWidth;
						BlockIngredientEntry ingredient = ((SlotControlBlockIngredient) get("item" + index)).getIngredient();
						if (ingredient != null)
							blocks.add(ingredient);
					}
				}
				
				bag.set(blocks.getClass(), blocks);
				((ItemBag) stack.getItem()).setInventory(stack, bag, null);
				
				reloadControls();
			} else if (event.source.name.startsWith("input")) {
				
				ItemStack input = ((SlotControl) event.source).slot.getStack();
				
				LittleIngredients ingredients = LittleIngredient.extractWithoutCount(input, true);
				if (ingredients != null) {
					ingredients.scale(input.getCount());
					
					boolean containsBlocks = ingredients.contains(BlockIngredient.class);
					boolean containsColor = ingredients.contains(ColorIngredient.class);
					
					if (bag.add(ingredients) == null) {
						
						input.setCount(0);
						((ItemBag) stack.getItem()).setInventory(stack, bag, null);
						
						if (containsBlocks) {
							updateSlots();
							player.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
						}
						
						if (containsColor) {
							reloadControls();
							player.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
						}
						
					} else
						bag = ((ItemBag) stack.getItem()).getInventory(stack);
					
				} else if (input.getItem() instanceof ILittleInventory) {
					
					ingredients = ((ILittleInventory) input.getItem()).getInventory(input);
					
					boolean containsBlocks = ingredients.contains(BlockIngredient.class);
					boolean containsColor = ingredients.contains(ColorIngredient.class);
					
					LittleIngredients remaining = bag.add(ingredients);
					((ItemBag) stack.getItem()).setInventory(stack, bag, null);
					
					if (remaining == null)
						remaining = new LittleIngredients();
					
					if (remaining.copy().sub(ingredients.copy()) != null) {
						if (containsBlocks) {
							updateSlots();
							player.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
						}
						
						if (containsColor) {
							reloadControls();
							player.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
						}
					}
					
					((ILittleInventory) input.getItem()).setInventory(input, remaining, null);
				}
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
				
				ItemStack stack;
				if (index < inventory.size()) {
					stack = new ItemStack(LittleTiles.blockIngredient);
					stack.setTagCompound(new NBTTagCompound());
					ItemBlockIngredient.saveIngredient(stack, inventory.get(index));
				} else
					stack = ItemStack.EMPTY;
				
				bagInventory.setInventorySlotContents(index, stack);
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
				
				ItemStack stack;
				if (index < inventory.size()) {
					stack = new ItemStack(LittleTiles.blockIngredient);
					stack.setTagCompound(new NBTTagCompound());
					ItemBlockIngredient.saveIngredient(stack, inventory.get(index));
				} else
					stack = ItemStack.EMPTY;
				
				bagInventory.setInventorySlotContents(index, stack);
				controls.add(new SlotControlBlockIngredient(new Slot(bagInventory, index, 5 + x * 18, 5 + y * 18) {
					@Override
					public boolean isItemValid(ItemStack stack) {
						return false;
					}
				}));
			}
		}
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
		if (nbt.getBoolean("color")) {
			ColorIngredientType type = ColorIngredientType.getType(nbt.getString("type"));
			ColorIngredient color = bag.get(ColorIngredient.class);
			if (color != null && !color.isEmpty()) {
				int amount = Math.min(type.getIngredient(color), ColorIngredient.bottleSize);
				if (amount > 0) {
					type.setIngredient(color, type.getIngredient(color) - amount);
					
					LittleInventory inventory = new LittleInventory(player);
					ItemStack colorStack = ItemColorIngredient.generateItemStack(type, amount);
					if (!inventory.addStack(colorStack))
						WorldUtils.dropItem(player, colorStack);
					
					((ItemBag) stack.getItem()).setInventory(stack, bag, null);
					onTick();
					reloadControls();
				}
			}
		}
	}
	
}
