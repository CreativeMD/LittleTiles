package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiIDButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.Vec3i;

public class SubGuiHammer extends SubGui {
	
	public SubGuiHammer()
	{
		super(200, 200);
	}
	
	public boolean isColored = false;
	
	public int sizeX = 1;
	public int sizeY = 1;
	public int sizeZ = 1;
	
	@Override
	public void createControls() {
		/*controls.add(new GuiIDButton("<", 45, 10, 0));
		controls.add(new GuiIDButton(">", 75, 10, 1));
		controls.add(new GuiIDButton("<", 45, 30, 2));
		controls.add(new GuiIDButton(">", 75, 30, 3));
		controls.add(new GuiIDButton("<", 45, 50, 4));
		controls.add(new GuiIDButton(">", 75, 50, 5));*/
		controls.add(new GuiSteppedSlider("sizeX", 35, 10, 50, 14, sizeX, 1, LittleTile.gridSize));
		controls.add(new GuiSteppedSlider("sizeY", 35, 30, 50, 14, sizeY, 1, LittleTile.gridSize));
		controls.add(new GuiSteppedSlider("sizeZ", 35, 50, 50, 14, sizeZ, 1, LittleTile.gridSize));
		
		controls.add(new GuiButton("HAMMER IT", 100, 10){

			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("sizeX", sizeX);
				nbt.setInteger("sizeY", sizeY);
				nbt.setInteger("sizeZ", sizeZ);
				GuiColorPlate plate = (GuiColorPlate) get("plate");
				//System.out.println(plate.getColor().getAlpha());
				int color = ColorUtils.RGBAToInt(plate.getColor());
				if(color != -1 && color != ColorUtils.WHITE)
					nbt.setInteger("color", color);
				sendPacketToServer(nbt);
			}
			
		});
		controls.add(new GuiSteppedSlider("colorX", 5, 75, 100, 5, 255, 0, 255));
		controls.add(new GuiSteppedSlider("colorY", 5, 85, 100, 5, 255, 0, 255));
		controls.add(new GuiSteppedSlider("colorZ", 5, 95, 100, 5, 255, 0, 255));
		controls.add(new GuiColorPlate("plate", 120, 80, 20, 20, new Vec3i(255, 255, 255)));
		
		//controls.add(new GuiCheckBox("translucent", 100, 30, false));
		
		GuiAvatarLabel label = new GuiAvatarLabel("", 100, 32, 0, null);
		label.name = "avatar";
		label.height = 60;
		controls.add(label);
		updateLabel();
	}
	
	public void updateLabel()
	{
		GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
		
		LittleTileSize size = new LittleTileSize(sizeX, sizeY, sizeZ);
		
		ItemStack dropstack = new ItemStack(LittleTiles.blockTile);
		dropstack.setTagCompound(new NBTTagCompound());
		size.writeToNBT("size", dropstack.getTagCompound());
		Block block = null;
		ItemStack slotStack = container.getSlots().get(0).getStack();
		int meta = 0;
		if(slotStack != null)
		{
			block = Block.getBlockFromItem(slotStack.getItem());
			meta = slotStack.getItemDamage();
			if(block == LittleTiles.blockTile)
			{
				ArrayList<LittleTilePreview> previews = ((ILittleTile) slotStack.getItem()).getLittlePreview(slotStack);
				if(previews.size() > 0)
				{
					block = previews.get(0).getPreviewBlock();
					meta = previews.get(0).getPreviewBlockMeta();
				}
				else
					block = null;
			}
		}
		if(block instanceof BlockAir || block == null)
			block = Blocks.STONE;
		GuiColorPlate plate = (GuiColorPlate) get("plate");
		new LittleTileBlockColored(block, meta, ColorUtils.colorToVec(plate.getColor())).saveTileExtra(dropstack.getTagCompound());
		
		label.avatar = new AvatarItemStack(dropstack);
	}
	
	@CustomEventSubscribe
	public void onSlotChange(SlotChangeEvent event)
	{
		ItemStack slotStack = container.getSlots().get(0).getStack();
		Block block = Block.getBlockFromItem(slotStack.getItem());
		if(block == LittleTiles.blockTile)
		{
			ArrayList<LittleTilePreview> previews = ((ILittleTile) slotStack.getItem()).getLittlePreview(slotStack);
			if(previews.size() > 0)
			{
				int colorInt = previews.get(0).getColor();
				Vec3i color = ColorUtils.IntToRGB(colorInt);
				if(colorInt == -1)
					color = new Vec3i(255, 255, 255);
				
				((GuiSteppedSlider) get("colorX")).value = color.getX();
				((GuiSteppedSlider) get("colorY")).value = color.getY();
				((GuiSteppedSlider) get("colorZ")).value = color.getZ();
				raiseEvent(new GuiControlChangedEvent((GuiControl) get("colorZ")));
			}
		}
		updateLabel();
	}
	
	@CustomEventSubscribe
	public void onChange(GuiControlChangedEvent event)
	{
		sizeX = (int) ((GuiSteppedSlider) get("sizeX")).value;
		sizeY = (int) ((GuiSteppedSlider) get("sizeY")).value;
		sizeZ = (int) ((GuiSteppedSlider) get("sizeZ")).value;
		GuiColorPlate plate = (GuiColorPlate) get("plate");
		plate.setColor(new Vec3i((int) ((GuiSteppedSlider) get("colorX")).value, (int) ((GuiSteppedSlider) get("colorY")).value, (int) ((GuiSteppedSlider) get("colorZ")).value));
		updateLabel();
	}
	
}
