package team.creative.littletiles.common.structure.connection.children;

import java.util.List;

import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

public class ItemChildrenList extends ChildrenList<LittleGroup> {
    
    public ItemChildrenList(LittleGroup parent, List<LittleGroup> children) {
        super(children);
        this.parent = parent;
    }
    
    @Override
    protected void added(LittleGroup child) {
        child.children.parent = parent;
    }
    
}
