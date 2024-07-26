package team.creative.littletiles.common.gui.signal;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.creativecore.common.util.type.itr.FilterListIterator;
import team.creative.creativecore.common.util.type.itr.NestedIterator;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignal;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;
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
    private boolean startedDragging = false;
    private GuiSignalNode selected;
    
    private Rect controllerRect;
    
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
        return new ConsecutiveIterator<>(hoverControls.iterator(), controls.iterator(), FilterIterator.skipNull(new NestedIterator<>(grid)));
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public GuiChildControl find(GuiControl control) {
        GuiChildControl child = super.find(control);
        if (child != null)
            return null;
        return findNode(control);
    }
    
    public GuiChildControl findNode(GuiControl control) {
        for (List<GuiChildControl> rows : grid)
            for (GuiChildControl child : rows)
                if (child != null && child.control == control)
                    return child;
        return null;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(GuiGraphics graphics, GuiChildControl control, Rect contentRect, Rect realContentRect, double scale, int mouseX, int mouseY) {
        if (realContentRect == null)
            return;
        
        scrolledX.tick();
        scrolledY.tick();
        zoom.tick();
        
        setScale(zoom.current());
        
        float controlScale = (float) scaleFactor();
        scale *= scaleFactor();
        double xOffset = getOffsetX();
        double yOffset = getOffsetY();
        
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(controlScale, controlScale, 1);
        controllerRect = realContentRect;
        renderControls(graphics, contentRect, realContentRect, mouseX, mouseY, FilterListIterator.skipNull(new ConsecutiveListIterator<>(grid).goEnd()), scale, xOffset, yOffset,
            false);
        controllerRect = null;
        pose.popPose();
        
        super.renderContent(graphics, control, contentRect, realContentRect, scale, mouseX, mouseY);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected void renderControl(GuiGraphics graphics, GuiChildControl child, GuiControl control, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY,
            boolean hover) {
        if (control instanceof GuiSignalNode com) {
            controllerRect.scissor();
            RenderSystem.disableDepthTest();
            if (com.hasUnderline()) {
                Font font = GuiRenderHelper.getFont();
                String underline = com.getUnderline();
                graphics.drawString(font, underline, child.getWidth() / 2 - font.width(underline) / 2, child.getHeight() + 4, ColorUtils.WHITE);
            }
            
            renderConnections(graphics.pose().last().pose(), child, com, scale, realRect.inside(mouseX, mouseY), mouseX, mouseY);
            RenderSystem.enableDepthTest();
            realRect.scissor();
        }
        
        super.renderControl(graphics, child, control, controlRect, realRect, scale, mouseX, mouseY, hover);
    }
    
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    private void renderConnections(Matrix4f matrix, GuiChildControl child, GuiSignalNode node, double scale, boolean hover, double mouseX, double mouseY) {
        RenderSystem.disableCull();
        RenderSystem.lineWidth((float) (2 * scale));
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        double originX = child.rect.minX - getContentOffset();
        double originY = child.rect.minY - getContentOffset();
        for (GuiSignalConnection connection : node) {
            GuiChildControl other = findNode(connection.from() == node ? connection.to() : connection.from());
            if (!hover)
                hover = other.control.toScreenRect(new Rect(0, 0, other.rect.getWidth(), other.rect.getHeight())).inside(mouseX, mouseY);
            if (connection.from() == node)
                renderConnection(matrix, child, other, hover, originX, originY);
            else
                renderConnection(matrix, other, child, hover, originX, originY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    private void renderConnection(Matrix4f matrix, GuiChildControl from, GuiChildControl to, boolean hover, double originX, double originY) {
        int color = hover ? ColorUtils.WHITE : ColorUtils.BLACK;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        builder.addVertex(matrix, (float) (from.rect.maxX - originX), (float) ((from.rect.minY + from.rect.maxY) * 0.5 - originY), 0).setColor(color).setNormal(1, 0, 0);
        builder.addVertex(matrix, (float) (to.rect.minX - originX), (float) ((to.rect.minY + to.rect.maxY) * 0.5 - originY), 0).setColor(color).setNormal(1, 0, 0);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }
    
    private void flowCell(GuiChildControl child, int x, int y) {
        if (getParent() == null)
            return;
        
        int widthNode = Math.min(cellWidth, child.getPreferredWidth(cellWidth));
        child.setWidth(widthNode, cellWidth);
        child.setX(cellWidth * x + cellWidth / 2 - child.getWidth() / 2);
        child.flowX();
        int heightNode = Math.min(cellHeight, child.getPreferredHeight(cellHeight));
        child.setHeight(heightNode, cellHeight);
        child.setY(cellHeight * y + cellHeight / 2 - child.getHeight() / 2);
        child.flowY();
    }
    
    @Override
    public void flowX(int width, int preferred) {
        super.flowX(width, preferred);
        for (int x = 0; x < grid.size(); x++) {
            List<GuiChildControl> rows = grid.get(x);
            for (int y = 0; y < rows.size(); y++) {
                GuiChildControl child = rows.get(y);
                if (child == null)
                    continue;
                int widthNode = Math.min(cellWidth, child.getPreferredWidth(cellWidth));
                child.setWidth(widthNode, cellWidth);
                child.setX(cellWidth * x + cellWidth / 2 - child.getWidth() / 2);
                child.flowX();
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
                if (child == null)
                    continue;
                int heightNode = Math.min(cellHeight, child.getPreferredHeight(cellHeight));
                child.setHeight(heightNode, cellHeight);
                child.setY(cellHeight * y + cellHeight / 2 - child.getHeight() / 2);
                child.flowY();
            }
        }
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        startedDragging = false;
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
        if (time > 0.2 && dragged != null) {
            set(dragged, (int) Math.max(0, (x * scaleFactorInv() + scrolledX.current()) / cellWidth), (int) Math.max(0, (y * scaleFactorInv() + scrolledY.current()) / cellHeight));
            startedDragging = true;
        }
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
        if (dragged != null && !startedDragging)
            select(dragged);
        startedDragging = false;
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
            
            GuiSignalConnection connection = new GuiSignalConnection(node, output);
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
            GuiSignalNode child = fill(bitwise ? ((SignalInputConditionNotBitwise) condition).condition : ((SignalInputConditionNot) condition).condition, signal, parsed,
                level + 1);
            GuiSignalConnection connection = new GuiSignalConnection(child, node);
            node.connect(connection);
            child.connect(connection);
            
        } else if (condition instanceof SignalInputConditionOperatorStackable) {
            node = new GuiSignalNodeOperator(((SignalInputConditionOperatorStackable) condition).operator());
            for (SignalInputCondition subCondition : ((SignalInputConditionOperatorStackable) condition).conditions) {
                GuiSignalNode child = fill(subCondition, signal, parsed, level + 1);
                GuiSignalConnection connection = new GuiSignalConnection(child, node);
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
    
    public void tryToggleConnectionToSelected(GuiSignalNode node) {
        if (selected != node) {
            GuiSignalConnection connection = selected.getConnectionTo(node);
            if (connection != null) {
                connection.disconnect(this);
                raiseEvent(new GuiControlChangedEvent(this));
            } else if (selected.canConnectTo(node) && node.canConnectFrom(selected)) {
                connection = new GuiSignalConnection(selected, node);
                selected.connect(connection);
                node.connect(connection);
                raiseEvent(new GuiControlChangedEvent(this));
            }
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
    
    public GuiChildControl remove(int col, int row) {
        if (col < grid.size()) {
            List<GuiChildControl> rows = grid.get(col);
            if (row < rows.size())
                return rows.set(row, null);
        }
        return null;
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
            GuiChildControl child = null;
            if (added)
                child = remove(node.x(), node.y());
            if (child == null)
                child = new GuiChildControl(node);
            rows.set(row, child);
            node.updatePosition(col, row);
            flowCell(child, col, row);
        }
    }
    
    public void reset() {
        controls.clear();
        grid.clear();
        dragged = null;
        selected = null;;
    }
    
}
