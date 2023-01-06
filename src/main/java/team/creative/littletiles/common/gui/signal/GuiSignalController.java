package team.creative.littletiles.common.gui.signal;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.list.PairList;

public class GuiSignalController extends GuiParent {
    
    protected int cellWidth = 60;
    protected int cellHeight = 40;
    public SmoothValue scrolledX = new SmoothValue(200);
    public SmoothValue scrolledY = new SmoothValue(200);
    public SmoothValue zoom = new SmoothValue(200, 1);
    private PairList<GuiSignalPosition, GuiChildControl> grid = new PairList<>();
    
    public GuiSignalController(String name) {
        super(name);
    }
    
    @Override
    public double getScaleFactor() {
        return zoom.current();
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
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack matrix, GuiChildControl control, Rect contentRect, Rect realContentRect, int mouseX, int mouseY) {
        if (realContentRect == null)
            return;
        double scale = getScaleFactor();
        double xOffset = getOffsetX();
        double yOffset = getOffsetY();
        
        renderContent(matrix, contentRect, realContentRect, mouseX, mouseY, grid.values(), scale, xOffset, yOffset, false);
        
        super.renderContent(matrix, control, contentRect, realContentRect, mouseX, mouseY);
    }
    
    @Override
    public void flowX(int width, int preferred) {
        super.flowX(width, preferred);
        for (Pair<GuiSignalPosition, GuiChildControl> node : grid) {
            int widthNode = Math.min(cellWidth, node.value.getPreferredWidth(cellWidth));
            node.value.setWidth(widthNode, cellWidth);
            node.value.setX(cellWidth * node.key.x + cellWidth / 2 - node.value.getWidth() / 2);
        }
    }
    
    @Override
    public void flowY(int width, int height, int preferred) {
        super.flowX(width, preferred);
        for (Pair<GuiSignalPosition, GuiChildControl> node : grid) {
            int heightNode = Math.min(cellHeight, node.value.getPreferredHeight(cellHeight));
            node.value.setHeight(heightNode, cellHeight);
            node.value.setY(cellHeight * node.key.y + cellHeight * 2 - node.value.getHeight() / 2);
        }
    }
    
    public static record GuiSignalPosition(int x, int y) {}
    
    public class GuiSignalNode extends GuiControl {
        
        public GuiSignalNode(String name) {
            super(name);
        }
        
    }
    
}
