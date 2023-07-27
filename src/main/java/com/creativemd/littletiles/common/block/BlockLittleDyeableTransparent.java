package com.creativemd.littletiles.common.block;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.api.IFakeRenderingBlock;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLittleDyeableTransparent extends Block implements ISpecialBlockHandler, IFakeRenderingBlock {
    
    public static final PropertyEnum<LittleDyeableTransparent> VARIANT = PropertyEnum.<LittleDyeableTransparent>create("variant", LittleDyeableTransparent.class);
    
    public BlockLittleDyeableTransparent() {
        super(Material.ROCK);
        setCreativeTab(LittleTiles.littleTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, LittleDyeableTransparent.CLEAN));
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
        for (int i = 0; i < LittleDyeableTransparent.values().length; i++)
            if (LittleDyeableTransparent.values()[i].shouldBeShown())
                items.add(new ItemStack(this, 1, i));
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, BlockLittleDyeableTransparent.LittleDyeableTransparent.byMetadata(meta));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }
    
    public static enum LittleDyeableTransparent implements IStringSerializable {
        
        CLEAN {
            @Override
            public boolean shouldBeShown() {
                return false;
            }
        },
        THICK {
            @Override
            public boolean shouldBeShown() {
                return false;
            }
        },
        THIN {
            @Override
            public boolean shouldBeShown() {
                return false;
            }
        },
        THINNER {
            @Override
            public boolean shouldBeShown() {
                return false;
            }
        },
        THINNEST {
            @Override
            public boolean shouldBeShown() {
                return false;
            }
        },
        WATER {
            @Override
            public boolean isWater() {
                return true;
            }
        },
        WHITE_WATER {
            @Override
            public boolean isWater() {
                return true;
            }
        };
        
        public boolean shouldBeShown() {
            return true;
        }
        
        public boolean isWater() {
            return false;
        }
        
        public static LittleDyeableTransparent byMetadata(int meta) {
            return values()[meta];
        }
        
        public int getMetadata() {
            return ordinal();
        }
        
        @Override
        public String getName() {
            return name().toLowerCase();
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
    public boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState state = tile.getBlockState();
        if (state.getValue(VARIANT).isWater() && hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket) {
            if (state.getValue(VARIANT) == LittleDyeableTransparent.WATER)
                tile.setBlock(LittleTiles.flowingWater, 0);
            else
                tile.setBlock(LittleTiles.whiteFlowingWater, 0);
            parent.getTe().updateTiles();
            return true;
        }
        return ISpecialBlockHandler.super.onBlockActivated(parent, tile, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }
    
    @Override
    public Vec3d getFogColor(IParentTileList parent, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks) {
        if (tile.getBlockState().getValue(VARIANT).isWater()) {
            float f12 = 0.0F;
            
            if (entity instanceof net.minecraft.entity.EntityLivingBase) {
                net.minecraft.entity.EntityLivingBase ent = (net.minecraft.entity.EntityLivingBase) entity;
                f12 = net.minecraft.enchantment.EnchantmentHelper.getRespirationModifier(ent) * 0.2F;
                
                if (ent.isPotionActive(net.minecraft.init.MobEffects.WATER_BREATHING))
                    f12 = f12 * 0.3F + 0.6F;
            }
            return new Vec3d(0.02F + f12, 0.02F + f12, 0.2F + f12);
        }
        return ISpecialBlockHandler.super.getFogColor(parent, tile, entity, originalColor, partialTicks);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
        if (LittleDyeableTransparent.values()[thisTile.getMeta()].isWater())
            return tile.getBlock() == LittleTiles.flowingWater;
        return false;
    }
    
}