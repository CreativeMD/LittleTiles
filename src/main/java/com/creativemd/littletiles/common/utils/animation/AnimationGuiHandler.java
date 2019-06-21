package com.creativemd.littletiles.common.utils.animation;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.IAnimationHandler;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.connection.IStructureChildConnector;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;

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
		this.offX = animation.origin.offX();
		this.offY = animation.origin.offY();
		this.offZ = animation.origin.offZ();
		this.rotX = animation.origin.rotX();
		this.rotY = animation.origin.rotY();
		this.rotZ = animation.origin.rotZ();
	}
	
	private int setDuration = 0;
	private int lastTick = -1;
	private StructureAbsolute center = null;
	private boolean loop = true;
	private boolean playing = false;
	private int tick = 0;
	private AnimationTimeline timeline;
	private AnimationState state = new AnimationState();
	
	private List<AnimationGuiHolder> subHolders = new ArrayList<>();
	private PairList<Integer, Integer> childActivation;
	private boolean childrenChanged = false;
	
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
	
	public void tick(LittlePreviews previews, EntityAnimation animation) {
		if (timeline == null)
			return;
		
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
		
		boolean hasChanged = false;
		if (childrenChanged) {
			subHolders.clear();
			if (childActivation != null) {
				for (Pair<Integer, Integer> pair : childActivation) {
					IStructureChildConnector connector = animation.structure.children.get(pair.key);
					if (connector != null && connector.isConnected(animation.world) && connector.getStructureWithoutLoading() instanceof LittleDoor) {
						LittleDoor child = (LittleDoor) connector.getStructureWithoutLoading();
						EntityAnimation childAnimation;
						if (!connector.isLinkToAnotherWorld())
							childAnimation = child.openDoor(null, new UUIDSupplier(), LittleDoor.EMPTY_OPENING_RESULT);
						else
							childAnimation = connector.getAnimation();
						GuiParent parent = new GuiParent("temp", 0, 0, 0, 0) {
						};
						AnimationGuiHolder holder = new AnimationGuiHolder(previews.getChildren().get(pair.key), new AnimationGuiHandler(pair.value, this), childAnimation);
						holder.handler.takeInitialState(childAnimation);
						LittleStructureGuiParser parser = LittleStructureRegistry.getParser(parent, holder.handler, LittleStructureRegistry.getParserClass("structure." + child.type.id + ".name"));
						parser.createControls(holder.previews, child);
						if (holder.handler.timeline != null)
							subHolders.add(holder);
					}
				}
			}
			hasChanged = true;
			childrenChanged = false;
		}
		
		for (AnimationGuiHolder holder : subHolders)
			holder.handler.tick(holder.previews, holder.animation);
		
		if (hasChanged)
			updateTimeline();
		
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
	
	public void setTimeline(AnimationTimeline timeline, PairList<Integer, Integer> children) {
		this.timeline = timeline;
		if (this.timeline != null) {
			this.timeline.offset(offset);
			this.setDuration = this.timeline.duration;
			updateTimeline();
		} else
			setDuration = 0;
		state.clear();
		
		this.childActivation = children == null ? null : new PairList<>(children);
		this.childrenChanged = true;
	}
	
	private static class AnimationGuiHolder {
		
		public final LittlePreviews previews;
		public final AnimationGuiHandler handler;
		public final EntityAnimation animation;
		
		public AnimationGuiHolder(LittlePreviews previews, AnimationGuiHandler handler, EntityAnimation animation) {
			this.previews = previews;
			this.handler = handler;
			this.animation = animation;
		}
	}
}
