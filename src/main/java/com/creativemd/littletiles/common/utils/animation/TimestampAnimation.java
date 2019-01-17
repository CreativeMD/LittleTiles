package com.creativemd.littletiles.common.utils.animation;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.structure.type.LittleAdvancedDoor;

public class TimestampAnimation extends Animation {
	
	public PairList<Integer, Double> offX;
	public PairList<Integer, Double> offY;
	public PairList<Integer, Double> offZ;
	
	public PairList<Integer, Double> rotX;
	public PairList<Integer, Double> rotY;
	public PairList<Integer, Double> rotZ;
	
	public TimestampAnimation(long duration, PairList<Integer, Double> offX, PairList<Integer, Double> offY, PairList<Integer, Double> offZ, PairList<Integer, Double> rotX, PairList<Integer, Double> rotY, PairList<Integer, Double> rotZ) {
		super(duration);
		this.offX = offX;
		this.offY = offY;
		this.offZ = offZ;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
	}
	
	public TimestampAnimation(int[] array) {
		super((((long) array[0]) << 32) | (array[1] & 0xffffffffL));
		int index = 2;
		for (int step = 0; step < 6; step++) {
			int size = array[index];
			index++;
			if (size != 0) {
				PairList<Integer, Double> list = LittleAdvancedDoor.loadPairList(array, index, size);
				switch (step) {
				case 0:
					offX = list;
					break;
				case 1:
					offY = list;
					break;
				case 2:
					offZ = list;
					break;
				case 3:
					rotX = list;
					break;
				case 4:
					rotY = list;
					break;
				case 5:
					rotZ = list;
					break;
				}
			}
			index += size;
		}
	}
	
	@Override
	public void tick(AnimationState currentState) {
		if (currentState.offset != null) {
			if (offX != null)
				currentState.offset.x = getValueAt(offX, tick);
			if (offY != null)
				currentState.offset.y = getValueAt(offY, tick);
			if (offZ != null)
				currentState.offset.z = getValueAt(offZ, tick);
		}
		if (currentState.rotation != null) {
			if (rotX != null)
				currentState.rotation.x = getValueAt(rotX, tick);
			if (rotY != null)
				currentState.rotation.y = getValueAt(rotY, tick);
			if (rotZ != null)
				currentState.rotation.z = getValueAt(rotZ, tick);
		}
	}
	
	@Override
	public void end(AnimationState currentState) {
		tick(currentState);
	}
	
	public static double getValueAt(PairList<Integer, Double> list, long tick) {
		int higher = list.size();
		for (int i = 0; i < list.size(); i++) {
			int otherTick = list.get(i).key;
			if (otherTick == tick)
				return list.get(i).value;
			if (otherTick > tick) {
				higher = i;
				break;
			}
		}
		
		if (higher == 0 || higher == list.size())
			return list.get(higher == 0 ? 0 : list.size() - 1).value;
		
		Pair<Integer, Double> before = list.get(higher - 1);
		Pair<Integer, Double> after = list.get(higher);
		double percentage = (double) (tick - before.key) / (after.key - before.key);
		return (after.value - before.value) * percentage + before.value;
	}
	
	public int[] getArray() {
		int[] offXArray = LittleAdvancedDoor.savePairList(offX);
		int[] offYArray = LittleAdvancedDoor.savePairList(offY);
		int[] offZArray = LittleAdvancedDoor.savePairList(offZ);
		int[] rotXArray = LittleAdvancedDoor.savePairList(rotX);
		int[] rotYArray = LittleAdvancedDoor.savePairList(rotY);
		int[] rotZArray = LittleAdvancedDoor.savePairList(rotZ);
		
		int[] array = new int[2 + 6 + (offXArray != null ? offXArray.length : 0) + (offYArray != null ? offYArray.length : 0) + (offZArray != null ? offZArray.length : 0) + (rotXArray != null ? rotXArray.length : 0) + (rotYArray != null ? rotYArray.length : 0) + (rotZArray != null ? rotZArray.length : 0)];
		array[0] = (int) (duration >> 32);
		array[1] = (int) duration;
		int index = 2;
		
		if (offXArray != null) {
			array[index] = offXArray.length;
			for (int i = 0; i < offXArray.length; i++) {
				array[index + 1 + i] = offXArray[i];
			}
			index += offXArray.length;
		} else
			array[index] = 0;
		index++;
		
		if (offYArray != null) {
			array[index] = offYArray.length;
			for (int i = 0; i < offYArray.length; i++) {
				array[index + 1 + i] = offYArray[i];
			}
			index += offYArray.length;
		} else
			array[index] = 0;
		index++;
		
		if (offZArray != null) {
			array[index] = offZArray.length;
			for (int i = 0; i < offZArray.length; i++) {
				array[index + 1 + i] = offZArray[i];
			}
			index += offZArray.length;
		} else
			array[index] = 0;
		index++;
		
		if (rotXArray != null) {
			array[index] = rotXArray.length;
			for (int i = 0; i < rotXArray.length; i++) {
				array[index + 1 + i] = rotXArray[i];
			}
			index += rotXArray.length;
		} else
			array[index] = 0;
		index++;
		
		if (rotYArray != null) {
			array[index] = rotYArray.length;
			for (int i = 0; i < rotYArray.length; i++) {
				array[index + 1 + i] = rotYArray[i];
			}
			index += rotYArray.length;
		} else
			array[index] = 0;
		index++;
		
		if (rotZArray != null) {
			array[index] = rotZArray.length;
			for (int i = 0; i < rotZArray.length; i++) {
				array[index + 1 + i] = rotZArray[i];
			}
			index += rotZArray.length;
		} else
			array[index] = 0;
		return array;
	}
}
