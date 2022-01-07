package team.creative.littletiles.common.gui.controls;

import net.minecraft.util.Mth;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;

public class GuiLTDistance extends GuiParent {
    
    public GuiLTDistance(String name, LittleGrid context, int distance) {
        super(name);
        add(new GuiTextfield("blocks", "").setNumbersIncludingNegativeOnly().setTooltip("blocks"));
        GuiStateButton contextBox = new GuiStateButton("grid", LittleGrid.getNames().indexOf(context + ""), 30, 0, 15, 12, LittleGrid.getNames().toArray(new String[0]));
        add(contextBox);
        add(new GuiTextfield("ltdistance", "").setNumbersIncludingNegativeOnly().setTooltip("grid distance"));
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
        int max = LittleTiles.CONFIG.general.maxDoorDistance * context.size;
        distance = Mth.clamp(distance, -max, max);
        
        GuiStateButton contextBox = (GuiStateButton) get("grid");
        contextBox.setState(LittleGrid.getNames().indexOf(context + ""));
        
        int blocks = distance / context.size;
        GuiTextfield blocksTF = (GuiTextfield) get("blocks");
        blocksTF.text = "" + blocks;
        blocksTF.setCursorPositionZero();
        
        GuiTextfield ltdistanceTF = (GuiTextfield) get("ltdistance");
        ltdistanceTF.text = "" + (distance - blocks * context.size);
        ltdistanceTF.setCursorPositionZero();
    }
    
    public int getDistance() {
        GuiTextfield blocksTF = (GuiTextfield) get("blocks");
        GuiTextfield ltdistanceTF = (GuiTextfield) get("ltdistance");
        LittleGrid context = getDistanceContext();
        
        try {
            int distance = Integer.parseInt(blocksTF.text) * context.size + Integer.parseInt(ltdistanceTF.text);
            int max = LittleTiles.CONFIG.general.maxDoorDistance * context.size;
            distance = Mth.clamp(distance, -max, max);
            return distance;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public LittleGrid getDistanceContext() {
        GuiStateButton contextBox = (GuiStateButton) get("grid");
        try {
            return LittleGrid.get(Integer.parseInt(contextBox.getCaption()));
        } catch (NumberFormatException e) {
            return LittleGrid.defaultGrid();
        }
    }
    
}
