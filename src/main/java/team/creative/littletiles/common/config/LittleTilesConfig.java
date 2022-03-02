package team.creative.littletiles.common.config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.config.premade.Permission;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.RenderingThread;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleBag;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class LittleTilesConfig {
    
    @CreativeConfig(requiresRestart = true)
    public Core core = new Core();
    
    @CreativeConfig
    public General general = new General();
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Building building = new Building();
    
    @CreativeConfig
    public Permission<LittleBuildingConfig> build = new Permission<LittleBuildingConfig>(new LittleBuildingConfig()).add("survival", new LittleBuildingConfig(true))
            .add("creative", new LittleBuildingConfig(false));
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Rendering rendering = new Rendering();
    
    public boolean isEditLimited(Player player) {
        return build.get(player).limitEditBlocks;
    }
    
    public boolean isPlaceLimited(Player player) {
        return build.get(player).limitPlaceBlocks;
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
        
        public NotAllowedToConvertBlockException(Player player) {
            super("exception.permission.convert");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.maxAffectedBlocks);
        }
    }
    
    public static class NotAllowedToEditException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToEditException(Player player) {
            super("exception.permission.edit");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.maxEditBlocks);
        }
        
    }
    
    public static class NotAllowedToPlaceException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToPlaceException(Player player) {
            super("exception.permission.place");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return LanguageUtils.translate(getMessage(), config.maxPlaceBlocks);
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
        
        public NotAllowedToPlaceColorException(Player player) {
            super("exception.permission.place.color");
            config = LittleTiles.CONFIG.build.get(player);
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
    
    public static class Rendering implements ICreativeConfig {
        
        @CreativeConfig
        public boolean hideMipmapWarning = false;
        
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
        
        @Override
        public void configured(Dist side) {
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
        
        @CreativeConfig
        public int defaultSelectedGrid = 16;
        
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
        public int base = 1;
        
        @CreativeConfig
        public int scale = 6;
        
        @CreativeConfig
        public int exponent = 2;
        
        @Override
        public void configured(Dist side) {
            LittleGrid.loadGrid(base, scale, exponent, LittleTiles.CONFIG.general.defaultSelectedGrid);
            ItemMultiTiles.currentContext = LittleGrid.defaultGrid();
            ItemLittleBag.maxStackSizeOfTiles = ItemLittleBag.maxStackSize * LittleGrid.overallDefault().count3d;
            LittleStructurePremade.reloadPremadeStructures();
            ItemMultiTiles.reloadExampleStructures();
        }
    }
    
}
