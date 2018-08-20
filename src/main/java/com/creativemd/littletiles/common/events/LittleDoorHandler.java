package com.creativemd.littletiles.common.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleDoorHandler {
	
	public static LittleDoorHandler client;
	public static LittleDoorHandler server;
	
	public static LittleDoorHandler getHandler(World world)
	{
		if(world.isRemote)
			return client;
		return server;
	}
	
	public final Side side;
	public LittleDoorHandler(Side side)
	{
		this.side = side;
	}
	
	public List<EntityAnimation> openDoors = new ArrayList<>();
	
	public List<EntityAnimation> findDoors(World world, AxisAlignedBB bb)
	{
		List<EntityAnimation> doors = new ArrayList<>();
		for (EntityAnimation door : openDoors) {
			if(door.world == world && door.getEntityBoundingBox().intersects(bb))
				doors.add(door);
		}
		return doors;
	}
	
	public void createDoor(EntityAnimation door)
	{
		openDoors.add(door);
	}
	
	@SubscribeEvent
	public void tick(WorldTickEvent event)
	{
		if(event.side == side && event.phase == Phase.END)
		{
			for (Iterator iterator = openDoors.iterator(); iterator.hasNext();) {
				EntityDoorAnimation door = (EntityDoorAnimation) iterator.next();
				
				door.onUpdateForReal();
				
				if(door.isDead)
					iterator.remove();
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickClient(ClientTickEvent event)
	{
		if(event.side == side && event.phase == Phase.END)
		{
			for (Iterator iterator = openDoors.iterator(); iterator.hasNext();) {
				EntityDoorAnimation door = (EntityDoorAnimation) iterator.next();
				if(door.isDead)
					iterator.remove();
				
				door.onUpdateForReal();
			}
		}
	}
	
	@SubscribeEvent
	public void worldCollision(GetCollisionBoxesEvent event)
	{
		if(event.getWorld().isRemote != side.isClient())
			return ;
		
		AxisAlignedBB box = event.getAabb();
		for (EntityAnimation<?> animation : findDoors(event.getWorld(), box)) {
			if(animation.noCollision)
				continue;
			
			OrientatedBoundingBox newAlignedBox = animation.origin.getOrientatedBox(box);			
			for (OrientatedBoundingBox bb : animation.worldCollisionBoxes) {
				if(bb.intersects(newAlignedBox))
					event.getCollisionBoxesList().add(bb);
			}
		}
	}
	
	private static Field wasPushedByDoor = ReflectionHelper.findField(EntityPlayerMP.class, "wasPushedByDoor");
	
	public static void setPushedByDoor(EntityPlayerMP player)
	{
		try {
			wasPushedByDoor.setInt(player, 10);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkIfEmpty(List<AxisAlignedBB> boxes, EntityPlayerMP player)
	{
		try {
			if(wasPushedByDoor.getInt(player) > 0)
				return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return boxes.isEmpty();
	}
	
}
