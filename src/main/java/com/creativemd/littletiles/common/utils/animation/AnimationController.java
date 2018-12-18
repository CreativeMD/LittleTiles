package com.creativemd.littletiles.common.utils.animation;

import java.util.HashMap;

import com.creativemd.creativecore.common.utils.type.PairList;

public class AnimationController {
	
	protected HashMap<String, AnimationState> states = new HashMap<>();
	protected HashMap<String, AnimationTimeline> stateTransition = new HashMap<>();
	protected AnimationState currentState;
	protected AnimationState aimedState;
	protected AnimationTimeline animation;
	
	public AnimationController addStateAndSelect(AnimationState state) {
		states.put(state.name, state);
		currentState = state;
		return this;
	}
	
	public AnimationController addState(AnimationState state) {
		states.put(state.name, state);
		return this;
	}
	
	public AnimationController generateAllTransistions(long duration) {
		for (String key : states.keySet()) {
			for (String key2 : states.keySet()) {
				if (!key.equals(key2))
					generateTransition(key, key2, duration);
			}
		}
		return this;
	}
	
	public AnimationController generateTransition(String from, String to, long duration) {
		AnimationState fromState = states.get(from);
		if (fromState == null)
			throw new RuntimeException("State '" + from + "' does not exist");
		
		AnimationState toState = states.get(to);
		if (toState == null)
			throw new RuntimeException("State '" + to + "' does not exist");
		
		PairList<Long, Animation> animations = new PairList<>();
		
		if (fromState.offset != null)
			if (toState.offset != null)
				animations.add(0L, fromState.offset.createAnimationTo(toState.offset, duration));
			else
				animations.add(0L, fromState.offset.createAnimationToZero(duration));
		else if (toState.offset != null)
			animations.add(0L, toState.offset.createAnimationFromZero(duration));
		
		if (fromState.rotation != null)
			if (toState.rotation != null)
				animations.add(0L, fromState.rotation.createAnimationTo(toState.rotation, duration));
			else
				animations.add(0L, fromState.rotation.createAnimationToZero(duration));
		else if (toState.rotation != null)
			animations.add(0L, toState.rotation.createAnimationFromZero(duration));
		
		stateTransition.put(from + ":" + to, new AnimationTimeline(duration, animations));
		
		return this;
	}
	
	public AnimationController removeTransition(String from, String to) {
		stateTransition.remove(from + ":" + to);
		return this;
	}
	
	public AnimationController addTransition(String from, String to, AnimationTimeline animation) {
		stateTransition.put(from + ":" + to, animation);
		return this;
	}
	
	public boolean isChanging() {
		return aimedState != null;
	}
	
	public AnimationState getState(String key) {
		return states.get(key);
	}
	
	public boolean hasState(String key) {
		return states.containsKey(key);
	}
	
	public void startTransition(String key) {
		AnimationState state = states.get(key);
		if (state == null)
			throw new RuntimeException("State '" + key + "' does not exist");
		
		aimedState = state;
		animation = stateTransition.get(currentState.name + ":" + aimedState.name);
		if (animation == null)
			throw new RuntimeException("Couldn't animate from '" + currentState.name + "' to '" + aimedState.name + "'");
	}
	
	public void endTransition() {
		currentState = aimedState;
		animation = null;
		aimedState = null;
	}
	
	public AnimationState tick() {
		if (isChanging()) {
			if (animation.tick(this))
				return animation.currentState;
			else
				endTransition();
		}
		return currentState;
	}
	
}
