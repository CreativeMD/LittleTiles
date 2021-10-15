package team.creative.littletiles.common.placement.shape;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;

public abstract class LittleShape {
    
    public final int pointsBeforePlacing;
    
    public LittleShape(int pointsBeforePlacing) {
        this.pointsBeforePlacing = pointsBeforePlacing;
    }
    
    public String getKey() {
        return ShapeRegistry.getShapeName(this);
    }
    
    @OnlyIn(Dist.CLIENT)
    public String getLocalizedName() {
        return GuiControl.translateOrDefault("shape." + getKey(), getKey());
    }
    
    public int maxAllowed() {
        return -1;
    }
    
    protected abstract void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution);
    
    public LittleBoxes getBoxes(ShapeSelection selection, boolean lowResolution) {
        LittleBoxes boxes = requiresNoOverlap() ? new LittleBoxesNoOverlap(selection.getPos(), selection.getGrid()) : new LittleBoxesSimple(selection.getPos(), selection
                .getGrid());
        addBoxes(boxes, selection, lowResolution);
        return boxes;
    }
    
    public boolean requiresNoOverlap() {
        return false;
    }
    
    public abstract void addExtraInformation(CompoundTag nbt, List<String> list);
    
    @OnlyIn(Dist.CLIENT)
    public abstract List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid);
    
    @OnlyIn(Dist.CLIENT)
    public abstract void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid);
    
    public abstract void rotate(CompoundTag nbt, Rotation rotation);
    
    public abstract void mirror(CompoundTag nbt, Axis axis);
    
}
