package com.creativemd.littletiles.client.render.overlay;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.util.tooltip.IItemTooltip;
import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class TooltipOverlay {
    
    private static Minecraft mc = Minecraft.getMinecraft();
    private static ItemStack lastRenderedItem;
    
    @SubscribeEvent
    public static void onRender(RenderTickEvent event) {
        if (event.phase == Phase.END && LittleTiles.CONFIG.rendering.showTooltip && mc.inGameHasFocus && !mc.gameSettings.hideGUI) {
            EntityPlayer player = mc.player;
            if (player != null && player.getHeldItemMainhand().getItem() instanceof IItemTooltip) {
                ItemStack stack = player.getHeldItemMainhand();
                String tooltipKey = stack.getItem().getRegistryName().getResourceDomain() + "." + stack.getItem().getRegistryName().getResourcePath() + ".tooltip";
                if (I18n.canTranslate(tooltipKey)) {
                    String[] lines = I18n.translateToLocalFormatted(tooltipKey, ((IItemTooltip) stack.getItem()).tooltipData(stack)).split("\\\\n");
                    GlStateManager.pushMatrix();
                    ScaledResolution res = new ScaledResolution(mc);
                    int y = res.getScaledHeight() - 2;
                    for (int i = lines.length - 1; i >= 0; i--) {
                        String s = lines[i];
                        
                        if (!Strings.isNullOrEmpty(s)) {
                            y -= mc.fontRenderer.FONT_HEIGHT;
                            int k = mc.fontRenderer.getStringWidth(s);
                            int l = 2;
                            int i1 = 2 + y;
                            Gui.drawRect(1, i1 - 1, 2 + k + 1, i1 + mc.fontRenderer.FONT_HEIGHT - 1, -1873784752);
                            mc.fontRenderer.drawString(s, 2, i1, 14737632);
                        }
                    }
                    GlStateManager.popMatrix();
                }
            }
        }
    }
    
}
