package team.creative.littletiles.common.gui.signal;

import team.creative.littletiles.common.gui.signal.mode.GuiSignalModeConfiguration;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;

public interface IConditionConfiguration {
    
    public GuiSignalComponent getOutput();
    
    public SignalInputCondition getCondition();
    
    public void setCondition(SignalInputCondition condition);
    
    public boolean hasModeConfiguration();
    
    public GuiSignalModeConfiguration getModeConfiguration();
    
    public void setModeConfiguration(GuiSignalModeConfiguration config);
    
    public void update();
    
}
