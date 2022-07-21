package team.creative.littletiles.common.block.little.tile.group;

import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleVolumes;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleGroupHolder extends LittleGroup {
    
    public final LittleStructure structure;
    
    public LittleGroupHolder(LittleStructure structure) {
        super(null, LittleGrid.min(), null);
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
    public LittleGroupHolder copy() {
        return new LittleGroupHolder(structure);
    }
    
    @Override
    public boolean hasChildren() {
        return false;
    }
    
    @Override
    public boolean transformable() {
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
    @Deprecated
    public void add(LittleGrid grid, LittleElement element, Iterable<LittleBox> boxes) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @Deprecated
    public void add(LittleGrid grid, LittleElement element, LittleBox box) {
        throw new UnsupportedOperationException();
    }
    
}
