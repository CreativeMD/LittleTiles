package com.creativemd.littletiles.common.config;

import com.creativemd.creativecore.common.config.api.CreativeConfig;

public class LittleGamemodeConfig {
	
	public boolean survival;
	
	public LittleGamemodeConfig(boolean survival) {
		this.survival = survival;
	}
	
	@CreativeConfig
	public boolean limitAffectedBlocks = survival;
	
	@CreativeConfig
	public int maxAffectedBlocks = 2;
	
	@CreativeConfig
	public int harvestLevelBlock = 1;
	
	@CreativeConfig
	public boolean editUnbreakable = !survival;
	
	@CreativeConfig
	@CreativeConfig.IntRange(min = 0, max = 255)
	public int minimumTransparency = survival ? 255 : 0;
	
	@CreativeConfig
	public boolean limitEditBlocks = survival;
	@CreativeConfig
	public int maxEditBlocks = 10;
	
	@CreativeConfig
	public boolean limitPlaceBlocks = survival;
	@CreativeConfig
	public int maxPlaceBlocks = 10;
	
}
