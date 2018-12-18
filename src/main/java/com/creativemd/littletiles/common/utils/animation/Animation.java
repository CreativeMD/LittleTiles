package com.creativemd.littletiles.common.utils.animation;

public abstract class Animation {
	
	public final long duration;
	
	public Animation(long duration) {
		this.duration = duration;
	}
	
	protected long tick;
	protected boolean done = false;
	
	public boolean tick(Animation parent, AnimationState currentState) {
		if (!isDone()) {
			
			if (tick == 0)
				begin(currentState);
			else
				tick(currentState);
			
			tick++;
			
			return true;
		}
		
		if (!done) {
			end(currentState);
			done = true;
		}
		
		return false;
	}
	
	public void begin(AnimationState currentState) {
		tick(currentState);
	}
	
	public abstract void tick(AnimationState currentState);
	
	public abstract void end(AnimationState currentState);
	
	public long progress() {
		return tick;
	}
	
	public void setProgress(long tick) {
		this.tick = tick;
	}
	
	public boolean isDone() {
		return tick >= duration;
	}
}
