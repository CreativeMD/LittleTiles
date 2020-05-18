package com.creativemd.littletiles.common.block;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.api.IFakeRenderingBlock;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.tile.LittleTile;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTTransparentColored extends Block implements ISpecialBlockHandler, IFakeRenderingBlock {
	
	public static final PropertyEnum<BlockLTTransparentColored.EnumType> VARIANT = PropertyEnum.<BlockLTTransparentColored.EnumType>create("variant", BlockLTTransparentColored.EnumType.class);
	
	public BlockLTTransparentColored() {
		super(Material.ROCK);
		setCreativeTab(LittleTiles.littleTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockLTTransparentColored.EnumType.clean));
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
		Block block = iblockstate.getBlock();
		
		if (block == this) {
			if (blockState.getValue(VARIANT) == iblockstate.getValue(VARIANT)) {
				return false;
			}
		}
		
		return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
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
		return this.getDefaultState().withProperty(VARIANT, BlockLTTransparentColored.EnumType.byMetadata(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { VARIANT });
	}
	
	public static enum EnumType implements IStringSerializable {
		
		clean,
		thick,
		thin,
		thinner,
		thinnest,
		
		water {
			@Override
			public boolean isWater() {
				return true;
			}
		},
		white_water {
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
	public boolean canWalkThrough(LittleTile tile) {
		return tile.getBlockState().getValue(VARIANT).isWater();
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
	
	@Override
	public IBlockState getFakeState(IBlockState state) {
		if (state.getValue(VARIANT).isWater())
			return Blocks.WATER.getDefaultState();
		return state;
	}
	
	@Override
	public boolean onBlockActivated(LittleTile tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state.getValue(VARIANT).isWater() && hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket) {
			if (state.getValue(VARIANT) == EnumType.water)
				tile.setBlock(LittleTiles.flowingWater, 0);
			else
				tile.setBlock(LittleTiles.whiteFlowingWater, 0);
			tile.te.updateTiles();
			return true;
		}
		return ISpecialBlockHandler.super.onBlockActivated(tile, worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}
	
	@Override
	public Vec3d getFogColor(World world, LittleTile tile, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
		if (state.getValue(VARIANT).isWater()) {
			float f12 = 0.0F;
			
			if (entity instanceof net.minecraft.entity.EntityLivingBase) {
				net.minecraft.entity.EntityLivingBase ent = (net.minecraft.entity.EntityLivingBase) entity;
				f12 = net.minecraft.enchantment.EnchantmentHelper.getRespirationModifier(ent) * 0.2F;
				
				if (ent.isPotionActive(net.minecraft.init.MobEffects.WATER_BREATHING))
					f12 = f12 * 0.3F + 0.6F;
			}
			return new Vec3d(0.02F + f12, 0.02F + f12, 0.2F + f12);
		}
		return ISpecialBlockHandler.super.getFogColor(world, tile, pos, state, entity, originalColor, partialTicks);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
		if (EnumType.values()[thisTile.getMeta()].isWater())
			return tile.getBlock() == LittleTiles.flowingWater;
		return false;
	}
	
}