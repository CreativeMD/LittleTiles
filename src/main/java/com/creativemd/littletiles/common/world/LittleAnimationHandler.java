package com.creativemd.littletiles.common.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class LittleAnimationHandler {
	
	public final World world;
	
	public LittleAnimationHandler(World world) {
		if (world == null)
			throw new RuntimeException("Creating handler for empty world!");
		
		this.world = world;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public List<EntityAnimation> openDoors = new CopyOnWriteArrayList<>();
	
	public List<EntityAnimation> findAnimations(AxisAlignedBB bb) {
		if (openDoors.isEmpty())
			return Collections.emptyList();
		
		List<EntityAnimation> doors = new ArrayList<>();
		for (EntityAnimation door : openDoors) {
			if (door.getEntityBoundingBox().intersects(bb))
				doors.add(door);
		}
		return doors;
	}
	
	public List<LittleDoor> findAnimations(BlockPos pos) {
		if (openDoors.isEmpty())
			return Collections.emptyList();
		
		AxisAlignedBB box = new AxisAlignedBB(pos);
		
		List<LittleDoor> doors = new ArrayList<>();
		for (EntityAnimation door : openDoors)
			if (door.structure instanceof LittleDoor && door.getEntityBoundingBox().intersects(box) && !doors.contains(door.structure))
				doors.add(((LittleDoor) door.structure).getParentDoor());
		return doors;
	}
	
	public EntityAnimation findAnimation(UUID uuid) {
		for (EntityAnimation animation : openDoors) {
			if (animation.getUniqueID().equals(uuid))
				return animation;
		}
		return null;
	}
	
	public void createDoor(EntityAnimation door) {
		openDoors.add(door);
	}
	
	@SubscribeEvent
	public void chunkUnload(ChunkEvent.Unload event) {
		if (event.getWorld() != world)
			return;
		
		openDoors.removeIf((x) -> {
			if (x.isDead) {
				x.markRemoved();
				return true;
			}
			return false;
		});
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() != world)
			return;
		
		openDoors.removeIf((x) -> {
			if (x.world == event.getWorld()) {
				x.markRemoved();
				return true;
			}
			return false;
		});
	}
	
	@SubscribeEvent
	public void worldCollision(GetCollisionBoxesEvent event) {
		if (event.getWorld() != world)
			return;
		
		AxisAlignedBB box = event.getAabb();
		for (EntityAnimation animation : findAnimations(box)) {
			if (animation.noCollision)
				continue;
			
			OrientatedBoundingBox newAlignedBox = animation.origin.getOrientatedBox(box);
			for (OrientatedBoundingBox bb : animation.worldCollisionBoxes) {
				if (bb.intersects(newAlignedBox))
					event.getCollisionBoxesList().add(bb);
			}
		}
	}
	
}
