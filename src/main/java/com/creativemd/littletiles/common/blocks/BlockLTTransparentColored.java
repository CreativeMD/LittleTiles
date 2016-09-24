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
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.nsc.transform.patmat.ScalaLogic.TreesAndTypesDomain.Var;

public class BlockLTTransparentColored extends Block{
	
	public static final PropertyEnum<BlockLTTransparentColored.EnumType> VARIANT = PropertyEnum.<BlockLTTransparentColored.EnumType>create("variant", BlockLTTransparentColored.EnumType.class);

	public BlockLTTransparentColored() {
		super(Material.ROCK);
		setCreativeTab(CreativeTabs.DECORATIONS);
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
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
		for (int i = 0; i < EnumType.values().length; i++) {
			list.add(new ItemStack(item, 1, i));
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

}