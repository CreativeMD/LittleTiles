package com.creativemd.littletiles.common.block;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
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

public class BlockLittleDyeable2 extends Block implements ISpecialBlockHandler {
    
    public static final PropertyEnum<LittleDyeableType2> VARIANT = PropertyEnum.<LittleDyeableType2>create("variant", LittleDyeableType2.class);
    
    public BlockLittleDyeable2() {
        super(Material.ROCK, MapColor.SNOW);
        setCreativeTab(LittleTiles.littleTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, LittleDyeableType2.GRAVEL));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (int i = 0; i < LittleDyeableType2.values().length; i++)
            items.add(new ItemStack(this, 1, i));
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, LittleDyeableType2.byMetadata(meta));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }
    
    public IBlockState get(LittleDyeableType2 type) {
        return getDefaultState().withProperty(VARIANT, type);
    }
    
    public enum LittleDyeableType2 implements IStringSerializable {
        
        GRAVEL,
        SAND,
        STONE,
        CORK,
        WHITE_OPAQUE_WATER {
            @Override
            public boolean isWater() {
                return true;
            }
        };
        
        public boolean isWater() {
            return false;
        }
        
        public static LittleDyeableType2 byMetadata(int meta) {
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
