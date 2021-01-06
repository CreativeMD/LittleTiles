package com.creativemd.littletiles.common.container;

import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.littletiles.common.recipe.WhitenRecipeRegistry;
import com.creativemd.littletiles.common.recipe.WhitenRecipeRegistry.WhitenRecipe;
import com.creativemd.littletiles.common.structure.type.premade.LittleWhitener;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerWhitener extends SubContainer {
	
	public LittleWhitener whitener;
	public InventoryBasic whiteInput = new InventoryBasic("whiteInput", false, 1);
	
	public SubContainerWhitener(EntityPlayer player, LittleWhitener whitener) {
		super(player);
		this.whitener = whitener;
		updateVolume();
	}
	
	@Override
	public void createControls() {
		addSlotToContainer(new Slot(whitener.inventory, 0, 8, 10) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return !WhitenRecipeRegistry.getRecipe(stack).isEmpty();
			}
		});
		addSlotToContainer(new Slot(whiteInput, 0, 8, 40) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return WhitenRecipeRegistry.getVolume(stack) > 0;
			}
		});
		addPlayerSlotsToContainer(player);
	}
	
	@CustomEventSubscribe
	public void slotChanged(SlotChangeEvent event) {
		int volume = WhitenRecipeRegistry.getVolume(whiteInput.getStackInSlot(0));
		if (volume > 0) {
			ItemStack stack = whiteInput.getStackInSlot(0);
			boolean added = false;
			while (!stack.isEmpty() && volume + whitener.whiteColor <= WhitenRecipeRegistry.whitenerTotalVolume) {
				stack.shrink(1);
				whitener.whiteColor += volume;
				added = true;
			}
			if (added)
				player.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
			updateVolume();
		}
	}
	
	public void updateVolume() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("volume", whitener.whiteColor);
		sendNBTToGui(nbt);
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		if (nbt.getBoolean("craft")) {
			int amount = nbt.getInteger("amount");
			ItemStack stack = whitener.inventory.getStackInSlot(0);
			int stackSize = 1;
			if (amount > 1)
				stackSize = stack.getCount();
			List<WhitenRecipe> recipes = WhitenRecipeRegistry.getRecipe(stack);
			if (!recipes.isEmpty()) {
				int index = 0;
				int variant = nbt.getInteger("variant");
				WhitenRecipe selected = null;
				IBlockState state = null;
				for (WhitenRecipe recipe : recipes) {
					if (variant >= index + recipe.results.length)
						index += recipe.results.length;
					else {
						selected = recipe;
						state = recipe.results[variant - index];
						break;
					}
				}
				if (selected == null)
					return;
				boolean result = !selected.isResult(stack);
				if (result && selected.needed > 0)
					stackSize = Math.min(stackSize, whitener.whiteColor / selected.needed);
				ItemStack newStack = new ItemStack(state.getBlock(), stackSize, state.getBlock().getMetaFromState(state));
				stack.shrink(stackSize);
				if (!player.addItemStackToInventory(newStack))
					player.dropItem(newStack, false);
				if (result && selected.needed > 0)
					whitener.whiteColor -= stackSize * selected.needed;
				updateVolume();
			}
		}
	}
	
}
