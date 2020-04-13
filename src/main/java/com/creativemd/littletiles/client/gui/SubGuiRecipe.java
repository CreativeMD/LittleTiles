package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBoxCategory;
import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiItemComboBox;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.controls.GuiAnimationViewer;
import com.creativemd.littletiles.client.gui.controls.IAnimationControl;
import com.creativemd.littletiles.common.entity.AnimationPreview;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviewsStructure;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiRecipe extends SubGuiConfigure implements IAnimationControl {
	
	public LittleStructure structure;
	public LittleStructureGuiParser parser;
	public PairList<String, PairList<String, Class<? extends LittleStructureGuiParser>>> craftables;
	
	public AnimationPreview animationPreview;
	public StructureHolder selected;
	protected LoadingThread loadingThread;
	
	public List<StructureHolder> hierarchy;
	public List<ItemStack> hierarchyStacks;
	public List<String> hierarchyNames;
	public LittlePreviews previews;
	
	public AnimationGuiHandler handler = new AnimationGuiHandler();
	
	public SubGuiRecipe(ItemStack stack) {
		super(350, 200, stack);
		
		PairList<String, Class<? extends LittleStructureGuiParser>> noneCategory = new PairList<>();
		noneCategory.add("structure.none.name", null);
		craftables = new PairList<>(LittleStructureRegistry.getCraftables());
		craftables.add(0, new Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>>("", noneCategory));
		
		previews = LittlePreview.getPreview(stack);
		
		hierarchy = new ArrayList<>();
		hierarchyStacks = new ArrayList<>();
		hierarchyNames = new ArrayList<>();
		
		addPreviews(previews, hierarchy, hierarchyStacks, hierarchyNames, "", null, -1);
	}
	
	protected static void addPreviews(LittlePreviews previews, List<StructureHolder> hierarchy, List<ItemStack> stacks, List<String> lines, String prefix, LittlePreviews parent, int childId) {
		StructureHolder holder = new StructureHolder(parent, childId, hierarchy.size());
		holder.previews = previews;
		holder.prefix = prefix;
		lines.add(holder.getDisplayName());
		
		ItemStack stack = new ItemStack(LittleTiles.multiTiles);
		LittlePreviews newPreviews = new LittlePreviews(previews.context);
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		for (LittlePreview preview : previews) {
			
			newPreviews.addWithoutCheckingPreview(preview.copy());
			minX = Math.min(minX, preview.box.minX);
			minY = Math.min(minY, preview.box.minY);
			minZ = Math.min(minZ, preview.box.minZ);
			minX = Math.min(minX, preview.box.maxX);
			minY = Math.min(minY, preview.box.maxY);
			minZ = Math.min(minZ, preview.box.maxZ);
		}
		
		for (LittlePreview preview : newPreviews) {
			preview.box.sub(minX, minY, minZ);
		}
		LittlePreview.savePreview(newPreviews, stack);
		stacks.add(stack);
		holder.explicit = stack;
		hierarchy.add(holder);
		
		if (previews.hasChildren()) {
			int i = 0;
			for (LittlePreviews child : previews.getChildren()) {
				addPreviews(child, hierarchy, stacks, lines, prefix + "-", previews, i);
				i++;
			}
		}
	}
	
	@Override
	public void onLoaded(AnimationPreview animationPreview) {
		onLoaded(this, animationPreview);
		
	}
	
	public void onLoaded(GuiParent parent, AnimationPreview animationPreview) {
		for (GuiControl control : parent.controls) {
			if (control instanceof IAnimationControl)
				((IAnimationControl) control).onLoaded(animationPreview);
			if (control instanceof GuiParent)
				onLoaded((GuiParent) control, animationPreview);
		}
	}
	
	@Override
	public void onTick() {
		super.onTick();
		if (loadingThread != null && loadingThread.result != null) {
			animationPreview = loadingThread.result;
			loadingThread = null;
			onLoaded(animationPreview);
			if (parser != null)
				parser.onLoaded(animationPreview);
		}
		if (animationPreview != null)
			handler.tick(animationPreview.previews, animationPreview.animation.structure, animationPreview.animation);
	}
	
	@Override
	public void saveConfiguration() {
		
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiComboBoxCategory<Class<? extends LittleStructureGuiParser>>("types", 0, 5, 90, craftables));
		
		controls.add(new GuiButton("clear", translate("selection.clear"), 105, 176, 38) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				openYesNoDialog(translate("selection.dialog.clear"));
			}
		});
		
		controls.add(new GuiLabel("tilescount", "", 208, 158));
		
		controls.add(new GuiItemComboBox("hierarchy", 100, 5, 200, hierarchyNames, hierarchyStacks));
		controls.add(new GuiAnimationViewer("renderer", 208, 30, 136, 135));
		controls.add(new GuiIconButton("play", 248, 172, 10) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				handler.play();
			}
		});
		controls.add(new GuiIconButton("pause", 268, 172, 9) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				handler.pause();
			}
		});
		controls.add(new GuiIconButton("stop", 288, 172, 11) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				handler.stop();
			}
		});
		controls.add(new GuiPanel("panel", 0, 30, 200, 135));
		
		controls.add(new GuiButton("save", 150, 176, 40) {
			@Override
			public void onClicked(int x, int y, int button) {
				savePreview();
				finializePreview(previews);
				
				stack.setTagCompound(new NBTTagCompound());
				LittlePreview.savePreview(previews, stack);
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("set_structure", true);
				nbt.setTag("stack", stack.getTagCompound());
				sendPacketToServer(nbt);
				closeGui();
			}
		});
		controls.add(new GuiTextfield("name", "", 2, 176, 95, 14).setCustomTooltip(translate("selection.structure.name")));
		
		loadStack(hierarchy.get(0));
	}
	
	public void loadStack(StructureHolder holder) {
		this.selected = holder;
		animationPreview = null;
		
		LittlePreviews previews = holder.previews;
		
		LittleStructure structure = previews.getStructure();
		
		GuiComboBoxCategory comboBox = (GuiComboBoxCategory) get("types");
		this.structure = structure;
		int index = 0;
		for (Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>> category : craftables) {
			int currentIndex = category.value.indexOfKey("structure." + (structure != null ? structure.type.id : "none") + ".name");
			if (currentIndex != -1) {
				comboBox.select(currentIndex + index);
				break;
			}
			index += category.value.size();
		}
		
		GuiTextfield textfield = (GuiTextfield) get("name");
		textfield.text = (structure != null && structure.name != null) ? structure.name : "";
		textfield.setCursorPositionZero();
		
		int size = previews.totalSize();
		GuiLabel label = ((GuiLabel) get("tilescount"));
		label.caption = size + " " + translate(size == 1 ? "selection.structure.tile" : "selection.structure.tiles");
		label.width = GuiRenderHelper.instance.getStringWidth(label.caption) + label.getContentOffset() * 2;
		if (loadingThread != null && loadingThread.isAlive())
			loadingThread.stop();
		loadingThread = new LoadingThread(previews);
		
		onStructureSelectorChanged();
	}
	
	public void onStructureSelectorChanged() {
		GuiPanel panel = (GuiPanel) get("panel");
		panel.controls.clear();
		
		GuiComboBoxCategory<Class<? extends LittleStructureGuiParser>> types = (GuiComboBoxCategory) get("types");
		Pair<String, Class<? extends LittleStructureGuiParser>> selected = types.getSelected();
		
		/*if (selected.value == null && this.selected.parent != null) {
			int index = 0;
			for (Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>> category : craftables) {
				int currentIndex = category.value.indexOfKey("structure." + (structure != null ? structure.type.id : "none") + ".name");
				if (currentIndex != -1) {
					types.select(currentIndex + index);
					break;
				}
				index += category.value.size();
			}
			return;
		}*/
		
		if (parser != null)
			removeListener(parser);
		
		LittleStructure saved = this.structure;
		if (saved != null && !selected.key.equals("structure." + saved.type.id + ".name"))
			saved = null;
		
		parser = LittleStructureRegistry.getParser(panel, handler, selected.value);
		if (parser != null) {
			handler.setTimeline(null, null);
			parser.createControls(this.selected.previews, saved);
			panel.refreshControls();
			addListener(parser);
			if (animationPreview != null) {
				onLoaded(panel, animationPreview);
			}
		} else
			parser = null;
		
		get("name").setEnabled(parser != null);
	}
	
	public void finializePreview(LittlePreviews previews) {
		if (previews.hasStructure())
			previews.getStructure().finializePreview(previews);
		
		if (previews.hasChildren())
			for (LittlePreviews child : previews.getChildren())
				finializePreview(child);
	}
	
	public void savePreview() {
		LittleStructure structure = null;
		LittlePreviews oldPreviews = selected.previews;
		if (SubGuiRecipe.this.parser != null) {
			GuiTextfield textfield = (GuiTextfield) get("name");
			structure = SubGuiRecipe.this.parser.parseStructure(oldPreviews);
			if (structure != null)
				structure.name = textfield.text.isEmpty() ? null : textfield.text;
			
			NBTTagCompound structureNBT = new NBTTagCompound();
			structure.writeToNBT(structureNBT);
			selected.previews = new LittlePreviewsStructure(structureNBT, oldPreviews.context);
		} else
			selected.previews = new LittlePreviews(oldPreviews.context);
		
		selected.previews.assign(oldPreviews);
		if (selected.parent != null)
			selected.parent.updateChild(selected.childId, selected.previews);
		else
			previews = selected.previews;
		
		hierarchyNames.set(selected.index, selected.getDisplayName());
	}
	
	@CustomEventSubscribe
	public void onComboChange(GuiControlChangedEvent event) {
		if (event.source.is("types"))
			onStructureSelectorChanged();
		else if (event.source.is("hierarchy")) {
			int index = ((GuiItemComboBox) event.source).index;
			if (index != selected.index) {
				savePreview();
				loadStack(hierarchy.get(index));
			}
		}
	}
	
	@Override
	public void onDialogClosed(String text, String[] buttons, String clicked) {
		if (clicked.equalsIgnoreCase("yes")) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("clear_content", true);
			sendPacketToServer(nbt);
		}
	}
	
	@Override
	public void receiveContainerPacket(NBTTagCompound nbt) {
		if (nbt.hasNoTags())
			stack.setTagCompound(null);
		else
			stack.setTagCompound(nbt);
	}
	
	public static class LoadingThread extends Thread {
		
		public final LittlePreviews previews;
		public AnimationPreview result;
		
		public LoadingThread(LittlePreviews previews) {
			start();
			this.previews = previews;
		}
		
		@Override
		public void run() {
			result = new AnimationPreview(previews);
		}
		
	}
	
	public static class StructureHolder {
		
		public final LittlePreviews parent;
		public final int childId;
		public final int index;
		public String prefix;
		public ItemStack explicit;
		public LittlePreviews previews;
		
		public StructureHolder(LittlePreviews parent, int childId, int index) {
			this.parent = parent;
			this.childId = childId;
			this.index = index;
		}
		
		public String getDisplayName() {
			String name = previews.getStructureName();
			if (name == null)
				if (previews.hasStructure())
					name = previews.getStructureId();
				else
					name = "none";
				
			if (parent != null)
				name += " " + childId;
			
			return prefix + name;
		}
		
	}
}
