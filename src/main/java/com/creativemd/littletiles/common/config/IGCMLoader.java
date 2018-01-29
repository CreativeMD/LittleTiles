package com.creativemd.littletiles.common.config;

import com.creativemd.igcm.api.ConfigBranch;
import com.creativemd.igcm.api.ConfigTab;
import com.creativemd.igcm.api.segments.BooleanSegment;
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
				registerElement("protection", new ConfigBranch("Protection", new ItemStack(Blocks.BARRIER)) {
					
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
					}
					
					@Override
					public void createChildren() {
						registerElement("strictMining", new BooleanSegment("Strict Mininig in Survival", false).setToolTip("If you can edit a vanilla block in survival directly (hammer/ paint brush."));
					}
				});
			}
		});
	}
	
}
