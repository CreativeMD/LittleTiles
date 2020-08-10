package com.creativemd.littletiles.common.block;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTColored2 extends Block implements ISpecialBlockHandler {
	
	public static final PropertyEnum<BlockLTColored2.EnumType> VARIANT = PropertyEnum.<BlockLTColored2.EnumType>create("variant", BlockLTColored2.EnumType.class);
	
	public BlockLTColored2() {
		super(Material.ROCK);
		setCreativeTab(LittleTiles.littleTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockLTColored2.EnumType.gravel));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		for (int i = 0; i < EnumType.values().length; i++) {
			items.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, BlockLTColored2.EnumType.byMetadata(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { VARIANT });
	}
	
	public enum EnumType implements IStringSerializable {
		
		gravel, sand, stone, wood, white_opaque_water {
			@Override
			public boolean isWater() {
				return true;
			}
		};
		
		public boolean isWater() {
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
	public LittleBox getCollisionBox(LittleTile tile, LittleBox defaultBox) {
		if (tile.getBlockState().getValue(VARIANT).isWater())
			return null;
		return defaultBox;
	}
	
	@Override
	public boolean isMaterial(LittleTile tile, Material material) {
		if (tile.getBlockState().getValue(VARIANT).isWater())
			return material == Material.WATER;
		return ISpecialBlockHandler.super.isMaterial(tile, material);
	}
	
	@Override
	public boolean isLiquid(LittleTile tile) {
		if (tile.getBlockState().getValue(VARIANT).isWater())
			return true;
		return ISpecialBlockHandler.super.isLiquid(tile);
	}
	
	@Override
	public boolean canBeConvertedToVanilla(LittleTile tile) {
		return !tile.getBlockState().getValue(VARIANT).isWater();
	}
	
}
