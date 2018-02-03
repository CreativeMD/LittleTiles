package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.IFakeRenderingBlock;
import com.creativemd.littletiles.common.api.blocks.ISpecialBlockHandler;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored.EnumType;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.nsc.transform.patmat.ScalaLogic.TreesAndTypesDomain.Var;

public class BlockLTTransparentColored extends Block implements ISpecialBlockHandler, IFakeRenderingBlock {
	
	public static final PropertyEnum<BlockLTTransparentColored.EnumType> VARIANT = PropertyEnum.<BlockLTTransparentColored.EnumType>create("variant", BlockLTTransparentColored.EnumType.class);

	public BlockLTTransparentColored() {
		super(Material.ROCK);
		setCreativeTab(LittleTiles.littleTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockLTTransparentColored.EnumType.clean));
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        Block block = iblockstate.getBlock();

        if (block == this)
        {
            if (blockState.getValue(VARIANT) == iblockstate.getValue(VARIANT))
            {
                return false;
            }
        }

        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
		for (int i = 0; i < EnumType.values().length; i++) {
			items.add(new ItemStack(this, 1, i));
		}
    }
	
	@Override
	public int damageDropped(IBlockState state)
    {
        return ((BlockLTTransparentColored.EnumType)state.getValue(VARIANT)).getMetadata();
    }
	
	@Override
	public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(VARIANT, BlockLTTransparentColored.EnumType.byMetadata(meta));
    }
	
	@Override
	public int getMetaFromState(IBlockState state)
    {
        return ((BlockLTTransparentColored.EnumType)state.getValue(VARIANT)).getMetadata();
    }
	
	@Override
	protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {VARIANT});
    }
	
	public static enum EnumType implements IStringSerializable {
		
		clean,
		thick,
		thin,
		thinner,
		thinnest,
		
		water;
		
		public static EnumType byMetadata(int meta)
		{
			return values()[meta];
		}
		
		public int getMetadata()
		{
			return ordinal();
		}

		@Override
		public String getName() {
			return name();
		}
	}

	@Override
	public List<LittleTileBox> getCollisionBoxes(LittleTileBlock tile, List<LittleTileBox> defaultBoxes) {
		if(tile.getBlockState().getValue(VARIANT) == EnumType.water)
			return new ArrayList<>();
		return defaultBoxes;
	}
	
	@Override
	public boolean isMaterial(LittleTileBlock tile, Material material) {
		if(tile.getBlockState().getValue(VARIANT) == EnumType.water)
			return material == Material.WATER;
		return ISpecialBlockHandler.super.isMaterial(tile, material);
	}
	
	@Override
	public boolean isLiquid(LittleTileBlock tile)
	{
		if(tile.getBlockState().getValue(VARIANT) == EnumType.water)
			return true;
		return ISpecialBlockHandler.super.isLiquid(tile);
	}
	
	@Override
	public boolean canBeConvertedToVanilla(LittleTileBlock tile) {
		return tile.getBlockState().getValue(VARIANT) != EnumType.water;
	}

	@Override
	public IBlockState getFakeState(IBlockState state) {
		if(state.getValue(VARIANT) == EnumType.water)
			return Blocks.WATER.getDefaultState();
		return state;
	}
	
	@Override
	public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state,
			EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY,
			float hitZ) {
		if(state.getValue(VARIANT) == EnumType.water && hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket)
		{
			tile.setBlock(LittleTiles.flowingWater, 0);
			tile.te.updateTiles();
			return true;
		}
		return ISpecialBlockHandler.super.onBlockActivated(tile, worldIn, pos, state, playerIn, hand, heldItem, side, hitX,
				hitY, hitZ);
	}

}