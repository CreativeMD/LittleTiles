package team.creative.littletiles.common.structure.connection;

import java.util.List;

import team.creative.littletiles.common.tile.group.LittleGroup;

public class ItemChildrenList extends ChildrenList<LittleGroup> {
    
    private final LittleGroup parent;
    
    public ItemChildrenList(LittleGroup parent, List<LittleGroup> children) {
        super(children);
        this.parent = parent;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    protected void added(LittleGroup child) {
        LittleGroup.setGroupParent(child, parent);
    }
    
}
