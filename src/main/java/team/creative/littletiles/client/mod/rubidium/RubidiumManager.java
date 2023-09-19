package team.creative.littletiles.client.mod.rubidium;

import net.minecraftforge.fml.ModList;

public class RubidiumManager {
    
    private static final String[] MODIDS = new String[] { "rubidium", "sodiumforged", "embeddium" };
    private static final boolean INSTALLED = check();
    
    private static boolean check() {
        ModList list = ModList.get();
        for (int i = 0; i < MODIDS.length; i++)
            if (list.isLoaded(MODIDS[i]))
                return true;
        return false;
    }
    
    public static boolean installed() {
        return INSTALLED;
    }
    
    public static void init() {
        if (installed())
            RubidiumInteractor.init();
    }
    
}
