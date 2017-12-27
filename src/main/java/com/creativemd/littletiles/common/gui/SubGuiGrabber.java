package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiIDButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemLittleGrabber;
import com.creativemd.littletiles.common.items.ItemLittleGrabber.GrabberMode;
import com.creativemd.littletiles.common.items.ItemLittleGrabber.PlacePreviewMode;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.Vec3i;

public abstract class SubGuiGrabber extends SubGui {
	
	public ItemStack stack;
	public final GrabberMode mode;
	public final int index;
	public final GrabberMode[] modes;
	
	public SubGuiGrabber(GrabberMode mode, ItemStack stack, int width, int height)
	{
		super(width, height);
		this.stack = stack;
		this.mode = mode;
		this.modes = ItemLittleGrabber.getModes();
		this.index = ItemLittleGrabber.indexOf(mode);
	}
	
	public abstract void saveChanges();
	
	@Override
	public void onClosed() {
		super.onClosed();
		ItemLittleGrabber.setMode(stack, mode);
		saveChanges();
		sendPacketToServer(stack.getTagCompound());
	}
	
	public void openNewGui(GrabberMode mode)
	{
		ItemLittleGrabber.setMode(stack, mode);
		GuiHandler.openGui("grabber", new NBTTagCompound(), getPlayer());
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiButton("<<", 0, 0, 10) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				int newIndex = index - 1;
				if(newIndex < 0)
					newIndex = modes.length - 1;
				openNewGui(modes[newIndex]);
			}
			
		});
		
		controls.add(new GuiButton(">>", 124, 0, 10) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				int newIndex = index + 1;
				if(newIndex >= modes.length)
					newIndex = 0;
				openNewGui(modes[newIndex]);
			}
			
		});
		
		controls.add(new GuiLabel(mode.getLocalizedName(), 20, 3));
	}
	
}
