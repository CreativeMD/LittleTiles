package com.creativemd.littletiles.common.config;

import com.creativemd.creativecore.common.config.api.CreativeConfig;

public class LittleGamemodeConfig {
	
	public LittleGamemodeConfig(boolean survival) {
		limitAffectedBlocks = survival;
		editUnbreakable = !survival;
		minimumTransparency = survival ? 255 : 0;
		limitEditBlocks = survival;
		limitPlaceBlocks = survival;
		harvestLevelBlock = survival ? 1 : 4;
	}
	
	@CreativeConfig
	public boolean limitAffectedBlocks;
	
	@CreativeConfig
	public int maxAffectedBlocks = 2;
	
	@CreativeConfig
	public int harvestLevelBlock;
	
	@CreativeConfig
	public boolean editUnbreakable;
	
	@CreativeConfig
	@CreativeConfig.IntRange(min = 0, max = 255)
	public int minimumTransparency;
	
	@CreativeConfig
	public boolean limitEditBlocks;
	@CreativeConfig
	public int maxEditBlocks = 10;
	
	@CreativeConfig
	public boolean limitPlaceBlocks;
	@CreativeConfig
	public int maxPlaceBlocks = 10;
	
}
