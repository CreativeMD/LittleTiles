package com.creativemd.littletiles.client.gui.controls;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAnalogeSlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.littletiles.common.structure.animation.event.PlaySoundEvent;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class SubGuiSoundSelector extends SubGui {
	
	public GuiPickSoundButton button;
	public List<String> possibleLines;
	public String selected;
	
	public SubGuiSoundSelector(GuiPickSoundButton button) {
		this.button = button;
		possibleLines = new ArrayList<>();
		for (ResourceLocation location : SoundEvent.REGISTRY.getKeys())
			possibleLines.add(location.toString());
		if (button.selected != null)
			selected = button.selected.getRegistryName().toString();
		else
			selected = null;
	}
	
	@Override
	public void createControls() {
		GuiTextfield search = new GuiTextfield("search", "", 0, 0, 165, 14);
		controls.add(search);
		controls.add(new GuiComboBox("sounds", 0, 22, 165, new ArrayList<>()));
		
		controls.add(new GuiLabel(translate("gui.sound.volume") + ":", 0, 44));
		controls.add(new GuiAnalogeSlider("volume", 60, 44, 60, 8, button.volume, 0F, 1F));
		controls.add(new GuiLabel(translate("gui.sound.pitch") + ":", 0, 64));
		controls.add(new GuiAnalogeSlider("pitch", 60, 64, 60, 8, button.pitch, 0.5F, 2F));
		
		controls.add(new GuiButton("play", 144, 44) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				SoundEvent event = getSelected();
				if (event != null)
					playSound(event, getVolume(), getPitch());
			}
		});
		controls.add(new GuiButton("save", 140, 143) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				SubGuiSoundSelector.this.button.selected = SubGuiSoundSelector.this.getSelected();
				SubGuiSoundSelector.this.button.volume = SubGuiSoundSelector.this.getVolume();
				SubGuiSoundSelector.this.button.pitch = SubGuiSoundSelector.this.getPitch();
				closeGui();
				SubGuiSoundSelector.this.button.raiseEvent(new GuiControlChangedEvent(SubGuiSoundSelector.this.button));
			}
		});
		controls.add(new GuiButton("cancel", 0, 143) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				closeGui();
			}
		});
		
		onChanged(new GuiControlChangedEvent(search));
	}
	
	@CustomEventSubscribe
	public void onChanged(GuiControlChangedEvent event) {
		GuiComboBox sounds = (GuiComboBox) get("sounds");
		GuiTextfield search = (GuiTextfield) get("search");
		
		if (event.source != null) {
			if (event.source.is("sounds"))
				selected = sounds.caption;
			else if (event.source.is("search")) {
				List<String> foundLines = new ArrayList<>();
				if (search.text.isEmpty())
					foundLines.addAll(possibleLines);
				else
					for (int i = 0; i < possibleLines.size(); i++)
						if (possibleLines.get(i).contains(search.text))
							foundLines.add(possibleLines.get(i));
				sounds.lines = foundLines;
				sounds.index = foundLines.indexOf(selected);
				sounds.caption = selected;
			}
		}
		
	}
	
	@Override
	public void onClosed() {
		this.button.gui = null;
	}
	
	public float getPitch() {
		GuiAnalogeSlider pitch = (GuiAnalogeSlider) get("pitch");
		return (float) pitch.value;
	}
	
	public float getVolume() {
		GuiAnalogeSlider volume = (GuiAnalogeSlider) get("volume");
		return (float) volume.value;
	}
	
	public SoundEvent getSelected() {
		ResourceLocation location = getSelectionLocation();
		if (location != null)
			return SoundEvent.REGISTRY.getObject(location);
		return null;
	}
	
	public ResourceLocation getSelectionLocation() {
		if (selected != null && !selected.isEmpty())
			return new ResourceLocation(selected);
		return null;
	}
	
	public static class GuiPickSoundButton extends GuiButton {
		
		public SubGuiSoundSelector gui;
		public SoundEvent selected;
		public float volume;
		public float pitch;
		
		public GuiPickSoundButton(String name, int x, int y, @Nullable PlaySoundEvent event) {
			super(name, translate("gui.door.pick-sound"), x, y, 50, 7);
			if (event != null) {
				this.selected = event.sound;
				this.volume = event.volume;
				this.pitch = event.pitch;
			} else {
				this.volume = 1.0F;
				this.pitch = 1.0F;
			}
		}
		
		@Override
		public void onClicked(int x, int y, int button) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("dialog", true);
			SubGuiSoundSelector dialog = new SubGuiSoundSelector(this);
			dialog.gui = getParent().getOrigin().gui;
			PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, dialog.gui.getLayers().size() - 1, false));
			dialog.container = new SubContainerEmpty(getPlayer());
			dialog.gui.addLayer(dialog);
			dialog.onOpened();
		}
	}
	
}
