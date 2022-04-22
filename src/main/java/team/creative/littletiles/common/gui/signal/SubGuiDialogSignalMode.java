package team.creative.littletiles.common.gui.signal;

import java.util.ArrayList;
import java.util.List;

import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.gui.signal.SubGuiDialogSignal.IConditionConfiguration;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;

public class SubGuiDialogSignalMode extends GuiLayer {
    
    public final IConditionConfiguration event;
    public GuiSignalModeConfiguration config;
    public SubGuiDialogSignal dialog;
    
    public SubGuiDialogSignalMode(SubGuiDialogSignal dialog, IConditionConfiguration event) {
        this.event = event;
        this.dialog = dialog;
        this.config = event.getModeConfiguration().copy();
    }
    
    @Override
    public void create() {
        List<String> lines = new ArrayList<>();
        for (SignalMode mode : SignalMode.values())
            lines.add(translate(mode.translateKey));
        GuiComboBox box = new GuiComboBox("mode", 0, 0, 100, lines);
        box.select(event.getModeConfiguration().getMode().ordinal());
        controls.add(box);
        controls.add(new GuiLabel("delay:", 0, 23));
        controls.add(new GuiTextfield("delay", "" + config.delay, 40, 21, 50, 12).setNumbersOnly());
        controls.add(new GuiButton("cancel", 0, 146) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                closeGui();
            }
        });
        controls.add(new GuiButton("save", 146, 146) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                SignalMode mode = SignalMode.values()[box.index];
                GuiTextfield text = (GuiTextfield) get("delay");
                int delay = text.parseInteger();
                config = mode.parseControls(SubGuiDialogSignalMode.this, delay);
                if (config != null)
                    event.setModeConfiguration(config);
                dialog.modeChanged();
                closeGui();
                
            }
        });
        changed(new GuiControlChangedEvent(box));
    }
    
    @CustomEventSubscribe
    public void changed(GuiControlChangedEvent event) {
        if (event.source.is("mode")) {
            GuiComboBox box = (GuiComboBox) event.source;
            SignalMode mode = SignalMode.values()[box.index];
            removeControls("mode", "delay:", "delay", "cancel", "save");
            mode.createControls(this, config);
        }
    }
    
}
