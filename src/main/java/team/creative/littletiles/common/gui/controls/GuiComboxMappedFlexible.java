package team.creative.littletiles.common.gui.controls;

import java.util.function.Function;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.text.TextMapBuilder;

public class GuiComboxMappedFlexible<K> extends GuiComboBoxMapped<K> {
    
    protected K forced;
    protected Function<K, Component> function;
    
    public GuiComboxMappedFlexible(String name, TextMapBuilder<K> lines, Function<K, Component> function) {
        super(name, lines);
        this.function = function;
    }
    
    @Override
    protected void updateDisplay() {
        if (forced != null) {
            text = CompiledText.createAnySize();
            text.setText(function.apply(forced));
        } else
            super.updateDisplay();
    }
    
    public void forceSelect(K key) {
        int index = indexOf(key);
        if (index == -1 || !select(index)) {
            forced = key;
            updateDisplay();
            raiseEvent(new GuiControlChangedEvent(this));
        }
    }
    
    @Override
    public boolean select(int index) {
        if (index >= 0 && index < lines.length) {
            forced = null;
            return super.select(index);
        }
        return false;
    }
    
    @Override
    public K getSelected() {
        if (forced != null)
            return forced;
        return super.getSelected();
    }
}
