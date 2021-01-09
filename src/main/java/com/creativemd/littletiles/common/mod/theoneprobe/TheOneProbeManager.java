package com.creativemd.littletiles.common.mod.theoneprobe;

import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class TheOneProbeManager {
    
    public static final String modid = "theoneprobe";
    
    private static boolean isinstalled = Loader.isModLoaded(modid);
    
    public static boolean isInstalled() {
        return isinstalled;
    }
    
    public static void init() {
        if (!isInstalled())
            return;
        
        initDirectly();
        
    }
    
    @Method(modid = modid)
    private static void initDirectly() {
        try {
            ITheOneProbe theoneprobe = (ITheOneProbe) ReflectionHelper.findField(Class.forName("mcjty.theoneprobe.TheOneProbe"), "theOneProbeImp").get(null);
            TheOneProbeInteractor interactor = new TheOneProbeInteractor();
            theoneprobe.registerBlockDisplayOverride(interactor);
            theoneprobe.registerEntityDisplayOverride(interactor);
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
