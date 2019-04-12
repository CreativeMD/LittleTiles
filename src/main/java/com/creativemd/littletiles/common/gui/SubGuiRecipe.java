package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBoxCategory;
import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.world.FakeWorld;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityAnimationController;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.gui.controls.GuiAnimationViewer;
import com.creativemd.littletiles.common.gui.controls.IAnimationControl;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class SubGuiRecipe extends SubGuiConfigure implements IAnimationControl {
	
	public LittleStructure structure;
	public LittleStructureGuiParser parser;
	public PairList<String, PairList<String, Class<? extends LittleStructureGuiParser>>> craftables;
	
	protected LoadingThread loadingThread;
	
	public boolean loaded = false;
	public EntityAnimation animation = null;
	public LittleTileBox entireBox;
	public LittleGridContext context;
	public AxisAlignedBB box;
	public LittlePreviews previews;
	
	public AnimationGuiHandler handler = new AnimationGuiHandler(this);
	
	public SubGuiRecipe(ItemStack stack) {
		super(350, 200, stack);
		loadingThread = new LoadingThread();
	}
	
	@Override
	public void onLoaded(EntityAnimation animation, LittleTileBox entireBox, LittleGridContext context, AxisAlignedBB box, LittlePreviews previews) {
		onLoaded(this, animation, entireBox, context, box);
		
	}
	
	public void onLoaded(GuiParent parent, EntityAnimation animation, LittleTileBox entireBox, LittleGridContext context, AxisAlignedBB box) {
		for (GuiControl control : parent.controls) {
			if (control instanceof IAnimationControl)
				((IAnimationControl) control).onLoaded(animation, entireBox, context, box, previews);
			if (control instanceof GuiParent)
				onLoaded((GuiParent) control, animation, entireBox, context, box);
		}
	}
	
	@Override
	public void onTick() {
		super.onTick();
		if (loadingThread == null && !loaded) {
			onLoaded(this, animation, entireBox, context, box);
			if (parser != null)
				parser.onLoaded(animation, entireBox, context, box, previews);
			loaded = true;
		}
		if (animation != null)
			handler.tick(animation);
	}
	
	@Override
	public void saveConfiguration() {
		
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiButton("clear", translate("selection.clear"), 105, 176, 38) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				openYesNoDialog(translate("selection.dialog.clear"));
			}
		});
		
		PairList<String, Class<? extends LittleStructureGuiParser>> noneCategory = new PairList<>();
		noneCategory.add("structure.none.name", null);
		craftables = new PairList<>(LittleStructureRegistry.getCraftables());
		craftables.add(0, new Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>>("", noneCategory));
		GuiComboBoxCategory comboBox = new GuiComboBoxCategory<Class<? extends LittleStructureGuiParser>>("types", 0, 5, 80, craftables);
		LittlePreviews previews = LittleTilePreview.getPreview(stack);
		LittleStructure structure = previews.getStructure();
		if (structure != null) {
			this.structure = structure;
			int index = 0;
			for (Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>> category : craftables) {
				int currentIndex = category.value.indexOfKey("structure." + structure.type.id + ".name");
				if (currentIndex != -1) {
					comboBox.select(currentIndex + index);
					break;
				}
				index += category.value.size();
			}
		}
		int size = previews.totalSize();
		controls.add(new GuiLabel("tiles", previews.totalSize() + " " + translate(size == 1 ? "selection.structure.tile" : "selection.structure.tiles"), 208, 158));
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
		controls.add(comboBox);
		controls.add(new GuiButton("save", 150, 176, 40) {
			@Override
			public void onClicked(int x, int y, int button) {
				if (SubGuiRecipe.this.parser != null) {
					GuiTextfield textfield = (GuiTextfield) get("name");
					LittleStructure structure = SubGuiRecipe.this.parser.parseStructure(stack);
					if (structure != null) {
						structure.name = textfield.text.isEmpty() ? null : textfield.text;
						NBTTagCompound structureNBT = new NBTTagCompound();
						structure.writeToNBT(structureNBT);
						stack.getTagCompound().setTag("structure", structureNBT);
					} else
						stack.getTagCompound().removeTag("structure");
					
				} else
					stack.getTagCompound().removeTag("structure");
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("set_structure", true);
				nbt.setTag("stack", stack.getTagCompound());
				sendPacketToServer(nbt);
				closeGui();
			}
		});
		controls.add(new GuiTextfield("name", (structure != null && structure.name != null) ? structure.name : "", 2, 176, 95, 14).setCustomTooltip(translate("selection.structure.name")));
		onChanged();
	}
	
	public void onChanged() {
		GuiPanel panel = (GuiPanel) get("panel");
		panel.controls.clear();
		
		GuiComboBoxCategory<Class<? extends LittleStructureGuiParser>> types = (GuiComboBoxCategory) get("types");
		Pair<String, Class<? extends LittleStructureGuiParser>> selected = types.getSelected();
		
		if (parser != null)
			removeListener(parser);
		
		LittleStructure saved = this.structure;
		if (saved != null && !selected.key.equals("structure." + saved.type.id + ".name"))
			saved = null;
		
		parser = LittleStructureRegistry.getParser(panel, handler, selected.value);
		if (parser != null) {
			parser.createControls(stack, saved);
			panel.refreshControls();
			addListener(parser);
			if (loaded) {
				onLoaded(panel, animation, entireBox, context, box);
			}
		} else
			parser = null;
		
		get("name").setEnabled(parser != null);
	}
	
	@CustomEventSubscribe
	public void onComboChange(GuiControlChangedEvent event) {
		if (event.source.is("types"))
			onChanged();
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
		stack.setTagCompound(nbt);
	}
	
	public class LoadingThread extends Thread {
		
		public LoadingThread() {
			start();
		}
		
		@Override
		public void run() {
			ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
			if (stack.getItem() instanceof ItemRecipe || (iTile != null && iTile.hasLittlePreview(stack))) {
				previews = iTile != null ? iTile.getLittlePreview(stack) : LittleTilePreview.getPreview(stack);
				BlockPos pos = new BlockPos(0, 75, 0);
				FakeWorld fakeWorld = FakeWorld.createFakeWorld("animationViewer", true);
				
				List<PlacePreviewTile> placePreviews = new ArrayList<>();
				previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
				
				HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, pos);
				ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
				LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, previews.getStructure(), PlacementMode.all, pos, null, null, null, null);
				for (Iterator iterator = fakeWorld.loadedTileEntityList.iterator(); iterator.hasNext();) {
					TileEntity te = (TileEntity) iterator.next();
					if (te instanceof TileEntityLittleTiles)
						blocks.add((TileEntityLittleTiles) te);
				}
				
				entireBox = previews.getSurroundingBox();
				context = previews.context;
				box = entireBox.getBox(context);
				
				animation = new EntityAnimation(fakeWorld, fakeWorld, (EntityAnimationController) new EntityAnimationController() {
					
					@Override
					protected void writeToNBTExtra(NBTTagCompound nbt) {
						
					}
					
					@Override
					protected void readFromNBT(NBTTagCompound nbt) {
						
					}
					
					@Override
					public boolean onRightClick() {
						return false;
					}
				}.addStateAndSelect("nothing", new AnimationState()), pos, UUID.randomUUID(), new StructureAbsolute(pos, entireBox, previews.context)) {
					
					@Override
					public boolean shouldAddDoor() {
						return false;
					}
				};
			}
			
			loadingThread = null;
		}
		
	}
}
