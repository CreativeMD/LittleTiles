package com.creativemd.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline.KeyDeselectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline.KeySelectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.KeyControl;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel.TimelineChannelDouble;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel.TimelineChannelInteger;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiToolTipEvent;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.gui.controls.GuiLTDistance;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.gui.dialogs.SubGuiDialogAxis.GuiAxisButton;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.LTStructureAnnotation;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.utils.animation.Animation;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.animation.AnimationTimeline;
import com.creativemd.littletiles.common.utils.animation.TimestampAnimation;
import com.creativemd.littletiles.common.utils.animation.transformation.OffsetTransformation;
import com.creativemd.littletiles.common.utils.animation.transformation.RotationTransformation;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleAdvancedDoor extends LittleDoorBase {
	
	public static PairList<Integer, Double> loadPairListDouble(int[] array) {
		PairList<Integer, Double> list = new PairList<>();
		int i = 0;
		while (i < array.length) {
			list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
			i += 3;
		}
		return list;
	}
	
	public static PairList<Integer, Integer> loadPairListInteger(int[] array) {
		PairList<Integer, Integer> list = new PairList<>();
		int i = 0;
		while (i < array.length) {
			list.add(array[i], array[i + 1]);
			i += 2;
		}
		return list;
	}
	
	public static PairList<Integer, Double> loadPairListDouble(int[] array, int from, int length) {
		PairList<Integer, Double> list = new PairList<>();
		int i = from;
		while (i < from + length) {
			list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
			i += 3;
		}
		return list;
	}
	
	public static int[] savePairListDouble(PairList<Integer, Double> list) {
		if (list == null)
			return null;
		
		int[] array = new int[list.size() * 3];
		for (int i = 0; i < list.size(); i++) {
			Pair<Integer, Double> pair = list.get(i);
			array[i * 3] = pair.key;
			long value = Double.doubleToLongBits(pair.value);
			array[i * 3 + 1] = (int) (value >> 32);
			array[i * 3 + 2] = (int) value;
		}
		return array;
	}
	
	public static int[] savePairListInteger(PairList<Integer, Integer> list) {
		if (list == null)
			return null;
		
		int[] array = new int[list.size() * 2];
		for (int i = 0; i < list.size(); i++) {
			Pair<Integer, Integer> pair = list.get(i);
			array[i * 2] = pair.key;
			array[i * 2 + 1] = pair.value;
		}
		return array;
	}
	
	public PairList<Integer, Double> interpolateToDouble(PairList<Integer, Integer> list) {
		if (list == null)
			return null;
		
		PairList<Integer, Double> converted = new PairList<>();
		for (Pair<Integer, Integer> pair : list) {
			converted.add(pair.key, offGrid.gridMCLength * pair.value);
		}
		
		return converted;
	}
	
	public PairList<Integer, Double> invert(PairList<Integer, Double> list) {
		if (list == null)
			return null;
		
		PairList<Integer, Double> inverted = new PairList<>();
		for (int i = list.size() - 1; i >= 0; i--) {
			Pair<Integer, Double> pair = list.get(i);
			inverted.add(duration - pair.key, pair.value);
		}
		return inverted;
	}
	
	public LittleAdvancedDoor(LittleStructureType type) {
		super(type);
	}
	
	@LTStructureAnnotation(color = ColorUtils.RED)
	public StructureRelative axisCenter;
	
	public PairList<Integer, Double> rotX;
	public PairList<Integer, Double> rotY;
	public PairList<Integer, Double> rotZ;
	
	public LittleGridContext offGrid;
	public PairList<Integer, Integer> offX;
	public PairList<Integer, Integer> offY;
	public PairList<Integer, Integer> offZ;
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		
		if (rotX != null)
			nbt.setIntArray("rotX", savePairListDouble(rotX));
		if (rotY != null)
			nbt.setIntArray("rotY", savePairListDouble(rotY));
		if (rotZ != null)
			nbt.setIntArray("rotZ", savePairListDouble(rotZ));
		
		if (offGrid != null) {
			nbt.setInteger("offGrid", offGrid.size);
			if (offX != null)
				nbt.setIntArray("offX", savePairListInteger(offX));
			if (offY != null)
				nbt.setIntArray("offY", savePairListInteger(offY));
			if (offZ != null)
				nbt.setIntArray("offZ", savePairListInteger(offZ));
		}
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		
		if (nbt.hasKey("rotX"))
			rotX = loadPairListDouble(nbt.getIntArray("rotX"));
		if (nbt.hasKey("rotY"))
			rotY = loadPairListDouble(nbt.getIntArray("rotY"));
		if (nbt.hasKey("rotZ"))
			rotZ = loadPairListDouble(nbt.getIntArray("rotZ"));
		
		if (nbt.hasKey("offGrid")) {
			offGrid = LittleGridContext.get(nbt.getInteger("offGrid"));
			if (nbt.hasKey("offX"))
				offX = loadPairListInteger(nbt.getIntArray("offX"));
			if (nbt.hasKey("offY"))
				offY = loadPairListInteger(nbt.getIntArray("offY"));
			if (nbt.hasKey("offZ"))
				offZ = loadPairListInteger(nbt.getIntArray("offZ"));
		}
	}
	
	@Override
	public boolean tryToPlacePreviews(World world, EntityPlayer player, UUID uuid, StructureAbsolute absolute) {
		LittleAbsolutePreviewsStructure previews = getAbsolutePreviews(getMainTile().te.getPos());
		LittleAdvancedDoor newDoor = (LittleAdvancedDoor) previews.getStructure();
		if (newDoor.axisCenter.getContext().size > previews.context.size)
			previews.convertTo(newDoor.axisCenter.getContext());
		else if (newDoor.axisCenter.getContext().size < previews.context.size)
			newDoor.axisCenter.convertTo(previews.context);
		
		RotationTransformation rotation = rotX != null || rotY != null || rotZ != null ? new RotationTransformation(rotX != null ? rotX.getLast().value : 0, rotY != null ? rotY.getLast().value : 0, rotZ != null ? rotZ.getLast().value : 0) : null;
		OffsetTransformation offset = offX != null || offY != null || offZ != null ? new OffsetTransformation(offX != null ? offX.getLast().value * offGrid.gridMCLength : 0, offY != null ? offY.getLast().value * offGrid.gridMCLength : 0, offZ != null ? offZ.getLast().value * offGrid.gridMCLength : 0) : null;
		
		PairList<Long, Animation> open = new PairList<>();
		open.add(0L, new TimestampAnimation(duration, interpolateToDouble(offX), interpolateToDouble(offY), interpolateToDouble(offZ), rotX, rotY, rotZ));
		
		PairList<Long, Animation> close = new PairList<>();
		close.add(0L, new TimestampAnimation(duration, invert(interpolateToDouble(offX)), invert(interpolateToDouble(offY)), invert(interpolateToDouble(offZ)), invert(rotX), invert(rotY), invert(rotZ)));
		
		DoorController controller = new DoorController(new AnimationState("closed", null, null), new AnimationState("opened", rotation, offset), false, duration, new AnimationTimeline(duration, open), new AnimationTimeline(duration, close));
		
		return place(world, player, previews, controller, uuid, absolute);
	}
	
	@Override
	public StructureAbsolute getAbsoluteAxis() {
		if (axisCenter == null)
			return new StructureAbsolute(getMainTile().te.getPos(), getMainTile().box, getMainTile().getContext());
		return new StructureAbsolute(lastMainTileVec != null ? lastMainTileVec : getMainTile().getAbsolutePos(), axisCenter);
	}
	
	public static class LittleAdvancedDoorParser extends LittleStructureGuiParser {
		
		public LittleGridContext context;
		
		public LittleAdvancedDoorParser(GuiParent parent) {
			super(parent);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(ItemStack stack, LittleStructure structure) {
			LittleAdvancedDoor door = structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null;
			List<TimelineChannel> channels = new ArrayList<>();
			channels.add(new TimelineChannelDouble("rot X").addKeyFixed(0, 0D).addKeys(door != null ? door.rotX : null));
			channels.add(new TimelineChannelDouble("rot Y").addKeyFixed(0, 0D).addKeys(door != null ? door.rotY : null));
			channels.add(new TimelineChannelDouble("rot Z").addKeyFixed(0, 0D).addKeys(door != null ? door.rotZ : null));
			channels.add(new TimelineChannelInteger("off X").addKeyFixed(0, 0).addKeys(door != null ? door.offX : null));
			channels.add(new TimelineChannelInteger("off Y").addKeyFixed(0, 0).addKeys(door != null ? door.offY : null));
			channels.add(new TimelineChannelInteger("off Z").addKeyFixed(0, 0).addKeys(door != null ? door.offZ : null));
			parent.controls.add(new GuiTimeline("timeline", 0, 0, 190, 67, door != null ? door.duration : 50, channels).setSidebarWidth(30));
			parent.controls.add(new GuiLabel("tick", "0", 150, 75));
			
			context = door != null ? (door.offGrid != null ? door.offGrid : LittleGridContext.get()) : LittleGridContext.get();
			parent.controls.add((GuiControl) new GuiTextfield("keyValue", "", 0, 75, 40, 10).setFloatOnly().setEnabled(false));
			parent.controls.add(new GuiLTDistance("keyDistance", 0, 75, context, 0).setVisible(false));
			parent.controls.add(new GuiAxisButton("axis", "open axis", 0, 100, 50, 10, LittleGridContext.get(stack.getTagCompound()), structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null));
			
			parent.controls.add(new GuiLabel("Duration:", 90, 112));
			parent.controls.add(new GuiTextfield("duration_s", structure instanceof LittleAdvancedDoor ? "" + ((LittleDoorBase) structure).duration : "" + 50, 140, 112, 40, 10).setNumbersOnly());
		}
		
		@SideOnly(Side.CLIENT)
		private KeyControl selected;
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onKeySelected(KeySelectedEvent event) {
			GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
			GuiLTDistance distance = (GuiLTDistance) parent.get("keyDistance");
			
			selected = (KeyControl) event.source;
			
			if (((KeyControl) event.source).value instanceof Double) {
				distance.setVisible(false);
				textfield.setEnabled(true);
				textfield.setVisible(true);
				textfield.text = "" + selected.value;
			} else {
				distance.setEnabled(true);
				distance.setVisible(true);
				textfield.setVisible(false);
				
				distance.setDistance(context, (int) selected.value);
			}
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onTextfieldChanges(GuiControlChangedEvent event) {
			if (event.source.is("keyDistance")) {
				
				if (!selected.modifiable)
					return;
				
				GuiLTDistance distance = (GuiLTDistance) event.source;
				LittleGridContext newContext = distance.getDistanceContext();
				if (newContext.size > context.size) {
					int scale = newContext.size / context.size;
					GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
					for (TimelineChannel channel : timeline.channels) {
						if (channel instanceof TimelineChannelInteger) {
							for (Object control : channel.controls) {
								((KeyControl<Integer>) control).value *= scale;
							}
						}
					}
					context = newContext;
				}
				
				int scale = context.size / newContext.size;
				selected.value = distance.getDistance();
			} else if (event.source.is("keyValue")) {
				if (!selected.modifiable)
					return;
				
				try {
					selected.value = Double.parseDouble(((GuiTextfield) event.source).text);
				} catch (NumberFormatException e) {
					
				}
			} else if (event.source.is("duration_s")) {
				try {
					GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
					timeline.setDuration(Integer.parseInt(((GuiTextfield) event.source).text));
				} catch (NumberFormatException e) {
					
				}
			}
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onKeyDeselected(KeyDeselectedEvent event) {
			selected = null;
			GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
			textfield.setEnabled(false);
			textfield.text = "";
			textfield.setCursorPositionZero();
			
			GuiLTDistance distance = (GuiLTDistance) parent.get("keyDistance");
			distance.setEnabled(false);
			distance.resetTextfield();
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void toolTip(GuiToolTipEvent event) {
			if (event.source.is("timeline")) {
				((GuiLabel) parent.get("tick")).caption = event.tooltip.get(0);
				event.CancelEvent();
			}
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleStructure parseStructure(ItemStack stack) {
			LittleAdvancedDoor door = createStructure(LittleAdvancedDoor.class);
			GuiTileViewer viewer = ((GuiAxisButton) parent.get("axis")).viewer;
			door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
			GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
			door.duration = timeline.getDuration();
			door.rotX = timeline.channels.get(0).getPairs();
			door.rotY = timeline.channels.get(1).getPairs();
			door.rotZ = timeline.channels.get(2).getPairs();
			door.offX = timeline.channels.get(3).getPairs();
			door.offY = timeline.channels.get(4).getPairs();
			door.offZ = timeline.channels.get(5).getPairs();
			door.offGrid = context;
			return door;
		}
		
	}
	
}
