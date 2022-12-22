package team.creative.littletiles.common.gui.controls;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.render.entity.LittleLevelEntityRenderer;
import team.creative.littletiles.common.animation.preview.AnimationPreview;

public class GuiAnimationViewer extends GuiControl implements IAnimationControl {
    
    public AnimationPreview preview;
    
    public SmoothValue rotX = new SmoothValue(200);
    public SmoothValue rotY = new SmoothValue(200);
    public SmoothValue rotZ = new SmoothValue(200);
    public SmoothValue distance = new SmoothValue(200);
    
    public boolean grabbed = false;
    public double grabX;
    public double grabY;
    
    public GuiAnimationViewer(String name) {
        super(name);
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        super.mouseMoved(rect, x, y);
        if (grabbed) {
            rotY.set(rotY.aimed() + x - grabX);
            rotX.set(rotX.aimed() + y - grabY);
            grabX = x;
            grabY = y;
        }
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (button == 0) {
            grabbed = true;
            grabX = x;
            grabY = y;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        if (button == 0)
            grabbed = false;
    }
    
    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        distance.set(Math.max(distance.aimed() + delta * -(Screen.hasControlDown() ? 5 : 1), 0));
        return true;
    }
    
    public static void makeLightBright() {
        /*try {
            LightTexture texture = Minecraft.getInstance().gameRenderer.lightTexture();
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    texture.lightPixels.setPixelRGBA(j, i, -1);
                }
            }
            
            texture.lightTexture.upload();
            
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }*/
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        if (preview == null)
            return;
        
        Minecraft mc = Minecraft.getInstance();
        makeLightBright();
        
        rotX.tick();
        rotY.tick();
        rotZ.tick();
        distance.tick();
        
        //RenderSystem.cullFace(CullFace.BACK);
        
        pose.pushPose();
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        RenderSystem.viewport((int) rect.minX, (int) rect.minY, (int) rect.maxX, (int) rect.maxY);
        pose.setIdentity();
        RenderSystem.setProjectionMatrix(pose.last().pose());
        pose.mulPoseMatrix(mc.gameRenderer.getProjectionMatrix(70));
        Matrix4f matrix = pose.last().pose();
        
        pose.translate(0, 0, -distance.current());
        RenderSystem.enableDepthTest();
        
        Vec3d rotationCenter = preview.animation.getCenter().rotationCenter;
        
        pose.mulPose(Axis.XP.rotation((float) rotX.current()));
        pose.mulPose(Axis.YP.rotation((float) rotY.current()));
        pose.mulPose(Axis.ZP.rotation((float) rotZ.current()));
        
        pose.translate(-preview.box.minX, -preview.box.minY, -preview.box.minZ);
        
        pose.translate(-rotationCenter.x, -rotationCenter.y, -rotationCenter.z);
        RenderSystem.setInverseViewRotationMatrix(new Matrix3f(pose.last().normal()).invert());
        
        LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(preview.animation, RenderType.solid(), pose, 0, 0, 0, matrix);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels().get() > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(preview.animation, RenderType.cutoutMipped(), pose, 0, 0, 0, matrix);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(preview.animation, RenderType.cutout(), pose, 0, 0, 0, matrix);
        
        LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(preview.animation, RenderType.translucent(), pose, 0, 0, 0, matrix);
        pose.popPose();
        
        Window window = mc.getWindow();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float) (window.getWidth() / window.getGuiScale()), (float) (window.getHeight() / window
                .getGuiScale()), 0.0F, 1000.0F, net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, 1000F - net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        
    }
    
    @Override
    public void onLoaded(AnimationPreview preview) {
        this.preview = preview;
        this.distance.setStart(preview.grid.toVanillaGrid(preview.entireBox.getLongestSide()) / 2D + 2);
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void init() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tick() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void flowX(int width, int preferred) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void flowY(int width, int height, int preferred) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected int preferredWidth() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    protected int preferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }
}