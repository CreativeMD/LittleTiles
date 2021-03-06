package com.creativemd.littletiles.common.util.selection.selector;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class TileSelectorBlock extends TileSelector {
    
    public Block block;
    
    public TileSelectorBlock(Block block) {
        this.block = block;
    }
    
    public TileSelectorBlock() {
        
    }
    
    @Override
    protected void saveNBT(NBTTagCompound nbt) {
        nbt.setString("block", block.getRegistryName().toString());
    }
    
    @Override
    protected void loadNBT(NBTTagCompound nbt) {
        block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block")));
    }
    
    @Override
    public boolean is(IParentTileList parent, LittleTile tile) {
        return tile.getBlock() == block;
    }
    
    public IBlockState getState() {
        return block.getDefaultState();
    }
    
}
