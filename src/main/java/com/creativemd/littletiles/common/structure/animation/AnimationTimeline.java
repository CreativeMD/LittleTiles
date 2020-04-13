package com.creativemd.littletiles.common.structure.animation;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.structure.animation.ValueTimeline.CosineTimeline;
import com.creativemd.littletiles.common.structure.animation.ValueTimeline.LinearTimeline;
import com.creativemd.littletiles.common.structure.type.door.LittleAdvancedDoor;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class AnimationTimeline {
	
	public int duration;
	public PairList<AnimationKey, ValueTimeline> values;
	
	public AnimationTimeline(NBTTagCompound nbt) {
		duration = nbt.getInteger("duration");
		values = new PairList<>();
		
		if (nbt.hasKey("values")) { // now
			NBTTagList list = nbt.getTagList("values", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound valueNBT = list.getCompoundTagAt(i);
				values.add(AnimationKey.getKey(valueNBT.getString("key")), ValueTimeline.read(valueNBT.getIntArray("data")));
				
			}
		} else {// before pre132
			NBTTagList list = nbt.getTagList("animations", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound animationNBT = list.getCompoundTagAt(i);
				this.values = getAnimation((int) animationNBT.getLong("time"), animationNBT.getInteger("type"), animationNBT.getIntArray("data")); // There was only one animation back then
			}
		}
	}
	
	/** Old code please do not use it!!!! Used to stay compatible to existing pre-releases */
	public static PairList<AnimationKey, ValueTimeline> getAnimation(int begin, int type, int[] array) {
		PairList<AnimationKey, ValueTimeline> values = new PairList<>();
		long duration = (((long) array[0]) << 32) | (array[1] & 0xffffffffL);
		double startX;
		double startY;
		double startZ;
		double endX;
		double endY;
		double endZ;
		if (type == 0 || type == 1) {
			if (array.length == 8) {
				startX = array[2];
				startY = array[3];
				startZ = array[4];
				endX = array[5];
				endY = array[6];
				endZ = array[7];
			} else {
				startX = Double.longBitsToDouble((((long) array[2]) << 32) | (array[3] & 0xffffffffL));
				startY = Double.longBitsToDouble((((long) array[4]) << 32) | (array[5] & 0xffffffffL));
				startZ = Double.longBitsToDouble((((long) array[6]) << 32) | (array[7] & 0xffffffffL));
				endX = Double.longBitsToDouble((((long) array[8]) << 32) | (array[9] & 0xffffffffL));
				endY = Double.longBitsToDouble((((long) array[10]) << 32) | (array[11] & 0xffffffffL));
				endZ = Double.longBitsToDouble((((long) array[12]) << 32) | (array[13] & 0xffffffffL));
			}
			switch (type) {
			case 0:
				if (startX != 0 || endX != 0) {
					ValueTimeline timeline = new CosineTimeline();
					timeline.addPoint(begin, startX);
					timeline.addPoint(begin + (int) duration, endX);
					values.add(AnimationKey.offX, timeline);
				}
				if (startY != 0 || endY != 0) {
					ValueTimeline timeline = new CosineTimeline();
					timeline.addPoint(begin, startY);
					timeline.addPoint(begin + (int) duration, endY);
					values.add(AnimationKey.offY, timeline);
				}
				if (startZ != 0 || endZ != 0) {
					ValueTimeline timeline = new CosineTimeline();
					timeline.addPoint(begin, startZ);
					timeline.addPoint(begin + (int) duration, endZ);
					values.add(AnimationKey.offZ, timeline);
				}
				return values;
			case 1:
				
				if (startX != 0 || endX != 0) {
					ValueTimeline timeline = new CosineTimeline();
					timeline.addPoint(begin, startX);
					timeline.addPoint(begin + (int) duration, endX);
					values.add(AnimationKey.rotX, timeline);
				}
				if (startY != 0 || endY != 0) {
					ValueTimeline timeline = new CosineTimeline();
					timeline.addPoint(begin, startY);
					timeline.addPoint(begin + (int) duration, endY);
					values.add(AnimationKey.rotY, timeline);
				}
				if (startZ != 0 || endZ != 0) {
					ValueTimeline timeline = new CosineTimeline();
					timeline.addPoint(begin, startZ);
					timeline.addPoint(begin + (int) duration, endZ);
					values.add(AnimationKey.rotZ, timeline);
				}
				return values;
			}
		} else {
			int index = 2;
			for (int step = 0; step < 6; step++) {
				int size = array[index];
				index++;
				if (size != 0) {
					PairList<Integer, Double> list = LittleAdvancedDoor.loadPairListDouble(array, index, size);
					ValueTimeline timeline = new LinearTimeline().addPoints(list);
					AnimationKey key = null;
					switch (step) {
					case 0:
						key = AnimationKey.offX;
						break;
					case 1:
						key = AnimationKey.offY;
						break;
					case 2:
						key = AnimationKey.offZ;
						break;
					case 3:
						key = AnimationKey.rotX;
						break;
					case 4:
						key = AnimationKey.rotY;
						break;
					case 5:
						key = AnimationKey.rotZ;
						break;
					}
					values.add(key, timeline);
				}
				index += size;
			}
			return values;
		}
		return null;
	}
	
	public AnimationTimeline(int duration, PairList<AnimationKey, ValueTimeline> values) {
		this.duration = duration;
		this.values = values;
	}
	
	public boolean tick(int tick, AnimationState state) {
		if (tick > duration)
			return false;
		
		for (Pair<AnimationKey, ValueTimeline> pair : values) {
			state.set(pair.key, pair.value.value(tick));
		}
		return true;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("duration", duration);
		NBTTagList list = new NBTTagList();
		for (Pair<AnimationKey, ValueTimeline> pair : values) {
			NBTTagCompound valueNBT = new NBTTagCompound();
			valueNBT.setString("key", pair.key.name);
			valueNBT.setIntArray("data", pair.value.write());
			list.appendTag(valueNBT);
		}
		nbt.setTag("values", list);
		return nbt;
	}
	
	public void offset(int offset) {
		duration += offset;
		for (Pair<AnimationKey, ValueTimeline> pair : values)
			pair.value.offset(offset);
	}
	
	public void transform(Rotation rotation) {
		PairList<AnimationKey, ValueTimeline> newPairs = new PairList<>();
		for (Pair<AnimationKey, ValueTimeline> pair : values) {
			Pair<AnimationKey, Double> result = pair.key.transform(rotation, 1);
			if (result != null) {
				if (result.value < 0)
					pair.value.flip();
				newPairs.add(result.key, pair.value);
			} else
				newPairs.add(pair);
		}
		this.values = newPairs;
	}
	
	public boolean isFirstAligned() {
		for (Pair<AnimationKey, ValueTimeline> pair : values) {
			if (!pair.key.isAligned(pair.value.first(pair.key)))
				return false;
		}
		return true;
	}
}
