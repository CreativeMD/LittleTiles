package team.creative.littletiles.common.config;

import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.ICreativeConfig;

public class LittleBagConfig implements ICreativeConfig {
    
    public int colorStorage = 10000000;
    public int inventoryWidth = 6;
    public int inventoryHeight = 4;
    public int inventorySize = inventoryWidth * inventoryHeight;
    
    @Override
    public void configured(Side side) {
        inventorySize = inventoryWidth * inventoryHeight;
    }
    
}
