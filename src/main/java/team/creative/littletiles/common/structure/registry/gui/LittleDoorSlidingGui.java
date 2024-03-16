package team.creative.littletiles.common.structure.registry.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiDirectionIndicator;
import team.creative.littletiles.common.gui.controls.GuiDistanceControl;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer;
import team.creative.littletiles.common.gui.controls.animation.GuiIsoAnimationViewer.GuiAnimationViewChangedEvent;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;
import team.creative.littletiles.common.structure.type.animation.LittleSlidingDoor;

@OnlyIn(Dist.CLIENT)
public class LittleDoorSlidingGui extends LittleDoorBaseGui {
    
    public LittleDoorSlidingGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
        
        registerEventChanged(x -> {
            if (x instanceof GuiAnimationViewChangedEvent || x.control.is("facing")) {
                GuiIsoAnimationViewer viewer = LittleDoorSlidingGui.this.get("viewer");
                GuiStateButtonMapped<Facing> direction = LittleDoorSlidingGui.this.get("facing");
                Facing facing = direction.getSelected();
                if (viewer.getXFacing().axis == facing.axis)
                    facing = viewer.getXFacing().positive == facing.positive ? Facing.EAST : Facing.WEST;
                else if (viewer.getYFacing().axis == facing.axis)
                    facing = viewer.getYFacing().positive == facing.positive ? Facing.UP : Facing.DOWN;
                else if (viewer.getZFacing().axis == facing.axis)
                    facing = viewer.getZFacing().positive == facing.positive ? Facing.SOUTH : Facing.NORTH;
                get("relative", GuiDirectionIndicator.class).setFacing(facing);
            }
            if (x.control.is("distance", "facing"))
                updateTimeline();
        });
    }
    
    @Override
    protected boolean hasAxis() {
        return false;
    }
    
    @Override
    protected void createSpecific(LittleDoor door) {
        LittleGrid grid;
        int distance;
        Facing facing;
        if (door instanceof LittleSlidingDoor sliding) {
            grid = sliding.grid;
            distance = sliding.distance;
            facing = sliding.direction;
        } else {
            grid = item.group.getGrid();
            distance = Math.max(item.group.getSize().get(Axis.Y) - 1, 1);
            facing = Facing.UP;
        }
        
        GuiParent settings = new GuiParent(GuiFlow.FIT_X);
        settings.spacing = 12;
        add(settings.setVAlign(VAlign.CENTER));
        
        settings.add(new GuiLabeledControl(Component.translatable("gui.door.distance").append(":"), new GuiDistanceControl("distance", grid, distance)));
        GuiParent direction = new GuiParent();
        settings.add(new GuiLabeledControl(Component.translatable("gui.door.direction").append(":"), direction.setVAlign(VAlign.STRETCH)));
        direction.add(new GuiStateButtonMapped<Facing>("facing", facing, new TextMapBuilder<Facing>().addComponent(Facing.VALUES, x -> x.translate())));
        direction.add(new GuiDirectionIndicator("relative", Facing.UP));
        
    }
    
    @Override
    public void create(LittleStructure structure) {
        super.create(structure);
        
        GuiStateButtonMapped<Facing> facingControl = get("facing");
        GuiIsoAnimationViewer viewer = get("viewer");
        
        if (facingControl.getSelected().axis == Axis.Y)
            viewer.setView(Facing.EAST);
        
        raiseEvent(new GuiControlChangedEvent(facingControl));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleSlidingDoor door = (LittleSlidingDoor) super.save(structure);
        GuiStateButtonMapped<Facing> facingControl = get("facing");
        GuiDistanceControl distance = get("distance");
        door.distance = distance.getDistance();
        door.grid = distance.getDistanceGrid();
        door.direction = facingControl.getSelected();
        return door;
    }
    
    @Override
    protected void save(PhysicalState state) {
        GuiStateButtonMapped<Facing> direction = get("facing");
        GuiDistanceControl distance = get("distance");
        
        state.off(0, 0, 0);
        state.rot(0, 0, 0);
        state.off(direction.getSelected(), distance.getVanillaDistance());
    }
    
}