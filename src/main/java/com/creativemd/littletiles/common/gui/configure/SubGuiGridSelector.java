package com.creativemd.littletiles.common.gui.configure;

import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll.SearchSelector;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.gui.LittleSubGuiUtils;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.selection.selector.AnySelector;
import com.creativemd.littletiles.common.utils.selection.selector.BlockSelector;
import com.creativemd.littletiles.common.utils.selection.selector.StateSelector;
import com.creativemd.littletiles.common.utils.selection.selector.TileSelector;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public abstract class SubGuiGridSelector extends SubGuiConfigure {

	public LittleGridContext context;
	public TileSelector selector;

	public SubGuiGridSelector(ItemStack stack, LittleGridContext context, TileSelector selector) {
		super(200, 140, stack);
		this.context = context;
		this.selector = selector;
	}

	public abstract void saveConfiguration(LittleGridContext context, TileSelector selector);

	@Override
	public void saveConfiguration() {
		GuiComboBox contextBox = (GuiComboBox) get("grid");
		try {
			context = LittleGridContext.get(Integer.parseInt(contextBox.caption));
		} catch (NumberFormatException e) {
			context = LittleGridContext.get();
		}

		if (((GuiCheckBox) get("any")).value)
			selector = null;
		else {
			GuiStackSelectorAll filter = (GuiStackSelectorAll) get("filter");
			ItemStack stackFilter = filter.getSelected();
			Block filterBlock = Block.getBlockFromItem(stackFilter.getItem());
			boolean meta = ((GuiCheckBox) get("meta")).value;
			selector = meta ? new StateSelector(filterBlock.getStateFromMeta(stackFilter.getItemDamage())) : new BlockSelector(filterBlock);
		}
		saveConfiguration(context, selector);
	}

	@Override
	public void createControls() {
		GuiComboBox contextBox = new GuiComboBox("grid", 0, 70, 15, LittleGridContext.getNames());
		contextBox.select(ItemMultiTiles.currentContext.size + "");
		controls.add(contextBox);

		controls.add(new GuiCheckBox("any", "any", 5, 5, selector == null || selector instanceof AnySelector));

		GuiStackSelectorAll guiSelector = new GuiStackSelectorAll("filter", 40, 5, 130, container.player, LittleSubGuiUtils.getCollector(getPlayer()), true);
		if (selector instanceof BlockSelector) {
			IBlockState state = ((BlockSelector) selector).getState();
			guiSelector.setSelectedForce(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
		}
		controls.add(guiSelector);
		controls.add(new GuiTextfield("search", "", 40, 27, 140, 14));
		controls.add(new GuiCheckBox("meta", "Metadata", 40, 45, selector instanceof StateSelector));
	}

	@CustomEventSubscribe
	public void onChanged(GuiControlChangedEvent event) {
		if (event.source.is("search")) {
			GuiStackSelectorAll inv = (GuiStackSelectorAll) get("filter");
			((SearchSelector) inv.collector.selector).search = ((GuiTextfield) event.source).text.toLowerCase();
			inv.updateCollectedStacks();
			inv.closeBox();
		}
	}

}
