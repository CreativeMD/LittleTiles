package team.creative.littletiles.common.mod.ctm;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class CTMManager {
    
    public static final String ctmID = "ctm";
    
    private static boolean isinstalled = ModList.get().isLoaded(ctmID);
    
    public static boolean isInstalled() {
        return isinstalled;
    }
    
    private static Field worldField = ObfuscationReflectionHelper.findField(ChunkCache.class, new String[] { "world", "field_72815_e" });
    
    private static Class optifineChunkCache;
    private static Field chunkCacheField;
    
    private static Class regionCacheClass = null;
    private static Field passthrough = null;
    private static Field stateCache = null;
    
    public static LevelAccessor getRealWorld(LevelAccessor world) {
        LevelAccessor realWorld;
        if (regionCacheClass.isInstance(world)) {
            try {
                // Okay I know, this is really really bad, but I don't know how to do it
                // different (while keeping performance at an acceptable level)
                // Incorrect cached values can happen, which results in really weird glitches
                // (sometimes blocks do connect, sometimes not).
                ((Map<BlockPos, BlockState>) stateCache.get(world)).clear();
                
                Object object = passthrough.get(world);
                if (object instanceof LevelAccessor)
                    realWorld = (LevelAccessor) object;
                else
                    realWorld = ((WeakReference<LevelAccessor>) object).get();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return null;
            }
        } else if (optifineChunkCache != null && optifineChunkCache.isInstance(world)) {
            try {
                realWorld = (LevelAccessor) chunkCacheField.get(world);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return null;
            }
        } else if (world instanceof ChunkCache) {
            try {
                realWorld = (LevelAccessor) worldField.get(world);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return null;
            }
        } else
            return world;
        
        return getRealWorld(realWorld);
    }
    
    /** The RegionCache caches incorrect values due to the IBlockAccessFake class,
     * there is only one way to fix and that is to get its parent and to check if
     * it's a fake. Kind of messy, but works */
    public static BlockState getCorrectStateOrigin(LevelAccessor world, BlockPos pos) {
        if (regionCacheClass == null) {
            try {
                regionCacheClass = Class.forName("team.chisel.ctm.client.util.RegionCache");
                passthrough = ObfuscationReflectionHelper.findField(regionCacheClass, "passthrough");
                stateCache = ObfuscationReflectionHelper.findField(regionCacheClass, "stateCache");
                
                optifineChunkCache = Class.forName("net.optifine.override");
                chunkCacheField = ObfuscationReflectionHelper.findField(optifineChunkCache, "chunkCache");
            } catch (ClassNotFoundException e) {
                
            }
        }
        
        return getRealWorld(world).getBlockState(pos);
    }
    
}
