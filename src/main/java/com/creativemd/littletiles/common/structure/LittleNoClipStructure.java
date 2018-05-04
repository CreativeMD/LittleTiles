package com.creativemd.littletiles.common.structure;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;

import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleNoClipStructure extends LittleStructure {
	
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
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		boolean slowness = this.web;
		if(structure instanceof LittleNoClipStructure)
			slowness = ((LittleNoClipStructure) structure).web;
		gui.controls.add(new GuiCheckBox("web", "slowness (cobwebs)", 3, 30, slowness));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public LittleStructure parseStructure(SubGui gui) {
		LittleNoClipStructure structure = new LittleNoClipStructure();
		structure.web = ((GuiCheckBox) gui.get("web")).value;
		
		return structure;
	}
	
	@Override
	public boolean shouldCheckForCollision() {
		return true;
	}
	
	@Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
		if(web)
			entityIn.setInWeb();
    }

}
