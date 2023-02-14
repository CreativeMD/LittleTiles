package team.creative.littletiles.common.gui.controls;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.entity.LittleLevelEntityRenderer;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.mixin.client.render.LightTextureAccessor;

public class GuiAnimationViewer extends GuiControl {
    
    public SmoothValue offX = new SmoothValue(200);
    public SmoothValue offY = new SmoothValue(200);
    public SmoothValue offZ = new SmoothValue(200);
    
    public SmoothValue rotX = new SmoothValue(200);
    public SmoothValue rotY = new SmoothValue(200);
    public SmoothValue rotZ = new SmoothValue(200);
    public SmoothValue distance = new SmoothValue(200);
    
    public ViewerDragMode grabMode = ViewerDragMode.NONE;
    private ProjectionMode projection = ProjectionMode.SHOWCASE;
    public double grabX;
    public double grabY;
    public GuiAnimationViewerStorage storage;
    private boolean initialized = false;
    
    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;
    
    public GuiAnimationViewer(String name, GuiAnimationViewerStorage storage) {
        super(name);
        this.storage = storage;
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        super.mouseMoved(rect, x, y);
        if (grabMode == ViewerDragMode.NONE)
            return;
        
        switch (grabMode) {
            case LEFT -> {
                rotY.set(rotY.aimed() + x - grabX);
                rotX.set(rotX.aimed() + y - grabY);
            }
            default -> projection.dragMouse(this, x - grabX, y - grabY);
        }
        grabX = x;
        grabY = y;
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (grabMode == ViewerDragMode.NONE) {
            grabMode = ViewerDragMode.of(button);
            grabX = x;
            grabY = y;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        if (button == grabMode.ordinal() - 1)
            grabMode = ViewerDragMode.NONE;
    }
    
    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        distance.set(Math.max(distance.aimed() + delta * -(Screen.hasControlDown() ? 5 : 1), 0));
        return true;
    }
    
    public void nextProjection() {
        setProjection(projection.next());
    }
    
    public void setProjection(ProjectionMode mode) {
        this.projection = mode;
        initialized = false;
    }
    
    public static int[][] makeLightBright() {
        int[][] pixels = new int[16][16];
        LightTextureAccessor texture = (LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++) {
                pixels[x][y] = texture.getLightPixels().getPixelRGBA(x, y);
                texture.getLightPixels().setPixelRGBA(x, y, ColorUtils.WHITE);
            }
        
        texture.getLightTexture().upload();
        return pixels;
    }
    
    public static void resetLight(int[][] pixels) {
        LightTextureAccessor texture = (LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                texture.getLightPixels().setPixelRGBA(x, y, pixels[x][y]);
        texture.getLightTexture().upload();
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderPreview(PoseStack pose, PoseStack projection, AnimationPreview preview, Minecraft mc) {
        preview.animation.getRenderManager().setupRender(preview.animation, new Vec3d(), null, false, false);
        LittleLevelEntityRenderer.INSTANCE.compileChunks(preview.animation);
        
        Matrix4f matrix = projection.last().pose();
        
        renderChunkLayer(preview, RenderType.solid(), pose, matrix);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels().get() > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        renderChunkLayer(preview, RenderType.cutoutMipped(), pose, matrix);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderChunkLayer(preview, RenderType.cutout(), pose, matrix);
        
        renderChunkLayer(preview, RenderType.translucent(), pose, matrix);
    }
    
    protected void renderChunkLayer(AnimationPreview preview, RenderType layer, PoseStack pose, Matrix4f matrix) {
        layer.setupRenderState();
        ShaderInstance shaderinstance = RenderSystem.getShader();
        
        for (int i = 0; i < 12; ++i)
            shaderinstance.setSampler("Sampler" + i, RenderSystem.getShaderTexture(i));
        
        if (shaderinstance.MODEL_VIEW_MATRIX != null)
            shaderinstance.MODEL_VIEW_MATRIX.set(pose.last().pose());
        
        if (shaderinstance.PROJECTION_MATRIX != null)
            shaderinstance.PROJECTION_MATRIX.set(matrix);
        
        if (shaderinstance.COLOR_MODULATOR != null)
            shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        
        if (shaderinstance.FOG_START != null)
            shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
        
        if (shaderinstance.FOG_END != null)
            shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        
        if (shaderinstance.FOG_COLOR != null)
            shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        
        if (shaderinstance.FOG_SHAPE != null)
            shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        
        if (shaderinstance.TEXTURE_MATRIX != null)
            shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        
        if (shaderinstance.GAME_TIME != null)
            shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        
        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.apply();
        
        LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(preview.animation, layer, pose, 0, 0, 0, matrix);
        shaderinstance.clear();
        VertexBuffer.unbind();
        layer.clearRenderState();
    }
    
    public PoseStack getProjectionMatrix(Minecraft mc, double fov, float width, float height) {
        PoseStack posestack = new PoseStack();
        posestack.setIdentity();
        posestack.mulPoseMatrix(new Matrix4f().setPerspective((float) (fov * Math.PI / 180F), width / height, 0.05F, mc.gameRenderer.getDepthFar()));
        return posestack;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUp.matches(keyCode, scanCode))
            forward = true;
        if (options.keyDown.matches(keyCode, scanCode))
            backward = true;
        if (options.keyRight.matches(keyCode, scanCode))
            left = true;
        if (options.keyLeft.matches(keyCode, scanCode))
            right = true;
        if (options.keyJump.matches(keyCode, scanCode))
            up = true;
        if (options.keyShift.matches(keyCode, scanCode))
            down = true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUp.matches(keyCode, scanCode))
            forward = false;
        if (options.keyDown.matches(keyCode, scanCode))
            backward = false;
        if (options.keyRight.matches(keyCode, scanCode))
            left = false;
        if (options.keyLeft.matches(keyCode, scanCode))
            right = false;
        if (options.keyJump.matches(keyCode, scanCode))
            up = false;
        if (options.keyShift.matches(keyCode, scanCode))
            down = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    public void resetView() {
        offX.set(0);
        offY.set(0);
        offZ.set(0);
        rotX.set(0);
        rotY.set(0);
        rotZ.set(0);
        initialized = false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        if (!storage.isReady())
            return;
        
        if (!initialized) {
            resetView();
            projection.init(this);
            initialized = true;
        }
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        int[][] pixels = makeLightBright();
        
        rotX.tick();
        rotY.tick();
        rotZ.tick();
        distance.tick();
        offX.tick();
        offY.tick();
        offZ.tick();
        
        float amount = mc.getDeltaFrameTime() * 2;
        if (Screen.hasControlDown())
            amount *= 4;
        if (forward)
            projection.forward(amount, this);
        if (backward)
            projection.forward(-amount, this);
        if (left)
            projection.lateral(amount, this);
        if (right)
            projection.lateral(-amount, this);
        if (up)
            projection.up(amount, this);
        if (down)
            projection.up(-amount, this);
        
        pose = RenderSystem.getModelViewStack();
        pose.pushPose();
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        double scale = window.getGuiScale();
        int height = (int) (rect.getHeight() * scale);
        RenderSystem.viewport((int) (rect.minX * scale), (int) (window.getHeight() - rect.minY * scale - height), (int) (rect.getWidth() * scale), height);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        PoseStack projection = getProjectionMatrix(mc, 70, (float) rect.getWidth(), (float) rect.getHeight());
        RenderSystem.setProjectionMatrix(projection.last().pose());
        
        pose.setIdentity();
        
        RenderSystem.enableDepthTest();
        
        Vec3d center = storage.center();
        this.projection.prepareRendering(pose, center, this);
        
        storage.render(pose, projection, this, mc);
        
        pose.popPose();
        
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        
        RenderSystem.setProjectionMatrix(new Matrix4f()
                .setOrtho(0.0F, (float) (window.getWidth() / window.getGuiScale()), (float) (window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, ForgeHooksClient
                        .getGuiFarPlane()));
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        resetLight(pixels);
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
    
    protected static enum ViewerDragMode {
        
        NONE,
        LEFT,
        RIGHT,
        MIDDLE;
        
        public static ViewerDragMode of(int button) {
            return switch (button) {
                case 0 -> LEFT;
                case 1 -> RIGHT;
                case 2 -> MIDDLE;
                default -> NONE;
            };
        }
        
    }
    
    public static enum ProjectionMode {
        
        SHOWCASE {
            
            @Override
            public void init(GuiAnimationViewer viewer) {
                viewer.distance.set(viewer.storage.longestSide() / 2D + 2);
            }
            
            @Override
            public void dragMouse(GuiAnimationViewer viewer, double x, double y) {
                double grabOffset = 0.01;
                switch (viewer.grabMode) {
                    case RIGHT -> {
                        Vector3f offset = new Vector3f((float) (x * grabOffset), 0, (float) (y * grabOffset));
                        apply(offset, viewer);
                    }
                    case MIDDLE -> {
                        Vector3f offset = new Vector3f((float) (x * grabOffset), (float) (y * -grabOffset), 0);
                        apply(offset, viewer);
                    }
                }
            }
            
            @Override
            public void forward(float amount, GuiAnimationViewer viewer) {
                viewer.rotX.add(amount);
            }
            
            @Override
            public void lateral(float amount, GuiAnimationViewer viewer) {
                viewer.rotY.add(-amount);
            }
            
            public void apply(Vector3f vec, GuiAnimationViewer viewer) {
                vec.rotate(Axis.XP.rotationDegrees((float) viewer.rotX.current()));
                vec.rotate(Axis.YP.rotationDegrees((float) viewer.rotY.current()));
                vec.rotate(Axis.ZP.rotationDegrees((float) viewer.rotZ.current()));
                viewer.offX.set(viewer.offX.aimed() + vec.x);
                viewer.offY.set(viewer.offY.aimed() + vec.y);
                viewer.offZ.set(viewer.offZ.aimed() + vec.z);
            }
            
            @Override
            public void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer) {
                
                pose.translate(viewer.offX.current(), viewer.offY.current(), viewer.offZ.current() - viewer.distance.current());
                
                pose.mulPose(Axis.XP.rotationDegrees((float) viewer.rotX.current()));
                pose.mulPose(Axis.YP.rotationDegrees((float) viewer.rotY.current()));
                pose.mulPose(Axis.ZP.rotationDegrees((float) viewer.rotZ.current()));
                
                pose.translate(-center.x, -center.y, -center.z);
            }
            
            @Override
            public ProjectionMode next() {
                return PLAYER;
            }
            
            @Override
            public void up(float amount, GuiAnimationViewer viewer) {
                viewer.offZ.add(amount * 0.05F);
            }
        },
        PLAYER {
            
            @Override
            public void init(GuiAnimationViewer viewer) {
                viewer.offZ.set(viewer.storage.longestSide() / 2D);
                viewer.offY.set(-viewer.storage.overall().getSize());
                viewer.rotX.set(45);
                viewer.rotY.set(180);
            }
            
            @Override
            public void dragMouse(GuiAnimationViewer viewer, double x, double y) {}
            
            @Override
            public void forward(float amount, GuiAnimationViewer viewer) {
                amount *= 0.1;
                viewer.offX.add(amount * (Math.sin(-viewer.rotY.aimed() * Math.PI / 180)));
                viewer.offZ.add(amount * (Math.cos(viewer.rotY.aimed() * Math.PI / 180)));
            }
            
            @Override
            public void lateral(float amount, GuiAnimationViewer viewer) {
                amount *= -0.1;
                viewer.offX.add(amount * (Math.cos(-viewer.rotY.aimed() * Math.PI / 180)));
                viewer.offZ.add(amount * (Math.sin(viewer.rotY.aimed() * Math.PI / 180)));
            }
            
            @Override
            public void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer) {
                pose.mulPose(Axis.XP.rotationDegrees((float) viewer.rotX.current()));
                pose.mulPose(Axis.YP.rotationDegrees((float) viewer.rotY.current()));
                pose.mulPose(Axis.ZP.rotationDegrees((float) viewer.rotZ.current()));
                
                pose.translate(viewer.offX.current(), viewer.offY.current(), viewer.offZ.current());
                pose.translate(-center.x, -center.y, -center.z);
                
            }
            
            @Override
            public ProjectionMode next() {
                return SHOWCASE;
            }
            
            @Override
            public void up(float amount, GuiAnimationViewer viewer) {
                viewer.offY.add(amount * -0.1);
            }
        };
        
        public abstract ProjectionMode next();
        
        public abstract void init(GuiAnimationViewer viewer);
        
        public abstract void prepareRendering(PoseStack pose, Vec3d center, GuiAnimationViewer viewer);
        
        public abstract void dragMouse(GuiAnimationViewer viewer, double x, double y);
        
        public abstract void forward(float amount, GuiAnimationViewer viewer);
        
        public abstract void lateral(float amount, GuiAnimationViewer viewer);
        
        public abstract void up(float amount, GuiAnimationViewer viewer);
        
    }
    
    public interface GuiAnimationViewerStorage {
        
        public boolean isReady();
        
        public double longestSide();
        
        public AABB overall();
        
        public Vec3d center();
        
        public boolean highlightSelected();
        
        public void highlightSelected(boolean value);
        
        @OnlyIn(Dist.CLIENT)
        public void render(PoseStack pose, PoseStack projection, GuiAnimationViewer viewer, Minecraft mc);
        
    }
}