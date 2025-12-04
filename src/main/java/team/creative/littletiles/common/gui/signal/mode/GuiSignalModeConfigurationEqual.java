package team.creative.littletiles.common.gui.signal.mode;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class GuiSignalModeConfigurationEqual extends GuiSignalModeConfiguration {
    
    public GuiSignalModeConfigurationEqual(int delay) {
        super(delay);
    }
    
    public GuiSignalModeConfigurationEqual(SignalOutputHandler handler) {
        this(handler != null ? handler.delay : 1);
    }
    
    @Override
    public SignalMode getMode() {
        return SignalMode.EQUAL;
    }
    
    @Override
    public GuiSignalModeConfiguration copy() {
        return new GuiSignalModeConfigurationEqual(delay);
    }
    
    @Override
    public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("delay", delay);
        return getMode().create(component, delay, nbt, false);
    }
    
}