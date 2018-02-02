package com.creativemd.littletiles.common.config;

import com.creativemd.igcm.api.ConfigBranch;
import com.creativemd.igcm.api.ConfigTab;
import com.creativemd.igcm.api.segments.BooleanSegment;
import com.creativemd.igcm.api.segments.IntegerSegment;
import com.creativemd.littletiles.LittleTiles;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class IGCMLoader {
	
	public static void initIGCM()
	{
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
					}
					
					@Override
					public void createChildren() {
						registerElement("strictMining", new BooleanSegment("Strict Mininig in Survival", false).setToolTip("If you can edit a vanilla block in survival directly (hammer/ paint brush)."));
						registerElement("editUnbreakable", new BooleanSegment("Edit Unbreakable Blocks", false).setToolTip("If you can edit an unbreakable block (bedrock) in survival."));
						registerElement("limitEditBlocksSurvival", new BooleanSegment("Limit Edit/Remove Range", false).setToolTip("If the amount a player can destroy or paint at once should be limited in survival."));
						registerElement("maxEditBlocks", new IntegerSegment("Max Edit/Remove Range", 10, 1, Integer.MAX_VALUE).setToolTip("Only affects the range if edit/remove limitation is enabled."));
						registerElement("limitPlaceBlocksSurvival", new BooleanSegment("Limit Place Range", false).setToolTip("If the amount a player can place at once should be limited in survival."));
						registerElement("maxPlaceBlocks", new IntegerSegment("Max Place Range", 10, 1, Integer.MAX_VALUE).setToolTip("Only affects the range if place limitation is enabled."));
					}
				});
			}
		});
	}
	
}
