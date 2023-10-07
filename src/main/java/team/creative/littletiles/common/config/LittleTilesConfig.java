package team.creative.littletiles.common.config;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.config.premade.Permission;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.build.RenderingThread;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleBag;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;

public class LittleTilesConfig {
    
    @CreativeConfig(requiresRestart = true)
    public Core core = new Core();
    
    @CreativeConfig
    public General general = new General();
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Building building = new Building();
    
    @CreativeConfig
    public Permission<LittleBuildingConfig> build = new Permission<LittleBuildingConfig>(new LittleBuildingConfig()).add("survival", new LittleBuildingConfig(true)).add("creative",
        new LittleBuildingConfig(false));
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Rendering rendering = new Rendering();
    
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
    
    public static class NotAllowedToConvertBlockException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToConvertBlockException(Player player, LittleBuildingConfig config) {
            super("exception.permission.convert");
            this.config = config;
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.affectedBlockLimit.value);
        }
    }
    
    public static class NotAllowedToEditException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToEditException(Player player, LittleBuildingConfig config) {
            super("exception.permission.edit");
            this.config = config;
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.editBlockLimit);
        }
        
    }
    
    public static class NotAllowedToPlaceException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToPlaceException(Player player, LittleBuildingConfig config) {
            super("exception.permission.place");
            this.config = config;
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.placeBlockLimit);
        }
        
    }
    
    public static class TooDenseException extends LittleActionException {
        
        public TooDenseException() {
            super("exception.permission.density");
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), LittleTiles.CONFIG.general.maxAllowedDensity);
        }
        
    }
    
    public static class NotAllowedToPlaceColorException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToPlaceColorException(Player player, LittleBuildingConfig config) {
            super("exception.permission.place.color");
            this.config = config;
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.minimumTransparency);
        }
        
    }
    
    public static class AreaProtected extends LittleActionException {
        
        public AreaProtected() {
            super("exception.permission.area-protected");
        }
        
    }
    
    public static class AreaTooLarge extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public AreaTooLarge(Player player, LittleBuildingConfig config) {
            super("exception.permission.recipe.size");
            this.config = config;
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.blueprintSizeLimit);
        }
        
    }
    
    public static class GridTooHighException extends LittleActionException {
        
        public LittleBuildingConfig config;
        public int attempted;
        
        public GridTooHighException(Player player, LittleBuildingConfig config, int attempted) {
            super("exception.permission.grid");
            this.config = config;
            this.attempted = attempted;
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), attempted, config.gridLimit.value);
        }
        
    }
    
    public static class Rendering implements ICreativeConfig {
        
        @CreativeConfig
        public boolean useQuadCache = false;
        
        @CreativeConfig
        public boolean useCubeCache = true;
        
        @CreativeConfig
        public int renderingThreadCount = 2;
        
        @CreativeConfig
        public boolean highlightStructureBox = true;
        
        @CreativeConfig
        public boolean previewLines = false;
        
        @CreativeConfig
        public double previewLineThickness = 2;
        
        @CreativeConfig
        public boolean enableRandomDisplayTick = false;
        
        @CreativeConfig
        public boolean uploadToVBODirectly = true;
        
        @CreativeConfig
        public boolean showTooltip = true;
        
        @CreativeConfig
        public int itemCacheDuration = 5000;
        
        @CreativeConfig
        public int entityCacheBuildThreads = 1;
        
        @Override
        public void configured(Side side) {
            if (side.isClient())
                RenderingThread.initThreads(renderingThreadCount);
        }
    }
    
    public static class General {
        
        @CreativeConfig
        public boolean allowFlowingWater = true;
        @CreativeConfig
        public boolean allowFlowingLava = true;
        
        @CreativeConfig
        public float storagePerPixel = 1;
        
        @CreativeConfig
        public boolean enableBed = true;
        
        @CreativeConfig
        public boolean enableAnimationCollision = true;
        @CreativeConfig
        public boolean enableCollisionMotion = true;
        
        @CreativeConfig
        public float dyeVolume = 2;
        
        @CreativeConfig
        public int maxAllowedDensity = 2048;
        
        @CreativeConfig
        public int maxDoorDistance = 512;
        
    }
    
    public static class Building {
        
        @CreativeConfig
        public boolean invertStickToGrid = false;
        
        @CreativeConfig
        public int maxSavedActions = 32;
        
        @CreativeConfig
        public boolean useALTForEverything = false;
        
        @CreativeConfig
        public boolean useAltWhenFlying = true;
    }
    
    public static class Core implements ICreativeConfig {
        
        @CreativeConfig
        public int highest = 32;
        
        @Override
        public void configured(Side side) {
            LittleGrid.configure(highest);
            if (side.isClient())
                configuredClient();
            ItemLittleBag.maxStackSizeOfTiles = (int) (ItemLittleBag.maxStackSize * LittleGrid.overallDefault().count3d);
            LittlePremadeRegistry.reload();
            ItemMultiTiles.reloadExampleStructures();
        }
        
        @OnlyIn(Dist.CLIENT)
        private void configuredClient() {
            LittleTilesClient.ACTION_HANDLER.setting.refreshGrid(Minecraft.getInstance().player);
        }
    }
    
}
