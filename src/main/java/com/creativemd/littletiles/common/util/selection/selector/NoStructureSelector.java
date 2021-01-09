package com.creativemd.littletiles.common.util.selection.selector;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import net.minecraft.nbt.NBTTagCompound;

public class NoStructureSelector extends TileSelector {
    
    public NoStructureSelector() {
        
    }
    
    @Override
    protected void saveNBT(NBTTagCompound nbt) {
        
    }
    
    @Override
    protected void loadNBT(NBTTagCompound nbt) {
        
    }
    
    @Override
    public boolean is(IParentTileList parent, LittleTile tile) {
        return !parent.isStructure();
    }
    
}
