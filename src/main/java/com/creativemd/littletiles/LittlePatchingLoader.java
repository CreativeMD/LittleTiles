package com.creativemd.littletiles;

import java.io.File;
import java.util.Map;

import com.creativemd.creativecore.transformer.TransformerNames;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.DependsOn;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@DependsOn(value = {"creativecore"})
public class LittlePatchingLoader implements IFMLLoadingPlugin {
	
	public static File location;

	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{LittleTilesTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return LittleTilesCore.class.getName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
		TransformerNames.obfuscated = (boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {
		return LittleTilesAfterTransformer.class.getName();
	}

}
