package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.SubContainerColorTube;
import com.creativemd.littletiles.common.gui.SubGuiColorTube;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemColorTube extends Item implements IGuiCreator{

	public ItemColorTube()
	{
		setCreativeTab(CreativeTabs.TOOLS);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	public static int getColor(ItemStack stack)
	{
		if(stack == null)
			return ColorUtils.WHITE;
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		if(!stack.getTagCompound().hasKey("color"))
			setColor(stack, ColorUtils.WHITE);
		return stack.getTagCompound().getInteger("color");
	}
	
	public static void setColor(ItemStack stack, int color)
	{
		if(stack == null)
			return ;
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("color", color);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("rightclick: dyes a tile");
		list.add("shift+rightclick: copies tile's color");
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			if(worldIn.isRemote)
			{
				ItemStack stack = player.getHeldItem(hand);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("color", getColor(stack));
				PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, player, 3, nbt));
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
    }
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		if(hand == EnumHand.OFF_HAND)
			return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand)); 
		if(!world.isRemote)
			GuiHandler.openGuiItem(player, world);
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiColorTube(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerColorTube(player, stack);
	}
}
