package team.creative.littletiles.common.gui.signal.dialog;

import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCounter;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeVirtualNumberInput;

public class GuiDialogSignalVirtualNumberInput extends GuiLayer {
    
    public GuiSignalNodeVirtualNumberInput input;
    public int number;
    
    public GuiDialogSignalVirtualNumberInput() {
        super("gui.dialog.signal.virtual_number", 100, 100);
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(int number, GuiSignalNodeVirtualNumberInput input) {
        this.input = input;
        this.number = number;
    }
    
    @Override
    public void create() {
        GuiCounter counter = new GuiCounter("number", number, 0, 256);
        add(counter.setExpandableX());
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("save", x -> {
            input.number = counter.getValue();
            input.updateLabel();
            closeThisLayer();
            
        }).setTranslate("gui.save"));
    }
    
}