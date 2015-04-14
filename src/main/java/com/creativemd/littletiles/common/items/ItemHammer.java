package com.creativemd.littletiles.common.items;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.creativecore.common.gui.IGuiCreator;
import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.core.CreativeCore;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.gui.SubGuiHammer;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemHammer extends Item implements IGuiCreator{
	
	public ItemHammer()
	{
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(!world.isRemote)
		{
			((EntityPlayerMP)player).openGui(CreativeCore.instance, 1, world, (int)player.posX, (int)player.posY, (int)player.posZ);
		}
		return stack;
	}

	@Override
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world,
			int x, int y, int z) {
		return new SubGuiHammer();
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack,
			World world, int x, int y, int z) {
		return new SubContainerHammer();
	}
	
}
