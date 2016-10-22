package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUtilityKnife extends Item implements ISpecialBlockSelector {
	
	public ItemUtilityKnife()
	{
		setCreativeTab(CreativeTabs.TOOLS);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("Allows you to chisel out a 1x1x1 tile");
		list.add("right-click places a 1x1x1 tile");
	}

	@Override
	public LittleTileBox getBox(TileEntityLittleTiles te, LittleTile tile, BlockPos pos, EntityPlayer player, RayTraceResult result) {
		if(tile.isStructureBlock)
			return null;
		LittleTileVec vec = new LittleTileVec(result.hitVec);
		vec.subVec(new LittleTileVec(pos));
		if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
			vec.subVec(new LittleTileVec(result.sideHit));
		return new LittleTileBox(vec);
	}

	@Override
	public LittleTileBox getBox(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			RayTraceResult result) {
		LittleTileVec vec = new LittleTileVec(result.hitVec);
		vec.subVec(new LittleTileVec(pos));
		if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
			vec.subVec(new LittleTileVec(result.sideHit));
		return new LittleTileBox(vec);
	}
	
}
