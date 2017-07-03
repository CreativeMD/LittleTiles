package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.container.SubContainerScrewdriver;
import com.creativemd.littletiles.common.gui.SubGuiScrewdriver;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleScrewdriver extends Item implements IGuiCreator{
	
	public ItemLittleScrewdriver(){
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		if(stack.getTagCompound().hasKey("x1"))
			list.add("1: x=" + stack.getTagCompound().getInteger("x1") + ",y=" + stack.getTagCompound().getInteger("y1")+ ",z=" + stack.getTagCompound().getInteger("z1"));
		else
			list.add("1: undefinded");
		
		if(stack.getTagCompound().hasKey("x2"))
			list.add("2: x=" + stack.getTagCompound().getInteger("x2") + ",y=" + stack.getTagCompound().getInteger("y2")+ ",z=" + stack.getTagCompound().getInteger("z2"));
		else
			list.add("2: undefinded");
		
		list.add("creative mode only");
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		ItemStack stack = player.getHeldItem(hand);
		if(hand == EnumHand.OFF_HAND)
			return new ActionResult(EnumActionResult.PASS, stack); 
		if(!world.isRemote && !player.isSneaking() && stack.hasTagCompound())
		{
			if(stack.getTagCompound().hasKey("x1") && stack.getTagCompound().hasKey("x2"))
				GuiHandler.openGuiItem(player, world);
			else
				player.sendMessage(new TextComponentTranslation("You have to select two positions first"));
		}
		return new ActionResult(EnumActionResult.SUCCESS, stack);
    }
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		ItemStack stack = player.getHeldItem(hand);
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		if(!world.isRemote)
		{
			if(player.isSneaking())
			{
				stack.getTagCompound().setInteger("x2", pos.getX());
				stack.getTagCompound().setInteger("y2", pos.getY());
				stack.getTagCompound().setInteger("z2", pos.getZ());
				player.sendMessage(new TextComponentTranslation("Second position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
			}else{
				stack.getTagCompound().setInteger("x1", pos.getX());
				stack.getTagCompound().setInteger("y1", pos.getY());
				stack.getTagCompound().setInteger("z1", pos.getZ());
				player.sendMessage(new TextComponentTranslation("First position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ() + " sneak to set the second pos!"));
			}
		}
		return EnumActionResult.SUCCESS;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiScrewdriver(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerScrewdriver(player);
	}
}
