package com.creativemd.littletiles.common.mods.coloredlights;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ColoredLightsManager {
	
	public static final String coloredlightsID = "coloredlights";
	
	private static boolean isinstalled = Loader.isModLoaded(coloredlightsID);
	
	public static boolean isInstalled() {
		return isinstalled;
	}
	
	private static Block invertedColorsBlock;
	
	public static Block getInvertedColorsBlock() {
		if (invertedColorsBlock == null)
			invertedColorsBlock = Block.REGISTRY.getObject(new ResourceLocation(coloredlightsID, "coloredLampInverted"));
		return invertedColorsBlock;
	}
	
	private static Method getHex;
	
	public static int getColorFromBlock(IBlockState state) {
		for (IProperty<?> property : state.getPropertyKeys()) {
			if (property.getName().equals("color")) {
				Object value = state.getValue(property);
				if (getHex == null)
					getHex = ReflectionHelper.findMethod(value.getClass(), "getHex", "getHex");
				try {
					return (int) getHex.invoke(value);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return ColorUtils.WHITE;
	}
	
}
