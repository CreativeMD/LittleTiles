package com.creativemd.littletiles.common.structure.registry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.client.gui.controls.IAnimationControl;
import com.creativemd.littletiles.common.entity.AnimationPreview;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

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
	public abstract void createControls(LittlePreviews previews, @Nullable LittleStructure structure);
	
	@SideOnly(Side.CLIENT)
	public abstract LittleStructure parseStructure(LittlePreviews previews);
	
	public <T extends LittleStructure> T createStructure(Class<T> structureClass) {
		LittleStructureType type = LittleStructureRegistry.getStructureType(structureClass);
		if (type == null)
			throw new RuntimeException("Could find structure for " + structureClass);
		return (T) type.createStructure();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onLoaded(AnimationPreview animationPreview) {
		
	}
	
}
