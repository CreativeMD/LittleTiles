package com.creativemd.littletiles.common.structure.type;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleNoClipStructure extends LittleStructure {
	
	public LittleNoClipStructure(LittleStructureType type) {
		super(type);
	}
	
	public boolean web = true;
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		web = nbt.getBoolean("web");
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setBoolean("web", web);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (web)
			entityIn.setInWeb();
	}
	
	public static class LittleNoClipStructureParser extends LittleStructureGuiParser {
		
		public LittleNoClipStructureParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			boolean slowness = true;
			if (structure instanceof LittleNoClipStructure)
				slowness = ((LittleNoClipStructure) structure).web;
			parent.controls.add(new GuiCheckBox("web", "slowness (cobwebs)", 3, 0, slowness));
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleNoClipStructure parseStructure(LittlePreviews previews) {
			LittleNoClipStructure structure = createStructure(LittleNoClipStructure.class);
			structure.web = ((GuiCheckBox) parent.get("web")).value;
			
			return structure;
		}
	}
	
}
