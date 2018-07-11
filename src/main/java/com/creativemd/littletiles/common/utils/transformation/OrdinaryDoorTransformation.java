package com.creativemd.littletiles.common.utils.transformation;

import javax.vecmath.Matrix3f;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

public class OrdinaryDoorTransformation extends DoorTransformation{
	
	public Rotation rotation;
	
	public OrdinaryDoorTransformation() {
		
	}
	
	public OrdinaryDoorTransformation(Rotation rotation) {
		this.rotation = rotation;
	}

	@Override
	public void performTransformation(EntityDoorAnimation animation, double progress) {
		switch(rotation)
		{
		case X_CLOCKWISE:
			animation.rotXTo(-90+progress*90);
			break;
		case X_COUNTER_CLOCKWISE:
			animation.rotXTo((1-progress)*90);
			break;
		case Y_CLOCKWISE:
			animation.rotYTo(-90+progress*90);
			break;
		case Y_COUNTER_CLOCKWISE:
			animation.rotYTo((1-progress)*90);
			break;
		case Z_CLOCKWISE:
			animation.rotZTo(-90+progress*90);
			break;
		case Z_COUNTER_CLOCKWISE:
			animation.rotZTo((1-progress)*90);
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
		if(object instanceof OrdinaryDoorTransformation)
			return ((OrdinaryDoorTransformation) object).rotation == rotation;
		return false;
	}

}
