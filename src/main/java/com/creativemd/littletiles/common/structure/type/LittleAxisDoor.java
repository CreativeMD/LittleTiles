package com.creativemd.littletiles.common.structure.type;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.vecmath.Vector3d;

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
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.StructureTypeRelative;
import com.creativemd.littletiles.common.structure.relative.LTStructureAnnotation;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTileRelativeAxis;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.animation.AnimationState;
import com.creativemd.littletiles.common.utils.animation.transformation.RotationTransformation;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleAxisDoor extends LittleDoorBase {
	
	public LittleAxisDoor(LittleStructureType type) {
		super(type);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		
		axis = Axis.values()[nbt.getInteger("axis")];
		
		if (nbt.hasKey("ndirection")) {
			doorRotation = new PlayerOrientatedRotation();
			((PlayerOrientatedRotation) doorRotation).normalAxis = EnumFacing.getFront(nbt.getInteger("ndirection")).getAxis();
		} else
			doorRotation = parseRotation(nbt);
	}
	
	@Override
	protected void failedLoadingRelative(NBTTagCompound nbt, StructureTypeRelative relative) {
		super.failedLoadingRelative(nbt, relative);
		if (relative.key.equals("axisCenter")) {
			
			LittleRelativeDoubledAxis doubledRelativeAxis;
			if (nbt.hasKey("ax")) {
				LittleTileVecContext vec = new LittleTileVecContext("a", nbt);
				if (getMainTile() != null)
					vec.sub(new LittleTileVecContext(getMainTile().getContext(), getMainTile().getMinVec()));
				doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.context, vec.vec, new LittleTileVec(1, 1, 1));
				
			} else if (nbt.hasKey("av")) {
				LittleTileVecContext vec = new LittleTileVecContext("av", nbt);
				doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.context, vec.vec, new LittleTileVec(1, 1, 1));
			} else {
				doubledRelativeAxis = new LittleRelativeDoubledAxis("avec", nbt);
			}
			axisCenter = new StructureRelative(StructureAbsolute.convertAxisToBox(doubledRelativeAxis.getNonDoubledVec(), doubledRelativeAxis.additional), doubledRelativeAxis.context);
		}
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		nbt.setInteger("axis", axis.ordinal());
		doorRotation.writeToNBT(nbt);
	}
	
	public AxisDoorRotation doorRotation;
	public Axis axis;
	@LTStructureAnnotation(color = ColorUtils.RED)
	public StructureRelative axisCenter;
	
	@Override
	public StructureAbsolute getAbsoluteAxis() {
		return new StructureAbsolute(lastMainTileVec != null ? lastMainTileVec : getMainTile().getAbsolutePos(), axisCenter);
	}
	
	@Override
	protected PlacePreviewTile getPlacePreview(StructureRelative relative, StructureTypeRelative type, LittlePreviews previews) {
		if (relative == axisCenter)
			return new PlacePreviewTileRelativeAxis(relative.getBox(), previews, relative, type, axis);
		return super.getPlacePreview(relative, type, previews);
	}
	
	@Override
	public void onFlip(World world, EntityPlayer player, ItemStack stack, LittleGridContext context, Axis axis, LittleTileVec doubledCenter) {
		super.onFlip(world, player, stack, context, axis, doubledCenter);
		doorRotation.flip(this, axis);
	}
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, LittleGridContext context, Rotation rotation, LittleTileVec doubledCenter) {
		super.onRotate(world, player, stack, context, rotation, doubledCenter);
		this.axis = RotationUtils.rotate(axis, rotation);
		doorRotation.rotate(this, rotation);
	}
	
	@Override
	public boolean tryToPlacePreviews(World world, EntityPlayer player, UUID uuid, StructureAbsolute absolute) {
		LittleAbsolutePreviewsStructure previews = getAbsolutePreviews(getMainTile().te.getPos());
		Rotation rotation = player != null ? doorRotation.getRotation(player, this, absolute) : doorRotation.getDefaultRotation(this, absolute);
		
		if (tryToPlacePreviews(world, player, previews.copy(), rotation, uuid, absolute))
			return true;
		return doorRotation.tryOpposite() && tryToPlacePreviews(world, player, previews, rotation.getOpposite(), uuid, absolute);
	}
	
	public boolean tryToPlacePreviews(World world, EntityPlayer player, LittleAbsolutePreviewsStructure previews, Rotation rotation, UUID uuid, StructureAbsolute absolute) {
		LittleAxisDoor newDoor = (LittleAxisDoor) previews.getStructure();
		if (newDoor.axisCenter.getContext().size > previews.context.size)
			previews.convertTo(newDoor.axisCenter.getContext());
		else if (newDoor.axisCenter.getContext().size < previews.context.size)
			newDoor.axisCenter.convertTo(previews.context);
		
		if (doorRotation.shouldRotatePreviews(this))
			previews.rotatePreviews(world, player, null, rotation, newDoor.axisCenter.getDoubledCenterVec());
		
		return place(world, player, previews, doorRotation.createController(rotation, this), uuid, absolute);
	}
	
	@Deprecated
	public static class LittleRelativeDoubledAxis extends LittleTileVecContext {
		
		public LittleTileVec additional;
		
		public LittleRelativeDoubledAxis(LittleGridContext context, LittleTileVec vec, LittleTileVec additional) {
			super(context, vec);
			this.additional = additional;
		}
		
		public LittleRelativeDoubledAxis(String name, NBTTagCompound nbt) {
			super(name, nbt);
		}
		
		@Override
		protected void loadFromNBT(String name, NBTTagCompound nbt) {
			int[] array = nbt.getIntArray(name);
			if (array.length == 3) {
				this.vec = new LittleTileVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get();
				this.additional = new LittleTileVec(vec.x % 2, vec.y % 2, vec.z % 2);
				this.vec.x /= 2;
				this.vec.y /= 2;
				this.vec.z /= 2;
			} else if (array.length == 4) {
				this.vec = new LittleTileVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get(array[3]);
				this.additional = new LittleTileVec(vec.x % 2, vec.y % 2, vec.z % 2);
				this.vec.x /= 2;
				this.vec.y /= 2;
				this.vec.z /= 2;
			} else if (array.length == 7) {
				this.vec = new LittleTileVec(array[0], array[1], array[2]);
				this.context = LittleGridContext.get(array[3]);
				this.additional = new LittleTileVec(array[4], array[5], array[6]);
			} else
				throw new InvalidParameterException("No valid coords given " + nbt);
		}
		
		public LittleTileVecContext getNonDoubledVec() {
			return new LittleTileVecContext(context, vec.copy());
		}
		
		public LittleTileVec getRotationVec() {
			LittleTileVec vec = this.vec.copy();
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
			LittleTileVec newVec = getRotationVec();
			newVec.convertTo(context, to);
			super.convertTo(to);
			additional = new LittleTileVec(newVec.x % 2, newVec.y % 2, newVec.z % 2);
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
		
		public LittleAxisDoorParser(GuiParent parent) {
			super(parent);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleAxisDoor parseStructure(int duration) {
			LittleAxisDoor door = createStructure(LittleAxisDoor.class);
			GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
			door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
			door.axis = viewer.getAxis();
			
			GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
			door.doorRotation = createRotation(((GuiTabStateButton) parent.get("doorRotation")).getState());
			door.doorRotation.parseGui(viewer, typePanel);
			door.duration = duration;
			return door;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(ItemStack stack, LittleStructure structure) {
			super.createControls(stack, structure);
			LittleAxisDoor door = null;
			if (structure instanceof LittleAxisDoor)
				door = (LittleAxisDoor) structure;
			
			LittleGridContext stackContext = LittleGridContext.get(stack.getTagCompound());
			LittleGridContext axisContext = stackContext;
			
			GuiTileViewer viewer = new GuiTileViewer("tileviewer", 0, 0, 100, 100, stackContext);
			
			boolean even = false;
			AxisDoorRotation doorRotation;
			if (door != null) {
				even = door.axisCenter.isEven();
				viewer.setEven(even);
				
				axisContext = door.axisCenter.getContext();
				viewer.setAxis(door.axis);
				door.axisCenter.convertToSmallest();
				viewer.setAxis(door.axisCenter.getBox(), axisContext);
				
				doorRotation = door.doorRotation;
				
			} else {
				viewer.setEven(false);
				viewer.setAxis(new LittleTileBox(0, 0, 0, 1, 1, 1), viewer.context);
				doorRotation = new PlayerOrientatedRotation();
			}
			viewer.visibleAxis = true;
			parent.addControl(viewer);
			
			parent.addControl(new GuiTabStateButton("doorRotation", rotationTypes.indexOf(doorRotation.getClass()), 110, 0, 12, rotationTypeNames.toArray(new String[0])));
			
			GuiPanel typePanel = new GuiPanel("typePanel", 110, 20, 80, 40);
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
						viewer.setAxis(EnumFacing.Axis.Y);
						break;
					case Y:
						viewer.setAxis(EnumFacing.Axis.Z);
						break;
					case Z:
						viewer.setAxis(EnumFacing.Axis.X);
						break;
					default:
						break;
					}
				}
			}.setCustomTooltip("change view"));
			parent.addControl(new GuiIconButton("flip view", 60, 107, 4) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.setViewDirection(viewer.getViewDirection().getOpposite());
				}
			}.setCustomTooltip("flip view"));
			
			parent.addControl(new GuiIconButton("up", 124, 83, 1) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveY(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1);
				}
			});
			
			parent.addControl(new GuiIconButton("right", 141, 100, 0) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveX(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1);
				}
			});
			
			parent.addControl(new GuiIconButton("left", 107, 100, 2) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveX(-(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1));
				}
			});
			
			parent.addControl(new GuiIconButton("down", 124, 100, 3) {
				
				@Override
				public void onClicked(int x, int y, int button) {
					viewer.moveY(-(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1));
				}
			});
			
			parent.controls.add(new GuiCheckBox("even", 147, 80, even));
			
			GuiStateButton contextBox = new GuiStateButton("grid", LittleGridContext.getNames().indexOf(axisContext + ""), 170, 100, 20, 12, LittleGridContext.getNames().toArray(new String[0]));
			parent.controls.add(contextBox);
			
			doorRotation.onSelected(viewer, typePanel);
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onButtonClicked(GuiControlClickEvent event) {
			GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
			if (event.source.is("even")) {
				viewer.setEven(((GuiCheckBox) event.source).value);
			}
		}
		
		@CustomEventSubscribe
		@SideOnly(Side.CLIENT)
		public void onStateChange(GuiControlChangedEvent event) {
			if (event.source.is("doorRotation")) {
				AxisDoorRotation rotation = createRotation(((GuiTabStateButton) event.source).getState());
				GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
				typePanel.controls.clear();
				rotation.onSelected((GuiTileViewer) parent.get("tileviewer"), typePanel);
			} else if (event.source.is("grid")) {
				GuiStateButton contextBox = (GuiStateButton) event.source;
				LittleGridContext context;
				try {
					context = LittleGridContext.get(Integer.parseInt(contextBox.caption));
				} catch (NumberFormatException e) {
					context = LittleGridContext.get();
				}
				
				GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
				LittleTileBox box = viewer.getBox();
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
		
		protected abstract void rotate(LittleAxisDoor door, Rotation rotation);
		
		protected abstract void flip(LittleAxisDoor door, Axis axis);
		
		protected abstract boolean tryOpposite();
		
		protected abstract Rotation getRotation(EntityPlayer player, LittleAxisDoor door, StructureAbsolute absolute);
		
		protected abstract Rotation getDefaultRotation(LittleAxisDoor door, StructureAbsolute absolute);
		
		protected abstract boolean shouldRotatePreviews(LittleAxisDoor door);
		
		protected abstract DoorController createController(Rotation rotation, LittleAxisDoor door);
		
		protected abstract void onSelected(GuiTileViewer viewer, GuiParent parent);
		
		protected abstract void parseGui(GuiTileViewer viewer, GuiParent parent);
		
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
		protected void rotate(LittleAxisDoor door, Rotation rotation) {
			this.normalAxis = RotationUtils.rotate(normalAxis, rotation);
		}
		
		@Override
		protected void flip(LittleAxisDoor door, Axis axis) {
			
		}
		
		@Override
		protected boolean tryOpposite() {
			return true;
		}
		
		@Override
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
		
		@Override
		protected Rotation getDefaultRotation(LittleAxisDoor door, StructureAbsolute absolute) {
			return Rotation.getRotation(door.axis, true);
		}
		
		@Override
		protected DoorController createController(Rotation rotation, LittleAxisDoor door) {
			RotationTransformation transformation = new RotationTransformation(0, 0, 0);
			switch (rotation) {
			case X_CLOCKWISE:
				transformation.x = -90;
				break;
			case X_COUNTER_CLOCKWISE:
				transformation.x = 90;
				break;
			case Y_CLOCKWISE:
				transformation.y = -90;
				break;
			case Y_COUNTER_CLOCKWISE:
				transformation.y = 90;
				break;
			case Z_CLOCKWISE:
				transformation.z = -90;
				break;
			case Z_COUNTER_CLOCKWISE:
				transformation.z = 90;
				break;
			}
			return new DoorController(new AnimationState("closed", transformation, null), new AnimationState("opened", null, null), true, door.duration);
		}
		
		@Override
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
		protected void parseGui(GuiTileViewer viewer, GuiParent parent) {
			normalAxis = viewer.getNormalAxis();
		}
		
		@Override
		protected boolean shouldRotatePreviews(LittleAxisDoor door) {
			return true;
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
		protected void rotate(LittleAxisDoor door, Rotation rotation) {
			if (door.axis != rotation.axis && rotation.clockwise)
				degree = -degree;
		}
		
		@Override
		protected void flip(LittleAxisDoor door, Axis axis) {
			if (door.axis != axis)
				degree = -degree;
		}
		
		@Override
		protected boolean tryOpposite() {
			return false;
		}
		
		@Override
		protected Rotation getRotation(EntityPlayer player, LittleAxisDoor door, StructureAbsolute absolute) {
			return getDefaultRotation(door, absolute);
		}
		
		@Override
		protected Rotation getDefaultRotation(LittleAxisDoor door, StructureAbsolute absolute) {
			return Rotation.getRotation(door.axis, degree > 0);
		}
		
		@Override
		protected DoorController createController(Rotation rotation, LittleAxisDoor door) {
			return new DoorController(new AnimationState("opened", new RotationTransformation(Rotation.getRotation(door.axis, degree > 0), degree), null), false, door.duration);
		}
		
		@Override
		protected void onSelected(GuiTileViewer viewer, GuiParent parent) {
			viewer.visibleNormalAxis = false;
			if (this.degree == 0)
				this.degree = 90;
			parent.addControl(new GuiTextfield("degree", "" + degree, 0, 0, 30, 12).setFloatOnly());
			
		}
		
		@Override
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
		protected boolean shouldRotatePreviews(LittleAxisDoor door) {
			return false;
		}
		
	}
	
}
