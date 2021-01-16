package com.creativemd.littletiles.client.render.overlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.cache.ItemModelCache;
import com.creativemd.littletiles.client.render.cache.RenderingThread;
import com.google.common.base.Strings;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

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
    private static Minecraft mc = Minecraft.getMinecraft();
    
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
            if (OptifineHelper.isActive() && OptifineHelper.isRenderRegions()) {
                String text = ChatFormatting.RED + "Disable Render Regions";
                int j = mc.fontRenderer.FONT_HEIGHT;
                int k = mc.fontRenderer.getStringWidth(text);
                int l = 2;
                int i1 = 2 + j * 2;
                Gui.drawRect(1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
                mc.fontRenderer.drawString(text, 2, i1, 14737632);
            }
            if (!LittleTiles.CONFIG.rendering.hideVBOWarning && !mc.gameSettings.useVbo) {
                String text = ChatFormatting.YELLOW + "please enable VBO and restart the world!";
                int j = mc.fontRenderer.FONT_HEIGHT;
                int k = mc.fontRenderer.getStringWidth(text);
                int l = 2;
                int i1 = 2 + j * 3;
                Gui.drawRect(1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
                mc.fontRenderer.drawString(text, 2, i1, 14737632);
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
