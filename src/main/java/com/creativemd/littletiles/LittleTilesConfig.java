package com.creativemd.littletiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.client.render.RenderingThread;
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
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface LittleConfig
	{
		
	}
	
	public static List<String> getConfigProperties()
	{
		List<String> properties = new ArrayList<>();
		loadProperties("", LittleTilesConfig.class, properties);
		return properties;
	}
	
	private static void loadProperties(String category, Class<?> clazz, List<String> properties)
	{
		/*for(Class<?> subClazz : clazz.getClasses())
		{
			Config config = subClazz.getAnnotation(Config.class);
			if(config != null)
				loadProperties(category + (category.isEmpty() ? "" : ".") + config.category(), subClazz, properties);
		}*/
		for(Field field : clazz.getFields())
		{
			Config.Name config = field.getAnnotation(Config.Name.class);
			if(config != null)
			{
				if(field.getType().getAnnotation(LittleConfig.class) != null)
					loadProperties(category + (category.isEmpty() ? "" : ".") + config.value(), field.getType(), properties);
				else
					properties.add(category + "." + config.value());
			}
		}
	}
	
	
	@Config.Name("core")
	@Config.LangKey("config.littletiles.core")
	@Config.RequiresMcRestart
	public static Core core = new Core();
	
	@Config.Name("building")
	@Config.LangKey("config.littletiles.building")
	public static Building building = new Building();
	
	@Config.Name("rendering")
	@Config.LangKey("config.littletiles.rendering")
	public static Rendering rendering = new Rendering();
	
	@Config.RequiresMcRestart
	@LittleConfig
	public static class Core
	{
		
		@Config.Name("defaultSize")
		@Config.RequiresMcRestart
		@Config.Comment("Needs to be part of the row. ATTENTION! This needs be equal for every client & server. Backup your world. This will make your tiles either shrink down or increase in size!")
		@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
		public int defaultSize = 16;
		
		@Config.Name("minSize")
		@Config.RequiresMcRestart
		@Config.Comment("The minimum grid size possible. ATTENTION! This needs be equal for every client & server. Backup your world.")
		@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
		public int minSize = 1;
		
		@Config.Name("scale")
		@Config.RequiresMcRestart
		@Config.Comment("How many grids there are. ATTENTION! This needs be equal for every client & server. Make sure that it is enough for the defaultSize to exist.")
		@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
		public int scale = 6;
		
		@Config.Name("exponent")
		@Config.RequiresMcRestart
		@Config.Comment("minSize ^ (exponent * scale). ATTENTION! This needs be equal for every client & server. Default is two -> (1, 2, 4, 8, 16, 32 etc.).")
		@Config.RangeInt(min = 2, max = Integer.MAX_VALUE)
		public int exponent = 2;
	}
	
	@LittleConfig
	public static class Building
	{
		
		@Config.Name("invertStickToGrid")
		@Config.LangKey("config.littletiles.invertStickToGrid")
		@Config.Comment("Whether tiles should stick to the vanilla grid when second mode is enabled or disabled.")
		public boolean invertStickToGrid = false;
		
		@Config.Name("maxSavedActions")
		@Config.LangKey("config.littletiles.maxSavedActions")
		@Config.RangeInt(min = 1)
		@Config.Comment("Number of actions which can be reverted.")
		public int maxSavedActions = 32;
		
		@Config.Name("useALTForEverything")
		@Config.LangKey("config.littletiles.useALTForEverything")
		@Config.Comment("Second mode will be activated if 'ALT' is pressed (default: sneaking).")
		public boolean useALTForEverything = false;
		
		@Config.Name("useAltWhenFlying")
		@Config.LangKey("config.littletiles.useAltWhenFlying")
		@Config.Comment("Press 'ALT' when flying instead of sneaking to activate second mode.")
		public boolean useAltWhenFlying = true;
	}
	
	@LittleConfig
	public static class Rendering
	{
		
		@Config.Name("hideParticleBlock")
		@Config.LangKey("config.littletiles.hideParticleBlock")
		@Config.Comment("Whether the particle block is visible or not")
		public boolean hideParticleBlock = false;
		
		@Config.Name("renderingThreadCount")
		@Config.LangKey("config.littletiles.renderingThreadCount")
		@Config.Comment("Number of threads for rendering blocks")
		public int renderingThreadCount = 2;
		
	}
	
	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(LittleTiles.modid)) {
			ConfigManager.sync(LittleTiles.modid, Config.Type.INSTANCE);
			RenderingThread.initThreads(rendering.renderingThreadCount);
		}
	}
}
