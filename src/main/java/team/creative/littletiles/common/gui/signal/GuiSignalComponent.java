package team.creative.littletiles.common.gui.signal;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import team.creative.littletiles.common.structure.LittleStructureType.InternalComponent;
import team.creative.littletiles.common.structure.LittleStructureType.InternalComponentOutput;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;

public record GuiSignalComponent(String name, boolean input, boolean external, int index, int bandwidth, String description, String totalName, SignalMode defaultMode) {
    
    public GuiSignalComponent {}
    
    public GuiSignalComponent(String name, String parentName, InternalComponent component, boolean input, boolean external, int index) {
        this(name, input, external, index, component.bandwidth, component.identifier, parentName + "." + component.identifier, component instanceof InternalComponentOutput output ? output.defaultMode : SignalMode.EQUAL);
    }
    
    public GuiSignalComponent(String name, @Nullable String description, String totalName, ISignalComponent component, boolean external, int index) {
        this(name, component.getComponentType() == SignalComponentType.INPUT, external, index, getBandwidth(component), description, totalName, SignalMode.EQUAL);
    }
    
    public GuiSignalComponent(String name, String totalName, int bandwidth, SignalComponentType type, boolean external, int index) {
        this(name, type == SignalComponentType.INPUT, external, index, bandwidth, null, totalName, SignalMode.EQUAL);
    }
    
    public String display() {
        return ChatFormatting.BOLD + name + " " + totalName + " " + ChatFormatting.RESET + bandwidth + "-bit";
    }
    
    public String info() {
        if (totalName == null)
            return name;
        return name + " " + totalName;
    }
    
    public String title() {
        if (description == null)
            return name;
        return description + " (" + name + ")";
    }
    
    private static int getBandwidth(ISignalComponent component) {
        try {
            return component.getBandwidth();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            throw new RuntimeException(e);
        }
    }
}
