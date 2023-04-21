package team.creative.littletiles.common.structure.registry.gui;

import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiTabsMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor.LittleAxisDoorRotation;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor.LittleAxisDoorRotationDirection;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor.LittleAxisDoorRotationFixed;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

@OnlyIn(Dist.CLIENT)
public class LittleDoorAxisGui extends LittleDoorBaseGui {
    
    public LittleDoorAxisGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    protected boolean hasAxis() {
        return true;
    }
    
    @Override
    protected void createSpecific(LittleDoor door) {
        GuiIsoAnimationViewer viewer = get("viewer");
        LittleAxisDoorRotation rotation;
        if (door instanceof LittleAxisDoor axis)
            rotation = axis.rotation;
        else
            rotation = new LittleAxisDoorRotationDirection(viewer.axis(), true);
        Axis axis = rotation.axis;
        viewer.setView(Facing.get(axis, true));
        GuiTabsMapped<Function<GuiParent, LittleAxisDoorRotation>> tabs = new GuiTabsMapped<>("tabs");
        tabs.createTab(x -> new LittleAxisDoorRotationDirection(viewer.axis(), x.get("direction", GuiStateButton.class).getState() == 0), Component
                .translatable("gui.door.rotation.direction"))
                .add(new GuiStateButton("direction", rotation instanceof LittleAxisDoorRotationDirection d && !d.clockwise ? 1 : 0, "gui.clockwise", "gui.counterclockwise"));
        
        GuiTextfield angle = new GuiTextfield("angle").setFloatOnly();
        angle.setText(rotation instanceof LittleAxisDoorRotationFixed d ? "" + d.degree : "90");
        tabs.createTab(x -> new LittleAxisDoorRotationFixed(viewer.axis(), x.get("angle", GuiTextfield.class).parseDouble()), Component.translatable("gui.door.rotation.angle"))
                .add(angle);
        add(tabs);
        
        if (rotation instanceof LittleAxisDoorRotationDirection)
            tabs.select(0);
        else
            tabs.select(1);
        
    }
    
    @Override
    protected void save(AnimationState state) {
        GuiTabsMapped<Function<GuiParent, LittleAxisDoorRotation>> tabs = get("tabs");
        
        state.off(0, 0, 0);
        state.rot(0, 0, 0);
        
        tabs.getSelected().apply(tabs.getTab(tabs.index())).apply(state);
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleAxisDoor door = (LittleAxisDoor) super.save(structure);
        
        GuiTabsMapped<Function<GuiParent, LittleAxisDoorRotation>> tabs = get("tabs");
        
        door.rotation = tabs.getSelected().apply(tabs.getTab(tabs.index()));
        
        return door;
    }
    
    @Override
    public void populateTimeline(AnimationTimeline timeline, int interpolation) {}
    
}
