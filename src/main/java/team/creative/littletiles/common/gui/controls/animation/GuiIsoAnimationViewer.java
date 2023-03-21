package team.creative.littletiles.common.gui.controls.animation;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationStorage;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;

public class GuiIsoAnimationViewer extends GuiControl {
    
    private static final int DRAG_TIME = 20;
    
    protected SmoothValue rotX = new SmoothValue(200);
    protected SmoothValue rotY = new SmoothValue(200);
    protected SmoothValue rotZ = new SmoothValue(200);
    protected SmoothValue scale = new SmoothValue(200, 1);
    
    protected SmoothValue offX = new SmoothValue(200);
    protected SmoothValue offY = new SmoothValue(200);
    
    private GuiIsoView view;
    
    private int clicked = -1;
    private boolean grabbed = false;
    public double grabX;
    public double grabY;
    public final GuiTreeItemStructure item;
    
    private LittleBox box;
    private LittleGrid grid;
    private boolean even;
    
    public boolean visibleAxis = true;
    
    public GuiIsoAnimationViewer(String name, GuiTreeItemStructure item, LittleBox box, LittleGrid grid) {
        super(name);
        this.item = item;
        this.setView(GuiIsoView.UP);
        this.box = box.copy();
        this.grid = grid;
    }
    
    public LittleBox getBox() {
        return box;
    }
    
    public LittleGrid getGrid() {
        return grid;
    }
    
    public void setAxis(LittleBox box, LittleGrid grid) {
        this.box = box.copy();
        this.grid = grid;
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public boolean isEven() {
        return even;
    }
    
    public void setEven(boolean even) {
        boolean changed = this.even != even;
        this.even = even;
        
        if (!changed || box == null)
            return;
        
        if (even) {
            box.minX -= 1;
            box.minY -= 1;
            box.minZ -= 1;
        } else {
            box.minX += 1;
            box.minY += 1;
            box.minZ += 1;
        }
        raiseEvent(new GuiControlChangedEvent(this));
    }
    
    public void setView(GuiIsoView view) {
        this.view = view;
        this.rotX.set(view.rotX);
        this.rotY.set(view.rotY);
        this.rotZ.set(view.rotZ);
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        super.mouseMoved(rect, x, y);
        if (!grabbed)
            return;
        offX.add((x - grabX) / 100);
        offY.add((y - grabY) / 100);
        grabX = x;
        grabY = y;
    }
    
    @Override
    public void mouseDragged(Rect rect, double x, double y, int button, double dragX, double dragY, double time) {
        if (clicked == button && time > DRAG_TIME)
            grabbed = true;
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (button == 2) {
            offX.set(0);
            offY.set(0);
            scale.set(1);
            setView(GuiIsoView.values()[(view.ordinal() + 1) % GuiIsoView.values().length]);
        }
        grabX = x;
        grabY = y;
        clicked = button;
        return true;
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        if (clicked == button && !grabbed)
            click(rect, x, y, button);
        clicked = -1;
        grabbed = false;
    }
    
    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        scale.add(4 * delta * (Screen.hasControlDown() ? 5 : 1));
        return true;
    }
    
    public void click(Rect rect, double x, double y, int button) {
        
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        GuiRecipeAnimationStorage storage = item.recipe.storage;
        
        if (!storage.isReady() || !storage.isReady(item))
            return;
        
        rotX.tick();
        rotY.tick();
        rotZ.tick();
        scale.tick();
        offX.tick();
        offY.tick();
        
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        
        pose = RenderSystem.getModelViewStack();
        pose.pushPose();
        
        RenderSystem.applyModelViewMatrix();
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        RenderSystem.enableDepthTest();
        
        double guiScale = window.getGuiScale();
        int height = (int) (rect.getHeight() * guiScale);
        RenderSystem.viewport((int) (rect.minX * guiScale), (int) (window.getHeight() - rect.minY * guiScale - height), (int) (rect.getWidth() * guiScale), height);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        PoseStack projection = new PoseStack();
        projection.setIdentity();
        projection.mulPoseMatrix(new Matrix4f().setOrtho(0.0F, (float) rect.getWidth(), 0, (float) rect.getHeight(), 1000.0F, ForgeHooksClient.getGuiFarPlane()));
        RenderSystem.setProjectionMatrix(projection.last().pose());
        
        Vec3d center = storage.center();
        
        pose.translate(rect.getWidth() / 2, rect.getHeight() / 2, 0);
        
        float scale = Math.max(1F, (float) (Math.min(rect.getWidth(), rect.getHeight()) + this.scale.current()));
        pose.scale(scale, scale, scale);
        
        pose.translate(offX.current(), offY.current(), 0);
        
        pose.mulPose(Axis.XP.rotationDegrees((float) rotX.current()));
        pose.mulPose(Axis.YP.rotationDegrees((float) rotY.current()));
        pose.mulPose(Axis.ZP.rotationDegrees((float) rotZ.current()));
        pose.translate(-center.x, -center.y, -center.z);
        
        pose.pushPose();
        RenderSystem.applyModelViewMatrix();
        //storage.renderItemAndChildren(pose, RenderSystem.getProjectionMatrix(), mc, item);
        storage.renderAll(pose, projection.last().pose(), mc);
        pose.popPose();
        
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (visibleAxis) {
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.applyModelViewMatrix();
            
            PoseStack empty = new PoseStack();
            empty.setIdentity();
            
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            
            bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            int colorAlpha = 255;
            RenderBox renderBox = box.getRenderingBox(grid);
            RenderSystem.disableDepthTest();
            RenderSystem.lineWidth(8.0F);
            renderBox.renderLines(empty, bufferbuilder, colorAlpha);
            
            tesselator.end();
        }
        
        pose.popPose();
        
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        RenderSystem.setProjectionMatrix(new Matrix4f()
                .setOrtho(0.0F, (float) (window.getWidth() / window.getGuiScale()), (float) (window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, ForgeHooksClient
                        .getGuiFarPlane()));
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void init() {}
    
    @Override
    public void tick() {}
    
    @Override
    public void flowX(int width, int preferred) {}
    
    @Override
    public void flowY(int width, int height, int preferred) {}
    
    @Override
    protected int preferredWidth(int availableWidth) {
        return 10;
    }
    
    @Override
    protected int preferredHeight(int width, int availableHeight) {
        return 10;
    }
    
    static enum GuiIsoView {
        
        UP(90, 90, 0, Facing.EAST, Facing.SOUTH, Facing.UP),
        DOWN(-90, 90, 0, Facing.EAST, Facing.NORTH, Facing.DOWN),
        EAST(0, 0, 0, Facing.SOUTH, Facing.UP, Facing.EAST),
        WEST(0, 180, 0, Facing.NORTH, Facing.UP, Facing.WEST),
        SOUTH(0, -90, 0, Facing.WEST, Facing.UP, Facing.SOUTH),
        NORTH(0, 90, 0, Facing.EAST, Facing.UP, Facing.SOUTH);
        
        public final float rotX;
        public final float rotY;
        public final float rotZ;
        
        public final Facing xAxis;
        public final Facing yAxis;
        public final Facing zAxis;
        
        private GuiIsoView(float rotX, float rotY, float rotZ, Facing xAxis, Facing yAxis, Facing zAxis) {
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            this.zAxis = zAxis;
        }
    }
    
}
