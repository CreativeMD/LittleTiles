package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.blocks.ISpecialBlockHandler;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored.EnumType;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.nsc.transform.patmat.ScalaLogic.TreesAndTypesDomain.Var;

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
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
		for (int i = 0; i < EnumType.values().length; i++) {
			items.add(new ItemStack(this, 1, i));
		}
    }
	
	@Override
	public int damageDropped(IBlockState state)
    {
        return ((BlockLTColored.EnumType)state.getValue(VARIANT)).getMetadata();
    }
	
	@Override
	public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(VARIANT, BlockLTColored.EnumType.byMetadata(meta));
    }
	
	@Override
	public int getMetaFromState(IBlockState state)
    {
        return ((BlockLTColored.EnumType)state.getValue(VARIANT)).getMetadata();
    }
	
	@Override
	protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {VARIANT});
    }
	
	@Override
    public int getLightValue(IBlockState state)
    {
		if(state.getValue(VARIANT) == EnumType.light_clean)
			return 15;
        return 0;
    }
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
		if(state.getValue(VARIANT) == EnumType.light_clean)
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
		lava,
		plank;
		
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
		if(tile.getBlockState().getValue(VARIANT) == EnumType.lava)
			return new ArrayList<>();
		return defaultBoxes;
	}

}
