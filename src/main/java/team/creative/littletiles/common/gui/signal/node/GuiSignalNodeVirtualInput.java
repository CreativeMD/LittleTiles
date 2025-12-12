package team.creative.littletiles.common.gui.signal.node;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalNodeAnchor;
import team.creative.littletiles.common.structure.signal.SignalContext;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualVariable;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public class GuiSignalNodeVirtualInput extends GuiSignalNode {
    
    public List<GuiSignalConnection> tos = new ArrayList<>();
    public SignalInputCondition[] conditions;
    
    private GuiLabel testLabel;
    private SignalInputCondition testCondition;
    private SignalContext testContext;
    
    public GuiSignalNodeVirtualInput(@Nullable SignalPosition position) {
        super("v[]", null);
        this.conditions = new SignalInputCondition[0];
    }
    
    public GuiSignalNodeVirtualInput(SignalInputVirtualVariable variable, @Nullable SignalPosition position) throws ParseException {
        super("v[]", null);
        this.conditions = variable.conditions;
        updateLabel();
    }
    
    public void updateLabel() {
        String conditionsText = "";
        for (int i = 0; i < conditions.length; i++) {
            if (i > 0)
                conditionsText += ",";
            conditionsText += conditions[i].write();
        }
        if (conditionsText.length() > 10)
            conditionsText = "...";
        button.setTitle(Component.literal("v[" + conditionsText + "]"));
        raiseEvent(new GuiControlChangedEvent(controller()));
    }
    
    @Override
    public boolean mouseDoubleClicked(double x, double y, int button) {
        LittleTilesGuiRegistry.VIRTUAL_INPUT_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(controller().inputs, this);
        return true;
    }
    
    @Override
    public boolean canConnectTo(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        for (GuiSignalConnection connectTo : tos)
            if (connectTo.to() == node)
                return false;
        return true;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        return false;
    }
    
    @Override
    public GuiSignalConnection getConnectionTo(GuiSignalNode node) {
        for (GuiSignalConnection connectTo : tos)
            if (connectTo.to() == node)
                return connectTo;
        return null;
    }
    
    @Override
    public void disconnect(GuiSignalConnection connection) {
        tos.remove(connection);
    }
    
    @Override
    public void connect(GuiSignalConnection connection) {
        tos.add(connection);
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
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed, @Nullable SignalContext testContext) throws GeneratePatternException {
        reset();
        SignalInputCondition condition = new SignalInputVirtualVariable(conditions, position());
        
        if (testContext != null) {
            if (testLabel == null)
                testLabel = new GuiLabel("");
            insertControlBefore(button, testLabel);
            testCondition = condition;
            this.testContext = testContext;
            testInputChanged();
        }
        
        return condition;
        
    }
    
    @Override
    public void resetTest() {
        if (testLabel != null)
            remove(testLabel);
        testCondition = null;
    }
    
    @Override
    public void testInputChanged() {
        if (testCondition == null || testLabel == null)
            return;
        
        testLabel.setTitle(controller().testSignalOutput(testCondition.test(testContext, false), -1));
    }
    
}
