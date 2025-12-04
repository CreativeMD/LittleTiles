package team.creative.littletiles.common.gui.control.filter;

import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;

public class GuiElementFilterName extends GuiElementFilter {
    
    protected GuiTextfield textfield;
    
    public GuiElementFilterName(String name) {
        add(textfield = new GuiTextfield("name").setText(name));
        textfield.setExpandableX();
    }
    
    @Override
    public BiFilter<IParentCollection, LittleTile> get() {
        var text = textfield.getText();
        if (!text.isBlank())
            return TileFilters.name(text);
        return null;
    }
    
}
