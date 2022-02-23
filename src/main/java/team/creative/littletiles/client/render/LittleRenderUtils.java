package team.creative.littletiles.client.render;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.creativecore.common.level.CreativeClientLevel;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.littletiles.client.render.level.LittleRenderChunk;

public class LittleRenderUtils {
    
    public static List<RenderType> CHUNK_RENDER_TYPES = RenderType.chunkBufferLayers();
    
    // ViewFrustum
    private static Field viewAreaField;
    
    public static ViewArea getViewArea() {
        if (viewAreaField == null)
            viewAreaField = ObfuscationReflectionHelper.findField(LevelRenderer.class, "f_109469_");
        try {
            return (ViewArea) viewAreaField.get(mc.levelRenderer);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static Minecraft mc = Minecraft.getInstance();
    
    public static BlockPos getRenderChunkPos(BlockPos pos) {
        int i = Mth.intFloorDiv(pos.getX(), 16);
        int j = Mth.intFloorDiv(pos.getY(), 16);
        int k = Mth.intFloorDiv(pos.getZ(), 16);
        return new BlockPos(i, j, k);
    }
    
    private static Method getRenderChunk = ObfuscationReflectionHelper.findMethod(ViewArea.class, "m_110866_", BlockPos.class);
    
    public static RenderChunk getRenderChunk(ViewArea view, BlockPos pos) {
        try {
            return (RenderChunk) getRenderChunk.invoke(view, pos);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static LittleRenderChunk getRenderChunk(IOrientatedLevel level, BlockPos pos) {
        if (level instanceof CreativeClientLevel && ((CreativeClientLevel) level).renderChunkSupplier != null)
            return (LittleRenderChunk) ((CreativeClientLevel) level).renderChunkSupplier.getRenderChunk((Level) level, pos);
        return null;
    }
    
}
