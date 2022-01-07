package team.creative.littletiles.common.gui.controls;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.action.LittleAction;

public class GuiAxisIndicatorControl extends GuiControl {
    
    public GuiAxisIndicatorControl(String name) {
        super(name, 30, 30);
    }
    
    @Override
    protected void renderContent(PoseStack matrix, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        GlStateManager.translate(width / 2D, height / 2D, 0);
        
        float partialTicks = mc.getRenderPartialTicks();
        Entity entity = GuiControl.mc.getRenderViewEntity();
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        GlStateManager.rotate(pitch, -1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-1.0F, -1.0F, -1.0F);
        {
            {
                float direction = pitch % 180;
                
                if (LittleAction.isUsingSecondMode(mc.player)) {
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(180, 0, 0, 1);
                    GuiRenderHelper.instance.drawStringWithShadow("up", -15, -50, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                    GlStateManager.popMatrix();
                    
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(180, 1, 0, 0);
                    GuiRenderHelper.instance.drawStringWithShadow("up", 15, -50, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                    GlStateManager.popMatrix();
                    
                } else {
                    if (direction < 45 && direction > -45) {
                        GlStateManager.pushMatrix();
                        GlStateManager.rotate(180, 0, 0, 1);
                        GuiRenderHelper.instance.drawStringWithShadow("up", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                        GlStateManager.popMatrix();
                        
                        GlStateManager.pushMatrix();
                        GlStateManager.rotate(180, 1, 0, 0);
                        GuiRenderHelper.instance.drawStringWithShadow("up", 30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                        GlStateManager.popMatrix();
                    } else {
                        GlStateManager.pushMatrix();
                        GlStateManager.rotate(180, 0, 0, 1);
                        GlStateManager.rotate(90, 1, 0, 0);
                        GuiRenderHelper.instance.drawStringWithShadow("up", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                        GlStateManager.popMatrix();
                        
                        GlStateManager.pushMatrix();
                        GlStateManager.rotate(180, 0, 0, 1);
                        GlStateManager.rotate(-90, 1, 0, 0);
                        GuiRenderHelper.instance.drawStringWithShadow("up", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                        GlStateManager.popMatrix();
                    }
                }
                
                GlStateManager.pushMatrix();
                
                GlStateManager.rotate(-90, 0, 1, 0);
                
                if (direction < 45 && direction > -45) {
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(180, 0, 0, 1);
                    GuiRenderHelper.instance.drawStringWithShadow("right", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                    GlStateManager.popMatrix();
                    
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(180, 1, 0, 0);
                    GuiRenderHelper.instance.drawStringWithShadow("right", 30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                    GlStateManager.popMatrix();
                } else {
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(180, 0, 0, 1);
                    GlStateManager.rotate(90, 1, 0, 0);
                    GuiRenderHelper.instance.drawStringWithShadow("right", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                    GlStateManager.popMatrix();
                    
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(180, 0, 0, 1);
                    GlStateManager.rotate(-90, 1, 0, 0);
                    GuiRenderHelper.instance.drawStringWithShadow("right", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
                    GlStateManager.popMatrix();
                }
                
                GlStateManager.popMatrix();
            }
            OpenGlHelper.renderDirections(GuiScreen.isCtrlKeyDown() ? 50 : 30);
            
        }
    }
    
}
