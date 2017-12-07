package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
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
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemLittleGrabber;
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

public class SubGuiGrabber extends SubGui {
	
	public ItemStack stack;
	public LittleTileSize size;
	
	public SubGuiGrabber(ItemStack stack)
	{
		super(140, 100);
		this.stack = stack;
	}
	
	public boolean isColored = false;
	
	@Override
	public void onClosed() {
		LittleTilePreview preview = ItemLittleGrabber.getPreview(stack);
		preview.box = new LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, size.sizeX, size.sizeY, size.sizeZ);
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		preview.setColor(ColorUtils.RGBAToInt(picker.color));
		ItemLittleGrabber.setPreview(stack, preview);
		sendPacketToServer(stack.getTagCompound());
	}
	
	@Override
	public void createControls() {
		LittleTilePreview preview = ItemLittleGrabber.getPreview(stack);
		size = preview.box.getSize();
		
		controls.add(new GuiSteppedSlider("sizeX", 25, 0, 50, 14, size.sizeX, 1, LittleTile.gridSize));
		controls.add(new GuiSteppedSlider("sizeY", 25, 20, 50, 14, size.sizeY, 1, LittleTile.gridSize));
		controls.add(new GuiSteppedSlider("sizeZ", 25, 40, 50, 14, size.sizeZ, 1, LittleTile.gridSize));
		
		Color color = ColorUtils.IntToRGBA(preview.getColor());
		color.setAlpha(255);
		controls.add(new GuiColorPicker("picker", 0, 65, color));
		
		GuiAvatarLabel label = new GuiAvatarLabel("", 110, 10, 0, null);
		label.name = "avatar";
		label.height = 60;
		label.avatarSize = 32;
		controls.add(label);
		updateLabel();
	}
	
	public void updateLabel()
	{
		GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
		
		LittleTilePreview preview = ItemLittleGrabber.getPreview(stack);
		
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		preview.setColor(ColorUtils.RGBAToInt(picker.color));
		preview.box = new LittleTileBox(0, 0, 0, size.sizeX, size.sizeY, size.sizeZ);
		
		label.avatar = new AvatarItemStack(ItemBlockTiles.getStackFromPreview(preview));
	}
	
	@CustomEventSubscribe
	public void onSlotChange(SlotChangeEvent event)
	{
		ItemStack slotStack = container.getSlots().get(0).getStack();
		Block block = Block.getBlockFromItem(slotStack.getItem());
		if(block == LittleTiles.blockTile)
		{
			List<LittleTilePreview> previews = ((ILittleTile) slotStack.getItem()).getLittlePreview(slotStack);
			if(previews.size() > 0)
			{
				int colorInt = previews.get(0).getColor();
				Vec3i color = ColorUtils.IntToRGB(colorInt);
				if(colorInt == -1)
					color = new Vec3i(255, 255, 255);
				
				GuiColorPicker picker = (GuiColorPicker) get("picker");
				picker.color.set(color.getX(), color.getY(), color.getZ());
			}
		}
		updateLabel();
	}
	
	@CustomEventSubscribe
	public void onClick(GuiControlClickEvent event)
	{
		if(event.source.is("sliced"))
			updateLabel();
	}
	
	@CustomEventSubscribe
	public void onChange(GuiControlChangedEvent event)
	{
		size.sizeX = (int) ((GuiSteppedSlider) get("sizeX")).value;
		size.sizeY = (int) ((GuiSteppedSlider) get("sizeY")).value;
		size.sizeZ = (int) ((GuiSteppedSlider) get("sizeZ")).value;
		updateLabel();
	}
	
}
