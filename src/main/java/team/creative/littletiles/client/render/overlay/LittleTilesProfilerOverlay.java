package team.creative.littletiles.client.render.overlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.creativecore.common.util.type.PairList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.RenderingThread;
import team.creative.littletiles.client.render.item.ItemModelCache;

public class LittleTilesProfilerOverlay {
    
    private static boolean showDebugInfo = false;
    public static int vanillaChunksUpdates = 0;
    public static int ltChunksUpdates = 0;
    public static int uploaded = 0;
    
    private static int updateTime = 20;
    private static int updateTicker = 0;
    
    private static List<Long> durations;
    private static long averageDuration;
    
    private static DecimalFormat df = new DecimalFormat("0.##");
    private static Minecraft mc = Minecraft.getInstance();
    
    public static boolean isActive() {
        return showDebugInfo;
    }
    
    private static String format(Object value) {
        if (value instanceof Double || value instanceof Float)
            return df.format(value);
        return value.toString();
    }
    
    private static String format(PairList<String, Object> details) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Pair<String, Object> pair : details) {
            if (!first)
                builder.append(",");
            else
                first = false;
            builder.append(ChatFormatting.YELLOW + pair.key + ChatFormatting.RESET + ":" + format(pair.value));
        }
        return builder.toString();
    }
    
    public static void start() {
        durations = new ArrayList<>();
        showDebugInfo = true;
    }
    
    public static void stop() {
        durations = null;
        showDebugInfo = false;
    }
    
    public static void finishBuildingCache(long duration) {
        synchronized (durations) {
            durations.add(duration);
        }
    }
    
    @SubscribeEvent
    public static void onTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            updateTicker++;
            if (updateTicker > updateTime) {
                vanillaChunksUpdates = 0;
                ltChunksUpdates = 0;
                uploaded = 0;
                updateTicker = 0;
                if (durations != null)
                    synchronized (durations) {
                        averageDuration = 0;
                        if (!durations.isEmpty()) {
                            for (int i = 0; i < durations.size(); i++)
                                averageDuration += durations.get(i);
                            averageDuration /= durations.size();
                            durations.clear();
                        }
                    }
            }
        }
    }
    
    @SubscribeEvent
    public static void onRender(RenderTickEvent event) {
        if (event.phase == Phase.END && mc.inGameHasFocus && !mc.gameSettings.hideGUI) {
            List<String> warnings = new ArrayList<>();
            if (OptifineHelper.isActive() && OptifineHelper.isRenderRegions())
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Disable Render Regions");
            if (OptifineHelper.isActive() && OptifineHelper.isAnisotropicFiltering())
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Disable Anisotropic Filtering");
            if (OptifineHelper.isActive() && OptifineHelper.isAntialiasing())
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Disable Antialiasing");
            if (!LittleTiles.CONFIG.rendering.hideVBOWarning && !mc.gameSettings.useVbo)
                warnings.add(ChatFormatting.YELLOW + "(LittleTiles) Please enable VBO and restart the world!");
            if (!LittleTiles.CONFIG.rendering.hideMipmapWarning && OptifineHelper.isActive() && mc.gameSettings.mipmapLevels == 0)
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Enable mipmap levels (needs to be > 0)");
            if (!warnings.isEmpty()) {
                GlStateManager.pushMatrix();
                for (int i = 0; i < warnings.size(); i++) {
                    String warning = warnings.get(i);
                    int k = mc.fontRenderer.getStringWidth(warning);
                    int i1 = 2 + mc.fontRenderer.FONT_HEIGHT * i;
                    Gui.drawRect(1, i1 - 1, 2 + k + 1, i1 + mc.fontRenderer.FONT_HEIGHT - 1, -1873784752);
                    mc.fontRenderer.drawString(warning, 2, i1, 14737632);
                }
                GlStateManager.popMatrix();
            }
            
            if (showDebugInfo) {
                GlStateManager.pushMatrix();
                List<String> list = new ArrayList<>();
                
                PairList<String, Object> details = new PairList<>();
                details.add("ThreadCount", RenderingThread.threads.size());
                details.add("Chunks", RenderingThread.chunks.size());
                details.add("Triggered", uploaded + "(" + vanillaChunksUpdates + ")/" + ltChunksUpdates);
                int queued = 0;
                for (RenderingThread thread : RenderingThread.threads)
                    if (thread != null)
                        queued += thread.updateCoords.size();
                details.add("Queue", queued);
                
                if (averageDuration > 1000)
                    details.add("Average", averageDuration / 1000 + "ms");
                else
                    details.add("Average", averageDuration + "ns");
                
                for (RenderingThread thread : RenderingThread.threads)
                    if (thread != null)
                        details.add("" + thread.getThreadIndex(), thread.updateCoords.size());
                    
                list.add(format(details));
                details.clear();
                
                details.add("Item Cache", ItemModelCache.countCaches());
                
                list.add(format(details));
                details.clear();
                
                for (int i = 0; i < list.size(); ++i) {
                    String s = list.get(i);
                    
                    if (!Strings.isNullOrEmpty(s)) {
                        int j = mc.fontRenderer.FONT_HEIGHT;
                        int k = mc.fontRenderer.getStringWidth(s);
                        int l = 2;
                        int i1 = 2 + j * i;
                        Gui.drawRect(1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
                        mc.fontRenderer.drawString(s, 2, i1, 14737632);
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }
    
}
