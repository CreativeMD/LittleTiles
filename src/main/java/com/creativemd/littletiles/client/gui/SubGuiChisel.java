package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.item.ItemBlockTiles;
import com.creativemd.littletiles.common.item.ItemLittleChisel;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.DragShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class SubGuiChisel extends SubGuiConfigure {
	
	public SubGuiChisel(ItemStack stack) {
		super(140, 180, stack);
		this.stack = stack;
	}
	
	public LittleGridContext getContext() {
		return ((ILittleTile) stack.getItem()).getPositionContext(stack);
	}
	
	@Override
	public void createControls() {
		LittlePreview preview = ItemLittleChisel.getPreview(stack);
		Color color = ColorUtils.IntToRGBA(preview.getColor());
		controls.add(new GuiColorPicker("picker", 2, 2, color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
		GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 75, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
		
		selector.setSelectedForce(preview.getBlockStack());
		controls.add(selector);
		GuiComboBox box = new GuiComboBox("shape", 0, 96, 134, new ArrayList<>(DragShape.keys()));
		box.select(ItemLittleChisel.getShape(stack).key);
		GuiScrollBox scroll = new GuiScrollBox("settings", 0, 117, 133, 58);
		controls.add(box);
		controls.add(scroll);
		onChange();
		
		GuiAvatarLabel label = new GuiAvatarLabel("", 115, 35, 0, null);
		label.name = "avatar";
		label.height = 60;
		label.avatarSize = 32;
		controls.add(label);
		
		updateLabel();
	}
	
	@CustomEventSubscribe
	public void onComboBoxChange(GuiControlChangedEvent event) {
		if (event.source.is("shape"))
			onChange();
		else if (event.source.is("picker", "preview"))
			updateLabel();
	}
	
	public void onChange() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		
		DragShape shape = DragShape.getShape(box.caption);
		scroll.controls.clear();
		scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound(), getContext()));
		scroll.refreshControls();
	}
	
	public void updateLabel() {
		
		GuiStackSelectorAll selector = (GuiStackSelectorAll) get("preview");
		ItemStack selected = selector.getSelected();
		LittlePreview preview;
		
		if (!selected.isEmpty() && selected.getItem() instanceof ItemBlock) {
			LittleTile tile = new LittleTile(((ItemBlock) selected.getItem()).getBlock(), selected.getMetadata());
			tile.setBox(new LittleBox(0, 0, 0, LittleGridContext.get().size, LittleGridContext.get().size, LittleGridContext.get().size));
			preview = tile.getPreviewTile();
		} else
			preview = ItemLittleChisel.getPreview(stack);
		
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		preview.setColor(ColorUtils.RGBAToInt(picker.color));
		
		GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
		label.avatar = new AvatarItemStack(ItemBlockTiles.getStackFromPreview(LittleGridContext.get(), preview));
	}
	
	@Override
	public void saveConfiguration() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		DragShape shape = DragShape.getShape(box.caption);
		
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		LittlePreview preview = ItemLittleChisel.getPreview(stack);
		
		GuiStackSelectorAll selector = (GuiStackSelectorAll) get("preview");
		ItemStack selected = selector.getSelected();
		
		if (!selected.isEmpty() && selected.getItem() instanceof ItemBlock) {
			LittleTile tile = new LittleTile(((ItemBlock) selected.getItem()).getBlock(), selected.getMetadata());
			tile.setBox(new LittleBox(0, 0, 0, LittleGridContext.get().size, LittleGridContext.get().size, LittleGridContext.get().size));
			preview = tile.getPreviewTile();
		} else
			preview = ItemLittleChisel.getPreview(stack);
		
		preview.setColor(ColorUtils.RGBAToInt(picker.color));
		
		ItemLittleChisel.setPreview(stack, preview);
		ItemLittleChisel.setShape(stack, shape);
		
		shape.saveCustomSettings(scroll, stack.getTagCompound(), getContext());
	}
}
