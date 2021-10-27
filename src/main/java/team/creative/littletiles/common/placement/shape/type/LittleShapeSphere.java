package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeSphere extends LittleShape {
    
    public LittleShapeSphere() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleBox box = selection.getOverallBox();
        
        boolean hollow = selection.getNBT().getBoolean("hollow");
        LittleVec size = box.getSize();
        if (lowResolution && size.getPercentVolume(boxes.grid) > 4) {
            boxes.add(box);
            return;
        }
        
        LittleVec invCenter = size.calculateInvertedCenter();
        invCenter.invert();
        
        double a = Math.pow(Math.max(1, size.x / 2), 2);
        double b = Math.pow(Math.max(1, size.y / 2), 2);
        double c = Math.pow(Math.max(1, size.z / 2), 2);
        
        double a2 = 1;
        double b2 = 1;
        double c2 = 1;
        
        int thickness = selection.getNBT().getInt("thickness");
        
        if (hollow && size.x > thickness * 2 && size.y > thickness * 2 && size.z > thickness * 2) {
            int all = size.x + size.y + size.z;
            
            double sizeXValue = (double) size.x / all;
            double sizeYValue = (double) size.y / all;
            double sizeZValue = (double) size.z / all;
            
            if (sizeXValue > 0.5)
                sizeXValue = 0.5;
            if (sizeYValue > 0.5)
                sizeYValue = 0.5;
            if (sizeZValue > 0.5)
                sizeZValue = 0.5;
            
            a2 = Math.pow(Math.max(1, (sizeXValue * all - thickness * 2) / 2), 2);
            b2 = Math.pow(Math.max(1, (sizeYValue * all - thickness * 2) / 2), 2);
            c2 = Math.pow(Math.max(1, (sizeZValue * all - thickness * 2) / 2), 2);
        } else
            hollow = false;
        
        boolean stretchedX = size.x % 2 == 0;
        boolean stretchedY = size.y % 2 == 0;
        boolean stretchedZ = size.z % 2 == 0;
        
        double centerX = size.x / 2;
        double centerY = size.y / 2;
        double centerZ = size.z / 2;
        
        LittleVec min = box.getMinVec();
        
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                for (int z = 0; z < size.z; z++) {
                    
                    double posX = x - centerX + (stretchedX ? 0.5 : 0);
                    double posY = y - centerY + (stretchedY ? 0.5 : 0);
                    double posZ = z - centerZ + (stretchedZ ? 0.5 : 0);
                    
                    double valueA = Math.pow(posX, 2) / a;
                    double valueB = Math.pow(posY, 2) / b;
                    double valueC = Math.pow(posZ, 2) / c;
                    
                    if (valueA + valueB + valueC <= 1) {
                        double valueA2 = Math.pow(posX, 2) / a2;
                        double valueB2 = Math.pow(posY, 2) / b2;
                        double valueC2 = Math.pow(posZ, 2) / c2;
                        if (!hollow || valueA2 + valueB2 + valueC2 > 1)
                            boxes.add(new LittleBox(new LittleVec(min.x + x, min.y + y, min.z + z)));
                    }
                }
            }
        }
        
        boxes.combineBoxesBlocks();
        
        if (lowResolution && boxes.size() > PlacementHelper.LOW_RESOLUTION_COUNT) {
            boxes.clear();
            boxes.add(box);
        }
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {
        if (nbt.getBoolean("hollow")) {
            list.add(new TranslatableComponent("gui.type").append(": ").append(new TranslatableComponent("gui.hollow")));
            list.add(new TranslatableComponent("gui.thickness").append(": " + nbt.getInt("thickness")).append(new TranslatableComponent("gui.pixel.length")));
        } else
            list.add(new TranslatableComponent("gui.type").append(": ").append(new TranslatableComponent("gui.solid")));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        
        controls.add(new GuiCheckBox("hollow", nbt.getBoolean("hollow")));
        controls.add(new GuiSteppedSlider("thickness", nbt.getInt("thickness"), 1, grid.count));
        
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        GuiCheckBox box = (GuiCheckBox) gui.get("hollow");
        nbt.putBoolean("hollow", box.value);
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.putInt("thickness", (int) slider.value);
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {}
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
}
