package com.creativemd.littletiles.common.mod.ctm;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CTMManager {
    
    public static final String ctmID = "ctm";
    
    private static boolean isinstalled = Loader.isModLoaded(ctmID);
    
    public static boolean isInstalled() {
        return isinstalled;
    }
    
    private static Field worldField = ReflectionHelper.findField(ChunkCache.class, new String[] { "world", "field_72815_e" });
    
    private static Class optifineChunkCache;
    private static Field chunkCacheField;
    
    private static Class regionCacheClass = null;
    private static Field passthrough = null;
    private static Field stateCache = null;
    
    public static IBlockAccess getRealWorld(IBlockAccess world) {
        IBlockAccess realWorld;
        if (regionCacheClass.isInstance(world)) {
            try {
                // Okay I know, this is really really bad, but I don't know how to do it
                // different (while keeping performance at an acceptable level)
                // Incorrect cached values can happen, which results in really weird glitches
                // (sometimes blocks do connect, sometimes not).
                ((Map<BlockPos, IBlockState>) stateCache.get(world)).clear();
                
                Object object = passthrough.get(world);
                if (object instanceof IBlockAccess)
                    realWorld = (IBlockAccess) object;
                else
                    realWorld = ((WeakReference<IBlockAccess>) object).get();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return null;
            }
        } else if (optifineChunkCache != null && optifineChunkCache.isInstance(world)) {
            try {
                realWorld = (IBlockAccess) chunkCacheField.get(world);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return null;
            }
        } else if (world instanceof ChunkCache) {
            try {
                realWorld = (IBlockAccess) worldField.get(world);
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
    public static IBlockState getCorrectStateOrigin(IBlockAccess world, BlockPos pos) {
        if (regionCacheClass == null) {
            try {
                regionCacheClass = Class.forName("team.chisel.ctm.client.util.RegionCache");
                passthrough = ReflectionHelper.findField(regionCacheClass, "passthrough");
                stateCache = ReflectionHelper.findField(regionCacheClass, "stateCache");
                
                optifineChunkCache = Class.forName("net.optifine.override");
                chunkCacheField = ReflectionHelper.findField(optifineChunkCache, "chunkCache");
            } catch (ClassNotFoundException e) {
                
            }
        }
        
        return getRealWorld(world).getBlockState(pos);
    }
    
}
