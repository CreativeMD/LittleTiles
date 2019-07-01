package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
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
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.gui.controls.GuiLTDistance;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.gui.dialogs.SubGuiDialogAxis.GuiAxisButton;
import com.creativemd.littletiles.common.gui.dialogs.SubGuiDoorEvents.GuiDoorEventsButton;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.LTStructureAnnotation;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.utils.animation.AnimationKey;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.animation.AnimationTimeline;
import com.creativemd.littletiles.common.utils.animation.ValueTimeline;
import com.creativemd.littletiles.common.utils.animation.ValueTimeline.LinearTimeline;
import com.creativemd.littletiles.common.utils.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.utils.animation.event.ChildActivateEvent;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.vec.LittleTransformation;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
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
	
	public static PairList<Integer, Double> loadPairListInteger(int[] array) {
		PairList<Integer, Double> list = new PairList<>();
		int i = 0;
		while (i < array.length) {
			list.add(array[i], (double) array[i + 1]);
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
	
	public ValueTimeline rotX;
	public ValueTimeline rotY;
	public ValueTimeline rotZ;
	
	public LittleGridContext offGrid;
	public ValueTimeline offX;
	public ValueTimeline offY;
	public ValueTimeline offZ;
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		
		NBTTagCompound animation = new NBTTagCompound();
		if (rotX != null)
			animation.setIntArray("rotX", rotX.write());
		if (rotY != null)
			animation.setIntArray("rotY", rotY.write());
		if (rotZ != null)
			animation.setIntArray("rotZ", rotZ.write());
		
		if (offGrid != null) {
			animation.setInteger("offGrid", offGrid.size);
			if (offX != null)
				animation.setIntArray("offX", offX.write());
			if (offY != null)
				animation.setIntArray("offY", offY.write());
			if (offZ != null)
				animation.setIntArray("offZ", offZ.write());
		}
		nbt.setTag("animation", animation);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		
		if (nbt.hasKey("animation")) {
			NBTTagCompound animation = nbt.getCompoundTag("animation");
			if (animation.hasKey("rotX"))
				rotX = ValueTimeline.read(animation.getIntArray("rotX"));
			if (animation.hasKey("rotY"))
				rotY = ValueTimeline.read(animation.getIntArray("rotY"));
			if (animation.hasKey("rotZ"))
				rotZ = ValueTimeline.read(animation.getIntArray("rotZ"));
			
			if (animation.hasKey("offGrid")) {
				offGrid = LittleGridContext.get(animation.getInteger("offGrid"));
				if (animation.hasKey("offX"))
					offX = ValueTimeline.read(animation.getIntArray("offX"));
				if (animation.hasKey("offY"))
					offY = ValueTimeline.read(animation.getIntArray("offY"));
				if (animation.hasKey("offZ"))
					offZ = ValueTimeline.read(animation.getIntArray("offZ"));
			}
		} else { // before pre132
			if (nbt.hasKey("rotX"))
				rotX = new LinearTimeline().addPoints(loadPairListDouble(nbt.getIntArray("rotX")));
			if (nbt.hasKey("rotY"))
				rotY = new LinearTimeline().addPoints(loadPairListDouble(nbt.getIntArray("rotY")));
			if (nbt.hasKey("rotZ"))
				rotZ = new LinearTimeline().addPoints(loadPairListDouble(nbt.getIntArray("rotZ")));
			
			if (nbt.hasKey("offGrid")) {
				offGrid = LittleGridContext.get(nbt.getInteger("offGrid"));
				if (nbt.hasKey("offX"))
					offX = new LinearTimeline().addPoints(loadPairListInteger(nbt.getIntArray("offX")));
				if (nbt.hasKey("offY"))
					offY = new LinearTimeline().addPoints(loadPairListInteger(nbt.getIntArray("offY")));
				if (nbt.hasKey("offZ"))
					offZ = new LinearTimeline().addPoints(loadPairListInteger(nbt.getIntArray("offZ")));
			}
		}
	}
	
	@Override
	public void onFlip(LittleGridContext context, Axis axis, LittleTileVec doubledCenter) {
		super.onFlip(context, axis, doubledCenter);
		
		switch (axis) {
		case X:
			if (rotX != null)
				rotX.flip();
			if (offX != null)
				offX.flip();
			break;
		case Y:
			if (rotY != null)
				rotY.flip();
			if (offY != null)
				offY.flip();
			break;
		case Z:
			if (rotZ != null)
				rotZ.flip();
			if (offZ != null)
				offZ.flip();
			break;
		}
	}
	
	@Override
	public void onRotate(LittleGridContext context, Rotation rotation, LittleTileVec doubledCenter) {
		super.onRotate(context, rotation, doubledCenter);
		ValueTimeline rotX = this.rotX;
		ValueTimeline rotY = this.rotY;
		ValueTimeline rotZ = this.rotZ;
		
		this.rotX = rotation.getX(rotX, rotY, rotZ);
		if (rotation.negativeX() && this.rotX != null)
			this.rotX.flip();
		this.rotY = rotation.getY(rotX, rotY, rotZ);
		if (rotation.negativeY() && this.rotY != null)
			this.rotY.flip();
		this.rotZ = rotation.getZ(rotX, rotY, rotZ);
		if (rotation.negativeZ() && this.rotZ != null)
			this.rotZ.flip();
		
		ValueTimeline offX = this.offX;
		ValueTimeline offY = this.offY;
		ValueTimeline offZ = this.offZ;
		
		this.offX = rotation.getX(offX, offY, offZ);
		if (rotation.negativeX() && this.offX != null)
			this.offX.flip();
		this.offY = rotation.getY(offX, offY, offZ);
		if (rotation.negativeY() && this.offY != null)
			this.offY.flip();
		this.offZ = rotation.getZ(offX, offY, offZ);
		if (rotation.negativeZ() && this.offZ != null)
			this.offZ.flip();
	}
	
	@Override
	public LittleTransformation[] getDoorTransformations(EntityPlayer player) {
		return new LittleTransformation[] {
		        new LittleTransformation(getMainTile().te.getPos(), 0, 0, 0, new LittleTileVec(0, 0, 0), new LittleTileVecContext()) };
	}
	
	@Override
	public void transformDoorPreview(LittleAbsolutePreviewsStructure previews, LittleTransformation transformation) {
		LittleAdvancedDoor newDoor = (LittleAdvancedDoor) previews.getStructure();
		if (newDoor.axisCenter.getContext().size > previews.context.size)
			previews.convertTo(newDoor.axisCenter.getContext());
		else if (newDoor.axisCenter.getContext().size < previews.context.size)
			newDoor.axisCenter.convertTo(previews.context);
	}
	
	@Override
	public DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, LittleAbsolutePreviewsStructure previews, LittleTransformation transformation, int completeDuration) {
		LittleAdvancedDoor newDoor = (LittleAdvancedDoor) previews.getStructure();
		int duration = newDoor.duration;
		
		PairList<AnimationKey, ValueTimeline> open = new PairList<>();
		PairList<AnimationKey, ValueTimeline> close = new PairList<>();
		
		AnimationState opened = new AnimationState();
		if (offX != null) {
			opened.set(AnimationKey.offX, offGrid.toVanillaGrid(offX.last()));
			open.add(AnimationKey.offX, offX.copy().factor(offGrid.gridMCLength));
			close.add(AnimationKey.offX, offX.invert(duration).factor(offGrid.gridMCLength));
		}
		if (offY != null) {
			opened.set(AnimationKey.offY, offGrid.toVanillaGrid(offY.last()));
			open.add(AnimationKey.offY, offY.copy().factor(offGrid.gridMCLength));
			close.add(AnimationKey.offY, offY.invert(duration).factor(offGrid.gridMCLength));
		}
		if (offZ != null) {
			opened.set(AnimationKey.offZ, offGrid.toVanillaGrid(offZ.last()));
			open.add(AnimationKey.offZ, offZ.copy().factor(offGrid.gridMCLength));
			close.add(AnimationKey.offZ, offZ.invert(duration).factor(offGrid.gridMCLength));
		}
		if (rotX != null) {
			opened.set(AnimationKey.rotX, rotX.last());
			open.add(AnimationKey.rotX, rotX);
			close.add(AnimationKey.rotX, rotX.invert(duration));
		}
		if (rotY != null) {
			opened.set(AnimationKey.rotY, rotY.last());
			open.add(AnimationKey.rotY, rotY);
			close.add(AnimationKey.rotY, rotY.invert(duration));
		}
		if (rotZ != null) {
			opened.set(AnimationKey.rotZ, rotZ.last());
			open.add(AnimationKey.rotZ, rotZ);
			close.add(AnimationKey.rotZ, rotZ.invert(duration));
		}
		
		return new DoorController(result, supplier, new AnimationState(), opened, stayAnimated ? null : false, duration, completeDuration, new AnimationTimeline(duration, open), new AnimationTimeline(duration, close));
	}
	
	@Override
	public StructureAbsolute getAbsoluteAxis() {
		if (axisCenter == null)
			return new StructureAbsolute(getMainTile().te.getPos(), getMainTile().box, getMainTile().getContext());
		return new StructureAbsolute(lastMainTileVec != null ? lastMainTileVec : getMainTile().getAbsolutePos(), axisCenter);
	}
	
	public static class LittleAdvancedDoorParser extends LittleStructureGuiParser {
		
		public LittleGridContext context;
		
		public LittleAdvancedDoorParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			LittleAdvancedDoor door = structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null;
			List<TimelineChannel> channels = new ArrayList<>();
			channels.add(new TimelineChannelDouble("rot X").addKeyFixed(0, 0D).addKeys(door != null && door.rotX != null ? door.rotX.getPointsCopy() : null));
			channels.add(new TimelineChannelDouble("rot Y").addKeyFixed(0, 0D).addKeys(door != null && door.rotY != null ? door.rotY.getPointsCopy() : null));
			channels.add(new TimelineChannelDouble("rot Z").addKeyFixed(0, 0D).addKeys(door != null && door.rotZ != null ? door.rotZ.getPointsCopy() : null));
			channels.add(new TimelineChannelInteger("off X").addKeyFixed(0, 0).addKeys(door != null && door.offX != null ? door.offX.getRoundedPointsCopy() : null));
			channels.add(new TimelineChannelInteger("off Y").addKeyFixed(0, 0).addKeys(door != null && door.offY != null ? door.offY.getRoundedPointsCopy() : null));
			channels.add(new TimelineChannelInteger("off Z").addKeyFixed(0, 0).addKeys(door != null && door.offZ != null ? door.offZ.getRoundedPointsCopy() : null));
			parent.controls.add(new GuiTimeline("timeline", 0, 0, 190, 67, door != null ? door.duration : 50, channels, handler).setSidebarWidth(30));
			parent.controls.add(new GuiLabel("tick", "0", 150, 75));
			
			context = door != null ? (door.offGrid != null ? door.offGrid : LittleGridContext.get()) : LittleGridContext.get();
			parent.controls.add((GuiControl) new GuiTextfield("keyValue", "", 0, 75, 40, 10).setFloatOnly().setEnabled(false));
			parent.controls.add(new GuiLTDistance("keyDistance", 0, 75, context, 0).setVisible(false));
			
			parent.controls.add(new GuiLabel("Position:", 90, 90));
			parent.controls.add((GuiControl) new GuiTextfield("keyPosition", "", 149, 90, 40, 10).setNumbersOnly().setEnabled(false));
			
			parent.controls.add(new GuiAxisButton("axis", "open axis", 0, 100, 50, 10, previews.context, structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null, handler));
			
			parent.controls.add(new GuiCheckBox("stayAnimated", CoreControl.translate("gui.door.stayAnimated"), 0, 120, structure instanceof LittleAdvancedDoor ? ((LittleDoorBase) structure).stayAnimated : false).setCustomTooltip(CoreControl.translate("gui.door.stayAnimatedTooltip")));
			parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
			parent.controls.add(new GuiTextfield("duration_s", structure instanceof LittleAdvancedDoor ? "" + ((LittleDoorBase) structure).duration : "" + 50, 149, 118, 40, 10).setNumbersOnly());
			parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
			updateTimeline();
		}
		
		public void updateTimeline() {
			GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
			GuiDoorEventsButton children = (GuiDoorEventsButton) parent.get("children_activate");
			AnimationTimeline animation = new AnimationTimeline(timeline.getDuration(), new PairList<>());
			
			ValueTimeline rotX = ValueTimeline.create(0, timeline.channels.get(0).getPairs());
			if (rotX != null)
				animation.values.add(AnimationKey.rotX, rotX);
			
			ValueTimeline rotY = ValueTimeline.create(0, timeline.channels.get(1).getPairs());
			if (rotY != null)
				animation.values.add(AnimationKey.rotY, rotY);
			
			ValueTimeline rotZ = ValueTimeline.create(0, timeline.channels.get(2).getPairs());
			if (rotZ != null)
				animation.values.add(AnimationKey.rotZ, rotZ);
			
			ValueTimeline offX = ValueTimeline.create(0, timeline.channels.get(3).getPairs());
			if (offX != null)
				animation.values.add(AnimationKey.offX, offX.factor(context.gridMCLength));
			
			ValueTimeline offY = ValueTimeline.create(0, timeline.channels.get(4).getPairs());
			if (offY != null)
				animation.values.add(AnimationKey.offY, offY.factor(context.gridMCLength));
			
			ValueTimeline offZ = ValueTimeline.create(0, timeline.channels.get(5).getPairs());
			if (offZ != null)
				animation.values.add(AnimationKey.offZ, offZ.factor(context.gridMCLength));
			
			handler.setTimeline(animation, children.events);
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
			
			GuiTextfield position = (GuiTextfield) parent.get("keyPosition");
			position.setEnabled(true);
			position.text = "" + selected.tick;
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onChange(GuiControlChangedEvent event) {
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
			} else if (event.source.is("keyPosition")) {
				if (!selected.modifiable)
					return;
				
				try {
					GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
					
					int tick = selected.tick;
					selected.tick = Integer.parseInt(((GuiTextfield) event.source).text);
					if (tick != selected.tick)
						timeline.adjustKeysPositionX();
				} catch (NumberFormatException e) {
					
				}
			} else if (event.source.is("duration_s")) {
				try {
					GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
					timeline.setDuration(Integer.parseInt(((GuiTextfield) event.source).text));
				} catch (NumberFormatException e) {
					
				}
			} else if (event.source.is("timeline") || event.source.is("children_activate"))
				updateTimeline();
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onKeyDeselected(KeyDeselectedEvent event) {
			selected = null;
			GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
			textfield.setEnabled(false);
			textfield.text = "";
			textfield.setCursorPositionZero();
			
			textfield = (GuiTextfield) parent.get("keyPosition");
			textfield.setEnabled(false);
			textfield.text = "";
			textfield.setCursorPositionZero();
			
			GuiLTDistance distance = (GuiLTDistance) parent.get("keyDistance");
			distance.setEnabled(false);
			distance.resetTextfield();
			
			updateTimeline();
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
		public LittleStructure parseStructure(LittlePreviews previews) {
			LittleAdvancedDoor door = createStructure(LittleAdvancedDoor.class);
			GuiTileViewer viewer = ((GuiAxisButton) parent.get("axis")).viewer;
			GuiDoorEventsButton button = (GuiDoorEventsButton) parent.get("children_activate");
			door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
			GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
			door.duration = timeline.getDuration();
			GuiCheckBox checkBox = (GuiCheckBox) parent.get("stayAnimated");
			door.stayAnimated = checkBox.value;
			door.events = button.events;
			List<LittlePreviews> previewChildren = previews.getChildren();
			
			if (!previewChildren.isEmpty()) {
				BitSet set = new BitSet(previewChildren.size());
				for (AnimationEvent event : door.events)
					if (event instanceof ChildActivateEvent)
						set.set(((ChildActivateEvent) event).childId);
					
				for (int i = 0; i < previewChildren.size(); i++)
					if (set.get(i))
						previewChildren.get(i).getStructureData().setBoolean("activateParent", true);
					else
						previewChildren.get(i).getStructureData().removeTag("activateParent");
			}
			
			door.rotX = ValueTimeline.create(0, timeline.channels.get(0).getPairs());
			door.rotY = ValueTimeline.create(0, timeline.channels.get(1).getPairs());
			door.rotZ = ValueTimeline.create(0, timeline.channels.get(2).getPairs());
			door.offX = ValueTimeline.create(0, timeline.channels.get(3).getPairs());
			door.offY = ValueTimeline.create(0, timeline.channels.get(4).getPairs());
			door.offZ = ValueTimeline.create(0, timeline.channels.get(5).getPairs());
			door.offGrid = context;
			return door;
		}
		
	}
	
}
