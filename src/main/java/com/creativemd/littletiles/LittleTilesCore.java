package com.creativemd.littletiles;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class LittleTilesCore extends DummyModContainer {
	
	public static final String modid = "littletilescore";
	public static final String version = "1.0.0";
	
	public LittleTilesCore() {
		
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = modid;
		meta.name = "LittleTilesCore";
		meta.version = version; //String.format("%d.%d.%d.%d", majorVersion, minorVersion, revisionVersion, buildVersion);
		meta.credits = "CreativeMD";
		meta.authorList = Arrays.asList("CreativeMD");
		meta.description = "";
		meta.url = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
		//CreativeCore.loadMod();
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		
		bus.register(this);
		//bus.register(CreativeCore.instance);
		return true;
	}
}
