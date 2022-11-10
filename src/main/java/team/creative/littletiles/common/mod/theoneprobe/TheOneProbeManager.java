package team.creative.littletiles.common.mod.theoneprobe;

import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.ModList;
import team.creative.creativecore.reflection.ReflectionHelper;

public class TheOneProbeManager {
    
    public static final String modid = "theoneprobe";
    
    private static boolean isinstalled = ModList.get().isLoaded(modid);
    
    public static boolean isInstalled() {
        return isinstalled;
    }
    
    public static void init() {
        if (!isInstalled())
            return;
        
        initDirectly();
    }
    
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
