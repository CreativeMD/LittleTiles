package team.creative.littletiles.common.gui.controls.filter;

import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;

public class GuiElementFilterColor extends GuiElementFilter {
    
    protected GuiColorPicker color;
    
    public GuiElementFilterColor(int color) {
        add(this.color = new GuiColorPicker("name", new Color(color), true, 0));
    }
    
    @Override
    public BiFilter<IParentCollection, LittleTile> get() {
        return TileFilters.color(color.color.toInt());
    }
    
}
