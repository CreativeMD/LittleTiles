package team.creative.littletiles.common.gui.signal;

import java.util.List;

import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents.GuiSignalEvent;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public interface IGuiSignalStructure {
    
    public GuiComponentSearch getSignalSearch();
    
    public GuiSignalEvent[] internalOutputs();
    
    public Iterable<GuiSignalEvent> externalOutputs();
    
    public void setSignalOutputs(List<GuiSignalEvent> events);
    
    public LittleStructureType getStructureType();
    
    public String getTitle();
    
    public Iterable<IGuiSignalStructure> children();
    
    public IGuiSignalStructure parent();
    
    public LittleStructure getStructure();
    
}
