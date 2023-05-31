package team.creative.littletiles.common.gui.controls;

import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.structure.animation.PhysicalState;

public class GuiPhysicalStateControl extends GuiTable {
    
    public GuiPhysicalStateControl(String name, PhysicalState state) {
        super(name);
        GuiRow offset = new GuiRow();
        addRow(offset);
        GuiColumn offX = new GuiColumn();
        offset.addColumn(offX);
        offX.add(new GuiLabeledControl("gui.door.offx", new GuiTextfield("offX", "" + state.offX()).setFloatOnly()));
        GuiColumn offY = new GuiColumn();
        offset.addColumn(offY);
        offY.add(new GuiLabeledControl("gui.door.offy", new GuiTextfield("offY", "" + state.offY()).setFloatOnly()));
        GuiColumn offZ = new GuiColumn();
        offset.addColumn(offZ);
        offZ.add(new GuiLabeledControl("gui.door.offz", new GuiTextfield("offZ", "" + state.offZ()).setFloatOnly()));
        
        GuiRow rotation = new GuiRow();
        addRow(rotation);
        GuiColumn rotX = new GuiColumn();
        rotation.addColumn(rotX);
        rotX.add(new GuiLabeledControl("gui.door.rotx", new GuiTextfield("rotX", "" + state.rotX()).setFloatOnly()));
        GuiColumn rotY = new GuiColumn();
        rotation.addColumn(rotY);
        rotY.add(new GuiLabeledControl("gui.door.roty", new GuiTextfield("rotY", "" + state.rotY()).setFloatOnly()));
        GuiColumn rotZ = new GuiColumn();
        rotation.addColumn(rotZ);
        rotZ.add(new GuiLabeledControl("gui.door.rotz", new GuiTextfield("rotZ", "" + state.rotZ()).setFloatOnly()));
        
        registerEventChanged(x -> {
            if (x.control instanceof GuiTextfield)
                raiseEvent(new GuiControlChangedEvent(this));
        });
    }
    
    public PhysicalState create() {
        PhysicalState state = new PhysicalState();
        state.offX(get("offX", GuiTextfield.class).parseDouble());
        state.offY(get("offY", GuiTextfield.class).parseDouble());
        state.offZ(get("offZ", GuiTextfield.class).parseDouble());
        state.rotX(get("rotX", GuiTextfield.class).parseDouble());
        state.rotY(get("rotY", GuiTextfield.class).parseDouble());
        state.rotZ(get("rotZ", GuiTextfield.class).parseDouble());
        return state;
    }
    
}
