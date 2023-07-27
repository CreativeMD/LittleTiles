package com.creativemd.littletiles.common.block;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.api.IFakeRenderingBlock;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.block.BlockLittleDyeable.LittleDyeableType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTFlowingLava extends Block implements ISpecialBlockHandler, IFakeRenderingBlock {
    
    public static final PropertyEnum<EnumFacing> DIRECTION = PropertyEnum.<EnumFacing>create("direction", EnumFacing.class);
    
    public final BlockLittleDyeable.LittleDyeableType still;
    
    public BlockLTFlowingLava(BlockLittleDyeable.LittleDyeableType still) {
        super(Material.LAVA);
        this.still = still;
        setCreativeTab(LittleTiles.littleTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(DIRECTION, EnumFacing.EAST));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        Block block = iblockstate.getBlock();
        
        if (block == this) {
            return false;
        }
        
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
    
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        /*
         * for (int i = 0; i < DIRECTION.getAllowedValues().size(); i++) { items.add(new
         * ItemStack(this, 1, i)); }
         */
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(DIRECTION).ordinal();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DIRECTION, EnumFacing.getFront(meta));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DIRECTION).ordinal();
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION);
    }
    
    @Override
    public boolean canWalkThrough(LittleTile tile) {
        return true;
    }
    
    @Override
    public boolean isMaterial(LittleTile tile, Material material) {
        return material == Material.LAVA;
    }
    
    @Override
    public boolean isLiquid(LittleTile tile) {
        return true;
    }
    
    @Override
    public boolean shouldCheckForCollision(LittleTile tile) {
        return true;
    }
    
    @Override
    public void onEntityCollidedWithBlock(IParentTileList parent, LittleTile tile, Entity entityIn) {
        AxisAlignedBB box = entityIn.getEntityBoundingBox();
        LittleVec center = new LittleVec(parent.getContext(), new Vec3d((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2)
            .subtract(new Vec3d(parent.getPos())));
        LittleBox testBox = new LittleBox(center, 1, 1, 1);
        if (tile.intersectsWith(testBox)) {
            double scale = 0.05;
            Vec3d vec = new Vec3d(tile.getBlockState().getValue(DIRECTION).getDirectionVec()).normalize();
            entityIn.motionX += vec.x * scale;
            entityIn.motionY += vec.y * scale;
            entityIn.motionZ += vec.z * scale;
        }
    }
    
    @Override
    public boolean canBeConvertedToVanilla(LittleTile tile) {
        return false;
    }
    
    @Override
    public IBlockState getFakeState(IBlockState state) {
        return Blocks.FLOWING_LAVA.getDefaultState();
    }
    
    @Override
    public boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket && LittleTiles.CONFIG.general.allowFlowingLava) {
            int meta = tile.getMeta() + 1;
            if (meta > EnumFacing.VALUES.length)
                tile.setBlock(LittleTiles.dyeableBlock, still.ordinal());
            else
                tile.setMeta(meta);
            parent.getTe().updateTiles();
            return true;
        }
        return ISpecialBlockHandler.super.onBlockActivated(parent, tile, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }
    
    @Override
    public Vec3d getFogColor(IParentTileList parent, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks) {
        return new Vec3d(0.6F, 0.1F, 0.0F);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
        if (tile.getBlock() == this)
            return true;
        if (tile.getBlock() == LittleTiles.dyeableBlock && LittleDyeableType.values()[tile.getMeta()].isLava())
            return true;
        return false;
    }
    
    @Override
    public void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
        preview.getTileData().setInteger("meta", RotationUtils.rotate(EnumFacing.getFront(preview.getMeta()), rotation).ordinal());
    }
    
    @Override
    public void flipPreview(Axis axis, LittlePreview preview, LittleVec doubledCenter) {
        EnumFacing facing = EnumFacing.getFront(preview.getMeta());
        if (facing.getAxis() == axis)
            facing = facing.getOpposite();
        preview.getTileData().setInteger("meta", facing.ordinal());
    }
    
    public static class LittleFlowingLavaPreview extends LittlePreview {
        
        public LittleFlowingLavaPreview(NBTTagCompound nbt) {
            super(nbt);
        }
        
        public LittleFlowingLavaPreview(LittleBox box, NBTTagCompound tileData) {
            super(box, tileData);
        }
    }
    
}
