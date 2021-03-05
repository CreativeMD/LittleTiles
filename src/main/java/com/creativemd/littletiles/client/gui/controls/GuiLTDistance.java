package com.creativemd.littletiles.client.gui.controls;

import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.ControlEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.math.MathHelper;

public class GuiLTDistance extends GuiParent {
    
    public GuiLTDistance(String name, int x, int y, LittleGridContext context, int distance) {
        super(name, x, y, 72, 12);
        marginWidth = 0;
        borderWidth = 0;
        addControl(new GuiTextfield("blocks", "", 0, 0, 20, 12).setNumbersIncludingNegativeOnly().setCustomTooltip("blocks"));
        GuiStateButton contextBox = new GuiStateButton("grid", LittleGridContext.getNames().indexOf(context + ""), 30, 0, 15, 12, LittleGridContext.getNames()
            .toArray(new String[0]));
        addControl(contextBox);
        addControl(new GuiTextfield("ltdistance", "", 52, 0, 20, 12).setNumbersIncludingNegativeOnly().setCustomTooltip("grid distance"));
        
        setStyle(Style.emptyStyleDisabled);
        setDistance(context, distance);
    }
    
    @Override
    public boolean hasStyle() {
        return false;
    }
    
    @Override
    public boolean raiseEvent(ControlEvent event) {
        if (event instanceof GuiControlChangedEvent)
            super.raiseEvent(new GuiControlChangedEvent(this));
        return super.raiseEvent(event);
    }
    
    public void resetTextfield() {
        ((GuiTextfield) get("blocks")).setCursorPositionZero();
        ((GuiTextfield) get("ltdistance")).setCursorPositionZero();
    }
    
    public void setDistance(LittleGridContext context, int distance) {
        int max = LittleTiles.CONFIG.general.maxDoorDistance * context.size;
        distance = MathHelper.clamp(distance, -max, max);
        
        GuiStateButton contextBox = (GuiStateButton) get("grid");
        contextBox.setState(LittleGridContext.getNames().indexOf(context + ""));
        
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
        LittleGridContext context = getDistanceContext();
        
        try {
            int distance = Integer.parseInt(blocksTF.text) * context.size + Integer.parseInt(ltdistanceTF.text);
            int max = LittleTiles.CONFIG.general.maxDoorDistance * context.size;
            distance = MathHelper.clamp(distance, -max, max);
            return distance;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public LittleGridContext getDistanceContext() {
        GuiStateButton contextBox = (GuiStateButton) get("grid");
        try {
            return LittleGridContext.get(Integer.parseInt(contextBox.getCaption()));
        } catch (NumberFormatException e) {
            return LittleGridContext.get();
        }
    }
    
}
