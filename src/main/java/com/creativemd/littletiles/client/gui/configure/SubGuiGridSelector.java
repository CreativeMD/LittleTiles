package com.creativemd.littletiles.client.gui.configure;

import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.client.gui.LittleSubGuiUtils;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.selection.selector.AnySelector;
import com.creativemd.littletiles.common.utils.selection.selector.StateSelector;
import com.creativemd.littletiles.common.utils.selection.selector.TileSelector;
import com.creativemd.littletiles.common.utils.selection.selector.TileSelectorBlock;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public abstract class SubGuiGridSelector extends SubGuiConfigure {
	
	public LittleGridContext context;
	public boolean activeFilter;
	public TileSelector selector;
	
	public SubGuiGridSelector(ItemStack stack, LittleGridContext context, boolean activeFilter, TileSelector selector) {
		super(200, 140, stack);
		this.context = context;
		this.activeFilter = activeFilter;
		this.selector = selector;
	}
	
	public abstract void saveConfiguration(LittleGridContext context, boolean activeFilter, TileSelector selector);
	
	@Override
	public void saveConfiguration() {
		GuiComboBox contextBox = (GuiComboBox) get("grid");
		try {
			context = LittleGridContext.get(Integer.parseInt(contextBox.caption));
		} catch (NumberFormatException e) {
			context = LittleGridContext.get();
		}
		
		activeFilter = !((GuiCheckBox) get("any")).value;
		GuiStackSelectorAll filter = (GuiStackSelectorAll) get("filter");
		ItemStack stackFilter = filter.getSelected();
		Block filterBlock = Block.getBlockFromItem(stackFilter.getItem());
		boolean meta = ((GuiCheckBox) get("meta")).value;
		selector = meta ? new StateSelector(filterBlock.getStateFromMeta(stackFilter.getItemDamage())) : new TileSelectorBlock(filterBlock);
		
		saveConfiguration(context, activeFilter, selector);
	}
	
	@Override
	public void createControls() {
		GuiComboBox contextBox = new GuiComboBox("grid", 5, 40, 15, LittleGridContext.getNames());
		contextBox.select(ItemMultiTiles.currentContext.size + "");
		controls.add(contextBox);
		
		controls.add(new GuiCheckBox("any", "any", 5, 5, selector == null || selector instanceof AnySelector || !activeFilter));
		
		GuiStackSelectorAll guiSelector = new GuiStackSelectorAll("filter", 40, 5, 130, container.player, LittleSubGuiUtils.getCollector(getPlayer()), true);
		if (selector instanceof TileSelectorBlock) {
			IBlockState state = ((TileSelectorBlock) selector).getState();
			guiSelector.setSelectedForce(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
		}
		controls.add(guiSelector);
		controls.add(new GuiCheckBox("meta", "Metadata", 40, 25, selector instanceof StateSelector));
	}
	
	@CustomEventSubscribe
	public void onChanged(GuiControlChangedEvent event) {
		if (event.source.is("meta", "filter"))
			((GuiCheckBox) get("any")).value = false;
	}
	
}
