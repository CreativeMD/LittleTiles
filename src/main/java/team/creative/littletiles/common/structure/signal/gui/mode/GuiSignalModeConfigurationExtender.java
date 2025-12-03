package team.creative.littletiles.common.structure.signal.gui.mode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.output.mode.SignalOutputHandlerExtender;

public class GuiSignalModeConfigurationExtender extends GuiSignalModeConfiguration {
    
    public int length;
    
    public GuiSignalModeConfigurationExtender(int delay, int length) {
        super(delay);
        this.length = length;
    }
    
    public GuiSignalModeConfigurationExtender(SignalOutputHandler handler) {
        super(handler);
        this.length = handler instanceof SignalOutputHandlerExtender extender ? extender.pulseLength : 10;
    }
    
    @Override
    public SignalMode getMode() {
        return SignalMode.EXTENDER;
    }
    
    @Override
    public GuiSignalModeConfiguration copy() {
        return new GuiSignalModeConfigurationExtender(delay, length);
    }
    
    @Override
    public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("delay", delay);
        nbt.putInt("length", length);
        return getMode().create(component, delay, nbt, false);
    }
    
    @Override
    public MutableComponent description(int configuredDelay) {
        return super.description(configuredDelay).append(" ").append(Component.translatable("gui.signal.length").append(": " + length));
    }
    
}