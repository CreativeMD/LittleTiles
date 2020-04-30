package com.creativemd.littletiles.common.mod.albedo;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.mod.coloredlights.ColoredLightsManager;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlbedoExtension {
	
	@CapabilityInject(ILightProvider.class)
	public static Capability<ILightProvider> LIGHT_PROVIDER_CAPABILITY;
	
	@SubscribeEvent
	public static void tileEntityCapability(AttachCapabilitiesEvent<TileEntity> event) {
		if (event.getObject() instanceof TileEntityLittleTiles) {
			TileEntityLittleTiles te = (TileEntityLittleTiles) event.getObject();
			event.addCapability(new ResourceLocation(LittleTiles.modid, "light"), new ICapabilityProvider() {
				
				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return LIGHT_PROVIDER_CAPABILITY == capability;
				}
				
				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					if (capability == LIGHT_PROVIDER_CAPABILITY)
						return (T) new ILightProvider() {
							
							@Override
							@SideOnly(Side.CLIENT)
							public void gatherLights(GatherLightsEvent paramGatherLightsEvent, Entity paramEntity) {
								if (ColoredLightsManager.isInstalled()) {
									AxisAlignedBB box = null;
									int color = -1;
									for (LittleTile tile : te) {
										if (tile.getBlock() == ColoredLightsManager.getInvertedColorsBlock()) {
											int tileColor = ColoredLightsManager.getColorFromBlock(tile.getBlockState());
											if (tile instanceof LittleTileColored)
												tileColor = ColorUtils.blend(tileColor, ((LittleTileColored) tile).color);
											if (box == null) {
												box = tile.getCompleteBox().getBox(te.getContext(), te.getPos());
												color = tileColor;
											} else {
												box = box.union(tile.getCompleteBox().getBox(te.getContext(), te.getPos()));
												color = ColorUtils.blend(color, tileColor);
											}
										}
									}
									
									if (box != null)
										paramGatherLightsEvent.add(new Light.Builder().pos(box.getCenter()).color(color, false).radius(15.0F).build());
								}
							}
						};
					return null;
				}
			});
		}
	}
	
}
