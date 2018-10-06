package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.shape.DragShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class SubGuiChisel extends SubGuiConfigure {
	
	public SubGuiChisel(ItemStack stack) {
		super(140, 150, stack);
		this.stack = stack;
	}
	
	public LittleGridContext getContext() {
		return ((ILittleTile) stack.getItem()).getPositionContext(stack);
	}
	
	@Override
	public void createControls() {
		LittleTilePreview preview = ItemLittleChisel.getPreview(stack);
		Color color = ColorUtils.IntToRGBA(preview.getColor());
		controls.add(new GuiColorPicker("picker", 2, 2, color, SpecialServerConfig.isTransparencyEnabled(getPlayer()), SpecialServerConfig.getMinimumTransparencty(getPlayer())));
		GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 45, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
		
		selector.setSelectedForce(preview.getBlockStack());
		controls.add(selector);
		GuiComboBox box = new GuiComboBox("shape", 0, 66, 134, new ArrayList<>(DragShape.shapes.keySet()));
		box.select(ItemLittleChisel.getShape(stack).key);
		GuiScrollBox scroll = new GuiScrollBox("settings", 0, 87, 133, 58);
		controls.add(box);
		controls.add(scroll);
		onChange();
	}
	
	@CustomEventSubscribe
	public void onComboBoxChange(GuiControlChangedEvent event) {
		if (event.source.is("shape"))
			onChange();
	}
	
	public void onChange() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		
		DragShape shape = DragShape.getShape(box.caption);
		scroll.controls.clear();
		scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound(), getContext()));
		scroll.refreshControls();
	}
	
	@Override
	public void saveConfiguration() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		DragShape shape = DragShape.getShape(box.caption);
		
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		LittleTilePreview preview = ItemLittleChisel.getPreview(stack);
		
		GuiStackSelectorAll selector = (GuiStackSelectorAll) get("preview");
		ItemStack selected = selector.getSelected();
		
		if (!selected.isEmpty() && selected.getItem() instanceof ItemBlock) {
			LittleTile tile = new LittleTileBlock(((ItemBlock) selected.getItem()).getBlock(), selected.getItemDamage());
			tile.box = new LittleTileBox(LittleGridContext.get().minPos, LittleGridContext.get().minPos, LittleGridContext.get().minPos, LittleGridContext.get().size, LittleGridContext.get().size, LittleGridContext.get().size);
			preview = tile.getPreviewTile();
		} else
			preview = ItemLittleChisel.getPreview(stack);
		
		preview.setColor(ColorUtils.RGBAToInt(picker.color));
		
		ItemLittleChisel.setPreview(stack, preview);
		ItemLittleChisel.setShape(stack, shape);
		
		shape.saveCustomSettings(scroll, stack.getTagCompound(), getContext());
	}
}
