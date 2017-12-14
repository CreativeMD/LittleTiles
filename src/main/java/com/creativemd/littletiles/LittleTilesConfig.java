package com.creativemd.littletiles;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = LittleTiles.modid, category = "")
@Mod.EventBusSubscriber
public class LittleTilesConfig {
	
	@Config.Name("core")
	@Config.LangKey("config.littletiles.core")
	public static Core core = new Core();
	
	@Config.Name("building")
	@Config.LangKey("config.littletiles.building")
	public static Building building = new Building();
	
	@Config.Name("rendering")
	@Config.LangKey("config.littletiles.rendering")
	public static Rendering rendering = new Rendering();
	
	@Config.RequiresMcRestart
	public static class Core
	{
		
			
	}
	
	public static class Building
	{
		
		@Config.Name("invertedShift")
		@Config.LangKey("config.littletiles.invertedShift")
		@Config.Comment("If shift behavior is inverted.")
		public boolean invertedShift = false;
		
		@Config.Name("maxSavedActions")
		@Config.LangKey("config.littletiles.maxSavedActions")
		@Config.RangeInt(min = 1)
		@Config.Comment("Number of actions which can be reverted")
		public int maxSavedActions = 32;
		
		@Config.Name("useALT")
		@Config.LangKey("config.littletiles.useALT")
		@Config.Comment("If true you will not have to sneak but instead hold ALT.")
		public boolean useALT = false;
		
		@Config.Name("onlyChangeWhenFlying")
		@Config.LangKey("config.littletiles.onlyChangeWhenFlying")
		@Config.Comment("Only change shift behavior when flying.")
		public boolean onlyChangeWhenFlying = true;
		
		@Config.Name("allowSneaking")
		@Config.LangKey("config.littletiles.allowSneaking")
		@Config.Comment("If useAlt is enable it will allow sneaking as well.")
		public boolean allowSneaking = false;
	}
	
	public static class Rendering
	{
		
		@Config.Name("hideParticleBlock")
		@Config.LangKey("config.littletiles.hideParticleBlock")
		@Config.Comment("Whether the particle block is visible or not")
		public boolean hideParticleBlock = false;
		
	}
	
	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(LittleTiles.modid)) {
			ConfigManager.sync(LittleTiles.modid, Config.Type.INSTANCE);
		}
	}
}
