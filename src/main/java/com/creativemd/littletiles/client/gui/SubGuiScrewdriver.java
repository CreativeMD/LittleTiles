package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.selection.selector.AndSelector;
import com.creativemd.littletiles.common.util.selection.selector.AnySelector;
import com.creativemd.littletiles.common.util.selection.selector.NoStructureSelector;
import com.creativemd.littletiles.common.util.selection.selector.StateSelector;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;
import com.creativemd.littletiles.common.util.selection.selector.TileSelectorBlock;

import net.minecraft.block.Block;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class SubGuiScrewdriver extends SubGuiConfigure {
	
	public static ItemStack lastSelectedSearchStack;
	public static ItemStack lastSelectedReplaceStack;
	
	public SubGuiScrewdriver(ItemStack stack) {
		super(200, 205, stack);
	}
	
	@Override
	public void createControls() {
		if (stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());
		
		controls.add(new GuiCheckBox("any", "any", 5, 8, false));
		GuiStackSelectorAll selector = new GuiStackSelectorAll("filter", 40, 5, 130, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
		if (lastSelectedSearchStack != null)
			selector.setSelectedForce(lastSelectedSearchStack);
		controls.add(selector);
		controls.add(new GuiCheckBox("meta", "Metadata", 40, 48, true));
		
		controls.add(new GuiCheckBox("remove", "Remove", 5, 60, false));
		
		controls.add(new GuiCheckBox("replace", "Replace with", 5, 73, false));
		
		selector = new GuiStackSelectorAll("replacement", 40, 87, 130, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
		if (lastSelectedReplaceStack != null)
			selector.setSelectedForce(lastSelectedReplaceStack);
		controls.add(selector);
		controls.add(new GuiTextfield("search2", "", 40, 109, 140, 14));
		controls.add(new GuiCheckBox("metaR", "Force metadata", 40, 133, true));
		
		Color color = new Color(255, 255, 255, 255);
		controls.add(new GuiCheckBox("colorize", "Colorize", 5, 146, false));
		
		controls.add(new GuiColorPicker("picker", 5, 160, color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
		
		controls.add(new GuiButton("undo", "undo", 150, 135, 40) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				try {
					LittleAction.undo();
				} catch (LittleActionException e) {
					getPlayer().sendStatusMessage(new TextComponentString(e.getLocalizedMessage()), true);
				}
			}
		});
		
		controls.add(new GuiButton("redo", "redo", 150, 155, 40) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				try {
					LittleAction.redo();
				} catch (LittleActionException e) {
					getPlayer().sendStatusMessage(new TextComponentString(e.getLocalizedMessage()), true);
				}
			}
		});
		
		controls.add(new GuiButton("run", "Do it!", 150, 175, 40) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				LittleAction action = getDesiredAction();
				if (action != null)
					if (action.execute())
						playSound(SoundEvents.BLOCK_LEVER_CLICK);
			}
		});
	}
	
	public LittleAction getDesiredAction() {
		int[] array = stack.getTagCompound().getIntArray("pos1");
		if (array.length != 3)
			return null;
		BlockPos pos = new BlockPos(array[0], array[1], array[2]);
		array = stack.getTagCompound().getIntArray("pos2");
		if (array.length != 3)
			return null;
		BlockPos pos2 = new BlockPos(array[0], array[1], array[2]);
		
		TileSelector selector;
		if (((GuiCheckBox) get("any")).value)
			selector = new AnySelector();
		else {
			GuiStackSelectorAll filter = (GuiStackSelectorAll) get("filter");
			ItemStack stackFilter = filter.getSelected();
			Block filterBlock = Block.getBlockFromItem(stackFilter.getItem());
			boolean meta = ((GuiCheckBox) get("meta")).value;
			selector = meta ? new StateSelector(BlockUtils.getState(filterBlock, stackFilter.getMetadata())) : new TileSelectorBlock(filterBlock);
		}
		
		selector = new AndSelector(new NoStructureSelector(), selector);
		
		LittleBoxes boxes = TileSelector.getAbsoluteBoxes(getPlayer().world, pos, pos2, selector);
		
		if (boxes.isEmpty())
			return null;
		
		boolean remove = ((GuiCheckBox) get("remove")).value;
		boolean replace = ((GuiCheckBox) get("replace")).value;
		boolean colorize = ((GuiCheckBox) get("colorize")).value;
		
		if (remove)
			return new LittleActionDestroyBoxes(boxes);
		else {
			List<LittleAction> actions = new ArrayList<>();
			
			if (replace) {
				GuiStackSelectorAll replacement = (GuiStackSelectorAll) get("replacement");
				ItemStack stackReplace = replacement.getSelected();
				if (stackReplace != null) {
					Block replacementBlock = Block.getBlockFromItem(stackReplace.getItem());
					if (!LittleAction.isBlockValid(BlockUtils.getState(replacementBlock, stackReplace.getMetadata()))) {
						openButtonDialogDialog("Invalid replacement block!", "ok");
						return null;
					}
					actions.add(new LittleActionDestroyBoxes(boxes));
					LittleAbsolutePreviews previews = new LittleAbsolutePreviews(pos, LittleGridContext.getMin());
					for (LittleBox box : boxes) {
						LittleTile tile = new LittleTile(replacementBlock, stackReplace.getMetadata());
						tile.setBox(box);
						previews.addPreview(pos, tile.getPreviewTile(), boxes.context);
					}
					actions.add(new LittleActionDestroyBoxes(boxes));
					actions.add(new LittleActionPlaceAbsolute(previews, PlacementMode.all, false));
				}
			}
			
			if (colorize) {
				GuiColorPicker picker = (GuiColorPicker) get("picker");
				actions.add(new LittleActionColorBoxes(boxes, ColorUtils.RGBAToInt(picker.color), false));
			}
			
			if (!actions.isEmpty())
				return new LittleActionCombined(actions.toArray(new LittleAction[0]));
		}
		
		if (!remove && !replace && !colorize)
			openButtonDialogDialog("You have to select a task!", "ok");
		
		return null;
	}
	
	@Override
	public void closeGui() {
		super.closeGui();
		lastSelectedSearchStack = ((GuiStackSelectorAll) get("filter")).getSelected();
		lastSelectedReplaceStack = ((GuiStackSelectorAll) get("replacement")).getSelected();
	}
	
	@Override
	public void saveConfiguration() {
		
	}
	
}
