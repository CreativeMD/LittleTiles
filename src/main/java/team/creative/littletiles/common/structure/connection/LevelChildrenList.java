package team.creative.littletiles.common.structure.connection;

import java.util.List;

import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LevelChildrenList extends ChildrenList<StructureChildConnection> {
    
    public LevelChildrenList(List<StructureChildConnection> children) {
        super(children);
    }
    
    public StructureChildConnection removeExtension(String key) throws CorruptedConnectionException, NotYetConnectedException {
        StructureChildConnection ex = super.removeExt(key);
        if (ex != null)
            ex.getStructure().removeParent();
        return ex;
    }
    
    @Override
    protected void added(StructureChildConnection child) {}
    
}
