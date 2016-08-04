package com.creativemd.littletiles.common.blocks;

import java.util.List;

import com.creativemd.littletiles.LittleTiles;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTColored extends Block{
	
	public static final PropertyEnum<BlockLTColored.EnumType> VARIANT = PropertyEnum.<BlockLTColored.EnumType>create("variant", BlockLTColored.EnumType.class);

	public BlockLTColored() {
		super(Material.ROCK);
		setCreativeTab(CreativeTabs.DECORATIONS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockLTColored.EnumType.clean));
	}
	
	//public static final String[] subBlocks = new String[]{"clean", "floor", "grainybig", "grainy", "grainylow", "brick", "bordered", "brickBig", "structured", "brokenBrickBig", "clay"};
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
		for (int i = 0; i < EnumType.values().length; i++) {
			list.add(new ItemStack(item, 1, i));
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
		clay;
		
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

}
