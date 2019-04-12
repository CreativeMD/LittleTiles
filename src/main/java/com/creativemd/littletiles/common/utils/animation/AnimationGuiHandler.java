package com.creativemd.littletiles.common.utils.animation;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.gui.controls.gui.timeline.IAnimationHandler;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.gui.controls.IAnimationControl;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;

public class AnimationGuiHandler implements IAnimationHandler {
	
	private IAnimationControl parent;
	
	public AnimationGuiHandler(IAnimationControl parent) {
		this.parent = parent;
	}
	
	private int lastTick = -1;
	private StructureAbsolute center = null;
	private boolean loop = true;
	private boolean playing = false;
	private int tick = 0;
	private AnimationTimeline timeline;
	private AnimationState state = new AnimationState();
	
	public void setCenter(StructureAbsolute center) {
		this.center = center;
	}
	
	@Override
	public void loop(boolean loop) {
		this.loop = loop;
	}
	
	@Override
	public void play() {
		playing = true;
	}
	
	@Override
	public void pause() {
		playing = false;
	}
	
	@Override
	public void stop() {
		playing = false;
		set(0);
	}
	
	@Override
	public void set(int tick) {
		this.tick = tick;
	}
	
	@Override
	public int get() {
		return tick;
	}
	
	public void tick(EntityAnimation animation) {
		if (playing) {
			if (tick > timeline.duration) {
				if (loop)
					tick = 0;
			} else
				tick++;
		}
		
		if (center != null) {
			animation.setCenter(center);
			center = null;
		}
		
		updateTick(animation);
	}
	
	public void updateTick(EntityAnimation animation) {
		animation.prevWorldOffsetX = animation.worldOffsetX;
		animation.prevWorldOffsetY = animation.worldOffsetY;
		animation.prevWorldOffsetZ = animation.worldOffsetZ;
		animation.prevWorldRotX = animation.worldRotX;
		animation.prevWorldRotY = animation.worldRotY;
		animation.prevWorldRotZ = animation.worldRotZ;
		
		if (tick == lastTick)
			return;
		
		lastTick = tick;
		if (timeline != null)
			timeline.tick(Math.min(tick, timeline.duration), state);
		else
			state.clear();
		
		Vector3d offset = state.getOffset();
		Vector3d rotation = state.getRotation();
		
		animation.moveAndRotateAnimation(offset.x - animation.worldOffsetX, offset.y - animation.worldOffsetY, offset.z - animation.worldOffsetZ, rotation.x - animation.worldRotX, rotation.y - animation.worldRotY, rotation.z - animation.worldRotZ);
	}
	
	public void setTimeline(AnimationTimeline timeline) {
		this.timeline = timeline;
		state.clear();
	}
	
}
