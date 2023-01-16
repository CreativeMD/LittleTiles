package team.creative.littletiles.common.gui.signal;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.itr.ConsecutiveIterator;
import team.creative.creativecore.common.util.type.itr.ConsecutiveListIterator;
import team.creative.creativecore.common.util.type.itr.NestedIterator;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignal;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeComponent;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeInput;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeNotOperator;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeOperator;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeOutput;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeVirtualInput;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeVirtualNumberInput;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNot;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNotBitwise;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualNumber;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualVariable;
import team.creative.littletiles.common.structure.signal.input.SignalInputVariable;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator.SignalInputConditionOperatorStackable;

public class GuiSignalController extends GuiParent {
    
    protected int cellWidth = 60;
    protected int cellHeight = 40;
    public SmoothValue scrolledX = new SmoothValue(200);
    public SmoothValue scrolledY = new SmoothValue(200);
    public SmoothValue zoom = new SmoothValue(200, 1);
    public double startScrollX;
    public double startScrollY;
    public int dragX;
    public int dragY;
    public boolean scrolling;
    
    private List<List<GuiChildControl>> grid = new ArrayList<>();
    
    public final List<GuiSignalComponent> inputs;
    private GuiSignalNodeOutput output;
    
    private GuiSignalNode dragged;
    private GuiSignalNode selected;
    
    public GuiSignalController(String name, GuiSignalComponent output, List<GuiSignalComponent> inputs) {
        super(name);
        this.inputs = inputs;
        setOutput(4, output);
    }
    
    @Override
    public double getOffsetX() {
        return -scrolledX.current();
    }
    
    @Override
    public double getOffsetY() {
        return -scrolledY.current();
    }
    
    @Override
    public Iterator<GuiChildControl> iterator() {
        return new ConsecutiveIterator<>(hoverControls.iterator(), controls.iterator(), new NestedIterator<>(grid));
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack matrix, GuiChildControl control, Rect contentRect, Rect realContentRect, double scale, int mouseX, int mouseY) {
        if (realContentRect == null)
            return;
        
        scrolledX.tick();
        scrolledY.tick();
        zoom.tick();
        
        setScale(zoom.current());
        
        scale *= scaleFactor();
        double xOffset = getOffsetX();
        double yOffset = getOffsetY();
        
        matrix.pushPose();
        matrix.scale((float) scale, (float) scale, (float) scale);
        
        renderContent(matrix, contentRect, realContentRect, mouseX, mouseY, new ConsecutiveListIterator<>(grid).goEnd(), scale, xOffset, yOffset, false);
        
        matrix.popPose();
        super.renderContent(matrix, control, contentRect, realContentRect, scale, mouseX, mouseY);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected void renderControl(PoseStack matrix, GuiChildControl child, GuiControl control, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY, boolean hover) {
        super.renderControl(matrix, child, control, controlRect, realRect, scale, mouseX, mouseY, hover);
        if (control instanceof GuiSignalNodeComponent com && com.hasUnderline()) {
            Font font = GuiRenderHelper.getFont();
            font.drawShadow(matrix, com.underline, child.getWidth() / 2 - font.width(com.underline) / 2, child.getHeight() + 4, ColorUtils.WHITE);
        }
    }
    
    @Override
    public void flowX(int width, int preferred) {
        super.flowX(width, preferred);
        for (int x = 0; x < grid.size(); x++) {
            List<GuiChildControl> rows = grid.get(x);
            for (int y = 0; y < rows.size(); y++) {
                GuiChildControl child = rows.get(y);
                int widthNode = Math.min(cellWidth, child.getPreferredWidth(cellWidth));
                child.setWidth(widthNode, cellWidth);
                child.setX(cellWidth * x + cellWidth / 2 - child.getWidth() / 2);
            }
        }
    }
    
    @Override
    public void flowY(int width, int height, int preferred) {
        super.flowX(width, preferred);
        for (int x = 0; x < grid.size(); x++) {
            List<GuiChildControl> rows = grid.get(x);
            for (int y = 0; y < rows.size(); y++) {
                GuiChildControl child = rows.get(y);
                int heightNode = Math.min(cellHeight, child.getPreferredHeight(cellHeight));
                child.setHeight(heightNode, cellHeight);
                child.setY(cellHeight * y + cellHeight * 2 - child.getHeight() / 2);
            }
        }
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (button == 2) {
            zoom.set(1);
            scrolledX.set(0);
            scrolledY.set(0);
            return true;
        }
        if (!super.mouseClicked(rect, x, y, button)) {
            select(null);
            scrolling = true;
            dragX = (int) x;
            dragY = (int) y;
            startScrollX = scrolledX.current();
            startScrollY = scrolledY.current();
        }
        return true;
    }
    
    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        if (!super.mouseScrolled(rect, x, y, delta))
            zoom.set(Mth.clamp(zoom.aimed() + delta * 0.2, 0.1, 2));
        return true;
    }
    
    @Override
    public void mouseDragged(Rect rect, double x, double y, int button, double dragX, double dragY, double time) {
        super.mouseDragged(rect, x, y, button, dragX, dragY, time);
        if (time > 200 && dragged != null)
            set(dragged, (int) Math.max(0, (x * scaleFactorInv() + scrolledX.current()) / cellWidth), (int) Math.max(0, (y * scaleFactorInv() + scrolledY.current()) / cellHeight));
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        if (scrolling) {
            scrolledX.set(Mth.clamp(dragX - x + startScrollX, -40, sizeX() * cellWidth));
            scrolledY.set(Mth.clamp(dragY - y + startScrollY, -40, sizeY() * cellHeight));
        }
        super.mouseMoved(rect, x, y);
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        super.mouseReleased(rect, x, y, button);
        scrolling = false;
        dragged = null;
    }
    
    public void setOutput(int cell, GuiSignalComponent output) {
        if (this.output != null)
            removeNode(this.output);
        this.output = new GuiSignalNodeOutput(output);
        setToFreeCell(cell, this.output);
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public void setCondition(SignalInputCondition condition, GuiDialogSignal signal) {
        reset();
        try {
            List<List<GuiSignalNode>> parsed = new ArrayList<>();
            GuiSignalNode node = fill(condition, signal, parsed, 0);
            for (int i = parsed.size() - 1; i >= 0; i--) {
                List<GuiSignalNode> rows = parsed.get(i);
                for (int j = rows.size() - 1; j >= 0; j--)
                    set(rows.get(j), parsed.size() - i - 1, rows.size() - j - 1);
            }
            setOutput(parsed.size(), output.component);
            
            GuiSignalConnection connection = new GuiSignalConnection(this, node, output);
            node.connect(connection);
            output.connect(connection);
            return;
        } catch (ParseException e) {
            reset();
        }
        setOutput(4, output.component);
    }
    
    private GuiSignalNode fill(SignalInputCondition condition, GuiDialogSignal signal, List<List<GuiSignalNode>> parsed, int level) throws ParseException {
        GuiSignalNode node;
        if (condition instanceof SignalInputConditionNot || condition instanceof SignalInputConditionNotBitwise) {
            boolean bitwise = condition instanceof SignalInputConditionNotBitwise;
            node = new GuiSignalNodeNotOperator(bitwise);
            GuiSignalNode child = fill(bitwise ? ((SignalInputConditionNotBitwise) condition).condition : ((SignalInputConditionNot) condition).condition, signal, parsed, level + 1);
            GuiSignalConnection connection = new GuiSignalConnection(this, child, node);
            node.connect(connection);
            child.connect(connection);
            
        } else if (condition instanceof SignalInputConditionOperatorStackable) {
            node = new GuiSignalNodeOperator(((SignalInputConditionOperatorStackable) condition).operator());
            for (SignalInputCondition subCondition : ((SignalInputConditionOperatorStackable) condition).conditions) {
                GuiSignalNode child = fill(subCondition, signal, parsed, level + 1);
                GuiSignalConnection connection = new GuiSignalConnection(this, child, node);
                node.connect(connection);
                child.connect(connection);
            }
        } else if (condition instanceof SignalInputVariable in)
            node = new GuiSignalNodeInput(in, signal.getInput(in.target));
        else if (condition instanceof SignalInputVirtualVariable variable)
            node = new GuiSignalNodeVirtualInput(variable);
        else if (condition instanceof SignalInputVirtualNumber number)
            node = new GuiSignalNodeVirtualNumberInput(number);
        else
            throw new ParseException("Invalid condition type", 0);
        while (parsed.size() <= level)
            parsed.add(new ArrayList<>());
        parsed.get(level).add(node);
        return node;
    }
    
    public GuiSignalNodeVirtualInput addVirtualInput() {
        return setToFreeCell(0, new GuiSignalNodeVirtualInput());
    }
    
    public GuiSignalNodeVirtualNumberInput addVirtualNumberInput() {
        return setToFreeCell(0, new GuiSignalNodeVirtualNumberInput());
    }
    
    public GuiSignalNodeInput addInput(GuiSignalComponent input) {
        return setToFreeCell(0, new GuiSignalNodeInput(input));
    }
    
    public GuiSignalNodeNotOperator addNotOperator(boolean bitwise) {
        return setToFreeCell(1, new GuiSignalNodeNotOperator(bitwise));
    }
    
    public GuiSignalNodeOperator addOperator(SignalLogicOperator operator) {
        return setToFreeCell(1, new GuiSignalNodeOperator(operator));
    }
    
    public GuiSignalNode selected() {
        return selected;
    }
    
    public void drag(GuiSignalNode node) {
        dragged = node;
    }
    
    public void select(GuiSignalNode node) {
        if (selected != null)
            selected.setDefaultColor(ColorUtils.WHITE);
        selected = node;
        if (selected != null)
            selected.setDefaultColor(ColorUtils.YELLOW);
    }
    
    public void tryConnectSelectedTo(GuiSignalNode node) {
        if (selected != node && selected.canConnectTo(node) && node.canConnectFrom(selected)) {
            GuiSignalConnection connection = new GuiSignalConnection(this, selected, node);
            selected.connect(connection);
            node.connect(connection);
            raiseEvent(new GuiControlChangedEvent(this));
        }
        select(null);
    }
    
    public SignalInputCondition generatePattern() throws GeneratePatternException {
        return output.generateCondition(new ArrayList<>());
    }
    
    public <T extends GuiSignalNode> T setToFreeCell(int startCol, T node) {
        for (int i = 0; i < 50 - startCol; i++) {
            if (grid.size() <= i)
                grid.add(new ArrayList<>());
            if (i < startCol)
                continue;
            List<GuiChildControl> rows = grid.get(i);
            for (int j = 0; j < 50; j++) {
                if (rows.size() <= j)
                    rows.add(null);
                if (rows.get(j) == null) {
                    set(node, i, j);
                    return node;
                }
            }
        }
        throw new RuntimeException("There are no empty cells left");
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
    
    public void removeNode(GuiSignalNode node) {
        if (selected != null)
            selected.setDefaultColor(ColorUtils.WHITE);
        selected = null;
        dragged = null;
        remove(node.x(), node.y());
        node.remove();
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public void remove(int col, int row) {
        if (col < grid.size()) {
            List<GuiChildControl> rows = grid.get(col);
            if (row < rows.size())
                rows.set(row, null);
        }
    }
    
    public void set(GuiSignalNode node, int col, int row) {
        boolean added = node.added();
        node.setParent(this);
        if (added)
            if (node.x() == col && node.y() == row)
                return;
        while (grid.size() <= col)
            grid.add(new ArrayList<>());
        List<GuiChildControl> rows = grid.get(col);
        while (rows.size() <= row)
            rows.add(null);
        if (rows.get(row) == null) {
            if (added)
                remove(node.x(), node.y());
            rows.set(row, new GuiChildControl(node));
            node.updatePosition(col, row);
        }
    }
    
    public void reset() {
        controls.clear();
        grid.clear();
        dragged = null;
        selected = null;;
    }
    
}
