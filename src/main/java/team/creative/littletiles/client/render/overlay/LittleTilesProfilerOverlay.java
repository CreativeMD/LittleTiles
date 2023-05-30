package team.creative.littletiles.client.render.overlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.build.RenderingThread;

public class LittleTilesProfilerOverlay {
    
    private static boolean showDebugInfo = false;
    public static int chunkUpdates = 0;
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
                chunkUpdates = 0;
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
    public static void onRender(RenderGuiEvent.Post event) {
        if (!mc.isPaused() && !mc.options.hideGui && mc.level != null && RenderingThread.THREADS != null) {
            
            RenderSystem.defaultBlendFunc();
            PoseStack pose = new PoseStack();
            
            List<String> warnings = new ArrayList<>();
            if (OptifineHelper.installed() && OptifineHelper.isRenderRegions())
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Disable Render Regions");
            if (OptifineHelper.installed() && OptifineHelper.isAnisotropicFiltering())
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Disable Anisotropic Filtering");
            if (OptifineHelper.installed() && OptifineHelper.isAntialiasing())
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Disable Antialiasing");
            if (!LittleTiles.CONFIG.rendering.hideMipmapWarning && OptifineHelper.installed() && mc.options.mipmapLevels().get() == 0)
                warnings.add(ChatFormatting.RED + "(LittleTiles) Optifine detected - Enable mipmap levels (needs to be > 0)");
            if (!warnings.isEmpty()) {
                for (int i = 0; i < warnings.size(); i++) {
                    String warning = warnings.get(i);
                    int k = mc.font.width(warning);
                    int i1 = 2 + mc.font.lineHeight * i;
                    Gui.fill(pose, 1, i1 - 1, 2 + k + 1, i1 + mc.font.lineHeight - 1, -1873784752);
                    mc.font.draw(pose, warning, 2, i1, 14737632);
                }
            }
            
            if (showDebugInfo) {
                List<String> list = new ArrayList<>();
                
                PairList<String, Object> details = new PairList<>();
                details.add("ThreadCount", RenderingThread.THREADS.size());
                details.add("Chunks", RenderingThread.CHUNKS.size());
                details.add("Triggered", uploaded + chunkUpdates);
                details.add("Queue", RenderingThread.queueSize());
                
                if (averageDuration > 1000)
                    details.add("Average", averageDuration / 1000 + "ms");
                else
                    details.add("Average", averageDuration + "ns");
                
                list.add(format(details));
                details.clear();
                
                details.add("Item Cache", LittleTilesClient.ITEM_RENDER_CACHE.countCaches());
                
                list.add(format(details));
                details.clear();
                
                for (int i = 0; i < list.size(); ++i) {
                    String s = list.get(i);
                    
                    if (!Strings.isNullOrEmpty(s)) {
                        int j = mc.font.lineHeight;
                        int k = mc.font.width(s);
                        int i1 = 2 + j * i;
                        GuiComponent.fill(pose, 1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
                        mc.font.draw(pose, s, 2, i1, 14737632);
                    }
                }
            }
        }
    }
    
}
