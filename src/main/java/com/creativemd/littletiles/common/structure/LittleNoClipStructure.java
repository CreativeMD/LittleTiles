package com.creativemd.littletiles.common.structure;

import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
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
	public boolean shouldCheckForCollision() {
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (web)
			entityIn.setInWeb();
	}

	public static class LittleNoClipStructureParser extends LittleStructureParser<LittleNoClipStructure> {

		public LittleNoClipStructureParser(String id, GuiParent parent) {
			super(id, parent);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(ItemStack stack, LittleStructure structure) {
			boolean slowness = true;
			if (structure instanceof LittleNoClipStructure)
				slowness = ((LittleNoClipStructure) structure).web;
			parent.controls.add(new GuiCheckBox("web", "slowness (cobwebs)", 3, 30, slowness));
		}

		@Override
		@SideOnly(Side.CLIENT)
		public LittleNoClipStructure parseStructure(ItemStack stack) {
			LittleNoClipStructure structure = new LittleNoClipStructure();
			structure.web = ((GuiCheckBox) parent.get("web")).value;

			return structure;
		}
	}

}
