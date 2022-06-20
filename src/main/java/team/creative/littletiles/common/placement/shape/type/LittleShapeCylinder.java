package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeCylinder extends LittleShape {
    
    public LittleShapeCylinder() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleBox box = selection.getOverallBox();
        
        boolean hollow = selection.getNBT().getBoolean("hollow");
        
        int direction = selection.getNBT().getInt("direction");
        
        LittleVec size = box.getSize();
        
        int sizeA = size.x;
        int sizeB = size.z;
        
        if (direction == 1) {
            sizeA = size.y;
            sizeB = size.z;
        } else if (direction == 2) {
            sizeA = size.x;
            sizeB = size.y;
        }
        
        //outer circle
        //Added D to the twos in order to get a decimal value
        double a = Math.pow(Math.max(1, sizeA / 2D), 2);
        double b = Math.pow(Math.max(1, sizeB / 2D), 2);
        
        double a2 = 1;
        double b2 = 1;
        
        int thickness = selection.getNBT().getInt("thickness");
        
        if (hollow && sizeA > thickness * 2 && sizeB > thickness * 2) {
            //Gets size for a circle that is 1 smaller than the thickness of the outer circle
            int sizeAValue = sizeA - thickness - 1;
            int sizeBValue = sizeB - thickness - 1;
            
            //inner circle
            a2 = Math.pow(Math.max(1, (sizeAValue) / 2D), 2);
            b2 = Math.pow(Math.max(1, (sizeBValue) / 2D), 2);
        } else
            hollow = false;
        
        boolean stretchedA = sizeA % 2 == 0;
        boolean stretchedB = sizeB % 2 == 0;
        
        double centerA = sizeA / 2;
        double centerB = sizeB / 2;
        
        LittleVec min = box.getMinVec();
        LittleVec max = box.getMaxVec();
        for (int incA = 0; incA < sizeA; incA++) {
            for (int incB = 0; incB < sizeB; incB++) {
                double posA = incA - centerA + (stretchedA ? 0.5 : 0);
                double posB = incB - centerB + (stretchedB ? 0.5 : 0);
                
                double valueA = Math.pow(posA, 2) / a;
                double valueB = Math.pow(posB, 2) / b;
                
                if (valueA + valueB <= 1) {
                    LittleBox toAdd = null;
                    switch (direction) {
                        case 0:
                            toAdd = new LittleBox(min.x + incA, min.y, min.z + incB, min.x + incA + 1, max.y, min.z + incB + 1);
                            break;
                        case 1:
                            toAdd = new LittleBox(min.x, min.y + incA, min.z + incB, max.x, min.y + incA + 1, min.z + incB + 1);
                            break;
                        case 2:
                            toAdd = new LittleBox(min.x + incA, min.y + incB, min.z, min.x + incA + 1, min.y + incB + 1, max.z);
                            break;
                    }
                    
                    if (hollow) {
                        double valueA2 = Math.pow(posA, 2) / a2;
                        double valueB2 = Math.pow(posB, 2) / b2;
                        //if the box is found in the inner circle, Do not add it.
                        if (!(valueA2 + valueB2 <= 1))
                            boxes.add(toAdd);
                    } else
                        boxes.add(toAdd);
                }
                
            }
            
        }
        
        boxes.combineBoxesBlocks();
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {
        if (nbt.getBoolean("hollow")) {
            list.add(Component.translatable("gui.type").append(": ").append(Component.translatable("gui.hollow")));
            list.add(Component.translatable("gui.thickness").append(": " + nbt.getInt("thickness")).append(Component.translatable("gui.pixel.length")));
        } else
            list.add(Component.translatable("gui.type").append(": ").append(Component.translatable("gui.solid")));
        
        int facing = nbt.getInt("direction");
        String text;
        switch (facing) {
            case 1:
                text = "x";
                break;
            case 2:
                text = "z";
                break;
            default:
                text = "y";
                break;
        }
        list.add(Component.translatable("gui.facing").append(": ").append(Component.translatable("gui.axis." + text)));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        
        controls.add(new GuiCheckBox("hollow", nbt.getBoolean("hollow")));
        controls.add(new GuiSteppedSlider("thickness", nbt.getInt("thickness"), 1, grid.count));
        controls.add(new GuiStateButton("direction", nbt.getInt("direction"), "facing: y", "facing: x", "facing: z"));
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        
        GuiCheckBox box = (GuiCheckBox) gui.get("hollow");
        nbt.putBoolean("hollow", box.value);
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.putInt("thickness", (int) slider.value);
        
        GuiStateButton state = (GuiStateButton) gui.get("direction");
        nbt.putInt("direction", state.getState());
        
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {
        int direction = nbt.getInt("direction");
        if (rotation.axis != Axis.Y)
            direction = 0;
        else {
            if (direction == 1)
                direction = 2;
            else
                direction = 1;
        }
        
        nbt.putInt("direction", direction);
    }
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
    
}
