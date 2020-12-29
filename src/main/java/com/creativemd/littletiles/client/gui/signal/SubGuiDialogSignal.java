package com.creativemd.littletiles.client.gui.signal;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTabStateButtonTranslated;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.client.gui.signal.GuiSignalController.GeneratePatternException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponent;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.event.SignalEvent;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

public class SubGuiDialogSignal extends SubGui {
	
	protected final LittlePreviews previews;
	protected final LittleStructureType type;
	protected final List<GuiSignalComponent> inputs = new ArrayList<>();
	protected final List<GuiSignalComponent> outputs = new ArrayList<>();
	
	public SubGuiDialogSignal(SignalEvent event, LittlePreviews previews, LittleStructureType type) {
		super(300, 200);
		this.previews = previews;
		this.type = type;
		gatherInputs(previews, type, "", "");
		gatherOutputs(previews, type, "", "");
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiLabel("result", translate("gui.signal.configuration.result"), 0, 0));
		controls.add(new GuiTabStateButtonTranslated("type", 0, "gui.signal.configuration.type", 215, 0, 14, "basic", "advanced"));
		
		loadBasic();
	}
	
	protected void loadBasic() {
		if (outputs.isEmpty())
			return;
		GuiSignalController controller = new GuiSignalController("controller", 0, 22, 294, 150, outputs.get(0));
		controls.add(controller);
		List<String> inputLines = new ArrayList<>();
		for (GuiSignalComponent entry : inputs)
			inputLines.add(entry.info());
		controls.add(new GuiComboBox("inputs", 0, 180, 80, inputLines));
		controls.add(new GuiButton("add", translate("gui.signal.configuration.add"), 88, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiComboBox inputsBox = (GuiComboBox) SubGuiDialogSignal.this.get("inputs");
				controller.addInput(inputs.get(inputsBox.index));
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
		
		List<String> outputLines = new ArrayList<>();
		for (GuiSignalComponent entry : outputs)
			outputLines.add(entry.info());
		controls.add(new GuiComboBox("outputs", 164, 180, 60, outputLines));
		
		controls.add(new GuiButton("save", translate("gui.signal.configuration.save"), 270, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	protected void loadAdvanced() {
		
	}
	
	@CustomEventSubscribe
	public void changed(GuiControlChangedEvent event) {
		if (event.source.is("outputs")) {
			GuiSignalController controller = (GuiSignalController) get("controller");
			controller.setOutput(outputs.get(((GuiComboBox) event.source).index));
		} else if (event.source.is("controller")) {
			GuiLabel label = (GuiLabel) get("result");
			try {
				label.caption = translate("gui.signal.configuration.result") + " " + ((GuiSignalController) event.source).generatePattern().toString();
			} catch (GeneratePatternException e) {
				label.caption = translate("gui.signal.configuration.result") + " " + translate(e.getMessage());
			}
			label.width = font.getStringWidth(label.caption) + label.getContentOffset() * 2;
		}
		
	}
	
	protected void gatherInputs(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix) {
		if (type != null && type.inputs != null)
			for (int i = 0; i < type.inputs.size(); i++)
				inputs.add(new GuiSignalComponent(prefix + "a" + i, totalNamePrefix, type.inputs.get(i), true, false, i));
			
		for (int i = 0; i < previews.getChildren().size(); i++) {
			LittlePreviews child = previews.getChildren().get(i);
			LittleStructureType structure = child.getStructureType();
			String name = child.getStructureName();
			if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == SignalComponentType.INPUT)
				inputs.add(new GuiSignalComponent(prefix + "i" + i, totalNamePrefix + (name != null ? name : "i" + i), (ISignalComponent) structure, true, i));
			
			gatherInputs(child, child.getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."));
		}
	}
	
	protected void gatherOutputs(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix) {
		if (type != null && type.outputs != null)
			for (int i = 0; i < type.outputs.size(); i++)
				outputs.add(new GuiSignalComponent(prefix + "b" + i, totalNamePrefix, type.outputs.get(i), false, false, i));
			
		for (int i = 0; i < previews.getChildren().size(); i++) {
			LittlePreviews child = previews.getChildren().get(i);
			LittleStructureType structure = child.getStructureType();
			String name = child.getStructureName();
			if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == SignalComponentType.OUTPUT)
				outputs.add(new GuiSignalComponent(prefix + "o" + i, totalNamePrefix + (name != null ? name : "o" + i), (ISignalComponent) structure, true, i));
			
			gatherOutputs(child, child.getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."));
		}
	}
	
	public static class GuiSignalComponent {
		
		public final String name;
		public final boolean input;
		public final boolean external;
		public final int index;
		public final int bandwidth;
		public final String totalName;
		
		public GuiSignalComponent(String name, String prefix, InternalComponent component, boolean input, boolean external, int index) {
			this.name = name;
			this.bandwidth = component.bandwidth;
			this.totalName = prefix + component.identifier;
			this.input = input;
			this.external = external;
			this.index = index;
		}
		
		public GuiSignalComponent(String name, String totalName, ISignalComponent component, boolean external, int index) {
			this.name = name;
			this.bandwidth = component.getBandwidth();
			this.totalName = totalName;
			this.input = component.getType() == SignalComponentType.INPUT;
			this.external = external;
			this.index = index;
		}
		
		public GuiSignalComponent(String name, int bandwidth, String totalName, boolean input, boolean external, int index) {
			this.name = name;
			this.bandwidth = bandwidth;
			this.totalName = totalName;
			this.input = input;
			this.external = external;
			this.index = index;
		}
		
		public String info() {
			return name + " " + totalName;
		}
	}
	
}
