package team.creative.littletiles.common.gui.controls;

import java.lang.reflect.Field;

import org.lwjgl.util.glu.Project;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Vector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;

public class GuiAnimationViewer extends GuiControl implements IAnimationControl {
    
    public EntityAnimation animation;
    public LittleGrid grid;
    public LittleVec min;
    
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
    public boolean mouseScrolled(Rect rect, double x, double y, double scrolled) {
        distance.set(Math.max(distance.aimed() + scrolled * -(Screen.hasControlDown() ? 5 : 1), 0));
        return true;
    }
    
    private static final Field lightmapTextureField = ReflectionHelper.findField(EntityRenderer.class, new String[] { "lightmapTexture", "field_78513_d" });
    private static final Field lightmapColorsField = ReflectionHelper.findField(EntityRenderer.class, new String[] { "lightmapColors", "field_78504_Q" });
    private static final Field lightmapUpdateNeededField = ReflectionHelper.findField(EntityRenderer.class, new String[] { "lightmapUpdateNeeded", "field_78536_aa" });
    
    public static void makeLightBright() {
        try {
            EntityRenderer renderer = Minecraft.getInstance().entityRenderer;
            
            int[] lightmapColors = (int[]) lightmapColorsField.get(renderer);
            for (int i = 0; i < 256; ++i)
                lightmapColors[i] = -16777216 | 255 << 16 | 255 << 8 | 255;
            
            ((DynamicTexture) lightmapTextureField.get(renderer)).updateDynamicTexture();
            lightmapUpdateNeededField.setBoolean(renderer, true);
            
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
        if (animation == null)
            return;
        
        makeLightBright();
        
        rotX.tick();
        rotY.tick();
        rotZ.tick();
        distance.tick();
        
        GlStateManager.disableDepth();
        
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.translate(width / 2D, height / 2D, 0);
        
        GlStateManager.pushMatrix();
        
        //mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        //mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager
                .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        int x = getPixelOffsetX();
        int y = getPixelOffsetY() - 1;
        int scale = getGuiScale();
        GlStateManager.viewport(x * scale, y * scale, width * scale, height * scale);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        Project.gluPerspective(90, (float) width / (float) height, 0.05F, 16 * 16);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        //GlStateManager.matrixMode(5890);
        GlStateManager.translate(0, 0, -distance.current());
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        
        Vector3d rotationCenter = new Vector3d(animation.center.rotationCenter);
        rotationCenter.y -= 75;
        
        GlStateManager.rotate((float) rotX.current(), 1, 0, 0);
        GlStateManager.rotate((float) rotY.current(), 0, 1, 0);
        GlStateManager.rotate((float) rotZ.current(), 0, 0, 1);
        
        GlStateManager.translate(-min.getPosX(context), -min.getPosY(context), -min.getPosZ(context));
        
        GlStateManager.translate(-rotationCenter.x, -rotationCenter.y, -rotationCenter.z);
        
        GlStateManager.pushMatrix();
        
        GlStateManager.translate(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ);
        GlStateManager.translate(0, -75, 0);
        
        LittleAnimationHandlerClient.render.doRender(animation, 0, 0, 0, 0, TickUtils.getPartialTickTime());
        
        GlStateManager.popMatrix();
        
        GlStateManager.matrixMode(5888);
        
        GlStateManager.popMatrix();
        
        GlStateManager.disableLighting();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        
        GlStateManager.viewport(0, 0, GuiControl.mc.displayWidth, GuiControl.mc.displayHeight);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        mc.entityRenderer.setupOverlayRendering();
        GlStateManager.disableDepth();
    }
    
    @Override
    public void onLoaded(AnimationPreview animationPreview) {
        this.animation = animationPreview.animation;
        this.distance.setStart(animationPreview.grid.toVanillaGrid(animationPreview.entireBox.getLongestSide()) / 2D + 2);
        this.grid = animationPreview.grid;
        this.min = animationPreview.entireBox.getMinVec();
    }
}
