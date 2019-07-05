package com.creativemd.littletiles.common.config;

import com.creativemd.igcm.api.ConfigBranch;
import com.creativemd.igcm.api.ConfigTab;
import com.creativemd.igcm.api.segments.BooleanSegment;
import com.creativemd.igcm.api.segments.FloatSegment;
import com.creativemd.igcm.api.segments.IntegerSegment;
import com.creativemd.igcm.api.segments.IntegerSliderSegment;
import com.creativemd.littletiles.LittleTiles;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class IGCMLoader {
	
	public static void initIGCM() {
		ConfigTab.root.registerElement("littletiles", new ConfigBranch("LittleTiles", new ItemStack(LittleTiles.hammer)) {
			
			@Override
			public void saveExtra(NBTTagCompound nbt) {
				
			}
			
			@Override
			public void loadExtra(NBTTagCompound nbt) {
				
			}
			
			@Override
			public boolean requiresSynchronization() {
				return true;
			}
			
			@Override
			public void onRecieveFrom(Side side) {
				SpecialServerConfig.allowFlowingWater = (Boolean) getValue("allowFlowingWater");
				SpecialServerConfig.allowFlowingLava = (Boolean) getValue("allowFlowingLava");
				SpecialServerConfig.enableBed = (Boolean) getValue("enableBed");
			}
			
			@Override
			public void createChildren() {
				registerElement("survival", new ConfigBranch("Survival", new ItemStack(Blocks.BARRIER)) {
					
					@Override
					public void saveExtra(NBTTagCompound nbt) {
						
					}
					
					@Override
					public void loadExtra(NBTTagCompound nbt) {
						
					}
					
					@Override
					public boolean requiresSynchronization() {
						return true;
					}
					
					@Override
					public void onRecieveFrom(Side side) {
						SpecialServerConfig.strictMining = (Boolean) getValue("strictMining");
						SpecialServerConfig.editUnbreakable = (Boolean) getValue("editUnbreakable");
						SpecialServerConfig.limitEditBlocksSurvival = (Boolean) getValue("limitEditBlocksSurvival");
						SpecialServerConfig.maxEditBlocks = (Integer) getValue("maxEditBlocks");
						SpecialServerConfig.limitPlaceBlocksSurvival = (Boolean) getValue("limitPlaceBlocksSurvival");
						SpecialServerConfig.maxPlaceBlocks = (Integer) getValue("maxPlaceBlocks");
						SpecialServerConfig.minimumTransparency = (Integer) getValue("minimumTransparency");
						SpecialServerConfig.storagePerPixel = (Float) getValue("storagePerPixel");
						SpecialServerConfig.highestHarvestTierSurvival = (Integer) getValue("highestHarvestTierSurvival");
					}
					
					@Override
					public void createChildren() {
						registerElement("strictMining", new BooleanSegment("Strict Mininig in Survival", false).setToolTip("If you can edit a vanilla block in survival directly (hammer/ paint brush)."));
						registerElement("highestHarvestTierSurvival", new IntegerSegment("Harvest Level in Survival", 0, 0, 10).setToolTip("0: any, 1: stone, 2: iron, 3: dimanond."));
						
						registerElement("editUnbreakable", new BooleanSegment("Edit Unbreakable Blocks", false).setToolTip("If you can edit an unbreakable block (bedrock) in survival."));
						registerElement("limitEditBlocksSurvival", new BooleanSegment("Limit Edit/Remove Range", false).setToolTip("If the amount a player can destroy or paint at once should be limited in survival."));
						registerElement("maxEditBlocks", new IntegerSegment("Max Edit/Remove Range", 10, 1, Integer.MAX_VALUE).setToolTip("Only affects the range if edit/remove limitation is enabled."));
						registerElement("limitPlaceBlocksSurvival", new BooleanSegment("Limit Place Range", false).setToolTip("If the amount a player can place at once should be limited in survival."));
						registerElement("maxPlaceBlocks", new IntegerSegment("Max Place Range", 10, 1, Integer.MAX_VALUE).setToolTip("Only affects the range if place limitation is enabled."));
						registerElement("minimumTransparency", new IntegerSliderSegment("Minimum Transparency in survival", 255, 0, 255).setToolTip("Transparency will be enabled if the minimum is smaller than 255."));
						registerElement("storagePerPixel", new FloatSegment("Storage per Pixel", 1F, 0F, Float.MAX_VALUE).setToolTip("Each pixel in default grid makes space for one itemstack (stacksize of one).", "64 pixel add up to one slot"));
					}
				});
				
				registerElement("allowFlowingWater", new BooleanSegment("Allow Flowing Water", true).setToolTip("If disabled the bucket cannot be used to change the water flow."));
				registerElement("allowFlowingLava", new BooleanSegment("Allow Flowing Lava", true).setToolTip("If disabled the bucket cannot be used to change the lava flow."));
				registerElement("enableBed", new BooleanSegment("Enable Bed", true).setToolTip("Whether players should be allowed to sleep in LT beds or not."));
			}
		});
	}
	
}
