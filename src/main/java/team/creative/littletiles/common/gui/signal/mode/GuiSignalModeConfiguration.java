package team.creative.littletiles.common.gui.signal.mode;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public abstract class GuiSignalModeConfiguration {
    
    public int delay;
    
    public GuiSignalModeConfiguration(SignalOutputHandler handler) {
        this(handler != null ? handler.delay : 1);
    }
    
    public GuiSignalModeConfiguration(int delay) {
        this.delay = delay;
    }
    
    public abstract SignalMode getMode();
    
    public abstract GuiSignalModeConfiguration copy();
    
    public abstract SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure);
    
    public MutableComponent description(int configuredDelay) {
        return Component.translatable(getMode().translateKey).append(" ").append(Component.translatable("gui.delay")).append(": " + configuredDelay);
    }
    
}