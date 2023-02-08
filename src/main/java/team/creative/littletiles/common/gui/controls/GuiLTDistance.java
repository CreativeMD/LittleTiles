package team.creative.littletiles.common.gui.controls;

import net.minecraft.util.Mth;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;

public class GuiLTDistance extends GuiParent {
    
    public GuiLTDistance(String name, LittleGrid context, int distance) {
        super(name);
        add(new GuiTextfield("blocks", "").setNumbersIncludingNegativeOnly().setTooltip(new TextBuilder().translate("gui.distance.blocks").build()));
        add(new GuiStateButtonMapped<LittleGrid>("grid", LittleGrid.mapBuilder()));
        add(new GuiTextfield("ltdistance", "").setNumbersIncludingNegativeOnly().setTooltip(new TextBuilder().translate("gui.distance.pixels").build()));
        setDistance(context, distance);
    }
    
    @Override
    public void raiseEvent(GuiEvent event) {
        if (event instanceof GuiControlChangedEvent)
            super.raiseEvent(new GuiControlChangedEvent(this));
        super.raiseEvent(event);
    }
    
    public void resetTextfield() {
        ((GuiTextfield) get("blocks")).setCursorPositionZero();
        ((GuiTextfield) get("ltdistance")).setCursorPositionZero();
    }
    
    public void setDistance(LittleGrid context, int distance) {
        int max = LittleTiles.CONFIG.general.maxDoorDistance * context.count;
        distance = Mth.clamp(distance, -max, max);
        
        GuiStateButtonMapped<LittleGrid> contextBox = (GuiStateButtonMapped<LittleGrid>) get("grid");
        contextBox.select(context);
        
        int blocks = distance / context.count;
        GuiTextfield blocksTF = (GuiTextfield) get("blocks");
        blocksTF.setText("" + blocks);
        blocksTF.setCursorPositionZero();
        
        GuiTextfield ltdistanceTF = (GuiTextfield) get("ltdistance");
        ltdistanceTF.setText("" + (distance - blocks * context.count));
        ltdistanceTF.setCursorPositionZero();
    }
    
    public int getDistance() {
        GuiTextfield blocksTF = (GuiTextfield) get("blocks");
        GuiTextfield ltdistanceTF = (GuiTextfield) get("ltdistance");
        LittleGrid context = getDistanceGrid();
        
        try {
            int distance = Integer.parseInt(blocksTF.getText()) * context.count + Integer.parseInt(ltdistanceTF.getText());
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
    
}
