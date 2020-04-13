package com.creativemd.littletiles.common.structure.animation.event;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AnimationEventGuiParser<T extends AnimationEvent> {
	
	@SideOnly(Side.CLIENT)
	public abstract void createControls(GuiParent parent, @Nullable T event, LittlePreviews previews);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	public abstract T parse(GuiParent parent, T event);
	
	@SideOnly(Side.CLIENT)
	public int getHeight() {
		return 30;
	}
	
}
