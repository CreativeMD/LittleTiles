package com.creativemd.littletiles;

import com.creativemd.creativecore.common.config.api.CreativeConfig;
import com.creativemd.creativecore.common.config.api.ICreativeConfig;
import com.creativemd.creativecore.common.config.sync.ConfigSynchronization;
import com.creativemd.littletiles.client.render.cache.RenderingThread;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.item.ItemBag;
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
	public Survival survival = new Survival();
	
	@CreativeConfig(type = ConfigSynchronization.CLIENT)
	public Building building = new Building();
	
	@CreativeConfig(type = ConfigSynchronization.CLIENT)
	public Rendering rendering = new Rendering();
	
	public boolean isEditLimited(EntityPlayer player) {
		if (survival.limitEditBlocksSurvival)
			return !player.isCreative();
		return false;
	}
	
	public boolean isPlaceLimited(EntityPlayer player) {
		if (survival.limitPlaceBlocksSurvival)
			return !player.isCreative();
		return false;
	}
	
	public boolean canEditBlock(EntityPlayer player, IBlockState state, BlockPos pos) {
		if (!player.isCreative())
			return state.getBlock().getHarvestLevel(state) > survival.highestHarvestTierSurvival;
		return true;
	}
	
	public boolean isTransparencyRestricted(EntityPlayer player) {
		if (player.isCreative())
			return false;
		return survival.minimumTransparency > 0;
	}
	
	public boolean isTransparencyEnabled(EntityPlayer player) {
		return player.isCreative() || survival.minimumTransparency < 255;
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
		
	}
	
	public static class Survival {
		
		@CreativeConfig
		public boolean strictMining = false;
		
		@CreativeConfig
		public boolean limitEditBlocksSurvival = false;
		@CreativeConfig
		public int maxEditBlocks = 10;
		
		@CreativeConfig
		public int highestHarvestTierSurvival = 10;
		
		@CreativeConfig
		public boolean editUnbreakable = false;
		
		@CreativeConfig
		public boolean limitPlaceBlocksSurvival = false;
		@CreativeConfig
		public int maxPlaceBlocks = 10;
		
		@CreativeConfig
		@CreativeConfig.IntRange(min = 0, max = 255)
		public int minimumTransparency = 255;
		
		@CreativeConfig
		public float dyeVolume = 2;
	}
	
	public static class NotAllowedToEditException extends LittleActionException {
		
		public NotAllowedToEditException() {
			super("exception.permission.edit");
		}
		
		@Override
		public String getLocalizedMessage() {
			return I18n.translateToLocalFormatted(getMessage(), LittleTiles.CONFIG.survival.maxEditBlocks);
		}
		
	}
	
	public static class NotAllowedToPlaceException extends LittleActionException {
		
		public NotAllowedToPlaceException() {
			super("exception.permission.place");
		}
		
		@Override
		public String getLocalizedMessage() {
			return I18n.translateToLocalFormatted(getMessage(), LittleTiles.CONFIG.survival.maxPlaceBlocks);
		}
		
	}
	
	public static class NotAllowedToPlaceColorException extends LittleActionException {
		
		public NotAllowedToPlaceColorException() {
			super("exception.permission.place.color");
		}
		
		@Override
		public String getLocalizedMessage() {
			return I18n.translateToLocalFormatted(getMessage(), LittleTiles.CONFIG.survival.minimumTransparency);
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
			ItemBag.maxStackSizeOfTiles = ItemBag.maxStackSize * LittleGridContext.get().maxTilesPerBlock;
			LittleStructurePremade.reloadPremadeStructures();
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
		public boolean enableRandomDisplayTick = false;
		
		@CreativeConfig
		public boolean uploadToVBODirectly = true;
		
		@Override
		public void configured() {
			RenderingThread.initThreads(renderingThreadCount);
		}
	}
}
