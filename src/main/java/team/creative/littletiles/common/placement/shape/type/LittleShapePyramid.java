package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapePyramid extends LittleShape {
    
    public LittleShapePyramid() {
        super(2);
    }
    
    public Facing getFacing(CompoundTag nbt) {
        if (nbt.contains("facing"))
            return Facing.get(nbt.getInt("facing"));
        return Facing.UP;
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleBox box = selection.getOverallBox();
        Facing facing = getFacing(selection.getNBT());
        Axis axis = facing.axis;
        int minAxis = box.getMin(axis);
        int maxAxis = box.getMax(axis);
        
        Axis one = axis.one();
        Axis two = axis.two();
        
        int minOne = box.getMin(one);
        int minTwo = box.getMin(two);
        int maxOne = box.getMax(one);
        int maxTwo = box.getMax(two);
        
        int counter = 0;
        
        if (facing.positive)
            for (int i = minAxis; i < maxAxis; i++) {
                LittleBox toAdd = new LittleBox(i, i, i, i + 1, i + 1, i + 1);
                toAdd.setMin(one, Math.min(minOne + counter, maxOne - counter));
                toAdd.setMin(two, Math.min(minTwo + counter, maxTwo - counter));
                toAdd.setMax(one, Math.max(minOne + counter, maxOne - counter));
                toAdd.setMax(two, Math.max(minTwo + counter, maxTwo - counter));
                boxes.add(toAdd);
                counter++;
            }
        else
            for (int i = maxAxis - 1; i >= minAxis; i--) {
                LittleBox toAdd = new LittleBox(i, i, i, i + 1, i + 1, i + 1);
                toAdd.setMin(one, Math.min(minOne + counter, maxOne - counter));
                toAdd.setMin(two, Math.min(minTwo + counter, maxTwo - counter));
                toAdd.setMax(one, Math.max(minOne + counter, maxOne - counter));
                toAdd.setMax(two, Math.max(minTwo + counter, maxTwo - counter));
                boxes.add(toAdd);
                counter++;
            }
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {
        list.add(Component.translatable("gui.facing").append(": ").append(Component.translatable("gui.direction." + getFacing(nbt).name().toLowerCase())));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        String[] states = new String[6];
        for (int i = 0; i < states.length; i++)
            states[i] = "facing: " + Facing.get(i).name().toLowerCase();
        controls.add(new GuiStateButton("direction", nbt.contains("facing") ? nbt.getInt("facing") : Facing.UP.ordinal(), states));
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        GuiStateButton state = (GuiStateButton) gui.get("direction");
        nbt.putInt("facing", state.getState());
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {
        Facing facing = getFacing(nbt);
        facing = rotation.rotate(facing);
        nbt.putInt("facing", facing.ordinal());
    }
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {
        Facing facing = getFacing(nbt);
        facing = axis.mirror(facing);
        nbt.putInt("facing", facing.ordinal());
    }
    
}
