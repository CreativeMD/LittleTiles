package com.creativemd.littletiles.common.utils.animation;

import java.util.BitSet;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.utils.animation.transformation.OffsetTransformation;
import com.creativemd.littletiles.common.utils.animation.transformation.RotationTransformation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class AnimationTimeline extends Animation {
	
	protected final AnimationState currentState;
	protected final PairList<Long, Animation> animations;
	
	protected BitSet activeAnimations;
	protected int animationIndex = 0;
	
	public AnimationTimeline(long duration, PairList<Long, Animation> animations) {
		super(duration);
		this.animations = animations;
		this.currentState = new AnimationState("ticking", new RotationTransformation(0, 0, 0), new OffsetTransformation(0, 0, 0));
		this.activeAnimations = new BitSet(animations.size());
	}
	
	public AnimationTimeline(NBTTagCompound nbt) {
		super(nbt.getLong("duration"));
		animations = new PairList<>();
		this.currentState = new AnimationState("ticking", new RotationTransformation(0, 0, 0), new OffsetTransformation(0, 0, 0));
		this.activeAnimations = new BitSet(animations.size());
		
		NBTTagList list = nbt.getTagList("animations", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound animationNBT = list.getCompoundTagAt(i);
			animations.add(animationNBT.getLong("time"), getAnimation(animationNBT.getInteger("type"), animationNBT.getIntArray("data")));
		}
	}
	
	public boolean tick(AnimationController controller) {
		return tick(this, currentState);
	}
	
	@Override
	public void tick(AnimationState currentState) {
		while (animationIndex < animations.size()) {
			Pair<Long, Animation> pair = animations.get(animationIndex);
			if (pair.key <= tick) {
				activeAnimations.set(animationIndex);
				animationIndex++;
			} else
				break;
		}
		
		int index = -1;
		while ((index = activeAnimations.nextSetBit(index + 1)) != -1) {
			if (!animations.get(index).value.tick(this, currentState))
				activeAnimations.set(index, false);
		}
	}
	
	@Override
	public void end(AnimationState currentState) {
		int index = 0;
		while ((index = activeAnimations.nextSetBit(index + 1)) != -1) {
			animations.get(index).value.end(currentState);
		}
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("duration", duration);
		NBTTagList list = new NBTTagList();
		for (Pair<Long, Animation> pair : animations) {
			NBTTagCompound animationNBT = new NBTTagCompound();
			int type = getAnimationType(pair.value);
			if (type == -1)
				throw new RuntimeException("Invalid animation!");
			
			animationNBT.setIntArray("data", getAnimationData(pair.value));
			animationNBT.setInteger("type", type);
			animationNBT.setLong("time", pair.key);
			list.appendTag(animationNBT);
		}
		nbt.setTag("animations", list);
		return nbt;
	}
	
	public static int getAnimationType(Animation animation) {
		if (animation instanceof OffsetAnimation)
			return 0;
		if (animation instanceof RotateAnimation)
			return 1;
		if (animation instanceof TimestampAnimation)
			return 2;
		return -1;
	}
	
	public static int[] getAnimationData(Animation animation) {
		if (animation instanceof OffsetAnimation)
			return ((OffsetAnimation) animation).getArray();
		if (animation instanceof RotateAnimation)
			return ((RotateAnimation) animation).getArray();
		if (animation instanceof TimestampAnimation)
			return ((TimestampAnimation) animation).getArray();
		return null;
	}
	
	public static Animation getAnimation(int type, int[] data) {
		switch (type) {
		case 0:
			return new OffsetAnimation(data);
		case 1:
			return new RotateAnimation(data);
		case 2:
			return new TimestampAnimation(data);
		}
		return null;
	}
}
