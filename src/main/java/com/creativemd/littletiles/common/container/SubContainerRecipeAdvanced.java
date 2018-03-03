package com.creativemd.littletiles.common.container;

import com.creativemd.littletiles.common.items.ItemRecipeAdvanced;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SubContainerRecipeAdvanced extends SubContainerConfigure {
	
	public BlockPos second;
	
	public SubContainerRecipeAdvanced(EntityPlayer player, ItemStack stack, BlockPos pos) {
		super(player, stack);
		this.second = pos;
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		super.onPacketReceive(nbt);
		((ItemRecipeAdvanced) stack.getItem()).saveRecipe(player.world, stack, second);
	}

}
