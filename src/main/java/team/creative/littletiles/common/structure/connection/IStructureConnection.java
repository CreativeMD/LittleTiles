package team.creative.littletiles.common.structure.connection;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public interface IStructureConnection {
    
    public BlockPos getStructurePosition();
    
    public LittleStructure getStructureUncached() throws CorruptedConnectionException, NotYetConnectedException;
    
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException;
    
    public default LittleStructure getStructureOrNull() {
        try {
            return getStructure();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return null;
    }
    
    public default void checkConnection() throws CorruptedConnectionException, NotYetConnectedException {
        getStructureUncached();
    }
    
    public int getIndex();
    
    public int getAttribute();
    
    public default boolean isLinkToAnotherWorld() {
        return false;
    }
    
}
