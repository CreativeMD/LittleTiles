package team.creative.littletiles.common.gui.controls;

import java.util.function.Consumer;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonHoldSlim;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.common.grid.LittleGrid;

public class GuiGridConfig extends GuiParent {
    
    public final GuiComboBoxMapped<LittleGrid> comboBox;
    public Consumer<LittleGrid> consumer;
    
    public GuiGridConfig(String name, LittleGrid grid, Consumer<LittleGrid> consumer) {
        super(name);
        flow = GuiFlow.STACK_X;
        valign = VAlign.CENTER;
        comboBox = new GuiComboBoxMapped<LittleGrid>("grid", LittleGrid.mapBuilder());
        comboBox.select(grid);
        add(new GuiButtonHoldSlim("left", x -> comboBox.previous()).setTranslate("gui.previous"));
        add(comboBox);
        add(new GuiButtonHoldSlim("right", x -> comboBox.next()).setTranslate("gui.next"));
        this.consumer = consumer;
        registerEventChanged(x -> {
            if (x.control.is("grid"))
                consumer.accept(get());
        });
    }
    
    public LittleGrid get() {
        return comboBox.getSelected();
    }
    
    public void select(LittleGrid grid) {
        comboBox.select(grid);
    }
    
}
