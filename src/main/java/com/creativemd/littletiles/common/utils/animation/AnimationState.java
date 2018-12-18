package com.creativemd.littletiles.common.utils.animation;

import com.creativemd.littletiles.common.utils.animation.transformation.OffsetTransformation;
import com.creativemd.littletiles.common.utils.animation.transformation.RotationTransformation;

import net.minecraft.nbt.NBTTagCompound;

public class AnimationState {
	
	public final String name;
	public final RotationTransformation rotation;
	public final OffsetTransformation offset;
	
	public AnimationState(NBTTagCompound nbt) {
		this.name = nbt.getString("name");
		if (nbt.hasKey("rotX"))
			rotation = new RotationTransformation(nbt.getDouble("rotX"), nbt.getDouble("rotY"), nbt.getDouble("rotZ"));
		else
			rotation = null;
		if (nbt.hasKey("offX"))
			offset = new OffsetTransformation(nbt.getDouble("offX"), nbt.getDouble("offY"), nbt.getDouble("offZ"));
		else
			offset = null;
	}
	
	public AnimationState(String name, RotationTransformation rotation, OffsetTransformation offset) {
		this.name = name;
		this.rotation = rotation;
		this.offset = offset;
	}
	
	public boolean isAligned() {
		return (rotation == null || rotation.isAligned()) && (offset == null || offset.isAligned());
	}
	
	public AnimationState copy() {
		return new AnimationState(name, (RotationTransformation) (rotation != null ? rotation.copy() : null), (OffsetTransformation) (offset != null ? offset.copy() : null));
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setString("name", name);
		
		if (rotation != null) {
			nbt.setDouble("rotX", rotation.x);
			nbt.setDouble("rotY", rotation.y);
			nbt.setDouble("rotZ", rotation.z);
		}
		
		if (offset != null) {
			nbt.setDouble("offX", offset.x);
			nbt.setDouble("offY", offset.y);
			nbt.setDouble("offZ", offset.z);
		}
		return nbt;
	}
}
