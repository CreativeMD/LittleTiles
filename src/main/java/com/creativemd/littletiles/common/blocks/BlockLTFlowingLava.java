package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.IFakeRenderingBlock;
import com.creativemd.littletiles.common.api.blocks.ISpecialBlockHandler;
import com.creativemd.littletiles.common.blocks.BlockLTColored.EnumType;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleUtils;
import com.google.common.collect.Iterables;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTFlowingLava extends Block implements ISpecialBlockHandler, IFakeRenderingBlock {
	
	public static final PropertyEnum<EnumFacing> DIRECTION = PropertyEnum.<EnumFacing>create("direction", EnumFacing.class);
	
	public final BlockLTColored.EnumType still;
	
	public BlockLTFlowingLava(BlockLTColored.EnumType still) {
		super(Material.LAVA);
		this.still = still;
		setCreativeTab(LittleTiles.littleTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(DIRECTION, EnumFacing.EAST));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        Block block = iblockstate.getBlock();

        if (block == this)
        {
        	return false;
        }

        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return false;
    }
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
		/*for (int i = 0; i < DIRECTION.getAllowedValues().size(); i++) {
			items.add(new ItemStack(this, 1, i));
		}*/
    }
	
	@Override
	public int damageDropped(IBlockState state)
    {
        return state.getValue(DIRECTION).ordinal();
    }
	
	@Override
	public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(DIRECTION, EnumFacing.getFront(meta));
    }
	
	@Override
	public int getMetaFromState(IBlockState state)
    {
        return state.getValue(DIRECTION).ordinal();
    }
	
	@Override
	protected BlockStateContainer createBlockState()
    {
		return new BlockStateContainer(this, DIRECTION);
    }
	
	@Override
	public List<LittleTileBox> getCollisionBoxes(LittleTileBlock tile, List<LittleTileBox> defaultBoxes) {
		return new ArrayList<>();
	}
	
	@Override
	public boolean isMaterial(LittleTileBlock tile, Material material) {
		return material == Material.LAVA;
	}
	
	@Override
	public boolean isLiquid(LittleTileBlock tile) {
		return true;
	}
	
	@Override
	public boolean shouldCheckForCollision(LittleTileBlock tile) {
		return true;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, LittleTileBlock tile, BlockPos pos, IBlockState state, Entity entityIn)
	{
		AxisAlignedBB box = entityIn.getEntityBoundingBox();
		LittleTileVec center = new LittleTileVec(tile.getContext(), new Vec3d((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2).subtract(new Vec3d(tile.te.getPos()))); 
		
		if(tile.box.isVecInsideBox(center.x, center.y, center.z))
		{
			double scale = 0.05;
			Vec3d vec = new Vec3d(tile.getBlockState().getValue(DIRECTION).getDirectionVec()).normalize();
			entityIn.motionX += vec.x * scale;
			entityIn.motionY += vec.y * scale;
			entityIn.motionZ += vec.z * scale;
		}
	}
	
	/*@Override
	public Vec3d modifyAcceleration(LittleTileBlock tile, Entity entityIn, Vec3d motion)
	{
		AxisAlignedBB box = entityIn.getEntityBoundingBox();
		LittleTileVec center = new LittleTileVec(tile.getContext(), new Vec3d((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2).subtract(new Vec3d(tile.te.getPos()))); 
		
		if(tile.box.isVecInsideBox(center.x, center.y, center.z))
		{
			double scale = 0.001;
			Vec3d vec = new Vec3d(tile.getBlockState().getValue(DIRECTION).getDirectionVec()).normalize();
			entityIn.motionX += vec.x * scale;
			entityIn.motionY += vec.y * scale;
			entityIn.motionZ += vec.z * scale;
		}
		return new Vec3d(tile.getBlockState().getValue(DIRECTION).getDirectionVec());
	}*/
	
	@Override
	public boolean canBeConvertedToVanilla(LittleTileBlock tile) {
		return false;
	}

	@Override
	public IBlockState getFakeState(IBlockState state) {
		return Blocks.FLOWING_LAVA.getDefaultState();
	}
	
	@Override
	public LittleTilePreview getPreview(LittleTileBlock tile) {
		NBTTagCompound nbt = new NBTTagCompound();
		tile.saveTileExtra(nbt);
		nbt.setString("tID", tile.getID());		
		return new LittleFlowingLavaPreview(tile.box.copy(), nbt);
	}
	
	@Override
	public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state,
			EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY,
			float hitZ) {
		if(hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket && SpecialServerConfig.allowFlowingLava)
		{
			int meta = tile.getMeta() + 1;
			if(meta > EnumFacing.VALUES.length)
				tile.setBlock(LittleTiles.coloredBlock, still.ordinal());
			else
				tile.setMeta(meta);
			tile.te.updateTiles();
			return true;
		}
		return ISpecialBlockHandler.super.onBlockActivated(tile, worldIn, pos, state, playerIn, hand, heldItem, side, hitX,
				hitY, hitZ);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTileBlock thisTile, LittleTileBlock tile)
	{
		if(tile.getBlock() == this)
			return true;
		if(tile.getBlock() == LittleTiles.coloredBlock && EnumType.values()[tile.getMeta()].isLava())
			return true;			
		return false;
	}
	
	public static class LittleFlowingLavaPreview extends LittleTilePreview {
		
		public LittleFlowingLavaPreview(NBTTagCompound nbt) {
			super(nbt);
		}

		public LittleFlowingLavaPreview(LittleTileBox box, NBTTagCompound tileData) {
			super(box, tileData);
		}
		
		
		@Override
		public void rotatePreview(Rotation rotation, LittleTileVec doubledCenter) {
			super.rotatePreview(rotation, doubledCenter);
			getTileData().setInteger("meta", RotationUtils.rotateFacing(EnumFacing.getFront(getPreviewBlockMeta()), rotation).ordinal());
		}
		
		@Override
		public void flipPreview(Axis axis, LittleTileVec doubledCenter) {
			super.flipPreview(axis, doubledCenter);
			EnumFacing facing = EnumFacing.getFront(getPreviewBlockMeta());
			if(facing.getAxis() == axis)
				facing = facing.getOpposite();
			getTileData().setInteger("meta", facing.ordinal());
		}
	}
	
}
