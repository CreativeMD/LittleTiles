package team.creative.littletiles.client.mod.rubidium;

import net.minecraftforge.fml.ModList;

public class RubidiumManager {
    
    private static final String MODID = "rubidium";
    private static final boolean INSTALLED = ModList.get().isLoaded(MODID);
    
    public static boolean installed() {
        return INSTALLED;
    }
    
    public static void init() {
        if (installed())
            RubidiumInteractor.init();
    }
    
}
