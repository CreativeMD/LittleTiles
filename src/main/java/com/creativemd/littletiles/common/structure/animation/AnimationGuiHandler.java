package com.creativemd.littletiles.common.structure.animation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.gui.controls.gui.timeline.IAnimationHandler;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

public class AnimationGuiHandler implements IAnimationHandler {
	
	public double offX;
	public double offY;
	public double offZ;
	public double rotX;
	public double rotY;
	public double rotZ;
	
	public int offset;
	
	public AnimationGuiHandler(int offset, AnimationGuiHandler copy) {
		this.offset = offset;
		this.lastTick = copy.lastTick;
		this.loop = copy.loop;
		this.playing = copy.playing;
		this.tick = copy.tick;
	}
	
	public AnimationGuiHandler() {
		
	}
	
	public void takeInitialState(EntityAnimation animation) {
		this.offX = animation.initalOffX;
		this.offY = animation.initalOffY;
		this.offZ = animation.initalOffZ;
		this.rotX = animation.initalRotX;
		this.rotY = animation.initalRotY;
		this.rotZ = animation.initalRotZ;
	}
	
	private int setDuration = 0;
	private int lastTick = -1;
	private StructureAbsolute center = null;
	private boolean loop = true;
	private boolean playing = false;
	private int tick = 0;
	private AnimationTimeline timeline;
	private AnimationState state = new AnimationState();
	
	public List<AnimationGuiHolder> subHolders = new ArrayList<>();
	private List<AnimationEvent> events;
	private boolean eventsChanged = false;
	
	public boolean hasTimeline() {
		return timeline != null;
	}
	
	public void setCenter(StructureAbsolute center) {
		this.center = center;
	}
	
	@Override
	public void loop(boolean loop) {
		this.loop = loop;
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.loop(loop);
	}
	
	@Override
	public void play() {
		playing = true;
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.play();
	}
	
	@Override
	public void pause() {
		playing = false;
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.pause();
	}
	
	@Override
	public void stop() {
		playing = false;
		set(0);
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.stop();
	}
	
	@Override
	public void set(int tick) {
		this.tick = tick;
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.set(tick);
	}
	
	@Override
	public int get() {
		return tick;
	}
	
	public void tick(LittlePreviews previews, LittleStructure structure, EntityAnimation animation) {
		if (timeline == null)
			return;
		
		if (playing) {
			
			for (AnimationEvent event : events)
				if (event.getTick() == tick)
					event.runGui(this);
				
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
		
		boolean hasChanged = false;
		if (eventsChanged) {
			for (AnimationGuiHolder holder : subHolders) {
				holder.handler.stop();
				if (holder.animation != null) {
					holder.handler.updateTick(holder.animation);
					holder.handler.updateTick(holder.animation);
				}
			}
			subHolders.clear();
			for (AnimationEvent event : events)
				event.prepareInGui(previews, structure, animation, this);
			hasChanged = true;
			eventsChanged = false;
		}
		
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.tick(holder.previews, holder.structure, holder.animation);
		
		if (hasChanged)
			updateTimeline();
		
		if (animation != null)
			updateTick(animation);
	}
	
	public void updateTick(EntityAnimation animation) {
		animation.origin.tick();
		
		if (tick == lastTick)
			return;
		
		lastTick = tick;
		if (timeline != null)
			timeline.tick(Math.min(tick, timeline.duration), state);
		else
			state.clear();
		
		Vector3d offset = state.getOffset();
		Vector3d rotation = state.getRotation();
		
		animation.moveAndRotateAnimation(offset.x - animation.origin.offX() + offX, offset.y - animation.origin.offY() + offY, offset.z - animation.origin.offZ() + offZ, rotation.x - animation.origin.rotX() + rotX, rotation.y - animation.origin.rotY() + -rotY, rotation.z - animation.origin.rotZ() + rotZ);
	}
	
	public int getMaxDuration() {
		int duration = setDuration;
		for (AnimationGuiHolder holder : subHolders)
			duration = Math.max(holder.handler.getMaxDuration(), duration);
		return duration;
	}
	
	public void updateTimeline() {
		syncTimelineDuration(getMaxDuration());
	}
	
	public void syncTimelineDuration(int duration) {
		this.timeline.duration = duration;
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.syncTimelineDuration(duration);
	}
	
	public void setTimeline(AnimationTimeline timeline, List<AnimationEvent> events) {
		this.timeline = timeline;
		if (this.timeline != null) {
			this.timeline.offset(offset);
			this.setDuration = this.timeline.duration;
			updateTimeline();
		} else
			setDuration = 0;
		state.clear();
		
		this.events = events == null ? new ArrayList<>() : new ArrayList<>(events);
		this.eventsChanged = true;
	}
	
	public static class AnimationGuiHolder {
		
		public final LittlePreviews previews;
		public final AnimationGuiHandler handler;
		public final LittleStructure structure;
		@Nullable
		public final EntityAnimation animation;
		
		public AnimationGuiHolder(LittlePreviews previews, AnimationGuiHandler handler, LittleStructure structure, @Nullable EntityAnimation animation) {
			this.previews = previews;
			this.handler = handler;
			this.structure = structure;
			this.animation = animation;
			if (animation != null)
				handler.takeInitialState(animation);
		}
	}
}
