package team.creative.littletiles.common.mod.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import team.creative.littletiles.LittleTilesRegistry;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.hideTarget(LittleTilesRegistry.BLOCK_TILES.value());
        registration.hideTarget(LittleTilesRegistry.BLOCK_TILES_RENDERED.value());
        registration.hideTarget(LittleTilesRegistry.BLOCK_TILES_TICKING.value());
        registration.hideTarget(LittleTilesRegistry.BLOCK_TILES_TICKING_RENDERED.value());
    }
}
