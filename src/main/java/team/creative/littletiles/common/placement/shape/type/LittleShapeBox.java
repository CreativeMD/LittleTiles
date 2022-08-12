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
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeBox extends LittleShape {
    
    public LittleShapeBox() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleBox box = selection.getOverallBox();
        if (selection.getNBT().getBoolean("hollow")) {
            int thickness = selection.getNBT().getInt("thickness");
            LittleVec size = box.getSize();
            if (thickness * 2 >= size.x || thickness * 2 >= size.y || thickness * 2 >= size.z)
                boxes.add(box);
            else {
                boxes.add(new LittleBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ + thickness));
                boxes.add(new LittleBox(box.minX, box.minY + thickness, box.minZ + thickness, box.minX + thickness, box.maxY - thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.maxX - thickness, box.minY + thickness, box.minZ + thickness, box.maxX, box.maxY - thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.minY, box.minZ + thickness, box.maxX, box.minY + thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.maxY - thickness, box.minZ + thickness, box.maxX, box.maxY, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.minY, box.maxZ - thickness, box.maxX, box.maxY, box.maxZ));
            }
        } else
            boxes.add(box);
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {
        if (nbt.getBoolean("hollow")) {
            list.add(Component.translatable("gui.type").append(": ").append(Component.translatable("gui.hollow")));
            list.add(Component.translatable("gui.thickness").append(": " + nbt.getInt("thickness")).append(Component.translatable("gui.pixel.length")));
        } else
            list.add(Component.translatable("gui.type").append(": ").append(Component.translatable("gui.solid")));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        controls.add(new GuiCheckBox("hollow", nbt.getBoolean("hollow")).setTranslate("gui.hollow"));
        controls.add(new GuiLabel("label").setTranslate("gui.thickness"));
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
