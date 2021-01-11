package com.creativemd.littletiles;

import com.creativemd.creativecore.common.config.api.CreativeConfig;
import com.creativemd.creativecore.common.config.api.ICreativeConfig;
import com.creativemd.creativecore.common.config.sync.ConfigSynchronization;
import com.creativemd.littletiles.client.render.cache.RenderingThread;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.config.LittleGamemodeConfig;
import com.creativemd.littletiles.common.item.ItemLittleBag;
import com.creativemd.littletiles.common.item.ItemMultiTiles;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

public class LittleTilesConfig {
    
    @CreativeConfig(requiresRestart = true)
    public Core core = new Core();
    
    @CreativeConfig
    public General general = new General();
    
    @CreativeConfig
    public LittleGamemodeConfig survival = new LittleGamemodeConfig(true);
    
    @CreativeConfig
    public LittleGamemodeConfig creative = new LittleGamemodeConfig(false);
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Building building = new Building();
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public Rendering rendering = new Rendering();
    
    public LittleGamemodeConfig getConfig(EntityPlayer player) {
        if (player.isCreative())
            return creative;
        return survival;
    }
    
    public boolean isEditLimited(EntityPlayer player) {
        return getConfig(player).limitEditBlocks;
    }
    
    public boolean isPlaceLimited(EntityPlayer player) {
        return getConfig(player).limitPlaceBlocks;
    }
    
    public boolean canEditBlock(EntityPlayer player, IBlockState state, BlockPos pos) {
        return state.getBlock().getHarvestLevel(state) <= getConfig(player).harvestLevelBlock;
    }
    
    public boolean isTransparencyRestricted(EntityPlayer player) {
        return getConfig(player).minimumTransparency > 0;
    }
    
    public boolean isTransparencyEnabled(EntityPlayer player) {
        return getConfig(player).minimumTransparency < 255;
    }
    
    public int getMinimumTransparency(EntityPlayer player) {
        if (player.isCreative())
            return 0;
        return survival.minimumTransparency;
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
        
    }
    
    public static class NotAllowedToConvertBlockException extends LittleActionException {
        
        public LittleGamemodeConfig config;
        
        public NotAllowedToConvertBlockException(EntityPlayer player) {
            super("exception.permission.convert");
            config = LittleTiles.CONFIG.getConfig(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.maxAffectedBlocks);
        }
    }
    
    public static class NotAllowedToEditException extends LittleActionException {
        
        public LittleGamemodeConfig config;
        
        public NotAllowedToEditException(EntityPlayer player) {
            super("exception.permission.edit");
            config = LittleTiles.CONFIG.getConfig(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.maxEditBlocks);
        }
        
    }
    
    public static class NotAllowedToPlaceException extends LittleActionException {
        
        public LittleGamemodeConfig config;
        
        public NotAllowedToPlaceException(EntityPlayer player) {
            super("exception.permission.place");
            config = LittleTiles.CONFIG.getConfig(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.maxPlaceBlocks);
        }
        
    }
    
    public static class NotAllowedToPlaceColorException extends LittleActionException {
        
        public LittleGamemodeConfig config;
        
        public NotAllowedToPlaceColorException(EntityPlayer player) {
            super("exception.permission.place.color");
            config = LittleTiles.CONFIG.getConfig(player);
        }
        
        @Override
        public String getLocalizedMessage() {
            return I18n.translateToLocalFormatted(getMessage(), config.minimumTransparency);
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
            ItemLittleBag.maxStackSizeOfTiles = ItemLittleBag.maxStackSize * LittleGridContext.get().maxTilesPerBlock;
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
        public boolean useQuadCache = false;
        
        @CreativeConfig
        public boolean useCubeCache = true;
        
        @CreativeConfig
        public boolean hideParticleBlock = false;
        
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
        
        @Override
        public void configured() {
            RenderingThread.initThreads(renderingThreadCount);
        }
    }
}
