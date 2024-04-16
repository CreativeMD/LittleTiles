package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.controls.GuiDirectionIndicator;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationPanel;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer.GuiAnimationViewChangedEvent;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.bed.LittleBed;

public class LittleBedGui extends LittleStructureGuiControl {
    
    public LittleBedGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
        registerEventChanged(x -> {
            if (x instanceof GuiAnimationViewChangedEvent || x.control.is("direction")) {
                GuiIsoAnimationViewer viewer = LittleBedGui.this.get("viewer");
                GuiStateButtonMapped<Facing> direction = LittleBedGui.this.get("direction");
                Facing facing = direction.getSelected();
                if (viewer.getXFacing().axis == facing.axis)
                    facing = viewer.getXFacing().positive == facing.positive ? Facing.EAST : Facing.WEST;
                else if (viewer.getYFacing().axis == facing.axis)
                    facing = viewer.getYFacing().positive == facing.positive ? Facing.UP : Facing.DOWN;
                else if (viewer.getZFacing().axis == facing.axis)
                    facing = viewer.getZFacing().positive == facing.positive ? Facing.SOUTH : Facing.NORTH;
                get("relativeDirection", GuiDirectionIndicator.class).setFacing(facing);
            }
        });
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        GuiParent right = new GuiParent();
        add(right);
        
        right.add(new GuiIsoAnimationPanel(item, new LittleBox(item.group.getMinVec()), item.group.getGrid(), false).setVisibleAxis(false).setViewerDim(200, 200));
        
        GuiParent left = new GuiParent();
        add(left);
        
        LittleVec size = item.group.getSize();
        Facing facing = Facing.EAST;
        if (size.x < size.z)
            facing = Facing.SOUTH;
        if (structure instanceof LittleBed)
            facing = ((LittleBed) structure).direction;
        GuiStateButtonMapped<Facing> button = new GuiStateButtonMapped<Facing>("direction", new TextMapBuilder<Facing>().addComponent(Facing.HORIZONTA_VALUES, x -> Component
                .literal(x.name)));
        button.select(facing);
        left.add(button);
        
        GuiDirectionIndicator indicator = new GuiDirectionIndicator("relativeDirection", Facing.UP);
        left.add(indicator);
        
        raiseEvent(new GuiAnimationViewChangedEvent(get("viewer")));
    }
    
    @Override
    public LittleStructure save(LittleStructure bed) {
        ((LittleBed) bed).direction = ((GuiStateButtonMapped<Facing>) get("direction")).getSelected();
        return bed;
    }
    
}