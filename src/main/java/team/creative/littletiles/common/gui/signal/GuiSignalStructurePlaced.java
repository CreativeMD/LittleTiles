package team.creative.littletiles.common.gui.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents.GuiSignalEvent;
import team.creative.littletiles.common.packet.structure.StructureOutputConfigurationChanged;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;

public class GuiSignalStructurePlaced implements IGuiSignalStructure {
    
    private IGuiSignalStructure parent;
    private final List<IGuiSignalStructure> children;
    public final LittleStructure structure;
    protected GuiComponentSearch signalSearch;
    protected GuiSignalEvent[] internalOutputs;
    protected HashMap<Integer, GuiSignalEvent> externalOutputs;
    
    public GuiSignalStructurePlaced(LittleStructure structure, boolean main, boolean lookForParent) {
        this.structure = structure;
        if (this.structure.hasParent() && lookForParent)
            try {
                this.parent = new GuiSignalStructurePlaced(structure.getParent().getStructure(), false, true);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        else
            this.parent = null;
        
        this.children = new ArrayList<>();
        for (StructureChildConnection s : structure.children.all())
            try {
                children.add(new GuiSignalStructurePlaced(s.getStructure(), false, false));
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        if (main) {
            signalSearch = new GuiComponentSearch(this);
            
            GuiSignalComponent[] internal = signalSearch.internalOutputs();
            if (internal != null) {
                this.internalOutputs = new GuiSignalEvent[internal.length];
                for (int i = 0; i < internal.length; i++) {
                    if (structure == null)
                        this.internalOutputs[i] = new GuiSignalEvent(internal[i], (InternalSignalOutput) null);
                    else
                        this.internalOutputs[i] = new GuiSignalEvent(internal[i], structure.getOutput(i));
                }
            } else
                this.internalOutputs = null;
            
            externalOutputs = new HashMap<>();
            for (GuiSignalComponent output : signalSearch.externalOutputs())
                if (structure == null)
                    externalOutputs.put(output.index(), new GuiSignalEvent(output, (InternalSignalOutput) null));
                else if (output.external())
                    externalOutputs.put(output.index(), new GuiSignalEvent(output, structure.getExternalOutput(output.index())));
        }
    }
    
    @Override
    public GuiComponentSearch getSignalSearch() {
        return signalSearch;
    }
    
    @Override
    public GuiSignalEvent[] internalOutputs() {
        return internalOutputs;
    }
    
    @Override
    public Iterable<GuiSignalEvent> externalOutputs() {
        return externalOutputs.values();
    }
    
    public void setInternalOutput(int index, GuiSignalEvent event) {
        if (internalOutputs != null && index >= 0 && index < internalOutputs.length)
            internalOutputs[index] = event;
    }
    
    public void setExternalOutput(int index, GuiSignalEvent event) {
        externalOutputs.put(index, event);
    }
    
    @Override
    public void setSignalOutputs(List<GuiSignalEvent> events) {
        for (GuiSignalEvent event : events)
            setSignalOutput(event.component.external(), event.component.index(), event);
        
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < internalOutputs.length; i++) {
            GuiSignalEvent event = internalOutputs[i];
            InternalSignalOutput output = structure.getOutput(i);
            output.condition = event.condition;
            output.handler = event.getHandler(output, structure);
            
            list.add(output.saveConfiguration(true, new CompoundTag()));
        }
        nbt.put("internal", list);
        
        CompoundTag external = new CompoundTag();
        for (GuiSignalEvent event : externalOutputs.values())
            external.put("" + event.component.index(), new SignalExternalOutputHandler(null, event.component.index(), event.condition, (x) -> event.getHandler(x, structure)).write(
                true));
        nbt.put("external", external);
        
        LittleTiles.NETWORK.sendToServer(new StructureOutputConfigurationChanged(structure.getStructureLocation(), nbt));
    }
    
    @Nullable
    public void setSignalOutput(boolean external, int index, GuiSignalEvent event) {
        if (external)
            setExternalOutput(index, event);
        else
            setInternalOutput(index, event);
    }
    
    @Override
    public LittleStructureType getStructureType() {
        return structure.type;
    }
    
    @Override
    public String getTitle() {
        int index = structure.hasParent() ? structure.getParent().childId : 0;
        if (structure.name == null)
            return structure.type.id + " " + index;
        return structure.name;
    }
    
    @Override
    public LittleStructure getStructure() {
        return structure;
    }
    
    @Override
    public Iterable<IGuiSignalStructure> children() {
        return children;
    }
    
    @Override
    public IGuiSignalStructure parent() {
        return parent;
    }
    
}
