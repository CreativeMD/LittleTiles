package com.creativemd.littletiles.common.util.outdated;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.entity.old.EntityOldDoorAnimation;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

@Deprecated
public abstract class OldDoorTransformation {
	
	private static HashMap<String, Class<? extends OldDoorTransformation>> transformationTypes = new HashMap<>();
	
	public static void registerTransformationType(String id, Class<? extends OldDoorTransformation> classType) {
		if (transformationTypes.containsKey(id))
			throw new IllegalArgumentException("id '" + id + "' is already taken");
		transformationTypes.put(id, classType);
	}
	
	public static String getIDFromClass(Class<? extends OldDoorTransformation> classType) {
		for (Iterator<Entry<String, Class<? extends OldDoorTransformation>>> iterator = transformationTypes.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Class<? extends OldDoorTransformation>> type = iterator.next();
			if (type.getValue() == classType)
				return type.getKey();
		}
		return "";
	}
	
	static {
		registerTransformationType("ordinary", RotateDoorTransformation.class);
		registerTransformationType("sliding", SlideDoorTransformation.class);
	}
	
	public static OldDoorTransformation loadFromNBT(NBTTagCompound nbt) {
		
		Class<? extends OldDoorTransformation> type = transformationTypes.get(nbt.getString("id"));
		if (type != null) {
			try {
				OldDoorTransformation transformation = type.getConstructor().newInstance();
				transformation.readFromNBT(nbt);
				return transformation;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException(type.getClass() + " does not contain an empty constructor.");
			}
		} else
			throw new IllegalArgumentException("id '" + nbt.getString("id") + "' could not be loaded.");
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		writeToNBTExtra(nbt);
		nbt.setString("id", getIDFromClass(this.getClass()));
		return nbt;
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
	protected abstract void readFromNBT(NBTTagCompound nbt);
	
	public abstract void performTransformation(EntityOldDoorAnimation animation, double progress);
	
	public abstract boolean equals(Object object);
	
	@Deprecated
	public static class RotateDoorTransformation extends OldDoorTransformation {
		
		public Rotation rotation;
		
		public RotateDoorTransformation() {
			
		}
		
		public RotateDoorTransformation(Rotation rotation) {
			this.rotation = rotation;
		}
		
		@Override
		public void performTransformation(EntityOldDoorAnimation animation, double progress) {
			switch (rotation) {
			case X_CLOCKWISE:
				animation.rotXTo(-90 + progress * 90);
				break;
			case X_COUNTER_CLOCKWISE:
				animation.rotXTo((1 - progress) * 90);
				break;
			case Y_CLOCKWISE:
				animation.rotYTo(-90 + progress * 90);
				break;
			case Y_COUNTER_CLOCKWISE:
				animation.rotYTo((1 - progress) * 90);
				break;
			case Z_CLOCKWISE:
				animation.rotZTo(-90 + progress * 90);
				break;
			case Z_COUNTER_CLOCKWISE:
				animation.rotZTo((1 - progress) * 90);
				break;
			}
		}
		
		@Override
		protected void writeToNBTExtra(NBTTagCompound nbt) {
			nbt.setInteger("rot", rotation.ordinal());
		}
		
		@Override
		protected void readFromNBT(NBTTagCompound nbt) {
			rotation = Rotation.values()[nbt.getInteger("rot")];
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof RotateDoorTransformation)
				return ((RotateDoorTransformation) object).rotation == rotation;
			return false;
		}
		
	}
	
	@Deprecated
	public static class SlideDoorTransformation extends OldDoorTransformation {
		
		public EnumFacing direction;
		public int distance;
		public LittleGridContext context;
		
		public SlideDoorTransformation() {
			
		}
		
		public SlideDoorTransformation(EnumFacing direction, LittleGridContext context, int distance) {
			this.direction = direction;
			this.distance = distance;
			this.context = context;
		}
		
		@Override
		protected void writeToNBTExtra(NBTTagCompound nbt) {
			nbt.setInteger("direction", direction.ordinal());
			nbt.setInteger("distance", distance);
			context.set(nbt);
		}
		
		@Override
		protected void readFromNBT(NBTTagCompound nbt) {
			direction = EnumFacing.getFront(nbt.getInteger("direction"));
			distance = nbt.getInteger("distance");
			context = LittleGridContext.get(nbt);
		}
		
		@Override
		public void performTransformation(EntityOldDoorAnimation animation, double progress) {
			double pushDistance = distance * context.pixelSize * (1 - progress);
			switch (direction) {
			case EAST:
				animation.moveXTo(animation.getAxisPos().getX() - pushDistance);
				break;
			case WEST:
				animation.moveXTo(animation.getAxisPos().getX() + pushDistance);
				break;
			case UP:
				animation.moveYTo(animation.getAxisPos().getY() - pushDistance);
				break;
			case DOWN:
				animation.moveYTo(animation.getAxisPos().getY() + pushDistance);
				break;
			case SOUTH:
				animation.moveZTo(animation.getAxisPos().getZ() - pushDistance);
				break;
			case NORTH:
				animation.moveZTo(animation.getAxisPos().getZ() + pushDistance);
				break;
			default:
				break;
			
			}
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof SlideDoorTransformation)
				return ((SlideDoorTransformation) object).direction == direction && ((SlideDoorTransformation) object).distance == distance && ((SlideDoorTransformation) object).context == context;
			return false;
		}
		
	}
	
}
