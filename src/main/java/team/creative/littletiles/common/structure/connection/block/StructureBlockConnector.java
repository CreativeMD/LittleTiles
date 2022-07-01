package team.creative.littletiles.common.structure.connection.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.MissingBlockException;
import team.creative.littletiles.common.structure.exception.MissingStructureException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class StructureBlockConnector {
    
    public final BlockPos pos;
    public final LittleStructure structure;
    private BETiles cachedBE;
    
    public StructureBlockConnector(LittleStructure structure, BlockPos pos) {
        this.structure = structure;
        this.pos = pos;
    }
    
    public BlockPos getAbsolutePos() {
        return structure.getPos().offset(pos);
    }
    
    public BETiles getBlockEntity() throws CorruptedConnectionException, NotYetConnectedException {
        if (cachedBE != null)
            if (cachedBE.isRemoved())
                cachedBE = null;
            else
                return cachedBE;
            
        Level level = structure.getLevel();
        
        BlockPos absoluteCoord = getAbsolutePos();
        LevelChunk chunk = level.getChunkAt(absoluteCoord);
        if (LevelUtils.checkIfChunkExists(chunk)) {
            BlockEntity be = level.getBlockEntity(absoluteCoord);
            if (be instanceof BETiles)
                return cachedBE = (BETiles) be;
            else
                throw new MissingBlockException(absoluteCoord);
        } else
            throw new NotYetConnectedException();
    }
    
    public void connect() throws CorruptedConnectionException, NotYetConnectedException {
        BETiles be = getBlockEntity();
        if (!be.hasLoaded())
            throw new NotYetConnectedException();
        IStructureParentCollection structure = be.getStructure(this.structure.getIndex());
        if (structure == null)
            throw new MissingStructureException(be.getBlockPos());
    }
    
    public IStructureParentCollection getList() throws CorruptedConnectionException, NotYetConnectedException {
        BETiles be = getBlockEntity();
        if (!be.hasLoaded())
            throw new NotYetConnectedException();
        IStructureParentCollection structure = be.getStructure(this.structure.getIndex());
        if (structure != null)
            return structure;
        throw new MissingStructureException(be.getBlockPos());
    }
    
    public int count() throws CorruptedConnectionException, NotYetConnectedException {
        return getList().size();
    }
    
    public void remove() throws CorruptedConnectionException, NotYetConnectedException {
        getBlockEntity().updateTiles((x) -> x.removeStructure(structure.getIndex()));
    }
    
}
