package com.creativemd.littletiles.common.block;

import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBucket;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.tile.LittleTile;

public class BlockLittleDyeable extends Block implements ISpecialBlockHandler {
    
    public static final PropertyEnum<LittleDyeableType> VARIANT = PropertyEnum.<LittleDyeableType>create("variant", LittleDyeableType.class);
    
    public BlockLittleDyeable() {
        super(Material.ROCK, MapColor.SNOW);
        setCreativeTab(LittleTiles.littleTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, LittleDyeableType.CLEAN));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (int i = 0; i < LittleDyeableType.values().length; i++)
            if (LittleDyeableType.values()[i].shouldBeShown())
                items.add(new ItemStack(this, 1, i));
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, LittleDyeableType.byMetadata(meta));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }
    
    @Override
    public int getLightValue(IBlockState state) {
        if (state.getValue(VARIANT) == LittleDyeableType.LIGHT_CLEAN)
            return 15;
        return 0;
    }
    
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(VARIANT) == LittleDyeableType.LIGHT_CLEAN)
            return 15;
        return 0;
    }
    
    public IBlockState get(LittleDyeableType type) {
        return getDefaultState().withProperty(VARIANT, type);
    }
    
    public enum LittleDyeableType implements IStringSerializable {
        
        CLEAN,
        FLOOR,
        GRAINY_BIG,
        GRAINY,
        GRAINY_LOW,
        BRICK,
        BORDERED,
        BRICK_BIG,
        CHISELED,
        BROKEN_BRICK_BIG,
        CLAY,
        LIGHT_CLEAN {
            @Override
            public boolean shouldBeShown() {
                return false;
            }
        },
        LAVA {
            @Override
            public boolean isLava() {
                return true;
            }
        },
        STRIPS,
        WHITE_LAVA {
            @Override
            public boolean isLava() {
                return true;
            }
        };
        
        public boolean shouldBeShown() {
            return true;
        }
        
        public boolean isLava() {
            return false;
        }
        
        public static LittleDyeableType byMetadata(int meta) {
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
        return tile.getBlockState().getValue(VARIANT).isLava();
    }
    
    @Override
    public boolean isMaterial(LittleTile tile, Material material) {
        if (tile.getBlockState().getValue(VARIANT).isLava())
            return material == Material.LAVA;
        return ISpecialBlockHandler.super.isMaterial(tile, material);
    }
    
    @Override
    public boolean isLiquid(LittleTile tile) {
        if (tile.getBlockState().getValue(VARIANT).isLava())
            return true;
        return ISpecialBlockHandler.super.isLiquid(tile);
    }
    
    @Override
    public boolean canBeConvertedToVanilla(LittleTile tile) {
        return !tile.getBlockState().getValue(VARIANT).isLava();
    }
    
    @Override
    public boolean onBlockActivated(IParentTileList list, LittleTile tile, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState state = tile.getBlockState();
        if (state.getValue(VARIANT).isLava() && hand == EnumHand.MAIN_HAND && heldItem.getItem() instanceof ItemBucket) {
            if (state.getValue(VARIANT) == LittleDyeableType.LAVA)
                tile.setBlock(LittleTiles.flowingLava, 0);
            else
                tile.setBlock(LittleTiles.whiteFlowingLava, 0);
            list.getTe().updateTiles();
            return true;
        }
        return ISpecialBlockHandler.super.onBlockActivated(list, tile, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }
    
    @Override
    public Vec3d getFogColor(IParentTileList list, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks) {
        if (tile.getBlockState().getValue(VARIANT).isLava())
            return new Vec3d(0.6F, 0.1F, 0.0F);
        return ISpecialBlockHandler.super.getFogColor(list, tile, entity, originalColor, partialTicks);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
        if (LittleDyeableType.values()[thisTile.getMeta()].isLava())
            return tile.getBlock() == LittleTiles.flowingLava;
        return false;
    }
    
}
