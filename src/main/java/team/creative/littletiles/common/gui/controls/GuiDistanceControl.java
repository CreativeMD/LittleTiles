package team.creative.littletiles.common.gui.controls;

import net.minecraft.util.Mth;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCounter;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;

public class GuiDistanceControl extends GuiParent {
    
    public GuiDistanceControl(String name, LittleGrid context, int distance) {
        super(name);
        add(new GuiCounter("blocks", 0).setTooltip(new TextBuilder().translate("gui.distance.blocks").build()));
        add(new GuiStateButtonMapped<LittleGrid>("grid", LittleGrid.mapBuilder()).setDim(20, 10));
        add(new GuiCounter("ltdistance", 0).setTooltip(new TextBuilder().translate("gui.distance.pixels").build()));
        setDistance(context, distance);
    }
    
    @Override
    public void raiseEvent(GuiEvent event) {
        if (event instanceof GuiControlChangedEvent)
            super.raiseEvent(new GuiControlChangedEvent(this));
        super.raiseEvent(event);
    }
    
    public void resetTextfield() {
        get("blocks", GuiCounter.class).resetTextfield();
        get("ltdistance", GuiCounter.class).resetTextfield();
    }
    
    public void setDistance(LittleGrid context, int distance) {
        int max = LittleTiles.CONFIG.general.maxDoorDistance * context.count;
        distance = Mth.clamp(distance, -max, max);
        
        GuiStateButtonMapped<LittleGrid> contextBox = get("grid");
        contextBox.select(context);
        
        int blocks = distance / context.count;
        GuiCounter blocksTF = get("blocks");
        blocksTF.setValue(blocks);
        blocksTF.resetTextfield();
        
        GuiCounter ltdistanceTF = get("ltdistance");
        ltdistanceTF.setValue(distance - blocks * context.count);
        ltdistanceTF.resetTextfield();
    }
    
    public int getDistance() {
        GuiCounter blocksTF = get("blocks");
        GuiCounter ltdistanceTF = get("ltdistance");
        LittleGrid context = getDistanceGrid();
        
        try {
            int distance = blocksTF.getValue() * context.count + ltdistanceTF.getValue();
            int max = LittleTiles.CONFIG.general.maxDoorDistance * context.count;
            distance = Mth.clamp(distance, -max, max);
            return distance;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public LittleGrid getDistanceGrid() {
        GuiStateButtonMapped<LittleGrid> contextBox = (GuiStateButtonMapped<LittleGrid>) get("grid");
        return contextBox.getSelected(LittleGrid.defaultGrid());
    }
    
    public double getVanillaDistance() {
        return getDistanceGrid().toVanillaGrid(getDistance());
    }
    
}
