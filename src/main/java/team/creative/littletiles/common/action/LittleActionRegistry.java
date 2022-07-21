package team.creative.littletiles.common.action;

import java.util.function.Supplier;

import team.creative.littletiles.LittleTiles;

public class LittleActionRegistry {
    
    public static <T extends LittleAction> void register(Class<T> clazz, Supplier<T> supplier) {
        LittleTiles.NETWORK.registerType(clazz, supplier);
    }
    
}
