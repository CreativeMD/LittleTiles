package team.creative.littletiles.common.action;

import team.creative.littletiles.LittleTiles;

public class LittleActionRegistry {
    
    public static void register(Class<? extends LittleAction> clazz) {
        LittleTiles.NETWORK.registerType(clazz);
    }
    
    public static void register(Class<? extends LittleAction>... classTypes) {
        for (int i = 0; i < classTypes.length; i++)
            LittleTiles.NETWORK.registerType(classTypes[i]);
    }
    
}
