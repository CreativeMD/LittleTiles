package com.creativemd.littletiles.client.render;

import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCubeLayerCache {
	
	private List<LittleRenderingCube> solid;
	private List<LittleRenderingCube> cutout_mipped;
	private List<LittleRenderingCube> cutout;
	private List<LittleRenderingCube> translucent;
	
	public List<LittleRenderingCube> getCubesByLayer(BlockRenderLayer layer) {
		switch (layer) {
		case SOLID:
			return solid;
		case CUTOUT_MIPPED:
			return cutout_mipped;
		case CUTOUT:
			return cutout;
		case TRANSLUCENT:
			return translucent;
		}
		return null;
	}
	
	public void setCubesByLayer(List<LittleRenderingCube> cubes, BlockRenderLayer layer) {
		switch (layer) {
		case SOLID:
			solid = cubes;
			break;
		case CUTOUT_MIPPED:
			cutout_mipped = cubes;
			break;
		case CUTOUT:
			cutout = cubes;
			break;
		case TRANSLUCENT:
			translucent = cubes;
			break;
		}
	}
	
	public boolean doesNeedUpdate() {
		return solid == null || cutout_mipped == null || cutout == null || translucent == null;
	}
	
	public void clearCache() {
		solid = null;
		cutout_mipped = null;
		cutout = null;
		translucent = null;
	}
	
	public void sortCache() {
		if (!OptifineHelper.isActive())
			return;
		
		for (Iterator iterator = solid.iterator(); iterator.hasNext();) {
			LittleRenderingCube littleRenderingCube = (LittleRenderingCube) iterator.next();
			if (littleRenderingCube.isEmissive) {
				cutout_mipped.add(littleRenderingCube);
				iterator.remove();
			}
		}
	}
	
}
