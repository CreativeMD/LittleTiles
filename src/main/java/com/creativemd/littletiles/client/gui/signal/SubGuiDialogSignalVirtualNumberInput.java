package com.creativemd.littletiles.client.gui.signal;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCounter;
import com.creativemd.littletiles.client.gui.signal.GuiSignalController.GuiSignalNodeVirtualNumberInput;

public class SubGuiDialogSignalVirtualNumberInput extends SubGui {
    
    public final GuiSignalNodeVirtualNumberInput input;
    public final int number;
    
    public SubGuiDialogSignalVirtualNumberInput(int number, GuiSignalNodeVirtualNumberInput input) {
        this.input = input;
        this.number = number;
    }
    
    @Override
    public void createControls() {
        GuiCounter counter = new GuiCounter("number", 0, 0, 40, number, 0, 256);
        controls.add(counter);
        controls.add(new GuiButton("cancel", 0, 146) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                closeGui();
            }
        });
        controls.add(new GuiButton("save", 146, 146) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                input.number = counter.getValue();
                input.updateLabel();
                closeGui();
            }
        });
    }
    
}
