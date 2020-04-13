package com.creativemd.littletiles.common.structure.animation;

import java.util.HashMap;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;

import net.minecraft.util.EnumFacing.Axis;

public class AnimationController {
	
	protected HashMap<String, AnimationControllerState> states = new HashMap<>();
	protected HashMap<String, AnimationTimeline> stateTransition = new HashMap<>();
	protected AnimationControllerState currentState;
	protected AnimationControllerState aimedState;
	
	protected int tick;
	protected AnimationState tickingState;
	protected AnimationTimeline animation;
	
	public AnimationController addStateAndSelect(String name, AnimationState state) {
		AnimationControllerState controllerState = new AnimationControllerState(name, state);
		states.put(name, controllerState);
		currentState = controllerState;
		return this;
	}
	
	public AnimationController addState(String name, AnimationState state) {
		states.put(name, new AnimationControllerState(name, state));
		return this;
	}
	
	public AnimationController generateAllTransistions(int duration) {
		for (String key : states.keySet()) {
			for (String key2 : states.keySet()) {
				if (!key.equals(key2))
					generateTransition(key, key2, duration);
			}
		}
		return this;
	}
	
	public AnimationTimeline getAnimation() {
		return animation;
	}
	
	public AnimationControllerState getCurrentState() {
		return currentState;
	}
	
	public AnimationControllerState getAimedState() {
		return aimedState;
	}
	
	public int getInterpolationType() {
		return 0;
	}
	
	public AnimationController generateTransition(String from, String to, int duration) {
		AnimationControllerState fromState = states.get(from);
		if (fromState == null)
			throw new RuntimeException("State '" + from + "' does not exist");
		
		AnimationControllerState toState = states.get(to);
		if (toState == null)
			throw new RuntimeException("State '" + to + "' does not exist");
		
		PairList<AnimationKey, ValueTimeline> values = new PairList<>();
		for (Pair<AnimationKey, Double> pair : fromState.state.getValues()) {
			ValueTimeline timeline = ValueTimeline.create(getInterpolationType());
			timeline.points.add(0, pair.value);
			if (toState.state.getValues().containsKey(pair.key))
				timeline.points.add(duration, toState.state.getValues().getValue(pair.key));
			else
				timeline.points.add(duration, pair.key.getDefault());
			values.add(pair.key, timeline);
		}
		
		for (Pair<AnimationKey, Double> pair : toState.state.getValues()) {
			if (values.containsKey(pair.key))
				continue;
			ValueTimeline timeline = ValueTimeline.create(getInterpolationType());
			timeline.points.add(0, pair.key.getDefault());
			timeline.points.add(duration, pair.value);
			values.add(pair.key, timeline);
		}
		
		stateTransition.put(from + ":" + to, new AnimationTimeline(duration, values));
		
		return this;
	}
	
	public AnimationController removeTransition(String from, String to) {
		stateTransition.remove(from + ":" + to);
		return this;
	}
	
	public AnimationController addTransition(String from, String to, AnimationTimeline animation) {
		return addTransition(from + ":" + to, animation);
	}
	
	public AnimationController addTransition(String key, AnimationTimeline animation) {
		stateTransition.put(key, animation);
		return this;
	}
	
	public boolean isChanging() {
		return aimedState != null;
	}
	
	public AnimationControllerState getState(String key) {
		return states.get(key);
	}
	
	public boolean hasState(String key) {
		return states.containsKey(key);
	}
	
	public void startTransition(String key) {
		AnimationControllerState state = states.get(key);
		if (state == null)
			throw new RuntimeException("State '" + key + "' does not exist");
		
		tick = 0;
		tickingState = new AnimationState();
		aimedState = state;
		animation = stateTransition.get(currentState.name + ":" + aimedState.name);
		if (animation == null)
			throw new RuntimeException("Couldn't animate from '" + currentState.name + "' to '" + aimedState.name + "'");
	}
	
	public void endTransition() {
		currentState = aimedState;
		animation = null;
		aimedState = null;
		tickingState = null;
	}
	
	public AnimationState getTickingState() {
		if (isChanging()) {
			animation.tick(tick, tickingState);
			return tickingState;
		}
		return currentState.state;
	}
	
	public AnimationState tick() {
		if (isChanging()) {
			if (animation.tick(tick, tickingState)) {
				tick++;
				return tickingState;
			} else
				endTransition();
			
		}
		return currentState.state;
	}
	
	public static class AnimationControllerState {
		
		public String name;
		public AnimationState state;
		
		public AnimationControllerState(String name, AnimationState state) {
			this.name = name;
			this.state = state;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AnimationControllerState)
				return name.equals(((AnimationControllerState) obj).name);
			return false;
		}
		
		public void transform(LittleTransformation transformation) {
			if (transformation.rotX != 0) {
				Rotation rotation = transformation.getRotation(Axis.X);
				for (int i = 0; i < Math.abs(transformation.rotX); i++)
					state.transform(rotation);
			}
			if (transformation.rotY != 0) {
				Rotation rotation = transformation.getRotation(Axis.Y);
				for (int i = 0; i < Math.abs(transformation.rotY); i++)
					state.transform(rotation);
			}
			if (transformation.rotZ != 0) {
				Rotation rotation = transformation.getRotation(Axis.Z);
				for (int i = 0; i < Math.abs(transformation.rotZ); i++)
					state.transform(rotation);
			}
		}
		
	}
	
}
