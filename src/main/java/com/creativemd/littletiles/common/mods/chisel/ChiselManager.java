package com.creativemd.littletiles.common.mods.chisel;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.world.IBlockAccessFake;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ChiselManager {
	
	public static final String chiselID = "chisel";
	
	private static boolean isinstalled = Loader.isModLoaded(chiselID);

	public static boolean isInstalled()
	{
		return isinstalled;
	}
	
	private static Class regionCacheClass = null;
	private static Field passthrough = null;
	private static Field stateCache = null;
	
	/**
	 * The RegionCache caches incorrect values due to the IBlockAccessFake class, there is only one way to fix and that is to get its parent and to check if it's a fake. Kind of messy, but works
	 */
	public static IBlockState getCorrectStateOrigin(IBlockAccess world, BlockPos pos)
	{
		try {
			if(regionCacheClass == null)
			{
				regionCacheClass = Class.forName("team.chisel.ctm.client.util.RegionCache");
				passthrough = ReflectionHelper.findField(regionCacheClass, "passthrough");
				stateCache = ReflectionHelper.findField(regionCacheClass, "stateCache");
			}
			
			if(regionCacheClass.isInstance(world))
			{
				// Okay I know, this is really really bad, but I don't know how to do it different (while keeping performance at an acceptable level)
				// Incorrect cached values can happen, which results in really weird glitches (sometimes blocks do connect, sometimes not).
				((Map<BlockPos, IBlockState>) stateCache.get(world)).clear();
				
				IBlockAccess realWorld;
				Object object = passthrough.get(world);
				if(object instanceof IBlockAccess)
					realWorld = (IBlockAccess) object;
				else
					realWorld = ((WeakReference<IBlockAccess>) object).get();
				//if(realWorld instanceof IBlockAccessFake && ((IBlockAccessFake) realWorld).pos.equals(pos))
					//return ((IBlockAccessFake) realWorld).fakeState;
				return realWorld.getBlockState(pos);
			}			
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return world.getBlockState(pos);
	}
	
	
}
