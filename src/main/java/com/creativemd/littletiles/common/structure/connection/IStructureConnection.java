package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;

import net.minecraft.util.math.BlockPos;

public interface IStructureConnection {
    
    public BlockPos getStructurePosition();
    
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException;
    
    public default void checkConnection() throws CorruptedConnectionException, NotYetConnectedException {
        getStructure();
    }
    
    public int getIndex();
    
    public int getAttribute();
    
    public default boolean isLinkToAnotherWorld() {
        return false;
    }
    
}
