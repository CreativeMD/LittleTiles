package com.creativemd.littletiles.common.utils.animation;

import java.util.BitSet;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.utils.animation.transformation.OffsetTransformation;
import com.creativemd.littletiles.common.utils.animation.transformation.RotationTransformation;

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
	
}
