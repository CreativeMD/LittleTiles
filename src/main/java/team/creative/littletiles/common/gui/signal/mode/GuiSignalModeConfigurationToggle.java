package team.creative.littletiles.common.gui.signal.mode;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class GuiSignalModeConfigurationToggle extends GuiSignalModeConfiguration {
    
    public GuiSignalModeConfigurationToggle(int delay) {
        super(delay);
    }
    
    public GuiSignalModeConfigurationToggle(SignalOutputHandler handler) {
        super(handler);
    }
    
    @Override
    public SignalMode getMode() {
        return SignalMode.TOGGLE;
    }
    
    @Override
    public GuiSignalModeConfiguration copy() {
        return new GuiSignalModeConfigurationToggle(delay);
    }
    
    @Override
    public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("delay", delay);
        return getMode().create(component, delay, nbt, false);
    }
    
}