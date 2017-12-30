package com.creativemd.littletiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
		
		@Config.Name("gridSize")
		@Config.RequiresMcRestart
		@Config.Comment("ATTENTION! This needs be equal for every client & server. Backup your world. This will make your tiles either shrink down or increase in size!")
		@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
		public int gridSize = 16;
	}
	
	@LittleConfig
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
	
	@LittleConfig
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
