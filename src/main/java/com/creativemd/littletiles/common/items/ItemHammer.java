package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.gui.SubGuiHammer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTilePreview;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemHammer extends Item implements IGuiCreator{
	
	public ItemHammer()
	{
		setCreativeTab(LittleTiles.littleTab);
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("used for hammering normal");
		list.add("blocks into small pieces");
		list.add("shift+rightclick will harvest and");
		list.add("drop all tiles inside one block");
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		if(hand == EnumHand.OFF_HAND || player.isSneaking())
			return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand)); 
		if(!world.isRemote)
			GuiHandler.openGuiItem(player, world);
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if(player.isSneaking())
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				if(!world.isRemote)
				{
					if(((TileEntityLittleTiles) tileEntity).getTiles().size() <= 1)
					{
						WorldUtils.dropItem(world, ((TileEntityLittleTiles) tileEntity).getTiles().get(0).getDrops(), pos);
					}else{
						ItemStack drop = new ItemStack(LittleTiles.multiTiles);
						LittleTilePreview.saveTiles(world, ((TileEntityLittleTiles) tileEntity).getTiles(), drop);
						WorldUtils.dropItem(world, drop, pos);
					}
					world.setBlockToAir(pos);
				}
				return EnumActionResult.SUCCESS;
			}
		}
        return EnumActionResult.PASS;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiHammer();
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerHammer(player);
	}
	
}
