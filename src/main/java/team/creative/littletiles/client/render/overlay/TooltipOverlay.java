package team.creative.littletiles.client.render.overlay;

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class TooltipOverlay {
    
    private static Minecraft mc = Minecraft.getInstance();
    
    @SubscribeEvent
    public static void onRender(RenderGuiEvent.Post event) {
        if (LittleTiles.CONFIG.rendering.showTooltip && mc.isWindowActive() && !mc.options.hideGui) {
            GuiGraphics graphics = event.getGuiGraphics();
            Player player = mc.player;
            if (player != null && player.getMainHandItem().getItem() instanceof IItemTooltip item) {
                ItemStack stack = player.getMainHandItem();
                String tooltipKey = stack.getItem().builtInRegistryHolder().key().location().getNamespace() + "." + stack.getItem().builtInRegistryHolder().key().location()
                        .getPath() + ".tooltip";
                tooltipKey = item.tooltipTranslateKey(stack, tooltipKey);
                if (LanguageUtils.can(tooltipKey)) {
                    String[] lines = Component.translatable(tooltipKey, item.tooltipData(stack)).getString().split("\\n");
                    
                    int y = mc.getWindow().getGuiScaledHeight() - 2;
                    for (int i = lines.length - 1; i >= 0; i--) {
                        String s = lines[i];
                        
                        if (!Strings.isNullOrEmpty(s)) {
                            y -= mc.font.lineHeight;
                            int k = mc.font.width(s);
                            int i1 = 2 + y;
                            graphics.fill(1, i1 - 1, 2 + k + 1, i1 + mc.font.lineHeight - 1, -1873784752);
                            graphics.drawString(mc.font, s, 2, i1, 14737632);
                        }
                    }
                }
            }
        }
    }
    
}
