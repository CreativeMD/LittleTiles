package team.creative.littletiles.common.gui.tool;

import net.minecraft.core.component.PatchedDataComponentMap;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.gui.control.GuiGridConfig;
import team.creative.littletiles.common.gui.control.GuiShapeConfiguration;
import team.creative.littletiles.common.gui.control.filter.GuiElementFilter;
import team.creative.littletiles.common.gui.control.filter.GuiFilterConfiguration;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public class GuiPaintBrush extends GuiConfigureTool {
    
    protected GuiElementFilter filter;
    
    public GuiPaintBrush(ContainerSlotView view) {
        super("paint_brush", 140, 200, view);
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        data.set(LittleTilesRegistry.COLOR.get(), get("picker", GuiColorPicker.class).color.toInt());
        data.set(LittleTilesRegistry.SHAPE.get(), get("shape", GuiShapeConfiguration.class).save());
        data.set(LittleTilesRegistry.FILTER.get(), get("filter", GuiFilterConfiguration.class).save());
        return true;
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        Color color = new Color(tool.get().getOrDefault(LittleTilesRegistry.COLOR, ColorUtils.WHITE));
        add(new GuiColorPicker("picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        add(new GuiShapeConfiguration(getPlayer(), "shape", tool));
        add(new GuiGridConfig("grid", getPlayer(), PlacementPlayerSetting.grid(getPlayer()), LittleTilesClient::grid));
        add(new GuiFilterConfiguration(getPlayer(), "filter", tool));
    }
    
}
