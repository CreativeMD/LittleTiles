package com.creativemd.littletiles.common.gui.controls;

import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

public class GuiLTDistance extends GuiParent {
	
	public GuiLTDistance(String name, int x, int y, LittleGridContext context, int distance) {
		super(name, x, y, 100, 100);
		marginWidth = 0;
		borderWidth = 0;
		addControl(new GuiTextfield("blocks", "", 0, 0, 20, 12).setNumbersOnly().setCustomTooltip("blocks"));
		addControl(new GuiComboBox("grid", 30, 0, 15, LittleGridContext.getNames()).setDimension(15, 12).setCustomTooltip("gridsize"));
		addControl(new GuiTextfield("ltdistance", "", 52, 0, 20, 12).setNumbersOnly().setCustomTooltip("grid distance"));
		
		setStyle(Style.emptyStyle);
		setDistance(context, distance);
	}
	
	@Override
	public boolean hasStyle() {
		return false;
	}
	
	public void setDistance(LittleGridContext context, int distance) {
		GuiComboBox contextBox = (GuiComboBox) get("grid");
		contextBox.select(context.size + "");
		
		int blocks = distance / context.size;
		GuiTextfield blocksTF = (GuiTextfield) get("blocks");
		blocksTF.text = "" + blocks;
		
		GuiTextfield ltdistanceTF = (GuiTextfield) get("ltdistance");
		ltdistanceTF.text = "" + (distance - blocks * context.size);
	}
	
	public int getDistance() {
		GuiTextfield blocksTF = (GuiTextfield) get("blocks");
		GuiTextfield ltdistanceTF = (GuiTextfield) get("ltdistance");
		LittleGridContext context = getDistanceContext();
		
		try {
			return Integer.parseInt(blocksTF.text) * context.size + Integer.parseInt(ltdistanceTF.text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public LittleGridContext getDistanceContext() {
		GuiComboBox contextBox = (GuiComboBox) get("grid");
		try {
			return LittleGridContext.get(Integer.parseInt(contextBox.caption));
		} catch (NumberFormatException e) {
			return LittleGridContext.get();
		}
	}
	
}
