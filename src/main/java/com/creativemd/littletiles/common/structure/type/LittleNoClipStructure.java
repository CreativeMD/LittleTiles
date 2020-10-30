package com.creativemd.littletiles.common.structure.type;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.input.ISignalStructureInternalInput;
import com.creativemd.littletiles.common.structure.signal.input.InternalSignalInput;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleNoClipStructure extends LittleStructure implements ISignalStructureInternalInput {
	
	public InternalSignalInput[] inputs;
	public boolean web = true;
	
	public LittleNoClipStructure(LittleStructureType type, IStructureTileList mainBlock) {
		super(type, mainBlock);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		web = nbt.getBoolean("web");
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setBoolean("web", web);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn) {
		if (web)
			entityIn.setInWeb();
	}
	
	@Override
	public void createInputs(NBTTagCompound nbt) {
		inputs = new InternalSignalInput[] { new InternalSignalInput(this, "detected", 1, nbt) };
	}
	
	@Override
	public InternalSignalInput getInput(int id) {
		if (id < inputs.length)
			return inputs[id];
		return null;
	}
	
	@Override
	public void saveInputs(NBTTagCompound nbt) {
		for (int i = 0; i < inputs.length; i++)
			inputs[i].writeToNBT(nbt);
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
			LittleNoClipStructure structure = createStructure(LittleNoClipStructure.class, null);
			structure.web = ((GuiCheckBox) parent.get("web")).value;
			
			return structure;
		}
	}
	
}
