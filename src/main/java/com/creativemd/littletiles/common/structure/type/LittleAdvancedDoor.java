package com.creativemd.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTimeline;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTimeline.KeyControl;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTimeline.KeyDeselectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTimeline.KeySelectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTimeline.TimelineChannel;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiToolTipEvent;
import com.creativemd.creativecore.common.utils.math.interpolation.Interpolation;
import com.creativemd.creativecore.common.utils.math.interpolation.LinearInterpolation;
import com.creativemd.creativecore.common.utils.math.vec.Vec1;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.entity.DoorController;
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

public class LittleAdvancedDoor extends LittleDoorBase {
	
	public static PairList<Integer, Double> loadPairList(int[] array) {
		PairList<Integer, Double> list = new PairList<>();
		int i = 0;
		while (i < array.length) {
			list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
			i += 3;
		}
		return list;
	}
	
	public static PairList<Integer, Double> loadPairList(int[] array, int from, int length) {
		PairList<Integer, Double> list = new PairList<>();
		int i = from;
		while (i < length) {
			list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
			i += 3;
		}
		return list;
	}
	
	public static int[] savePairList(PairList<Integer, Double> list) {
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
	
	public Interpolation<Vec1> convertToInterpolation(PairList<Integer, Double> list) {
		if (list == null)
			return null;
		
		double[] times = new double[list.size()];
		Vec1[] points = new Vec1[list.size()];
		double tickTime = 1D / duration;
		int i = 0;
		for (Pair<Integer, Double> pair : list) {
			times[i] = pair.key * tickTime;
			points[i] = new Vec1(pair.value);
			i++;
		}
		return new LinearInterpolation<Vec1>(times, points);
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
	
	public PairList<Integer, Double> offX;
	public PairList<Integer, Double> offY;
	public PairList<Integer, Double> offZ;
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		
		if (rotX != null)
			nbt.setIntArray("rotX", savePairList(rotX));
		if (rotY != null)
			nbt.setIntArray("rotY", savePairList(rotY));
		if (rotZ != null)
			nbt.setIntArray("rotZ", savePairList(rotZ));
		
		if (offX != null)
			nbt.setIntArray("offX", savePairList(offX));
		if (offY != null)
			nbt.setIntArray("offY", savePairList(offY));
		if (offZ != null)
			nbt.setIntArray("offZ", savePairList(offZ));
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		
		if (nbt.hasKey("rotX"))
			rotX = loadPairList(nbt.getIntArray("rotX"));
		if (nbt.hasKey("rotY"))
			rotY = loadPairList(nbt.getIntArray("rotY"));
		if (nbt.hasKey("rotZ"))
			rotZ = loadPairList(nbt.getIntArray("rotZ"));
		
		if (nbt.hasKey("offX"))
			offX = loadPairList(nbt.getIntArray("offX"));
		if (nbt.hasKey("offY"))
			offY = loadPairList(nbt.getIntArray("offY"));
		if (nbt.hasKey("offZ"))
			offZ = loadPairList(nbt.getIntArray("offZ"));
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
		OffsetTransformation offset = offX != null || offY != null || offZ != null ? new OffsetTransformation(offX != null ? offX.getLast().value : 0, offY != null ? offY.getLast().value : 0, offZ != null ? offZ.getLast().value : 0) : null;
		
		PairList<Long, Animation> open = new PairList<>();
		open.add(0L, new TimestampAnimation(duration, offX, offY, offZ, rotX, rotY, rotZ));
		
		PairList<Long, Animation> close = new PairList<>();
		close.add(0L, new TimestampAnimation(duration, invert(offX), invert(offY), invert(offZ), invert(rotX), invert(rotY), invert(rotZ)));
		
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
		
		public LittleAdvancedDoorParser(GuiParent parent) {
			super(parent);
		}
		
		@Override
		public void createControls(ItemStack stack, LittleStructure structure) {
			LittleAdvancedDoor door = structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null;
			List<TimelineChannel> channels = new ArrayList<>();
			channels.add(new TimelineChannel("rot X").addKeys(door != null ? door.rotX : null));
			channels.add(new TimelineChannel("rot Y").addKeys(door != null ? door.rotY : null));
			channels.add(new TimelineChannel("rot Z").addKeys(door != null ? door.rotZ : null));
			channels.add(new TimelineChannel("off X").addKeys(door != null ? door.offX : null));
			channels.add(new TimelineChannel("off Y").addKeys(door != null ? door.offY : null));
			channels.add(new TimelineChannel("off Z").addKeys(door != null ? door.offZ : null));
			parent.controls.add(new GuiTimeline("timeline", 0, 0, 190, 67, door != null ? door.duration : 50, channels).setSidebarWidth(30));
			parent.controls.add(new GuiLabel("tick", "0", 150, 75));
			parent.controls.add((GuiControl) new GuiTextfield("keyValue", "", 0, 75, 40, 10).setFloatOnly().setEnabled(false));
			parent.controls.add(new GuiAxisButton("axis", "open axis", 0, 100, 50, 10, LittleGridContext.get(stack.getTagCompound()), structure instanceof LittleAdvancedDoor ? (LittleAdvancedDoor) structure : null));
			
			parent.controls.add(new GuiLabel("Duration:", 90, 112));
			parent.controls.add(new GuiTextfield("duration_s", structure instanceof LittleAdvancedDoor ? "" + ((LittleDoorBase) structure).duration : "" + 50, 140, 112, 40, 10).setNumbersOnly());
		}
		
		private KeyControl selected;
		
		@CustomEventSubscribe
		public void onKeySelected(KeySelectedEvent event) {
			GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
			textfield.setEnabled(true);
			selected = (KeyControl) event.source;
			textfield.text = "" + selected.value;
		}
		
		@CustomEventSubscribe
		public void onTextfieldChanges(GuiControlChangedEvent event) {
			if (event.source.is("keyValue")) {
				try {
					selected.value = Double.parseDouble(((GuiTextfield) event.source).text);
				} catch (NumberFormatException e) {
					
				}
			}
		}
		
		@CustomEventSubscribe
		public void onKeyDeselected(KeyDeselectedEvent event) {
			selected = null;
			GuiTextfield textfield = (GuiTextfield) parent.get("keyValue");
			textfield.setEnabled(false);
			textfield.text = "";
			textfield.setCursorPositionZero();
		}
		
		@CustomEventSubscribe
		public void toolTip(GuiToolTipEvent event) {
			if (event.source.is("timeline")) {
				((GuiLabel) parent.get("tick")).caption = event.tooltip.get(0);
				event.CancelEvent();
			}
		}
		
		@Override
		public LittleStructure parseStructure(ItemStack stack) {
			GuiTextfield textfield = (GuiTextfield) parent.get("duration_s");
			int duration = 0;
			try {
				duration = Integer.parseInt(textfield.text);
			} catch (NumberFormatException e) {
				
			}
			LittleAdvancedDoor door = createStructure(LittleAdvancedDoor.class);
			GuiTileViewer viewer = ((GuiAxisButton) parent.get("axis")).viewer;
			door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
			door.duration = duration;
			GuiTimeline timeline = (GuiTimeline) parent.get("timeline");
			door.rotX = timeline.channels.get(0).getPairs();
			door.rotY = timeline.channels.get(1).getPairs();
			door.rotZ = timeline.channels.get(2).getPairs();
			door.offX = timeline.channels.get(3).getPairs();
			door.offY = timeline.channels.get(4).getPairs();
			door.offZ = timeline.channels.get(5).getPairs();
			return door;
		}
		
	}
	
}
