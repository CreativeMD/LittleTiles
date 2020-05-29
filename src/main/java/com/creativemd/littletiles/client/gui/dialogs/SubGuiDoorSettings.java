package com.creativemd.littletiles.client.gui.dialogs;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiDoorSettings extends SubGui {
	
	public GuiDoorSettingsButton button;
	
	public SubGuiDoorSettings(GuiDoorSettingsButton button) {
		this.button = button;
	}
	
	@Override
	public void createControls() {
		controls.add((GuiControl) new GuiCheckBox("stayAnimated", CoreControl.translate("gui.door.stayAnimated"), 0, 0, button.stayAnimated).setCustomTooltip(CoreControl.translate("gui.door.stayAnimatedTooltip")).setEnabled(button.stayAnimatedPossible));
		controls.add(new GuiCheckBox("rightclick", CoreControl.translate("gui.door.rightclick"), 0, 15, button.disableRightClick));
		controls.add(new GuiCheckBox("noClip", CoreControl.translate("gui.door.noClip"), 0, 30, button.noClip));
		
		controls.add(new GuiButton("Close", 0, 143) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				closeGui();
			}
		});
	}
	
	@Override
	public void onClosed() {
		super.onClosed();
		GuiCheckBox stayAnimated = (GuiCheckBox) get("stayAnimated");
		GuiCheckBox rightclick = (GuiCheckBox) get("rightclick");
		GuiCheckBox noClip = (GuiCheckBox) get("noClip");
		button.stayAnimated = stayAnimated.value;
		button.disableRightClick = rightclick.value;
		button.noClip = noClip.value;
	}
	
	public static class GuiDoorSettingsButton extends GuiButton {
		
		public SubGuiDoorSettings gui;
		public boolean stayAnimated;
		public boolean disableRightClick;
		public boolean noClip;
		
		public boolean stayAnimatedPossible = true;
		
		public GuiDoorSettingsButton(String name, int x, int y, boolean stayAnimated, boolean disableRightClick, boolean noClip) {
			super(name, translate("gui.door.settings"), x, y, 40, 7);
			this.stayAnimated = stayAnimated;
			this.disableRightClick = disableRightClick;
			this.noClip = noClip;
		}
		
		@Override
		public void onClicked(int x, int y, int button) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("dialog", true);
			SubGuiDoorSettings dialog = new SubGuiDoorSettings(this);
			dialog.gui = getParent().getOrigin().gui;
			PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, dialog.gui.getLayers().size() - 1, false));
			dialog.container = new SubContainerEmpty(getPlayer());
			dialog.gui.addLayer(dialog);
			dialog.onOpened();
		}
	}
	
}
