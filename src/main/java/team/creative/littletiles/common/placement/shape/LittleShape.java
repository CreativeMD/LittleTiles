package team.creative.littletiles.common.placement.shape;

import net.minecraft.network.chat.Component;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;

public abstract class LittleShape<T> {
    
    public final int pointsBeforePlacing;
    private String path;
    
    public LittleShape(int pointsBeforePlacing) {
        this.pointsBeforePlacing = pointsBeforePlacing;
    }
    
    public String getKey() {
        return ShapeRegistry.REGISTRY.getId(this);
    }
    
    public void setTranslation(String path) {
        this.path = path;
    }
    
    public Component translatable() {
        return Component.translatable(path);
    }
    
    public int maxAllowed() {
        return -1;
    }
    
    protected abstract void build(LittleBoxes boxes, ShapeSelection selection, T config);
    
    public LittleBoxes build(ShapeSelection selection, T config) {
        LittleBoxes boxes = requiresNoOverlap(selection, config) ? new LittleBoxesNoOverlap(selection.pos, selection.grid) : new LittleBoxesSimple(selection.pos, selection.grid);
        build(boxes, selection, config);
        return boxes;
    }
    
    protected boolean requiresNoOverlap(ShapeSelection selection, T config) {
        return false;
    }
    
}
