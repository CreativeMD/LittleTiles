package com.creativemd.littletiles.client.gui.signal;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Mouse;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.math.SmoothValue;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.gui.signal.SubGuiDialogSignal.GuiSignalComponent;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNot;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNotBitwise;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualNumber;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualVariable;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputVariable;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputVariable.SignalInputVariableEquation;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputVariable.SignalInputVariableOperator;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputVariable.SignalInputVariablePattern;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator.SignalInputConditionOperatorStackable;
import com.creativemd.littletiles.common.structure.signal.logic.SignalPatternParser;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndex;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndexRange;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndexSingle;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChildCustomIndex;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChildIndex;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChildIndexRange;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class GuiSignalController extends GuiParent {
    
    private GuiSignalNodeOutput output;
    
    protected int cellWidth = 60;
    protected int cellHeight = 40;
    
    public final List<GuiSignalComponent> inputs;
    
    public SmoothValue scrolledX = new SmoothValue(200);
    public SmoothValue scrolledY = new SmoothValue(200);
    public SmoothValue zoom = new SmoothValue(200, 1);
    public double startScrollX;
    public double startScrollY;
    public int dragX;
    public int dragY;
    public boolean scrolling;
    
    private List<List<GuiSignalNode>> grid = new ArrayList<>();
    private List<GuiGridLine> cols = new ArrayList<>();
    private List<GuiGridLine> rows = new ArrayList<>();
    
    public GuiSignalNode dragged;
    public boolean startedDragging = false;
    public GuiSignalNode selected;
    
    public GuiSignalController(String name, int x, int y, int width, int height, GuiSignalComponent output, List<GuiSignalComponent> inputs) {
        super(name, x, y, width, height);
        this.inputs = inputs;
        setOutput(4, output);
    }
    
    @Override
    public float getScaleFactor() {
        return (float) zoom.current();
    }
    
    @Override
    public double getOffsetX() {
        return -scrolledX.current();
    }
    
    @Override
    public double getOffsetY() {
        return -scrolledY.current();
    }
    
    public SignalInputCondition generatePattern() throws GeneratePatternException {
        return output.generateCondition(new ArrayList<>());
    }
    
    public int sizeX() {
        return grid.size();
    }
    
    public int sizeY() {
        int rows = 0;
        for (int i = 0; i < grid.size(); i++)
            rows = Math.max(rows, grid.get(i).size());
        return rows;
    }
    
    public void setToFreeCell(int startCol, GuiSignalNode node) {
        for (int i = 0; i < 50 - startCol; i++) {
            if (grid.size() <= i)
                grid.add(new ArrayList<>());
            if (i < startCol)
                continue;
            List<GuiSignalNode> rows = grid.get(i);
            for (int j = 0; j < 50; j++) {
                if (rows.size() <= j)
                    rows.add(null);
                if (rows.get(j) == null) {
                    set(node, i, j);
                    return;
                }
            }
        }
    }
    
    public void remove(int col, int row) {
        if (col < grid.size()) {
            List<GuiSignalNode> rows = grid.get(col);
            if (row < rows.size())
                rows.set(row, null);
        }
    }
    
    public void set(GuiSignalNode node, int col, int row) {
        boolean added = node.added;
        if (added)
            if (node.col == col && node.row == row)
                return;
        while (grid.size() <= col)
            grid.add(new ArrayList<>());
        List<GuiSignalNode> rows = grid.get(col);
        while (rows.size() <= row)
            rows.add(null);
        if (rows.get(row) == null) {
            if (added)
                remove(node.col, node.row);
            rows.set(row, node);
            node.updatePosition(col, row);
        }
        
    }
    
    public void reset() {
        controls.clear();
        grid.clear();
        cols.clear();
        rows.clear();
        dragged = null;
        selected = null;
        startedDragging = false;
    }
    
    public void setCondition(SignalInputCondition condition, SubGuiDialogSignal signal) {
        reset();
        try {
            List<List<GuiSignalNode>> parsed = new ArrayList<>();
            GuiSignalNode node = fill(condition, signal, parsed, 0);
            for (int i = parsed.size() - 1; i >= 0; i--) {
                List<GuiSignalNode> rows = parsed.get(i);
                for (int j = rows.size() - 1; j >= 0; j--) {
                    set(rows.get(j), parsed.size() - i - 1, rows.size() - j - 1);
                    for (NodeConnection con : rows.get(j).toConnections())
                        con.build();
                }
            }
            setOutput(parsed.size(), output.component);
            
            NodeConnection connection = new NodeConnection(node, output);
            node.connect(connection);
            output.connect(connection);
            connection.build();
            return;
        } catch (ParseException e) {
            reset();
        }
        setOutput(4, output.component);
    }
    
    private GuiSignalNode fill(SignalInputCondition condition, SubGuiDialogSignal signal, List<List<GuiSignalNode>> parsed, int level) throws ParseException {
        GuiSignalNode node;
        if (condition instanceof SignalInputConditionNot || condition instanceof SignalInputConditionNotBitwise) {
            boolean bitwise = condition instanceof SignalInputConditionNotBitwise;
            node = new GuiSignalNodeNotOperator(bitwise);
            GuiSignalNode child = fill(bitwise ? ((SignalInputConditionNotBitwise) condition).condition : ((SignalInputConditionNot) condition).condition, signal, parsed, level + 1);
            NodeConnection connection = new NodeConnection(child, node);
            node.connect(connection);
            child.connect(connection);
            
        } else if (condition instanceof SignalInputConditionOperatorStackable) {
            node = new GuiSignalNodeOperator(((SignalInputConditionOperatorStackable) condition).operator());
            for (SignalInputCondition subCondition : ((SignalInputConditionOperatorStackable) condition).conditions) {
                GuiSignalNode child = fill(subCondition, signal, parsed, level + 1);
                NodeConnection connection = new NodeConnection(child, node);
                node.connect(connection);
                child.connect(connection);
            }
        } else if (condition instanceof SignalInputVariable)
            node = new GuiSignalNodeInput((SignalInputVariable) condition, signal);
        else if (condition instanceof SignalInputVirtualVariable)
            node = new GuiSignalNodeVirtualInput((SignalInputVirtualVariable) condition, signal);
        else if (condition instanceof SignalInputVirtualNumber)
            node = new GuiSignalNodeVirtualNumberInput((SignalInputVirtualNumber) condition, signal);
        else
            throw new ParseException("Invalid condition type", 0);
        while (parsed.size() <= level)
            parsed.add(new ArrayList<>());
        parsed.get(level).add(node);
        addControl(node);
        return node;
    }
    
    public GuiSignalNodeVirtualInput addVirtualInput() {
        GuiSignalNodeVirtualInput node = new GuiSignalNodeVirtualInput();
        setToFreeCell(0, node);
        addControl(node);
        return node;
    }
    
    public GuiSignalNodeVirtualNumberInput addVirtualNumberInput() {
        GuiSignalNodeVirtualNumberInput node = new GuiSignalNodeVirtualNumberInput();
        setToFreeCell(0, node);
        addControl(node);
        return node;
    }
    
    public GuiSignalNodeInput addInput(GuiSignalComponent input) {
        GuiSignalNodeInput node = new GuiSignalNodeInput(input);
        setToFreeCell(0, node);
        addControl(node);
        return node;
    }
    
    public GuiSignalNodeNotOperator addNotOperator(boolean bitwise) {
        GuiSignalNodeNotOperator node = new GuiSignalNodeNotOperator(bitwise);
        setToFreeCell(1, node);
        addControl(node);
        return node;
    }
    
    public GuiSignalNodeOperator addOperator(SignalLogicOperator operator) {
        GuiSignalNodeOperator node = new GuiSignalNodeOperator(operator);
        setToFreeCell(1, node);
        addControl(node);
        return node;
    }
    
    public void removeNode(GuiSignalNode node) {
        startedDragging = false;
        if (selected != null)
            selected.color = ColorUtils.WHITE;
        selected = null;
        dragged = null;
        controls.remove(node);
        remove(node.col, node.row);
        node.remove();
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public void select(GuiSignalNode node) {
        if (selected != null)
            selected.color = ColorUtils.WHITE;
        selected = node;
        if (selected != null)
            selected.color = ColorUtils.YELLOW;
    }
    
    @Override
    public boolean mouseScrolled(int x, int y, int scrolled) {
        if (!super.mouseScrolled(x, y, scrolled))
            zoom.set(MathHelper.clamp(zoom.aimed() + scrolled * 0.2, 0.1, 2));
        return true;
    }
    
    @Override
    public boolean mousePressed(int x, int y, int button) {
        if (button == 2) {
            zoom.set(1);
            scrolledX.set(0);
            scrolledY.set(0);
            return true;
        }
        if (!super.mousePressed(x, y, button)) {
            select(null);
            scrolling = true;
            dragX = x;
            dragY = y;
            startScrollX = scrolledX.current();
            startScrollY = scrolledY.current();
        }
        return true;
    }
    
    @Override
    public void mouseMove(int x, int y, int button) {
        if (scrolling) {
            scrolledX.set(MathHelper.clamp(dragX - x + startScrollX, -40, sizeX() * cellWidth));
            scrolledY.set(MathHelper.clamp(dragY - y + startScrollY, -40, sizeY() * cellHeight));
        }
        super.mouseMove(x, y, button);
    }
    
    @Override
    protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
        scrolledX.tick();
        scrolledY.tick();
        zoom.tick();
        super.renderContent(helper, style, width, height);
    }
    
    @Override
    public void mouseDragged(int x, int y, int button, long time) {
        super.mouseDragged(x, y, button, time);
        if (time > 200 && dragged != null) {
            startedDragging = true;
            set(dragged, (int) Math.max(0, ((x - posX) * 1 / getScaleFactor() + scrolledX.current()) / cellWidth), (int) Math
                .max(0, ((y - posY) * 1 / getScaleFactor() + scrolledY.current()) / cellHeight));
        }
    }
    
    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        //if (!startedDragging)
        //select(dragged);
        //else
        //select(null);
        
        scrolling = false;
        startedDragging = false;
        dragged = null;
    }
    
    public void setOutput(int cell, GuiSignalComponent output) {
        if (this.output != null)
            removeNode(this.output);
        this.output = new GuiSignalNodeOutput(output);
        setToFreeCell(cell, this.output);
        addControl(this.output);
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public GuiGridLine getCol(int x) {
        while (cols.size() <= x)
            cols.add(new GuiGridLine(true, cols.size()));
        return cols.get(x);
    }
    
    public GuiGridLine getRow(int y) {
        while (rows.size() <= y)
            rows.add(new GuiGridLine(false, rows.size()));
        return rows.get(y);
    }
    
    public void tryConnect(GuiSignalNode node1, GuiSignalNode node2) {
        if (node1 != node2 && node1.canConnectTo(node2) && node2.canConnectFrom(node1)) {
            NodeConnection connection = new NodeConnection(node1, node2);
            connection.build();
            node1.connect(connection);
            node2.connect(connection);
            raiseEvent(new GuiControlChangedEvent(this));
        }
        select(null);
    }
    
    public class GuiGridLine {
        
        private List<NodeConnectionLine> lines = new ArrayList<>();
        private final boolean col;
        private final int pos;
        
        public GuiGridLine(boolean col, int pos) {
            this.col = col;
            this.pos = pos;
        }
        
        public NodeConnectionLine addLine(NodeConnectionPoint from, NodeConnectionPoint to) {
            NodeConnectionLine line = new NodeConnectionLine(this, from, to);
            lines.add(line);
            refresh();
            return line;
        }
        
        public void removeLine(NodeConnectionLine line) {
            lines.remove(line);
            refresh();
        }
        
        public void refresh() {
            int distance = 0;
            if (col)
                for (int i = 0; i < lines.size(); i++) {
                    NodeConnectionLine line = lines.get(i);
                    line.setX(pos * cellWidth + 2 + distance);
                    if (distance > 0)
                        distance = -distance;
                    else
                        distance = -distance + 2;
                }
            else
                for (int i = 0; i < lines.size(); i++) {
                    NodeConnectionLine line = lines.get(i);
                    line.setY(pos * cellHeight + 2 + distance);
                    if (distance > 0)
                        distance = -distance;
                    else
                        distance = -distance + 2;
                }
            
        }
    }
    
    public abstract class GuiSignalNode extends GuiButton implements Iterable<NodeConnection> {
        
        private String error;
        private int col;
        private int row;
        
        private boolean added = false;
        
        public GuiSignalNode(String caption) {
            super(caption, caption, 0, 0, GuiRenderHelper.instance.getStringWidth(caption), 8);
        }
        
        public void reset() {
            customTooltip = null;
            color = ColorUtils.WHITE;
        }
        
        public void setError(String error) {
            setCustomTooltip(translate(error));
            color = ColorUtils.RED;
        }
        
        public int getCol() {
            return col;
        }
        
        public int getRow() {
            return row;
        }
        
        public void updatePosition(int col, int row) {
            this.col = col;
            this.row = row;
            posX = col * cellWidth + cellWidth / 2 - width / 2;
            posY = row * cellHeight + cellHeight / 2 - height / 2;
            added = true;
            
            for (NodeConnection connection : this)
                connection.rebuild();
        }
        
        private static final long DOUBLE_CLICK_WAITING_TIME = 200;
        private long doubleClickTime = -1;
        private int button;
        private int mouseX;
        private int mouseY;
        
        @Override
        public boolean mousePressed(int x, int y, int button) {
            if (doubleClickTime == -1 || button != this.button) {
                doubleClickTime = System.currentTimeMillis();
                this.button = button;
                this.mouseX = x;
                this.mouseY = y;
            } else {
                doubleClickTime = -1;
                onDoubleClicked(x, y, button);
                playSound(SoundEvents.UI_TOAST_IN);
                playSound(SoundEvents.UI_BUTTON_CLICK);
            }
            return true;
        }
        
        @Override
        protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
            if (doubleClickTime != -1 && System.currentTimeMillis() > doubleClickTime + DOUBLE_CLICK_WAITING_TIME) {
                playSound(SoundEvents.UI_BUTTON_CLICK);
                onClicked(mouseX, mouseY, button);
                doubleClickTime = -1;
            }
            super.renderContent(helper, style, width, height);
        }
        
        public void onDoubleClicked(int x, int y, int button) {
            
        }
        
        @Override
        public void onClicked(int x, int y, int button) {
            if (button == 1 && !(this instanceof GuiSignalNodeOutput)) {
                removeNode(this);
                return;
            }
            if (GuiSignalController.this.selected != null)
                tryConnect(GuiSignalController.this.selected, this);
            else if (Mouse.isButtonDown(this.button))
                GuiSignalController.this.dragged = this;
            else
                GuiSignalController.this.select(this);
        }
        
        public abstract SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException;
        
        public abstract void removeConnection(NodeConnection connection);
        
        public abstract boolean canConnectTo(GuiSignalNode node);
        
        public abstract boolean canConnectFrom(GuiSignalNode node);
        
        public abstract void connect(NodeConnection connection);
        
        public abstract void remove();
        
        public abstract int indexOf(NodeConnection connection);
        
        public abstract Iterable<NodeConnection> toConnections();
        
    }
    
    public class GuiSignalNodeOperator extends GuiSignalNode {
        
        public final SignalLogicOperator operator;
        private List<NodeConnection> from = new ArrayList<>();
        private List<NodeConnection> to = new ArrayList<>();
        
        public GuiSignalNodeOperator(SignalLogicOperator operator) {
            super(operator.display);
            this.operator = operator;
        }
        
        @Override
        public boolean canConnectTo(GuiSignalNode node) {
            for (NodeConnection connectTo : to)
                if (connectTo.to == node)
                    return false;
            return true;
        }
        
        @Override
        public boolean canConnectFrom(GuiSignalNode node) {
            for (NodeConnection connectFrom : from)
                if (connectFrom.from == node)
                    return false;
            return true;
        }
        
        @Override
        public void removeConnection(NodeConnection connection) {
            if (connection.to == this)
                from.remove(connection);
            else
                to.remove(connection);
        }
        
        @Override
        public Iterator<NodeConnection> iterator() {
            return new IteratorIterator(from.iterator(), to.iterator());
        }
        
        @Override
        public Iterable<NodeConnection> toConnections() {
            return to;
        }
        
        @Override
        public void connect(NodeConnection connection) {
            if (connection.to == this)
                from.add(connection);
            else
                to.add(connection);
        }
        
        @Override
        public void remove() {
            for (NodeConnection connection : new ArrayList<>(from))
                connection.remove();
            for (NodeConnection connection : new ArrayList<>(to))
                connection.remove();
        }
        
        @Override
        public int indexOf(NodeConnection connection) {
            if (connection.to == this)
                return from.indexOf(connection);
            else
                return to.indexOf(connection);
        }
        
        @Override
        public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
            reset();
            if (from.isEmpty())
                throw new GeneratePatternException(this, "empty");
            if (processed.contains(this))
                throw new GeneratePatternException(this, "circular");
            processed.add(this);
            if (from.size() == 1)
                return from.get(0).from.generateCondition(processed);
            List<SignalInputCondition> parsed = new ArrayList<>();
            for (int i = 0; i < from.size(); i++)
                try {
                    parsed.add(from.get(i).from.generateCondition(new ArrayList<>(processed)));
                } catch (GeneratePatternException e) {}
            
            if (parsed.isEmpty())
                throw new GeneratePatternException(this, "novalidchildren");
            if (parsed.size() == 1)
                return parsed.get(0);
            return operator.create(parsed.toArray(new SignalInputCondition[parsed.size()]));
        }
    }
    
    public class GuiSignalNodeNotOperator extends GuiSignalNode {
        
        public final boolean bitwise;
        private NodeConnection from;
        private List<NodeConnection> to = new ArrayList<>();
        
        public GuiSignalNodeNotOperator(boolean bitwise) {
            super(bitwise ? "b-not" : "not");
            this.bitwise = bitwise;
        }
        
        @Override
        public boolean canConnectTo(GuiSignalNode node) {
            for (NodeConnection connectTo : to)
                if (connectTo.to == node)
                    return false;
            return true;
        }
        
        @Override
        public boolean canConnectFrom(GuiSignalNode node) {
            return from == null;
        }
        
        @Override
        public void removeConnection(NodeConnection connection) {
            if (connection.to == this)
                from = null;
            else
                to.remove(connection);
        }
        
        @Override
        public Iterator<NodeConnection> iterator() {
            return new Iterator<GuiSignalController.NodeConnection>() {
                
                public int index = 0;
                public Iterator<NodeConnection> iterator = to.iterator();
                
                @Override
                public boolean hasNext() {
                    if (index == 0)
                        return from != null || iterator.hasNext();
                    else if (index == 1)
                        return iterator.hasNext();
                    return false;
                }
                
                @Override
                public NodeConnection next() {
                    if (index == 0) {
                        index++;
                        if (from != null)
                            return from;
                        else {
                            index++;
                            return iterator.next();
                        }
                    } else if (index == 1)
                        return iterator.next();
                    
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        @Override
        public Iterable<NodeConnection> toConnections() {
            return to;
        }
        
        @Override
        public void connect(NodeConnection connection) {
            if (connection.to == this)
                from = connection;
            else
                to.add(connection);
        }
        
        @Override
        public void remove() {
            if (from != null)
                from.remove();
            for (NodeConnection connection : new ArrayList<>(to))
                connection.remove();
        }
        
        @Override
        public int indexOf(NodeConnection connection) {
            if (connection.to == this)
                return 0;
            return to.indexOf(connection);
        }
        
        @Override
        public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
            reset();
            if (from == null)
                throw new GeneratePatternException(this, "empty");
            if (processed.contains(this))
                throw new GeneratePatternException(this, "circular");
            processed.add(this);
            return bitwise ? new SignalInputConditionNotBitwise(from.from.generateCondition(processed)) : new SignalInputConditionNot(from.from.generateCondition(processed));
        }
    }
    
    public abstract class GuiSignalNodeComponent extends GuiSignalNode {
        
        public final String underline;
        public final GuiSignalComponent component;
        
        public GuiSignalNodeComponent(GuiSignalComponent component) {
            super(component.name);
            this.component = component;
            this.underline = component.totalName;
        }
        
        @Override
        protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
            super.renderContent(helper, style, width, height);
            if (!underline.equals(caption))
                helper.font.drawStringWithShadow(underline, width / 2 - helper.font.getStringWidth(underline) / 2, 14, ColorUtils.WHITE);
        }
    }
    
    public class GuiSignalNodeInput extends GuiSignalNodeComponent {
        
        public List<NodeConnection> tos = new ArrayList<>();
        public SignalCustomIndex[] indexes;
        public int operator = 0; // 0 none, 1 logic operator, 2 pattern, 3 equation
        public SignalLogicOperator logic;
        public int[] pattern;
        public SignalInputCondition equation;
        
        public GuiSignalNodeInput(GuiSignalComponent component) {
            super(component);
        }
        
        public GuiSignalNodeInput(SignalInputVariable variable, SubGuiDialogSignal signal) throws ParseException {
            super(signal.getInput(variable.target));
            SignalTarget target = variable.target.getNestedTarget();
            if (target instanceof SignalTargetChildCustomIndex)
                indexes = ((SignalTargetChildCustomIndex) target).indexes;
            else if (target instanceof SignalTargetChildIndex)
                indexes = new SignalCustomIndex[] { new SignalCustomIndexSingle(((SignalTargetChildIndex) target).index) };
            else if (target instanceof SignalTargetChildIndexRange)
                indexes = new SignalCustomIndex[] {
                        new SignalCustomIndexRange(((SignalTargetChildIndexRange) target).index, ((SignalTargetChildIndexRange) target).index + ((SignalTargetChildIndexRange) target).length - 1) };
            if (variable instanceof SignalInputVariableOperator) {
                operator = 1;
                logic = ((SignalInputVariableOperator) variable).operator;
            } else if (variable instanceof SignalInputVariablePattern) {
                operator = 2;
                pattern = ((SignalInputVariablePattern) variable).indexes;
            } else if (variable instanceof SignalInputVariableEquation) {
                operator = 3;
                equation = ((SignalInputVariableEquation) variable).condition;
            } else
                operator = 0;
            updateLabel();
        }
        
        public void updateLabel() {
            caption = component.name;
            int length = 0;
            
            if (indexes != null) {
                String rangeText = getRange();
                if (rangeText.length() > 6) {
                    rangeText = "...";
                    length += 3;
                } else
                    length += rangeText.length();
                caption += "[" + rangeText + "]";
            }
            String operatorText = "";
            switch (operator) {
            case 1:
                operatorText = (logic == SignalLogicOperator.AND ? "&" : logic.operator) + "";
                break;
            case 2:
                for (int i = 0; i < pattern.length; i++)
                    operatorText += "" + (pattern[i] >= 2 ? "*" : pattern[i]);
                break;
            case 3:
                if (equation != null)
                    operatorText = equation.write();
                break;
            }
            if (operatorText.length() + length > 10)
                operatorText = "...";
            if (!operatorText.isEmpty())
                caption += "{" + operatorText + "}";
            width = font.getStringWidth(caption) + getContentOffset() * 2;
            posX = getCol() * cellWidth + cellWidth / 2 - width / 2;
            raiseEvent(new GuiControlChangedEvent(GuiSignalController.this));
        }
        
        public String getRange() {
            if (indexes == null)
                return "";
            String result = "";
            for (int i = 0; i < indexes.length; i++) {
                if (i > 0)
                    result += ",";
                result += indexes[i].write();
            }
            return result;
        }
        
        @Override
        public void onDoubleClicked(int x, int y, int button) {
            openClientLayer(new SubGuiDialogSignalInput(this));
        }
        
        @Override
        public boolean canConnectTo(GuiSignalNode node) {
            for (NodeConnection connectTo : tos)
                if (connectTo.to == node)
                    return false;
            return true;
        }
        
        @Override
        public boolean canConnectFrom(GuiSignalNode node) {
            return false;
        }
        
        @Override
        public void removeConnection(NodeConnection connection) {
            tos.remove(connection);
        }
        
        @Override
        public void connect(NodeConnection connection) {
            tos.add(connection);
        }
        
        @Override
        public Iterator<NodeConnection> iterator() {
            return tos.iterator();
        }
        
        @Override
        public Iterable<NodeConnection> toConnections() {
            return tos;
        }
        
        @Override
        public void remove() {
            for (NodeConnection connection : new ArrayList<>(tos))
                connection.remove();
        }
        
        @Override
        public int indexOf(NodeConnection connection) {
            return tos.indexOf(connection);
        }
        
        @Override
        public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
            reset();
            try {
                SignalTarget target = SignalTarget.parseTarget(new SignalPatternParser(component.name + (indexes != null ? "[" + getRange() + "]" : "")), false, false);
                switch (operator) {
                case 1:
                    return new SignalInputVariableOperator(target, logic);
                case 2:
                    return new SignalInputVariablePattern(target, pattern);
                case 3:
                    if (equation != null)
                        return new SignalInputVariableEquation(target, equation);
                default:
                    return new SignalInputVariable(target);
                }
                
            } catch (ParseException e) {
                throw new GeneratePatternException(this, "Invalid target");
            }
            
        }
    }
    
    public class GuiSignalNodeVirtualInput extends GuiSignalNode {
        
        public List<NodeConnection> tos = new ArrayList<>();
        public SignalInputCondition[] conditions;
        
        public GuiSignalNodeVirtualInput() {
            super("v[]");
            this.conditions = new SignalInputCondition[0];
        }
        
        public GuiSignalNodeVirtualInput(SignalInputVirtualVariable variable, SubGuiDialogSignal signal) throws ParseException {
            super("v[]");
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
            caption = "v[" + conditionsText + "]";
            width = font.getStringWidth(caption) + getContentOffset() * 2;
            posX = getCol() * cellWidth + cellWidth / 2 - width / 2;
            raiseEvent(new GuiControlChangedEvent(GuiSignalController.this));
        }
        
        @Override
        public void onDoubleClicked(int x, int y, int button) {
            openClientLayer(new SubGuiDialogSignalVirtualInput(inputs, this));
        }
        
        @Override
        public boolean canConnectTo(GuiSignalNode node) {
            for (NodeConnection connectTo : tos)
                if (connectTo.to == node)
                    return false;
            return true;
        }
        
        @Override
        public boolean canConnectFrom(GuiSignalNode node) {
            return false;
        }
        
        @Override
        public void removeConnection(NodeConnection connection) {
            tos.remove(connection);
        }
        
        @Override
        public void connect(NodeConnection connection) {
            tos.add(connection);
        }
        
        @Override
        public Iterator<NodeConnection> iterator() {
            return tos.iterator();
        }
        
        @Override
        public Iterable<NodeConnection> toConnections() {
            return tos;
        }
        
        @Override
        public void remove() {
            for (NodeConnection connection : new ArrayList<>(tos))
                connection.remove();
        }
        
        @Override
        public int indexOf(NodeConnection connection) {
            return tos.indexOf(connection);
        }
        
        @Override
        public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
            reset();
            return new SignalInputVirtualVariable(conditions);
        }
    }
    
    public class GuiSignalNodeVirtualNumberInput extends GuiSignalNode {
        
        public List<NodeConnection> tos = new ArrayList<>();
        public int number;
        
        public GuiSignalNodeVirtualNumberInput() {
            super("" + 0);
            this.number = 0;
        }
        
        public GuiSignalNodeVirtualNumberInput(SignalInputVirtualNumber variable, SubGuiDialogSignal signal) throws ParseException {
            super("" + variable.number);
            this.number = variable.number;
            updateLabel();
        }
        
        public void updateLabel() {
            caption = "" + number;
            width = font.getStringWidth(caption) + getContentOffset() * 2;
            posX = getCol() * cellWidth + cellWidth / 2 - width / 2;
            raiseEvent(new GuiControlChangedEvent(GuiSignalController.this));
        }
        
        @Override
        public void onDoubleClicked(int x, int y, int button) {
            openClientLayer(new SubGuiDialogSignalVirtualNumberInput(number, this));
        }
        
        @Override
        public boolean canConnectTo(GuiSignalNode node) {
            for (NodeConnection connectTo : tos)
                if (connectTo.to == node)
                    return false;
            return true;
        }
        
        @Override
        public boolean canConnectFrom(GuiSignalNode node) {
            return false;
        }
        
        @Override
        public void removeConnection(NodeConnection connection) {
            tos.remove(connection);
        }
        
        @Override
        public void connect(NodeConnection connection) {
            tos.add(connection);
        }
        
        @Override
        public Iterator<NodeConnection> iterator() {
            return tos.iterator();
        }
        
        @Override
        public Iterable<NodeConnection> toConnections() {
            return tos;
        }
        
        @Override
        public void remove() {
            for (NodeConnection connection : new ArrayList<>(tos))
                connection.remove();
        }
        
        @Override
        public int indexOf(NodeConnection connection) {
            return tos.indexOf(connection);
        }
        
        @Override
        public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
            reset();
            return new SignalInputVirtualNumber(number);
        }
    }
    
    public class GuiSignalNodeOutput extends GuiSignalNodeComponent {
        
        public NodeConnection from;
        
        public GuiSignalNodeOutput(GuiSignalComponent component) {
            super(component);
        }
        
        @Override
        public boolean canConnectTo(GuiSignalNode node) {
            return false;
        }
        
        @Override
        public boolean canConnectFrom(GuiSignalNode node) {
            return from == null;
        }
        
        @Override
        public Iterator<NodeConnection> iterator() {
            return new Iterator<NodeConnection>() {
                
                public boolean has = from != null;
                
                @Override
                public boolean hasNext() {
                    return has;
                }
                
                @Override
                public NodeConnection next() {
                    has = false;
                    return from;
                }
            };
        }
        
        @Override
        public Iterable<NodeConnection> toConnections() {
            return new Iterable<GuiSignalController.NodeConnection>() {
                
                @Override
                public Iterator<NodeConnection> iterator() {
                    return new Iterator<GuiSignalController.NodeConnection>() {
                        
                        @Override
                        public boolean hasNext() {
                            return false;
                        }
                        
                        @Override
                        public NodeConnection next() {
                            return null;
                        }
                        
                    };
                }
            };
        }
        
        @Override
        public void removeConnection(NodeConnection connection) {
            from = null;
        }
        
        @Override
        public void connect(NodeConnection connection) {
            from = connection;
        }
        
        @Override
        public void remove() {
            if (from != null)
                from.remove();
        }
        
        @Override
        public int indexOf(NodeConnection connection) {
            return 0;
        }
        
        @Override
        public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
            reset();
            if (from == null)
                throw new GeneratePatternException(this, "empty");
            return from.from.generateCondition(processed);
        }
    }
    
    private class NodeConnectionPoint {
        
        private int x;
        private int y;
        
        public GuiNodeConnectionPart before;
        public GuiNodeConnectionPart after;
        
        public NodeConnectionPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void setX(int x) {
            this.x = x;
            if (before != null)
                before.updateBounds();
            if (after != null)
                after.updateBounds();
        }
        
        public void setY(int y) {
            this.y = y;
            if (before != null)
                before.updateBounds();
            if (after != null)
                after.updateBounds();
        }
        
    }
    
    private class NodeConnectionLine {
        
        private final NodeConnectionPoint from;
        private final NodeConnectionPoint to;
        public final GuiGridLine line;
        
        public NodeConnectionLine(GuiGridLine line, NodeConnectionPoint from, NodeConnectionPoint to) {
            this.line = line;
            this.from = from;
            this.to = to;
        }
        
        public void remove() {
            this.line.removeLine(this);
        }
        
        public void setX(int x) {
            from.setX(x);
            to.setX(x);
        }
        
        public void setY(int y) {
            from.setY(y);
            to.setY(y);
        }
        
    }
    
    public class NodeConnection {
        
        public final GuiSignalNode from;
        public final GuiSignalNode to;
        
        private List<GuiNodeConnectionPart> controls = new ArrayList<>();
        private List<NodeConnectionLine> lines = new ArrayList<>();
        
        public NodeConnection(GuiSignalNode from, GuiSignalNode to) {
            this.from = from;
            this.to = to;
        }
        
        public void addPart(GuiNodeConnectionPart part) {
            addControl(part);
            controls.add(part);
        }
        
        public void build() {
            int startX = from.col;
            int startY = from.row;
            int endX = to.col;
            int endY = to.row;
            
            NodeConnectionPoint startPoint = new NodeConnectionPoint(from.posX + from.width, from.posY + from.height / 2);
            NodeConnectionPoint endPoint = new NodeConnectionPoint(to.posX, to.posY + to.height / 2);
            boolean neighbor = startX + 1 == endX;
            if (startY == endY && neighbor)
                addPart(new GuiNodeConnectionPart(startPoint, endPoint, this));
            else {
                GuiGridLine startCol = getCol(startX + 1);
                
                NodeConnectionPoint colStart = new NodeConnectionPoint(0, from.posY + from.height / 2);
                NodeConnectionPoint colEnd;
                if (neighbor)
                    colEnd = new NodeConnectionPoint(0, to.posY + to.height / 2); // can connect directly
                else
                    colEnd = new NodeConnectionPoint(0, 0);
                
                addPart(new GuiNodeConnectionPart(startPoint, colStart, this));
                lines.add(startCol.addLine(colStart, colEnd));
                addPart(new GuiNodeConnectionPart(colStart, colEnd, this));
                
                if (neighbor)
                    addPart(new GuiNodeConnectionPart(colEnd, endPoint, this));
                else {
                    GuiGridLine startRow = getRow(startY > endY ? startY : endY);
                    NodeConnectionPoint rowEnd = new NodeConnectionPoint(0, 0);
                    lines.add(startRow.addLine(colEnd, rowEnd));
                    addPart(new GuiNodeConnectionPart(colEnd, rowEnd, this));
                    
                    GuiGridLine endCol = getCol(endX);
                    NodeConnectionPoint endColEnd = new NodeConnectionPoint(0, to.posY + to.height / 2);
                    lines.add(endCol.addLine(rowEnd, endColEnd));
                    addPart(new GuiNodeConnectionPart(rowEnd, endColEnd, this));
                    
                    addPart(new GuiNodeConnectionPart(endColEnd, endPoint, this));
                }
            }
        }
        
        public void rebuild() {
            for (GuiNodeConnectionPart part : controls)
                removeControl(part);
            for (NodeConnectionLine line : lines)
                line.remove();
            build();
        }
        
        public void remove() {
            from.removeConnection(this);
            to.removeConnection(this);
            
            for (GuiNodeConnectionPart part : controls)
                removeControl(part);
            for (NodeConnectionLine line : lines)
                line.remove();
            
            raiseEvent(new GuiControlChangedEvent(GuiSignalController.this));
        }
    }
    
    public static class GuiNodeConnectionPart extends GuiControl {
        
        private NodeConnectionPoint from;
        private NodeConnectionPoint to;
        public final NodeConnection connection;
        
        public GuiNodeConnectionPart(NodeConnectionPoint from, NodeConnectionPoint to, NodeConnection connection) {
            super("", 0, 0, 0, 0);
            this.from = from;
            this.from.after = this;
            this.to = to;
            this.to.before = this;
            this.connection = connection;
            updateBounds();
        }
        
        public void updateBounds() {
            posX = Math.min(from.x, to.x);
            posY = Math.min(from.y, to.y);
            if (from.x == to.x) {
                width = 1;
                height = Math.max(from.y, to.y) - posY;
            } else {
                width = Math.max(from.x, to.x) - posX;
                height = 1;
            }
        }
        
        @Override
        protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {}
        
        @Override
        public boolean mousePressed(int x, int y, int button) {
            if (button == 1)
                connection.remove();
            return super.mousePressed(x, y, button);
        }
    }
    
    public class IteratorIterator<T> implements Iterator<T> {
        private final Iterator<T> is[];
        private int current;
        
        public IteratorIterator(Iterator<T>... iterators) {
            is = iterators;
            current = 0;
        }
        
        @Override
        public boolean hasNext() {
            while (current < is.length && !is[current].hasNext())
                current++;
            
            return current < is.length;
        }
        
        @Override
        public T next() {
            while (current < is.length && !is[current].hasNext())
                current++;
            
            return is[current].next();
        }
        
    }
    
    public static class GeneratePatternException extends Exception {
        
        public final GuiSignalNode node;
        
        public GeneratePatternException(GuiSignalNode node, String msg) {
            super("exception.pattern." + msg);
            this.node = node;
            this.node.setError(getMessage());
        }
        
    }
    
}
