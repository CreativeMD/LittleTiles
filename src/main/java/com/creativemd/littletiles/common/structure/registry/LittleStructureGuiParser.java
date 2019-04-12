package com.creativemd.littletiles.common.structure.registry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.gui.controls.IAnimationControl;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructureGuiParser implements IAnimationControl {
	
	public final GuiParent parent;
	public final AnimationGuiHandler handler;
	
	public LittleStructureGuiParser(GuiParent parent, AnimationGuiHandler handler) {
		this.parent = parent;
		this.handler = handler;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract void createControls(ItemStack stack, @Nullable LittleStructure structure);
	
	@SideOnly(Side.CLIENT)
	public abstract LittleStructure parseStructure(ItemStack stack);
	
	public <T extends LittleStructure> T createStructure(Class<T> structureClass) {
		LittleStructureType type = LittleStructureRegistry.getStructureType(structureClass);
		if (type == null)
			throw new RuntimeException("Could find structure for " + structureClass);
		return (T) type.createStructure();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onLoaded(EntityAnimation animation, LittleTileBox entireBox, LittleGridContext context, AxisAlignedBB box, LittlePreviews previews) {
		
	}
	
}
