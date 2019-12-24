package com.creativemd.littletiles.client.gui;

import java.util.UUID;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.littletiles.common.entity.EntityAnimation;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiDiagnose extends SubGui {
	
	public UUID uuid;
	public EntityAnimation animation;
	
	public boolean isChanging;
	
	public SubGuiDiagnose(UUID uuid, EntityAnimation animation) {
		this.uuid = uuid;
		this.animation = animation;
	}
	
	@Override
	public void createControls() {
		if (animation == null)
			controls.add(new GuiLabel(ChatFormatting.RED + "Could not find animation!", 0, 0));
		else {
			String text = animation.getCachedUniqueIdString();
			controls.add(new GuiLabel(text.substring(0, 24), 0, 0));
			controls.add(new GuiLabel(text.substring(24), 0, 10));
		}
		
		controls.add(new GuiButton("Destroy animation", 0, 100) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("uuid", uuid.toString());
				sendPacketToServer(nbt);
				if (animation != null)
					animation.destroyAnimation();
				closeGui();
			}
		});
		
		if (animation == null)
			return;
		
		if (animation.controller != null) {
			String text;
			isChanging = animation.controller.isChanging();
			if (animation.controller.isChanging())
				text = ChatFormatting.YELLOW + animation.controller.getCurrentState().name + ChatFormatting.WHITE + " -> " + ChatFormatting.GREEN + "" + animation.controller.getAimedState().name;
			else
				text = ChatFormatting.GREEN + "" + animation.controller.getCurrentState().name;
			controls.add(new GuiLabel(text, 0, 20));
		} else
			controls.add(new GuiLabel(ChatFormatting.RED + "Broken controller", 0, 20));
		
	}
	
	@Override
	public void onTick() {
		super.onTick();
		if (animation != null && animation.controller != null && isChanging != animation.controller.isChanging()) {
			controls.clear();
			createControls();
			refreshControls();
		}
	}
	
}
