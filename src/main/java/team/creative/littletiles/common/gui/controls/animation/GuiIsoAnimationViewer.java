package team.creative.littletiles.common.gui.controls.animation;

import org.joml.Matrix3f;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationStorage;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;

public class GuiIsoAnimationViewer extends GuiControl {
    
    private static final float MAXIMUM_ZOOM = 1F / (float) LittleGrid.defaultGrid().pixelLength;
    
    protected SmoothValue rotX = new SmoothValue(200);
    protected SmoothValue rotY = new SmoothValue(200);
    protected SmoothValue rotZ = new SmoothValue(200);
    protected SmoothValue scale = new SmoothValue(200, 1);
    
    protected SmoothValue offX = new SmoothValue(200);
    protected SmoothValue offY = new SmoothValue(200);
    
    private GuiIsoView view;
    
    private boolean initialized = false;
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
        raiseEvent(new GuiAnimationAxisChangedEvent(this));
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
        raiseEvent(new GuiAnimationAxisChangedEvent(this));
    }
    
    public void setView(GuiIsoView view) {
        this.view = view;
        this.rotX.set(view.rotX);
        this.rotY.set(view.rotY);
        this.rotZ.set(view.rotZ);
        if (getParent() != null)
            raiseEvent(new GuiAnimationViewChangedEvent(this));
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        super.mouseMoved(rect, x, y);
        if (!grabbed)
            return;
        double scale = calculateScale(rect);
        offX.add((x - grabX) / scale);
        offY.add((y - grabY) / scale);
        grabX = x;
        grabY = y;
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        switch (button) {
            case 0 -> {
                grabX = x;
                grabY = y;
                grabbed = true;
            }
            case 1 -> clickToSetAxis(rect, x, y);
            case 2 -> resetView();
        }
        return true;
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        grabbed = false;
    }
    
    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        scale.add(0.05 * delta * (Screen.hasControlDown() ? 5 : 1));
        if (Math.pow(scale.aimed(), 2) > MAXIMUM_ZOOM)
            scale.set(Math.sqrt(MAXIMUM_ZOOM));
        if (scale.aimed() < 0)
            scale.set(0);
        return true;
    }
    
    public void clickToSetAxis(Rect rect, double x, double y) {
        GuiRecipeAnimationStorage storage = item.recipe.storage;
        if (!storage.isReady() || !storage.isReady(item))
            return;
        
        float scale = calculateScale(rect);
        Vec3d center = storage.center();
        
        team.creative.creativecore.common.util.math.base.Axis one = view.xAxis.axis;
        int posOne = grid.toGrid(-offX.current() * view.xAxis.offset() + ((x - rect.getWidth() / 2) / scale) * view.xAxis.offset() + center.get(one));
        int sizeOne = box.getSize(one);
        box.setMax(one, posOne + sizeOne);
        box.setMin(one, posOne);
        
        team.creative.creativecore.common.util.math.base.Axis two = view.yAxis.axis;
        int posTwo = grid.toGrid((-offY.current() + (y - rect.getHeight() / 2) / scale) * -view.yAxis.offset() + center.get(two));
        int sizeTwo = box.getSize(two);
        box.setMax(two, posTwo + sizeTwo);
        box.setMin(two, posTwo);
        
        raiseEvent(new GuiAnimationAxisChangedEvent(this));
        
        playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON);
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    public void resetView() {
        offX.set(0);
        offY.set(0);
        
        scale.set(Math.sqrt(0.9 / item.recipe.storage.longestSide()));
    }
    
    public void nextAxis() {
        setView(team.creative.creativecore.common.util.math.base.Axis
                .values()[(view.zAxis.axis.ordinal() + 1) % team.creative.creativecore.common.util.math.base.Axis.values().length].facing(true));
    }
    
    public void mirrorView() {
        setView(view.zAxis.opposite());
    }
    
    public void setView(Facing facing) {
        setView(switch (facing) {
            case EAST -> GuiIsoView.EAST;
            case WEST -> GuiIsoView.WEST;
            case UP -> GuiIsoView.UP;
            case DOWN -> GuiIsoView.DOWN;
            case SOUTH -> GuiIsoView.SOUTH;
            case NORTH -> GuiIsoView.NORTH;
        });
    }
    
    protected float calculateScale(Rect rect) {
        float dimensionScale = Math.min((float) rect.getWidth(), (float) rect.getHeight());
        return (float) (dimensionScale * Math.pow(this.scale.current(), 2));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        GuiRecipeAnimationStorage storage = item.recipe.storage;
        
        if (!storage.isReady() || !storage.isReady(item))
            return;
        
        if (!initialized) {
            resetView();
            initialized = true;
        }
        
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
        Matrix3f matrix3f = new Matrix3f(projection.last().normal()).invert();
        RenderSystem.setInverseViewRotationMatrix(matrix3f);
        
        Vec3d center = storage.center();
        
        pose.translate(rect.getWidth() / 2, rect.getHeight() / 2, 0);
        
        float scale = calculateScale(rect);
        pose.scale(scale, scale, scale);
        
        pose.translate(offX.current(), -offY.current(), 0);
        
        pose.mulPose(Axis.XP.rotationDegrees((float) rotX.current()));
        pose.mulPose(Axis.YP.rotationDegrees((float) rotY.current()));
        pose.mulPose(Axis.ZP.rotationDegrees((float) rotZ.current()));
        pose.translate(-center.x, -center.y, -center.z);
        
        storage.renderAll(pose, projection.last().pose(), mc);
        
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (visibleAxis) {
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
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
        
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        
        {
            pose.pushPose();
            RenderSystem.applyModelViewMatrix();
            
            int axisWidth = 20;
            int axisHeight = 16;
            pose.translate(rect.getWidth() / 2 - axisWidth / 2, rect.getHeight() - axisHeight - 2, 0);
            Minecraft.getInstance().font.drawShadow(pose, view.xAxis.axis.name(), 0, 0, ColorUtils.WHITE);
            GuiIcon icon = view.xAxis.positive ? GuiIcon.ARROW_RIGHT : GuiIcon.ARROW_LEFT;
            RenderSystem.setShaderTexture(0, icon.location());
            RenderSystem.setShaderColor(0, 0, 0, 1);
            GuiRenderHelper.textureRect(pose, 5, 0, icon.minX(), icon.minY(), icon.minX() + icon.width(), icon.minY() + icon.height());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            GuiRenderHelper.textureRect(pose, 4, 0, icon.minX(), icon.minY(), icon.minX() + icon.width(), icon.minY() + icon.height());
            
            pose.popPose();
        }
        
        RenderSystem.applyModelViewMatrix();
        
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
    
    public Facing getXFacing() {
        return view.xAxis;
    }
    
    public Facing getYFacing() {
        return view.yAxis;
    }
    
    public Facing getZFacing() {
        return view.zAxis;
    }
    
    public static class GuiAnimationAxisChangedEvent extends GuiControlChangedEvent {
        
        public GuiAnimationAxisChangedEvent(GuiIsoAnimationViewer viewer) {
            super(viewer);
        }
        
    }
    
    public static class GuiAnimationViewChangedEvent extends GuiControlChangedEvent {
        
        public GuiAnimationViewChangedEvent(GuiIsoAnimationViewer viewer) {
            super(viewer);
        }
        
    }
    
    static enum GuiIsoView {
        
        UP(90, 90, 0, Facing.SOUTH, Facing.EAST, Facing.UP),
        DOWN(-90, 90, 0, Facing.SOUTH, Facing.WEST, Facing.DOWN),
        EAST(0, -90, 0, Facing.NORTH, Facing.UP, Facing.EAST),
        WEST(0, 90, 0, Facing.SOUTH, Facing.UP, Facing.WEST),
        SOUTH(0, 0, 0, Facing.EAST, Facing.UP, Facing.SOUTH),
        NORTH(0, 180, 0, Facing.WEST, Facing.UP, Facing.NORTH);
        
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
