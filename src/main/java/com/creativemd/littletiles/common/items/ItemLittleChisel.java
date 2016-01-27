package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.creativecore.common.gui.IGuiCreator;
import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.core.CreativeCore;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.SubContainerChisel;
import com.creativemd.littletiles.common.gui.SubGuiChisel;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemLittleChisel extends Item implements IGuiCreator{
	
	public ItemLittleChisel(){
		setCreativeTab(CreativeTabs.tabTools);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    protected String getIconString()
    {
        return LittleTiles.modid + ":LTChisel";
    }
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		
		if(stack.stackTagCompound.hasKey("x1"))
			list.add("1: x=" + stack.stackTagCompound.getInteger("x1") + ",y=" + stack.stackTagCompound.getInteger("y1")+ ",z=" + stack.stackTagCompound.getInteger("z1"));
		else
			list.add("1: undefinded");
		
		if(stack.stackTagCompound.hasKey("x2"))
			list.add("2: x=" + stack.stackTagCompound.getInteger("x2") + ",y=" + stack.stackTagCompound.getInteger("y2")+ ",z=" + stack.stackTagCompound.getInteger("z2"));
		else
			list.add("2: undefinded");
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
		if(!world.isRemote && !player.isSneaking() && stack.stackTagCompound != null)
		{
			if(stack.stackTagCompound.hasKey("x1") && stack.stackTagCompound.hasKey("x2"))
				((EntityPlayerMP)player).openGui(CreativeCore.instance, 1, world, (int)player.posX, (int)player.posY, (int)player.posZ);
			else
				player.addChatMessage(new ChatComponentText("You have to select two positions first"));
		}
        return stack;
    }
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		
		if(!world.isRemote)
		{
			if(player.isSneaking())
			{
				stack.stackTagCompound.setInteger("x2", x);
				stack.stackTagCompound.setInteger("y2", y);
				stack.stackTagCompound.setInteger("z2", z);
				player.addChatMessage(new ChatComponentText("Second position: x=" + x + ",y=" + y + ",z=" + z));
			}else{
				stack.stackTagCompound.setInteger("x1", x);
				stack.stackTagCompound.setInteger("y1", y);
				stack.stackTagCompound.setInteger("z1", z);
				player.addChatMessage(new ChatComponentText("First position: x=" + x + ",y=" + y + ",z=" + z + " sneak to set the second pos!"));
			}
		}
		return true;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
		return new SubGuiChisel(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
		return new SubContainerChisel(player);
	}
}
