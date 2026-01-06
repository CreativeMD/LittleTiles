package team.creative.littletiles.common.structure.registry.gui;

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.parent.GuiTabsMapped;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.control.GuiGridConfig;
import team.creative.littletiles.common.gui.control.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.control.animation.GuiIsoAnimationViewer.GuiAnimationAxisChangedEvent;
import team.creative.littletiles.common.gui.control.animation.GuiIsoAnimationViewer.GuiAnimationViewChangedEvent;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor.LittleAxisDoorRotation;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor.LittleAxisDoorRotationDirection;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor.LittleAxisDoorRotationFixed;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

@OnlyIn(Dist.CLIENT)
public class LittleDoorAxisGui extends LittleDoorBaseGui {
    
    public LittleDoorAxisGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
        registerEventChanged(x -> {
            if (x.control.is("even"))
                get("viewer", GuiIsoAnimationViewer.class).setEven(((GuiCheckBox) x.control).value);
            if (x.control.is("angle", "direction"))
                updateTimeline();
        });
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
        tabs.createTab(x -> new LittleAxisDoorRotationDirection(viewer.axis(), (Boolean) x.get("direction", GuiStateButton.class).selected()), Component.translatable(
            "gui.door.rotation.direction")).add(
                new GuiStateButton<Boolean>("direction", rotation instanceof LittleAxisDoorRotationDirection d && d.clockwise, new TextMapBuilder<Boolean>().addComponent(false,
                    Component.translatable("gui.clockwise")).addComponent(true, Component.translatable("gui.counterclockwise"))));
        
        GuiTextfield angle = new GuiTextfield("angle").setFloatOnly();
        angle.setText(rotation instanceof LittleAxisDoorRotationFixed d ? "" + d.degree : "90");
        tabs.createTab(x -> new LittleAxisDoorRotationFixed(viewer.axis(), x.get("angle", GuiTextfield.class).parseDouble()), Component.translatable("gui.door.rotation.angle"))
                .add(angle);
        add(tabs);
        
        if (rotation instanceof LittleAxisDoorRotationDirection)
            tabs.select(0);
        else
            tabs.select(1);
        
        add(new GuiCheckBox("even", viewer.isEven()).setTranslate("gui.door.axis.even"));
        
        add(new GuiGridConfig("grid", getPlayer(), viewer.getGrid(), x -> {
            LittleBox box = viewer.getBox();
            box.convertTo(viewer.getGrid(), x);
            
            if (viewer.isEven())
                box.maxX = box.minX + 2;
            else
                box.maxX = box.minX + 1;
            
            if (viewer.isEven())
                box.maxY = box.minY + 2;
            else
                box.maxY = box.minY + 1;
            
            if (viewer.isEven())
                box.maxZ = box.minZ + 2;
            else
                box.maxZ = box.minZ + 1;
            
            viewer.setAxis(box, x);
        }));
        
        registerEvent(GuiAnimationAxisChangedEvent.class, x -> item.setNewCenter(new StructureAbsolute(new BlockPos(0, 0, 0), viewer.getBox().copy(), viewer.getGrid())));
        registerEvent(GuiAnimationViewChangedEvent.class, x -> updateTimeline());
        raiseEvent(new GuiAnimationAxisChangedEvent(viewer));
    }
    
    @Override
    protected void save(PhysicalState state) {
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
    
}
