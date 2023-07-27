package com.creativemd.littletiles.common.mod.coloredlights;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColoredLightsManager {
    
    public static final String coloredlightsId = "coloredlights";
    
    private static boolean isinstalled = Loader.isModLoaded(coloredlightsId);
    
    public static boolean isInstalled() {
        return isinstalled;
    }
    
    private static Block invertedColorsBlock;
    
    public static boolean isBlockFromColoredBlocks(Block block) {
        if (block.getRegistryName() == null)
            return false;
        return coloredlightsId.equals(block.getRegistryName().getNamespace()) && ("coloredLamp"
            .equalsIgnoreCase(block.getRegistryName().getPath()) || "coloredLampInverted".equalsIgnoreCase(block.getRegistryName().getPath()));
    }
    
    public static Block getInvertedColorsBlock() {
        if (invertedColorsBlock == null)
            invertedColorsBlock = Block.REGISTRY.getObject(new ResourceLocation(coloredlightsId, "coloredLampInverted"));
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
