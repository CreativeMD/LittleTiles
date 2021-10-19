package com.creativemd.littletiles;

import com.creativemd.creativecore.common.config.api.CreativeConfig;
import com.creativemd.creativecore.common.config.api.ICreativeConfig;
import com.creativemd.creativecore.common.config.premade.Permission;
import com.creativemd.creativecore.common.config.sync.ConfigSynchronization;
import com.creativemd.littletiles.client.render.cache.RenderingThread;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.config.LittleBuildingConfig;
import com.creativemd.littletiles.common.item.ItemLittleBag;
import com.creativemd.littletiles.common.item.ItemMultiTiles;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;

public class LittleTilesConfig {
    
    @CreativeConfig(requiresRestart = true)
    public Core core = new Core();
    
    @CreativeConfig
    public General general = new General();
    
    @CreativeConfig
    public Permission<LittleBuildingConfig> build = new Permission<LittleBuildingConfig>(new LittleBuildingConfig()).add("survival", new LittleBuildingConfig(true))
        .add("creative", new LittleBuildingConfig(false));
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Building building = new Building();
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Rendering rendering = new Rendering();
    
    public boolean isEditLimited(EntityPlayer player) {
        return build.get(player).limitEditBlocks;
    }
    
    public boolean isPlaceLimited(EntityPlayer player) {
        return build.get(player).limitPlaceBlocks;
    }
    
    public boolean canEditBlock(EntityPlayer player, IBlockState state, BlockPos pos) {
        return state.getBlock().getHarvestLevel(state) <= build.get(player).harvestLevelBlock;
    }
    
    public boolean isTransparencyRestricted(EntityPlayer player) {
        return build.get(player).minimumTransparency > 0;
    }
    
    public boolean isTransparencyEnabled(EntityPlayer player) {
        return build.get(player).minimumTransparency < 255;
    }
    
    public int getMinimumTransparency(EntityPlayer player) {
        return build.get(player).minimumTransparency;
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
        public boolean allowConverationToChiselsAndBits = true;
        
    }
    
    public static class NotAllowedToConvertBlockException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToConvertBlockException(EntityPlayer player) {
            super("exception.permission.convert");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.maxAffectedBlocks);
        }
    }
    
    public static class NotAllowedToEditException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToEditException(EntityPlayer player) {
            super("exception.permission.edit");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.maxEditBlocks);
        }
        
    }
    
    public static class NotAllowedToPlaceException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToPlaceException(EntityPlayer player) {
            super("exception.permission.place");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.maxPlaceBlocks);
        }
        
    }
    
    public static class TooDenseException extends LittleActionException {
        
        public TooDenseException() {
            super("exception.permission.density");
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), LittleTiles.CONFIG.general.maxAllowedDensity);
        }
        
    }
    
    public static class NotAllowedToPlaceColorException extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public NotAllowedToPlaceColorException(EntityPlayer player) {
            super("exception.permission.place.color");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.minimumTransparency);
        }
        
    }
    
    public static class AreaProtected extends LittleActionException {
        
        public AreaProtected() {
            super("exception.permission.area-protected");
        }
        
    }
    
    public static class AreaTooLarge extends LittleActionException {
        
        public LittleBuildingConfig config;
        
        public AreaTooLarge(EntityPlayer player) {
            super("exception.permission.recipe.size");
            config = LittleTiles.CONFIG.build.get(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.recipeBlocksLimit);
        }
        
    }
    
    public static class Core implements ICreativeConfig {
        
        @CreativeConfig
        public int defaultSize = 16;
        
        @CreativeConfig
        public int minSize = 1;
        
        @CreativeConfig
        public int scale = 6;
        
        @CreativeConfig
        public int exponent = 2;
        
        @CreativeConfig
        public boolean forceToSaveDefaultSize = false;
        
        @Override
        public void configured() {
            LittleGridContext.loadGrid(minSize, defaultSize, scale, exponent);
            ItemMultiTiles.currentContext = LittleGridContext.get();
            ItemLittleBag.maxStackSizeOfTiles = (int) (ItemLittleBag.maxStackSize * LittleGridContext.get().maxTilesPerBlock);
            LittleStructurePremade.reloadPremadeStructures();
            ItemMultiTiles.reloadExampleStructures();
        }
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
    
    public static class Rendering implements ICreativeConfig {
        
        @CreativeConfig
        public boolean hideVBOWarning = false;
        
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
        public boolean enhancedResorting = true;
        
        @Override
        public void configured() {}
        
        @Override
        public void configured(Side side) {
            if (side.isClient())
                RenderingThread.initThreads(renderingThreadCount);
        }
    }
}
