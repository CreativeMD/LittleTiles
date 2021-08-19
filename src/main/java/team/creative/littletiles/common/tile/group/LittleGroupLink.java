package team.creative.littletiles.common.tile.group;

import java.util.Collections;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.volume.LittleVolumes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleGroupLink implements LittleGroup {
    
    public final LittleStructure structure;
    
    public LittleGroupLink(LittleStructure structure) {
        super();
        this.structure = structure;
    }
    
    @Override
    public boolean hasStructure() {
        return true;
    }
    
    @Override
    public String getStructureId() {
        return structure.type.id;
    }
    
    @Override
    public String getStructureName() {
        return structure.name;
    }
    
    @Override
    public int getSmallest() {
        return LittleGrid.min().count;
    }
    
    @Override
    public LittleStructureType getStructureType() {
        return structure.type;
    }
    
    @Override
    public boolean containsIngredients() {
        return true;
    }
    
    @Override
    public void moveBoxes(LittleVecGrid vec) {}
    
    @Override
    public void mirrorBoxes(Axis axis, LittleVec doubledCenter) {}
    
    @Override
    public void rotateBoxes(Rotation rotation, LittleVec doubledCenter) {}
    
    @Override
    public void advancedScaleBoxes(int from, int to) {}
    
    @Override
    public LittleGroupLink copy() {
        return new LittleGroupLink(structure);
    }
    
    @Override
    public Iterable<LittleGroup> children() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public int childrenCount() {
        return 0;
    }
    
    @Override
    public LittleGrid getGrid() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public LittleGroup getParent() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean hasChildren() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public LittleGroupType type() {
        return LittleGroupType.LINK;
    }
    
    @Override
    public int tileCount() {
        return 0;
    }
    
    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public double getVolume() {
        return 0;
    }
    
    @Override
    public LittleVolumes getVolumes() {
        return null;
    }
    
    @Override
    public void combineBlockwiseInternal() {}
    
}
