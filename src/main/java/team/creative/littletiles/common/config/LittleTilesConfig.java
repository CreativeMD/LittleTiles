package team.creative.littletiles.common.config;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.config.premade.Permission;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;

public class LittleTilesConfig {
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public LittleConfigBuilding building = new LittleConfigBuilding();
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public LittleConfigRendering rendering = new LittleConfigRendering();
    
    @CreativeConfig(requiresRestart = true)
    public Core core = new Core();
    
    @CreativeConfig
    public General general = new General();
    
    @CreativeConfig
    public Permission<LittlePermissionBuild> build = new Permission<LittlePermissionBuild>(new LittlePermissionBuild()).add("survival", new LittlePermissionBuild(true)).add(
        "creative", new LittlePermissionBuild(false));
    
    @CreativeConfig
    public Permission<LittlePermissionInteract> interact = new Permission<LittlePermissionInteract>(new LittlePermissionInteract());
    
    @CreativeConfig
    public LittleConfigSignal signal = new LittleConfigSignal();
    
    public boolean isEditLimited(Player player) {
        return build.get(player).editBlockLimit.isEnabled();
    }
    
    public boolean isPlaceLimited(Player player) {
        return build.get(player).placeBlockLimit.isEnabled();
    }
    
    public boolean canEditBlock(Player player, BlockState state, BlockPos pos) {
        return build.get(player).harvestLevelBlock.is(state.getBlock());
    }
    
    public boolean isTransparencyRestricted(Player player) {
        return build.get(player).minimumTransparency > 0;
    }
    
    public boolean isTransparencyEnabled(Player player) {
        return build.get(player).minimumTransparency < 255;
    }
    
    public int getMinimumTransparency(Player player) {
        return build.get(player).minimumTransparency;
    }
    
    public static class General {
        
        @CreativeConfig
        public double storagePerPixel = 1;
        
        @CreativeConfig
        public boolean enableAnimationCollision = true;
        @CreativeConfig
        public boolean enableCollisionMotion = true;
        
        @CreativeConfig
        public int alphaBlockVolume = 255;
        
        @CreativeConfig
        public float dyeVolume = 2;
        
        @CreativeConfig
        public int maxAllowedDensity = 2048;
        
        @CreativeConfig
        public int maxDoorDistance = 512;
        
        @CreativeConfig
        public double maxDoorRotation = 1440;
        
        @CreativeConfig
        public double maxParticlesPerSecond = 200;
        
        @CreativeConfig
        public LittleConfigBag bag = new LittleConfigBag();
        
        @CreativeConfig
        public int messageStructureLength = 4098;
        
    }
    
    public static class Core implements ICreativeConfig {
        
        @CreativeConfig
        public int highest = 32;
        
        @Override
        public void configured(Side side) {
            LittleGrid.configure(highest);
            if (side.isClient())
                configuredClient();
            LittlePremadeRegistry.reload();
        }
        
        @OnlyIn(Dist.CLIENT)
        private void configuredClient() {
            Minecraft mc = Minecraft.getInstance();
            LittleTilesClient.ACTION_HANDLER.setting.refreshGrid(mc.player);
            ItemMultiTiles.reloadExampleStructures(mc.getResourceManager());
        }
    }
    
}
