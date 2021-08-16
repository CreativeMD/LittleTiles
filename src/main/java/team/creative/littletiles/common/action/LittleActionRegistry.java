package team.creative.littletiles.common.action;

import team.creative.littletiles.LittleTiles;

public class LittleActionRegistry {
    
    public static void registerLittleAction(Class<? extends LittleAction>... classTypes) {
        for (int i = 0; i < classTypes.length; i++)
            LittleTiles.NETWORK.registerType(classTypes[i]);
    }
    
}
