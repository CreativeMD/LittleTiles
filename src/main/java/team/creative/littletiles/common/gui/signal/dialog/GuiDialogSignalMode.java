package team.creative.littletiles.common.gui.signal.dialog;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignal.IConditionConfiguration;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;

public class GuiDialogSignalMode extends GuiLayer {
    
    public IConditionConfiguration event;
    public GuiSignalModeConfiguration config;
    public GuiDialogSignal dialog;
    
    public GuiDialogSignalMode() {
        super("gui.dialog.signal.mode", 100, 100);
        registerEventChanged(this::changed);
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(GuiDialogSignal dialog, IConditionConfiguration event) {
        this.event = event;
        this.dialog = dialog;
        this.config = event.getModeConfiguration().copy();
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        
        GuiComboBoxMapped<SignalMode> box = new GuiComboBoxMapped<SignalMode>("mode", new TextMapBuilder<SignalMode>()
                .addComponent(SignalMode.values(), x -> Component.translatable(x.translateKey)));
        box.select(event.getModeConfiguration().getMode());
        add(box.setExpandableX());
        
        GuiParent delayLine = new GuiParent(GuiFlow.STACK_X);
        add(delayLine.setExpandableX());
        
        delayLine.add(new GuiLabel("delay_label").setTitle(Component.translatable("gui.delay").append(":")));
        delayLine.add(new GuiTextfield("delay", "" + config.delay).setNumbersOnly().setExpandableX());
        
        GuiParent panel = new GuiParent("panel", GuiFlow.STACK_Y);
        add(panel.setExpandable());
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("save", x -> {
            SignalMode mode = box.getSelected();
            GuiTextfield text = (GuiTextfield) get("delay");
            int delay = text.parseInteger();
            config = mode.parseControls(GuiDialogSignalMode.this, delay);
            if (config != null)
                event.setModeConfiguration(config);
            dialog.modeChanged();
            closeThisLayer();
            
        }).setTranslate("gui.save"));
        
        changed(new GuiControlChangedEvent(box));
    }
    
    public void changed(GuiControlChangedEvent event) {
        if (event.control.is("mode")) {
            GuiComboBoxMapped<SignalMode> box = (GuiComboBoxMapped<SignalMode>) event.control;
            SignalMode mode = box.getSelected();
            GuiParent panel = get("panel");
            panel.clear();
            mode.createControls(panel, config);
            reflow();
        }
    }
    
}