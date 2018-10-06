package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.blocks.ISpecialBlockHandler;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTColored extends Block implements ISpecialBlockHandler {
	
	public static final PropertyEnum<BlockLTColored.EnumType> VARIANT = PropertyEnum.<BlockLTColored.EnumType>create("variant", BlockLTColored.EnumType.class);
	
	public BlockLTColored() {
		super(Material.ROCK);
		setCreativeTab(LittleTiles.littleTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockLTColored.EnumType.clean));
	}
	
	//public static final String[] subBlocks = new String[]{"clean", "floor", "grainybig", "grainy", "grainylow", "brick", "bordered", "brickBig", "structured", "brokenBrickBig", "clay"};
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
		for (int i = 0; i < EnumType.values().length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return ((BlockLTColored.EnumType) state.getValue(VARIANT)).getMetadata();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, BlockLTColored.EnumType.byMetadata(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ((BlockLTColored.EnumType) state.getValue(VARIANT)).getMetadata();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { VARIANT });
	}
	
	@Override
	public int getLightValue(IBlockState state) {
		if (state.getValue(VARIANT) == EnumType.light_clean)
			return 15;
		return 0;
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state.getValue(VARIANT) == EnumType.light_clean)
			return 15;
		return 0;
	}
	
	public static enum EnumType implements IStringSerializable {
		
		clean,
		floor,
		grainy_big,
		grainy,
		grainy_low,
		brick,
		bordered,
		brick_big,
		structured,
		broken_brick_big,
		clay,
		light_clean,
		lava {
			@Override
			public boolean isLava() {
				return true;
			}
		},
		plank,
		white_lava {
			@Override
			public boolean isLava() {
				return true;
			}
		};
		
		public boolean isLava() {
			return false;
		}
		
		public static EnumType byMetadata(int meta) {
			return values()[meta];
		}
		
		public int getMetadata() {
			return ordinal();
		}
		
		@Override
		public String getName() {
			return name();
		}
	}
	
	@Override
	public List<LittleTileBox> getCollisionBoxes(LittleTileBlock tile, List<LittleTileBox> defaultBoxes) {
		if (tile.getBlockState().getValue(VARIANT).isLava())
			return new ArrayList<>();
		return defaultBoxes;
	}
	
	@Override
	public boolean isMaterial(LittleTileBlock tile, Material material) {
		if (tile.getBlockState().getValue(VARIANT).isLava())
			return material == Material.LAVA;
		return ISpecialBlockHandler.super.isMaterial(tile, material);
	}
	
	@Override
	public boolean isLiquid(LittleTileBlock tile) {
		if (tile.getBlockState().getValue(VARIANT).isLava())
			return true;
		return ISpecialBlockHandler.super.isLiquid(tile);
	}
	
	@Override
	public boolean canBeConvertedToVanilla(LittleTileBlock tile) {
		return !tile.getBlockState().getValue(VARIANT).isLava();
	}
	
	@Override
	public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state.getValue(VARIANT).isLava() && hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket) {
			if (state.getValue(VARIANT) == EnumType.lava)
				tile.setBlock(LittleTiles.flowingLava, 0);
			else
				tile.setBlock(LittleTiles.whiteFlowingLava, 0);
			tile.te.updateTiles();
			return true;
		}
		return ISpecialBlockHandler.super.onBlockActivated(tile, worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTileBlock thisTile, LittleTileBlock tile) {
		if (EnumType.values()[thisTile.getMeta()].isLava())
			return tile.getBlock() == LittleTiles.flowingLava;
		return false;
	}
	
}
