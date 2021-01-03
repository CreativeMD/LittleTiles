package com.creativemd.littletiles.client.gui.signal;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTabStateButtonTranslated;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.client.gui.signal.GuiSignalController.GuiSignalNodeInput;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator;
import com.creativemd.littletiles.common.structure.signal.logic.SignalPatternParser;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndex;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

public class SuiGuiDialogSignalInput extends SubGui {
	
	public final GuiSignalNodeInput input;
	
	public int bandwidth;
	
	public SuiGuiDialogSignalInput(GuiSignalNodeInput input) {
		this.input = input;
	}
	
	@Override
	public void createControls() {
		
		controls.add(new GuiLabel(input.component.name + "[", 0, 0));
		controls.add(new GuiTextfield("range", input.getRange(), 20, 0, 100, 10));
		controls.add(new GuiLabel("result", "] " + input.component.bandwidth + " ->", 124, 0));
		controls.add(new GuiTabStateButtonTranslated("type", input.operator, "gui.signal.configuration.input.operation.type.", 0, 20, 10, "none", "operation", "pattern", "equation"));
		controls.add(new GuiScrollBox("config", 0, 40, 170, 96));
		controls.add(new GuiButton("cancel", 0, 146) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				closeGui();
			}
		});
		controls.add(new GuiButton("save", 146, 146) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiTextfield range = (GuiTextfield) get("range");
				try {
					input.indexes = parseRange(range.text);
				} catch (ParseException e) {}
				
				GuiTabStateButtonTranslated tab = (GuiTabStateButtonTranslated) get("type");
				GuiScrollBox panel = (GuiScrollBox) get("config");
				input.operator = tab.getState();
				switch (tab.getState()) {
				case 0:
					
					break;
				case 1:
					GuiStateButton operation = (GuiStateButton) panel.get("operation");
					input.logic = SignalLogicOperator.values()[operation.getState()];
					break;
				case 2:
					int[] indexes = new int[bandwidth];
					for (int i = 0; i < bandwidth; i++) {
						GuiStateButton stateButton = (GuiStateButton) panel.get(i + "");
						indexes[i] = stateButton.getState();
					}
					input.pattern = indexes;
					break;
				case 3:
					GuiTextfield textfield = (GuiTextfield) get("equation");
					try {
						input.equation = SignalInputCondition.parseExpression(new SignalPatternParser(textfield.text), new char[0], false, true);
					} catch (ParseException e) {
						input.equation = null;
					}
					
					break;
				}
				
				input.updateLabel();
				closeGui();
			}
		});
		updateBandwidth();
	}
	
	@CustomEventSubscribe
	public void changed(GuiControlChangedEvent event) {
		if (event.source.is("type")) {
			GuiTabStateButtonTranslated tab = (GuiTabStateButtonTranslated) get("type");
			GuiScrollBox panel = (GuiScrollBox) get("config");
			panel.controls.clear();
			switch (tab.getState()) {
			case 0:
				
				break;
			case 1:
				panel.addControl(new GuiStateButton("operation", input.logic == null ? 0 : input.logic.ordinal(), 0, 0, 40, 14, SignalLogicOperator.AND.display, SignalLogicOperator.OR.display, SignalLogicOperator.XOR.display));
				break;
			case 2:
				for (int i = 0; i < bandwidth; i++) {
					int state = 2;
					if (input.pattern != null && input.pattern.length > i)
						state = input.pattern[i];
					
					panel.addControl(new GuiLabel(i + ":", 10, i * 20 + 3));
					panel.addControl(new GuiStateButton(i + "", state, 30, i * 20, 60, "false", "true", "ignore"));
				}
				break;
			case 3:
				panel.addControl(new GuiTextfield("equation", input.equation != null ? input.equation.write() : "", 0, 0, 100, 10));
				panel.addControl(new GuiLabel("d<index>", 0, 20));
				break;
			}
			panel.refreshControls();
		} else if (event.source.is("range"))
			updateBandwidth();
	}
	
	public void updateBandwidth() {
		GuiTextfield range = (GuiTextfield) get("range");
		try {
			SignalCustomIndex[] indexes = parseRange(range.text);
			if (indexes == null)
				bandwidth = input.component.bandwidth;
			else {
				bandwidth = 0;
				for (int i = 0; i < indexes.length; i++)
					bandwidth += indexes[i].length();
			}
		} catch (ParseException e) {
			bandwidth = input.component.bandwidth;
		}
		GuiLabel result = (GuiLabel) get("result");
		result.caption = "] " + input.component.bandwidth + " -> " + bandwidth;
		result.width = font.getStringWidth(result.caption) + result.getContentOffset() * 2;
		raiseEvent(new GuiControlChangedEvent(get("type")));
	}
	
	private SignalCustomIndex[] parseRange(String range) throws ParseException {
		SignalPatternParser parser = new SignalPatternParser(range);
		
		List<SignalCustomIndex> indexes = new ArrayList<>();
		while (parser.hasNext()) {
			indexes.add(SignalTarget.parseIndex(parser));
			if (parser.hasNext() && parser.next(true) == ',')
				continue;
			else
				break;
		}
		if (indexes.isEmpty())
			return null;
		return indexes.toArray(new SignalCustomIndex[indexes.size()]);
	}
	
}
