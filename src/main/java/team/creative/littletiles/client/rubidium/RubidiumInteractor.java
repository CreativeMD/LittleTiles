package team.creative.littletiles.client.rubidium;

import net.minecraftforge.fml.ModList;

public class RubidiumInteractor {
    
    private static final String MODID = "rubidium";
    private static final boolean installed = ModList.get().isLoaded(MODID);
    
    public static boolean isInstalled() {
        return installed;
    }
    
}
