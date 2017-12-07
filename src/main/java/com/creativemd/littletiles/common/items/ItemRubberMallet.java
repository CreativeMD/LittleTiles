package com.creativemd.littletiles.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRubberMallet extends Item {
	
	public ItemRubberMallet()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		
	}
	
}
