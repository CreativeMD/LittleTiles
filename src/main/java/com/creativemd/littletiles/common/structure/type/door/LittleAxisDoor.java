package com.creativemd.littletiles.common.structure.type.door;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTabStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.client.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.client.gui.controls.GuiTileViewer.GuiTileViewerAxisChangedEvent;
import com.creativemd.littletiles.common.entity.AnimationPreview;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.animation.AnimationKey;
import com.creativemd.littletiles.common.structure.animation.AnimationState;
import com.creativemd.littletiles.common.structure.animation.AnimationTimeline;
import com.creativemd.littletiles.common.structure.animation.ValueTimeline;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviewRelativeAxis;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleAxisDoor extends LittleDoorBase {
	
	public LittleAxisDoor(LittleStructureType type) {
		super(type);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		
		if (nbt.hasKey("ndirection")) {
			doorRotation = new PlayerOrientatedRotation();
			((PlayerOrientatedRotation) doorRotation).normalAxis = EnumFacing.getFront(nbt.getInteger("ndirection")).getAxis();
		} else
			doorRotation = parseRotation(nbt);
	}
	
	@Override
	protected Object failedLoadingRelative(NBTTagCompound nbt, StructureDirectionalField relative) {
		if (relative.key.equals("axisCenter")) {
			
			LittleRelativeDoubledAxis doubledRelativeAxis;
			if (nbt.hasKey("ax")) {
				LittleVecContext vec = new LittleVecContext("a", nbt);
				if (getMainTile() != null)
					vec.sub(new LittleVecContext(getMainTile().getMinVec(), getMainTile().getContext()));
				doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.getContext(), vec.getVec(), new LittleVec(1, 1, 1));
				
			} else if (nbt.hasKey("av")) {
				LittleVecContext vec = new LittleVecContext("av", nbt);
				doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.getContext(), vec.getVec(), new LittleVec(1, 1, 1));
			} else {
				doubledRelativeAxis = new LittleRelativeDoubledAxis("avec", nbt);
			}
			return new StructureRelative(StructureAbsolute.convertAxisToBox(doubledRelativeAxis.getNonDoubledVec(), doubledRelativeAxis.additional), doubledRelativeAxis.getContext());
		} else
			return super.failedLoadingRelative(nbt, relative);
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		doorRotation.writeToNBT(nbt);
	}
	
	public AxisDoorRotation doorRotation;
	@StructureDirectional
	public Axis axis;
	@StructureDirectional(color = ColorUtils.RED)
	public StructureRelative axisCenter;
	
	@Override
	public StructureAbsolute getAbsoluteAxis() {
		return new StructureAbsolute(lastMainTileVec != null ? lastMainTileVec : getMainTile().getAbsolutePos(), axisCenter);
	}
	
	@Override
	public DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, Placement placement, LittleTransformation transformation, int completeDuration) {
		return doorRotation.createController(result, supplier, transformation.getRotation(axis), this, completeDuration, interpolation);
	}
	
	@Override
	public LittleTransformation[] getDoorTransformations(@Nullable EntityPlayer player) {
		return doorRotation.getDoorTransformations(this, player);
	}
	
	@Override
	public void transformDoorPreview(LittleAbsolutePreviews previews, LittleTransformation transformation) {
		StructureRelative axisCenter = (StructureRelative) previews.getStructureType().loadDirectional(previews, "axisCenter");
		axisCenter.forceContext(previews);
		transformation.doubledRotationCenter = axisCenter.getDoubledCenterVec();
	}
	
	@Deprecated
	public static class LittleRelativeDoubledAxis extends LittleVecContext {
		
		public LittleVec additional;
		
		public LittleRelativeDoubledAxis(LittleGridContext context, LittleVec vec, LittleVec additional) {
			super(vec, context);
			this.additional = additional;
		}
		
		public LittleRelativeDoubledAxis(String name, NBTTagCompound nbt) {
			super();
			
			int[] array = nbt.getIntArray(name);
			if (array.length == 3) {
				this.vec = new LittleVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get();
				this.additional = new LittleVec(vec.x % 2, vec.y % 2, vec.z % 2);
				this.vec.x /= 2;
				this.vec.y /= 2;
				this.vec.z /= 2;
			} else if (array.length == 4) {
				this.vec = new LittleVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get(array[3]);
				this.additional = new LittleVec(vec.x % 2, vec.y % 2, vec.z % 2);
				this.vec.x /= 2;
				this.vec.y /= 2;
				this.vec.z /= 2;
			} else if (array.length == 7) {
				this.vec = new LittleVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get(array[3]);
				this.additional = new LittleVec(array[4], array[5], array[6]);
			} else
				throw new InvalidParameterException("No valid coords given " + nbt);
		}
		
		public LittleVecContext getNonDoubledVec() {
			return new LittleVecContext(vec.copy(), context);
		}
		
		public LittleVec getRotationVec() {
			LittleVec vec = this.vec.copy();
			vec.scale(2);
			vec.add(additional);
			return vec;
		}
		
		@Override
		public LittleRelativeDoubledAxis copy() {
			return new LittleRelativeDoubledAxis(context, vec.copy(), additional.copy());
		}
		
		@Override
		public void convertTo(LittleGridContext to) {
			LittleVec newVec = getRotationVec();
			newVec.convertTo(context, to);
			super.convertTo(to);
			additional = new LittleVec(newVec.x % 2, newVec.y % 2, newVec.z % 2);
			vec = newVec;
			vec.x /= 2;
			vec.y /= 2;
			vec.z /= 2;
		}
		
		@Override
		public void convertToSmallest() {
			if (isEven())
				super.convertToSmallest();
		}
		
		public int getSmallestContext() {
			if (isEven())
				return vec.getSmallestContext(this.context);
			return this.context.size;
		}
		
		@Override
		public boolean equals(Object paramObject) {
			if (paramObject instanceof LittleRelativeDoubledAxis)
				return super.equals(paramObject) && additional.equals(((LittleRelativeDoubledAxis) paramObject).additional);
			return false;
		}
		
		public boolean isEven() {
			return additional.x % 2 == 0;
		}
		
		@Override
		public void writeToNBT(String name, NBTTagCompound nbt) {
			nbt.setIntArray(name, new int[] { vec.x, vec.y, vec.z, context.size, additional.x, additional.y, additional.z });
		}
		
		@Override
		public String toString() {
			return "[" + vec.x + "," + vec.y + "," + vec.z + ",grid:" + context.size + ",additional:" + additional + "]";
		}
	}
	
	public static class LittleAxisDoorParser extends LittleDoorBaseParser {
		
		public LittleAxisDoorParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleAxisDoor parseStructure() {
			LittleAxisDoor door = createStructure(LittleAxisDoor.class);
			GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
			door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
			door.axis = viewer.getAxis();
			
			GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
			door.doorRotation = createRotation(((GuiTabStateButton) parent.get("doorRotation")).getState());
			door.doorRotation.parseGui(viewer, typePanel);
			return door;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			LittleAxisDoor door = null;
			if (structure instanceof LittleAxisDoor)
				door = (LittleAxisDoor) structure;
			
			LittleGridContext stackContext = previews.getContext();
			LittleGridContext axisContext = stackContext;
			
			GuiTileViewer viewer = new GuiTileViewer("tileviewer", 0, 0, 100, 100, stackContext);
			parent.addControl(viewer);
			boolean even = false;
			AxisDoorRotation doorRotation;
			if (door != null) {
				even = door.axisCenter.isEven();
				viewer.setEven(even);
				
				door.axisCenter.convertToSmallest();
				axisContext = door.axisCenter.getContext();
				viewer.setViewAxis(door.axis);
				viewer.setAxis(door.axisCenter.getBox(), axisContext);
				
				doorRotation = door.doorRotation;
				
			} else {
				viewer.setEven(false);
				viewer.setAxis(new LittleBox(0, 0, 0, 1, 1, 1), viewer.context);
				doorRotation = new PlayerOrientatedRotation();
			}
			viewer.visibleAxis = true;
			
			parent.addControl(new GuiTabStateButton("doorRotation", rotationTypes.indexOf(doorRotation.getClass()), 110, 0, 12, rotationTypeNames.toArray(new String[0])));
			
			GuiPanel typePanel = new GuiPanel("typePanel", 110, 20, 80, 25);
			parent.addControl(typePanel);
			parent.addControl(new GuiIconButton("reset view", 20, 107, 8) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.offsetX.set(0);
					viewer.offsetY.set(0);
					viewer.scale.set(40);
				}
			}.setCustomTooltip("reset view"));
			parent.addControl(new GuiIconButton("change view", 40, 107, 7) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					switch (viewer.getAxis()) {
					case X:
						viewer.setViewAxis(EnumFacing.Axis.Y);
						break;
					case Y:
						viewer.setViewAxis(EnumFacing.Axis.Z);
						break;
					case Z:
						viewer.setViewAxis(EnumFacing.Axis.X);
						break;
					default:
						break;
					}
					updateTimeline();
				}
			}.setCustomTooltip("change view"));
			parent.addControl(new GuiIconButton("flip view", 60, 107, 4) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.setViewDirection(viewer.getViewDirection().getOpposite());
				}
			}.setCustomTooltip("flip view"));
			
			parent.addControl(new GuiIconButton("up", 124, 58, 1) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveY(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1);
				}
			});
			
			parent.addControl(new GuiIconButton("right", 141, 75, 0) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveX(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1);
				}
			});
			
			parent.addControl(new GuiIconButton("left", 107, 75, 2) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveX(-(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1));
				}
			});
			
			parent.addControl(new GuiIconButton("down", 124, 75, 3) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveY(-(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1));
				}
			});
			
			parent.controls.add(new GuiCheckBox("even", 147, 55, even));
			
			GuiStateButton contextBox = new GuiStateButton("grid", LittleGridContext.getNames().indexOf(axisContext + ""), 170, 75, 20, 12, LittleGridContext.getNames().toArray(new String[0]));
			parent.controls.add(contextBox);
			
			doorRotation.onSelected(viewer, typePanel);
			
			super.createControls(previews, structure);
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onAxisChanged(GuiTileViewerAxisChangedEvent event) {
			GuiTileViewer viewer = (GuiTileViewer) event.source;
			handler.setCenter(new StructureAbsolute(new BlockPos(0, 75, 0), viewer.getBox().copy(), viewer.getAxisContext()));
		}
		
		@Override
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onChanged(GuiControlChangedEvent event) {
			super.onChanged(event);
			AxisDoorRotation rotation = createRotation(((GuiTabStateButton) parent.get("doorRotation")).getState());
			if (rotation.shouldUpdateTimeline((GuiControl) event.source))
				updateTimeline();
			else if (event.source.is("doorRotation")) {
				GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
				typePanel.controls.clear();
				rotation.onSelected((GuiTileViewer) parent.get("tileviewer"), typePanel);
				updateTimeline();
			} else if (event.source.is("grid")) {
				GuiStateButton contextBox = (GuiStateButton) event.source;
				LittleGridContext context;
				try {
					context = LittleGridContext.get(Integer.parseInt(contextBox.caption));
				} catch (NumberFormatException e) {
					context = LittleGridContext.get();
				}
				
				GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
				LittleBox box = viewer.getBox();
				box.convertTo(viewer.getAxisContext(), context);
				
				if (viewer.isEven())
					box.maxX = box.minX + 2;
				else
					box.maxX = box.minX + 1;
				
				if (viewer.isEven())
					box.maxY = box.minY + 2;
				else
					box.maxY = box.minY + 1;
				
				if (viewer.isEven())
					box.maxZ = box.minZ + 2;
				else
					box.maxZ = box.minZ + 1;
				
				viewer.setAxis(box, context);
			}
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onButtonClicked(GuiControlClickEvent event) {
			GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
			if (event.source.is("even")) {
				viewer.setEven(((GuiCheckBox) event.source).value);
			}
		}
		
		@Override
		public void populateTimeline(AnimationTimeline timeline, int interpolation) {
			GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
			GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
			AxisDoorRotation doorRotation = createRotation(((GuiTabStateButton) parent.get("doorRotation")).getState());
			doorRotation.parseGui(viewer, typePanel);
			
			doorRotation.populateTimeline(timeline, timeline.duration, interpolation, AnimationKey.getRotation(viewer.getAxis()));
		}
		
		@Override
		public void onLoaded(AnimationPreview animationPreview) {
			super.onLoaded(animationPreview);
			GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
			onAxisChanged(new GuiTileViewerAxisChangedEvent(viewer));
		}
	}
	
	private static List<Class<? extends AxisDoorRotation>> rotationTypes = new ArrayList<>();
	private static List<String> rotationTypeNames = new ArrayList<>();
	static {
		rotationTypes.add(PlayerOrientatedRotation.class);
		rotationTypeNames.add("orientated");
		rotationTypes.add(FixedRotation.class);
		rotationTypeNames.add("fixed");
	}
	
	protected static AxisDoorRotation parseRotation(NBTTagCompound nbt) {
		int index = nbt.getInteger("rot-type");
		
		if (index >= 0 && index < rotationTypes.size()) {
			Class<? extends AxisDoorRotation> rotationType = rotationTypes.get(index);
			try {
				AxisDoorRotation rotation = rotationType.getConstructor().newInstance();
				rotation.readFromNBT(nbt);
				return rotation;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		throw new RuntimeException("Invalid axis door rotation found index: " + index);
	}
	
	protected static AxisDoorRotation createRotation(int index) {
		if (index >= 0 && index < rotationTypes.size()) {
			Class<? extends AxisDoorRotation> rotationType = rotationTypes.get(index);
			try {
				return rotationType.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		throw new RuntimeException("Invalid axis door rotation found index: " + index);
	}
	
	public abstract static class AxisDoorRotation {
		
		protected abstract void writeToNBTCore(NBTTagCompound nbt);
		
		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("rot-type", rotationTypes.indexOf(this.getClass()));
			writeToNBTCore(nbt);
		}
		
		protected abstract void readFromNBT(NBTTagCompound nbt);
		
		protected abstract void rotate(Axis doorAxis, Rotation rotation);
		
		protected abstract void flip(Axis doorAxis, Axis axis);
		
		protected abstract boolean shouldRotatePreviews(LittleAxisDoor door);
		
		protected abstract DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, Rotation rotation, LittleAxisDoor door, int completeDuration, int interpolation);
		
		protected abstract LittleTransformation[] getDoorTransformations(LittleAxisDoor door, @Nullable EntityPlayer player);
		
		@SideOnly(Side.CLIENT)
		public abstract boolean shouldUpdateTimeline(GuiControl control);
		
		@SideOnly(Side.CLIENT)
		protected abstract void onSelected(GuiTileViewer viewer, GuiParent parent);
		
		@SideOnly(Side.CLIENT)
		protected abstract void parseGui(GuiTileViewer viewer, GuiParent parent);
		
		public abstract void populateTimeline(AnimationTimeline timeline, int duration, int interpolation, AnimationKey key);
		
	}
	
	public static class PlayerOrientatedRotation extends AxisDoorRotation {
		
		public Axis normalAxis;
		
		@Override
		protected void writeToNBTCore(NBTTagCompound nbt) {
			nbt.setInteger("normal", normalAxis.ordinal());
		}
		
		@Override
		protected void readFromNBT(NBTTagCompound nbt) {
			normalAxis = Axis.values()[nbt.getInteger("normal")];
		}
		
		@Override
		protected void rotate(Axis doorAxis, Rotation rotation) {
			this.normalAxis = RotationUtils.rotate(normalAxis, rotation);
		}
		
		@Override
		protected void flip(Axis doorAxis, Axis axis) {
			
		}
		
		protected Rotation getRotation(EntityPlayer player, LittleAxisDoor door, StructureAbsolute absolute) {
			Vector3d axisVec = absolute.getCenter();
			Vec3d playerVec = player.getPositionVector();
			double playerRotation = MathHelper.wrapDegrees(player.rotationYaw);
			boolean clockwise;
			Axis third = RotationUtils.getDifferentAxis(door.axis, normalAxis);
			boolean toTheSide = RotationUtils.get(third, playerVec) <= RotationUtils.get(third, axisVec);
			
			switch (third) {
			case X:
				clockwise = !(playerRotation <= -90 || playerRotation >= 90);
				break;
			case Y:
				clockwise = player.rotationPitch <= 0;
				break;
			case Z:
				clockwise = playerRotation > 0 && playerRotation <= 180;
				break;
			default:
				clockwise = false;
				break;
			}
			return Rotation.getRotation(door.axis, toTheSide == clockwise);
		}
		
		protected Rotation getDefaultRotation(LittleAxisDoor door, StructureAbsolute absolute) {
			return Rotation.getRotation(door.axis, true);
		}
		
		@Override
		protected DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, Rotation rotation, LittleAxisDoor door, int completeDuration, int interpolation) {
			if (door.stayAnimated)
				return new DoorController(result, supplier, new AnimationState(), new AnimationState().set(AnimationKey.getRotation(rotation.axis), rotation.clockwise ? 90 : -90), null, door.duration, completeDuration, interpolation);
			return new DoorController(result, supplier, new AnimationState().set(AnimationKey.getRotation(rotation.axis), rotation.clockwise ? -90 : 90), new AnimationState(), true, door.duration, completeDuration, interpolation);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		protected void onSelected(GuiTileViewer viewer, GuiParent parent) {
			parent.addControl(new GuiButton("swap normal", 0, 0) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.changeNormalAxis();
				}
				
			});
			
			if (normalAxis != null)
				viewer.setNormalAxis(normalAxis);
			viewer.visibleNormalAxis = true;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		protected void parseGui(GuiTileViewer viewer, GuiParent parent) {
			normalAxis = viewer.getNormalAxis();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean shouldUpdateTimeline(GuiControl control) {
			return false;
		}
		
		@Override
		protected boolean shouldRotatePreviews(LittleAxisDoor door) {
			return !door.stayAnimated;
		}
		
		@Override
		public void populateTimeline(AnimationTimeline timeline, int duration, int interpolation, AnimationKey key) {
			timeline.values.add(key, ValueTimeline.create(interpolation).addPoint(0, 0D).addPoint(duration, 90D));
		}
		
		@Override
		protected LittleTransformation[] getDoorTransformations(LittleAxisDoor door, EntityPlayer player) {
			StructureAbsolute absolute = door.getAbsoluteAxis();
			Rotation rotation = player != null ? getRotation(player, door, absolute) : getDefaultRotation(door, absolute);
			return new LittleTransformation[] { new LittleTransformation(door.getMainTile().te.getPos(), rotation), new LittleTransformation(door.getMainTile().te.getPos(), rotation.getOpposite()) };
		}
		
	}
	
	public static class FixedRotation extends AxisDoorRotation {
		
		public double degree;
		
		@Override
		protected void writeToNBTCore(NBTTagCompound nbt) {
			nbt.setDouble("degree", degree);
		}
		
		@Override
		protected void readFromNBT(NBTTagCompound nbt) {
			degree = nbt.getDouble("degree");
		}
		
		@Override
		protected void rotate(Axis doorAxis, Rotation rotation) {
			degree = Rotation.getRotation(doorAxis, degree > 0 ? true : false).clockwise ? Math.abs(degree) : -Math.abs(degree);
		}
		
		@Override
		protected void flip(Axis doorAxis, Axis axis) {
			if (doorAxis != axis)
				degree = -degree;
		}
		
		@Override
		protected DoorController createController(DoorOpeningResult result, UUIDSupplier supplier, Rotation rotation, LittleAxisDoor door, int completeDuration, int interpolation) {
			return new DoorController(result, supplier, new AnimationState().set(AnimationKey.getRotation(door.axis), degree), door.stayAnimated ? null : false, door.duration, completeDuration, interpolation);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		protected void onSelected(GuiTileViewer viewer, GuiParent parent) {
			viewer.visibleNormalAxis = false;
			if (this.degree == 0)
				this.degree = 90;
			parent.addControl(new GuiTextfield("degree", "" + degree, 0, 0, 30, 12).setFloatOnly());
			
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		protected void parseGui(GuiTileViewer viewer, GuiParent parent) {
			float degree;
			try {
				degree = Float.parseFloat(((GuiTextfield) parent.get("degree")).text);
			} catch (NumberFormatException e) {
				degree = 0;
			}
			this.degree = degree;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean shouldUpdateTimeline(GuiControl control) {
			return control.is("degree");
		}
		
		@Override
		protected boolean shouldRotatePreviews(LittleAxisDoor door) {
			return false;
		}
		
		@Override
		public void populateTimeline(AnimationTimeline timeline, int duration, int interpolation, AnimationKey key) {
			timeline.values.add(key, ValueTimeline.create(interpolation).addPoint(0, 0D).addPoint(duration, degree));
		}
		
		@Override
		protected LittleTransformation[] getDoorTransformations(LittleAxisDoor door, EntityPlayer player) {
			return new LittleTransformation[] { new LittleTransformation(door.getMainTile().te.getPos(), 0, 0, 0, new LittleVec(0, 0, 0), new LittleVecContext()) };
		}
		
	}
	
	public static class LittleAxisDoorType extends LittleDoorType {
		
		public LittleAxisDoorType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
			super(id, category, structureClass, attribute);
		}
		
		@Override
		protected PlacePreview getPlacePreview(Object value, StructureDirectionalField type, LittlePreviews previews) {
			if (type.key.equals("axisCenter"))
				return new PlacePreviewRelativeAxis(((StructureRelative) value).getBox(), (StructureRelative) value, type, Axis.values()[previews.structure.getInteger("axis")]);
			return super.getPlacePreview(value, type, previews);
		}
		
		@Override
		public void flip(LittlePreviews previews, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
			super.flip(previews, context, axis, doubledCenter);
			AxisDoorRotation doorRotation = parseRotation(previews.structure);
			
			doorRotation.flip(Axis.values()[previews.structure.getInteger("axis")], axis);
			doorRotation.writeToNBT(previews.structure);
		}
		
		@Override
		public void rotate(LittlePreviews previews, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
			super.rotate(previews, context, rotation, doubledCenter);
			AxisDoorRotation doorRotation = parseRotation(previews.structure);
			doorRotation.rotate(Axis.values()[previews.structure.getInteger("axis")], rotation);
			doorRotation.writeToNBT(previews.structure);
		}
		
	}
	
}
