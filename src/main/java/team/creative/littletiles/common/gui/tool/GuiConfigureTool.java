package team.creative.littletiles.common.gui.tool;

import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.grid.LittleGrid;

public abstract class GuiConfigureTool extends GuiConfigure {
    
    public GuiConfigureTool(String name, int width, int height, ContainerSlotView tool) {
        super(name, width, height, tool);
    }
    
    public GuiConfigureTool(String name, ContainerSlotView tool) {
        super(name, tool);
    }
    
    public LittleGrid getGrid() {
        return ((ILittleTool) tool.get().getItem()).getPositionGrid(tool.get());
    }
    
}
