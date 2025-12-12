package team.creative.littletiles.common.gui.signal.node;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.util.type.itr.IterableIterator;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalNodeAnchor;
import team.creative.littletiles.common.structure.signal.SignalContext;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public class GuiSignalNodeOutput extends GuiSignalNodeComponent {
    
    public GuiSignalConnection from;
    
    private GuiLabel testLabel;
    private SignalInputCondition testCondition;
    private SignalContext testContext;
    
    public GuiSignalNodeOutput(GuiSignalComponent component, @Nullable SignalPosition position) {
        super(component, position);
        button.setTitle(Component.translatable("gui.signal.out").append(": " + component.name()));
    }
    
    @Override
    public boolean canConnectTo(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        return false;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        return from == null;
    }
    
    @Override
    public Iterable<GuiSignalConnection> toConnections() {
        return new IterableIterator<GuiSignalConnection>() {
            
            @Override
            public boolean hasNext() {
                return false;
            }
            
            @Override
            public GuiSignalConnection next() {
                return null;
            }
            
        };
    }
    
    @Override
    public GuiSignalConnection getConnectionTo(GuiSignalNode node) {
        return null;
    }
    
    @Override
    public void disconnect(GuiSignalConnection connection) {
        from = null;
    }
    
    @Override
    public void connect(GuiSignalConnection connection) {
        from = connection;
    }
    
    @Override
    public void remove() {
        if (from != null)
            from.disconnect(controller());
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed, @Nullable SignalContext testContext) throws GeneratePatternException {
        reset();
        if (from == null)
            throw new GeneratePatternException(this, "empty");
        
        SignalInputCondition condition = from.from().generateCondition(processed, testContext);
        
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
    public boolean removable() {
        return false;
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
        
        testLabel.setTitle(controller().testSignalOutput(testCondition.test(testContext, false), component.bandwidth()));
    }
    
}
