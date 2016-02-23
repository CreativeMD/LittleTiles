package com.creativemd.littletiles.common.items;

import java.util.List;

import javax.swing.Icon;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.creativecore.common.gui.IGuiCreator;
import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.core.CreativeCore;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.SubContainerColorTube;
import com.creativemd.littletiles.common.gui.SubGuiColorTube;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemColorTube extends Item implements IGuiCreator{

	public ItemColorTube()
	{
		setCreativeTab(CreativeTabs.tabTools);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@SideOnly(Side.CLIENT)
	public static IIcon overlay;

	@Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister registry)
    {
        this.itemIcon = registry.registerIcon(LittleTiles.modid + ":LTColorTube");
        this.overlay = registry.registerIcon(LittleTiles.modid + ":LTColorTube-Overlay");
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass)
    {
        if (pass == 0)
        	return ColorUtils.WHITE;
        return getColor(stack);
    }
	
	public static int getColor(ItemStack stack)
	{
		if(stack == null)
			return ColorUtils.WHITE;
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		if(!stack.stackTagCompound.hasKey("color"))
			setColor(stack, ColorUtils.WHITE);
		return stack.stackTagCompound.getInteger("color");
	}
	
	public static void setColor(ItemStack stack, int color)
	{
		if(stack == null)
			return ;
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setInteger("color", color);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int meta, int pass)
    {
        return pass == 1 ? this.overlay : this.itemIcon;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("rightclick: dyes a tile");
		list.add("shift+rightclick: copies tile's color");
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			if(world.isRemote)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("color", getColor(stack));
				PacketHandler.sendPacketToServer(new LittleBlockPacket(x, y, z, player, 3, nbt));
			}
			return true;
		}
		return false;
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
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
		return new SubGuiColorTube(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
		return new SubContainerColorTube(player, stack);
	}
}
