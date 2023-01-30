package team.creative.littletiles.common.gui.signal.node;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignal;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualNumber;

public class GuiSignalNodeVirtualNumberInput extends GuiSignalNode {
    
    public List<GuiSignalConnection> tos = new ArrayList<>();
    public int number;
    
    public GuiSignalNodeVirtualNumberInput() {
        super("" + 0);
        this.number = 0;
    }
    
    public GuiSignalNodeVirtualNumberInput(SignalInputVirtualNumber variable) throws ParseException {
        super("" + variable.number);
        this.number = variable.number;
        updateLabel();
    }
    
    public void updateLabel() {
        setTitle(Component.literal("" + number));
        raiseEvent(new GuiControlChangedEvent(controller()));
    }
    
    @Override
    public boolean mouseDoubleClicked(Rect rect, double x, double y, int button) {
        GuiDialogSignal.VIRTUAL_NUMBER_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(number, this);
        return true;
    }
    
    @Override
    public boolean canConnectTo(GuiSignalNode node) {
        for (GuiSignalConnection connectTo : tos)
            if (connectTo.to() == node)
                return false;
        return true;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node) {
        return false;
    }
    
    @Override
    public void disconnect(GuiSignalConnection connection) {
        tos.remove(connection);
    }
    
    @Override
    public GuiSignalConnection getConnectionTo(GuiSignalNode node) {
        for (GuiSignalConnection connectTo : tos)
            if (connectTo.to() == node)
                return connectTo;
        return null;
    }
    
    @Override
    public void connect(GuiSignalConnection connection) {
        tos.add(connection);
    }
    
    @Override
    public Iterator<GuiSignalConnection> iterator() {
        return tos.iterator();
    }
    
    @Override
    public Iterable<GuiSignalConnection> toConnections() {
        return tos;
    }
    
    @Override
    public void remove() {
        for (GuiSignalConnection connection : new ArrayList<>(tos))
            connection.disconnect(controller());
    }
    
    @Override
    public int indexOf(GuiSignalConnection connection) {
        return tos.indexOf(connection);
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        reset();
        return new SignalInputVirtualNumber(number);
    }
    
}
