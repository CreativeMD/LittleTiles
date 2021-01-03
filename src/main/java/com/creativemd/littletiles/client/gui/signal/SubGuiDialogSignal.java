package com.creativemd.littletiles.client.gui.signal;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTabStateButtonTranslated;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiSignalEvents;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiSignalEvents.GuiSignalEvent;
import com.creativemd.littletiles.client.gui.signal.GuiSignalController.GeneratePatternException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponent;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponentOutput;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

public class SubGuiDialogSignal extends SubGui {
	
	public SubGuiSignalEvents dialog;
	protected final GuiSignalEvent event;
	
	public SubGuiDialogSignal(SubGuiSignalEvents dialog, GuiSignalEvent event) {
		super(300, 200);
		this.dialog = dialog;
		this.event = event;
	}
	
	public GuiSignalComponent getInput(SignalTarget target) throws ParseException {
		for (GuiSignalComponent component : dialog.button.inputs)
			if (component.name.equals(target.writeBase()))
				return component;
		throw new ParseException("input not found", 0);
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiLabel("result", translate("gui.signal.configuration.result"), 0, 0));
		controls.add(new GuiTabStateButtonTranslated("type", 0, "gui.signal.configuration.type", 215, 0, 14, "basic", "advanced"));
		
		loadBasic();
	}
	
	protected void loadBasic() {
		GuiSignalController controller = new GuiSignalController("controller", 0, 22, 294, 150, event.component);
		controls.add(controller);
		List<String> inputLines = new ArrayList<>();
		for (GuiSignalComponent entry : dialog.button.inputs)
			inputLines.add(entry.info());
		controls.add(new GuiComboBox("inputs", 0, 180, 80, inputLines));
		controls.add(new GuiButton("add", translate("gui.signal.configuration.add"), 88, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiComboBox inputsBox = (GuiComboBox) SubGuiDialogSignal.this.get("inputs");
				controller.addInput(dialog.button.inputs.get(inputsBox.index));
			}
		});
		
		List<String> opteratorLines = new ArrayList<>();
		opteratorLines.add(SignalLogicOperator.AND.display);
		opteratorLines.add(SignalLogicOperator.OR.display);
		opteratorLines.add(SignalLogicOperator.XOR.display);
		opteratorLines.add("not");
		opteratorLines.add(SignalLogicOperator.BITWISE_AND.display);
		opteratorLines.add(SignalLogicOperator.BITWISE_OR.display);
		opteratorLines.add(SignalLogicOperator.BITWISE_XOR.display);
		opteratorLines.add("b-not");
		controls.add(new GuiComboBox("operators", 103, 180, 40, opteratorLines));
		controls.add(new GuiButton("add", translate("gui.signal.configuration.addop"), 150, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiComboBox operatorsBox = (GuiComboBox) SubGuiDialogSignal.this.get("operators");
				int index = operatorsBox.index;
				if (index < 3)
					controller.addOperator(SignalLogicOperator.values()[index]);
				else if (index == 3)
					controller.addNotOperator(false);
				else if (index == 7)
					controller.addNotOperator(true);
				else
					controller.addOperator(SignalLogicOperator.values()[index - 1]);
			}
		});
		
		if (event.condition != null)
			controller.setCondition(event.condition, this);
		
		changed(new GuiControlChangedEvent(controller));
		
		controls.add(new GuiButton("save", translate("gui.signal.configuration.save"), 270, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				try {
					event.condition = controller.generatePattern();
					event.updatePanel();
					closeGui();
				} catch (GeneratePatternException e) {}
			}
		});
	}
	
	protected void loadAdvanced() {
		
	}
	
	@CustomEventSubscribe
	public void changed(GuiControlChangedEvent event) {
		if (event.source.is("controller")) {
			GuiLabel label = (GuiLabel) get("result");
			try {
				label.caption = translate("gui.signal.configuration.result") + " " + ((GuiSignalController) event.source).generatePattern().toString();
			} catch (GeneratePatternException e) {
				label.caption = translate("gui.signal.configuration.result") + " " + translate(e.getMessage());
			}
			label.width = font.getStringWidth(label.caption) + label.getContentOffset() * 2;
		}
		
	}
	
	public static class GuiSignalComponent {
		
		public final String name;
		public final boolean input;
		public final boolean external;
		public final int index;
		public final int bandwidth;
		public final String totalName;
		public final SignalMode defaultMode;
		
		public GuiSignalComponent(String name, String prefix, InternalComponent component, boolean input, boolean external, int index) {
			this.name = name;
			this.bandwidth = component.bandwidth;
			this.totalName = prefix + component.identifier;
			this.input = input;
			this.external = external;
			this.index = index;
			if (component instanceof InternalComponentOutput)
				this.defaultMode = ((InternalComponentOutput) component).defaultMode;
			else
				this.defaultMode = SignalMode.EQUAL;
		}
		
		public GuiSignalComponent(String name, String totalName, ISignalComponent component, boolean external, int index) {
			this.name = name;
			this.bandwidth = component.getBandwidth();
			this.totalName = totalName;
			this.input = component.getType() == SignalComponentType.INPUT;
			this.external = external;
			this.index = index;
			this.defaultMode = SignalMode.EQUAL;
		}
		
		public String display() {
			if (name.equals(totalName))
				return ChatFormatting.BOLD + name + " " + ChatFormatting.RESET + bandwidth + "-bit";
			return ChatFormatting.BOLD + name + " " + totalName + " " + ChatFormatting.RESET + bandwidth + "-bit";
		}
		
		public String info() {
			return name + " " + totalName;
		}
	}
	
}
