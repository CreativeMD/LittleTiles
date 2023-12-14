package team.creative.littletiles.common.gui.controls;

import java.util.function.Consumer;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonHoldSlim;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;

public class GuiGridConfig extends GuiParent {
    
    public final GuiComboBoxMapped<LittleGrid> comboBox;
    public Consumer<LittleGrid> consumer;
    
    public GuiGridConfig(String name, Player player, LittleGrid grid, Consumer<LittleGrid> consumer) {
        super(name);
        flow = GuiFlow.STACK_X;
        valign = VAlign.CENTER;
        comboBox = new GuiComboBoxMapped<LittleGrid>("grid", LittleTiles.CONFIG.build.get(player).gridBuilder());
        comboBox.select(grid);
        add(new GuiButtonHoldSlim("left", x -> comboBox.previous()).setTranslate("gui.previous"));
        add(comboBox);
        add(new GuiButtonHoldSlim("right", x -> comboBox.next()).setTranslate("gui.next"));
        this.consumer = consumer;
        registerEventChanged(x -> {
            if (x.control.is("grid") && this.consumer != null)
                this.consumer.accept(get());
        });
    }
    
    public LittleGrid get() {
        return comboBox.getSelected();
    }
    
    public void select(LittleGrid grid) {
        comboBox.select(grid);
    }
    
}
