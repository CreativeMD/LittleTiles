package com.creativemd.littletiles.common.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.littletiles.client.render.entity.RenderAnimation;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleDoorHandler {
	
	public static LittleDoorHandler client;
	public static LittleDoorHandler server;
	
	public static LittleDoorHandler getHandler(World world) {
		if (world.isRemote)
			return client;
		return server;
	}
	
	public final Side side;
	
	public LittleDoorHandler(Side side) {
		this.side = side;
	}
	
	public List<EntityAnimation> openDoors = new ArrayList<>();
	
	public List<EntityAnimation> findDoors(World world, AxisAlignedBB bb) {
		if (openDoors.isEmpty())
			return Collections.emptyList();
		
		List<EntityAnimation> doors = new ArrayList<>();
		for (EntityAnimation door : openDoors) {
			if (door.world == world && door.getEntityBoundingBox().intersects(bb))
				doors.add(door);
		}
		return doors;
	}
	
	public void createDoor(EntityAnimation door) {
		openDoors.add(door);
	}
	
	@SubscribeEvent
	public void tick(WorldTickEvent event) {
		if (event.side == side && event.phase == Phase.END) {
			for (Iterator iterator = openDoors.iterator(); iterator.hasNext();) {
				EntityDoorAnimation door = (EntityDoorAnimation) iterator.next();
				
				door.onUpdateForReal();
				
				if (door.isDead)
					iterator.remove();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public Render render;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderTick(RenderWorldLastEvent event) {
		if (side.isClient()) {
			if (render == null)
				render = new RenderAnimation(Minecraft.getMinecraft().getRenderManager());
			
			float partialTicks = event.getPartialTicks();
			
			Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
			if (renderViewEntity == null || openDoors.isEmpty())
				return;
			double camX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
			double camY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
			double camZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;
			
			ICamera camera = new Frustum();
			camera.setPosition(camX, camY, camZ);
			
			for (EntityAnimation door : openDoors) {
				
				if (!render.shouldRender(door, camera, camX, camY, camZ) || door.isDead)
					continue;
				
				if (door.ticksExisted == 0) {
					door.lastTickPosX = door.posX;
					door.lastTickPosY = door.posY;
					door.lastTickPosZ = door.posZ;
				}
				
				double d0 = door.lastTickPosX + (door.posX - door.lastTickPosX) * (double) partialTicks;
				double d1 = door.lastTickPosY + (door.posY - door.lastTickPosY) * (double) partialTicks;
				double d2 = door.lastTickPosZ + (door.posZ - door.lastTickPosZ) * (double) partialTicks;
				float f = door.prevRotationYaw + (door.rotationYaw - door.prevRotationYaw) * partialTicks;
				int i = door.getBrightnessForRender();
				
				if (door.isBurning()) {
					i = 15728880;
				}
				
				int j = i % 65536;
				int k = i / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				try {
					// render.setRenderOutlines(render.getRenderManager().renderOutlines);
					render.doRender(door, d0 - TileEntityRendererDispatcher.staticPlayerX, d1 - TileEntityRendererDispatcher.staticPlayerY, d2 - TileEntityRendererDispatcher.staticPlayerZ, f, partialTicks);
				} catch (Throwable throwable1) {
					throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Rendering entity in world"));
				}
				
				/*
				 * try { if (!this.renderOutlines) { render.doRenderShadowAndFire(entityIn, x,
				 * y, z, yaw, partialTicks); } } catch (Throwable throwable2) { throw new
				 * ReportedException(CrashReport.makeCrashReport(throwable2,
				 * "Post-rendering entity in world")); }
				 * 
				 * if (this.debugBoundingBox && !entityIn.isInvisible() && !p_188391_10_ &&
				 * !Minecraft.getMinecraft().isReducedDebug()) { try {
				 * this.renderDebugBoundingBox(entityIn, x, y, z, yaw, partialTicks); } catch
				 * (Throwable throwable) { throw new
				 * ReportedException(CrashReport.makeCrashReport(throwable,
				 * "Rendering entity hitbox in world")); } }
				 */
			}
			
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickClient(ClientTickEvent event) {
		if (event.side == side && event.phase == Phase.END) {
			for (Iterator iterator = openDoors.iterator(); iterator.hasNext();) {
				EntityDoorAnimation door = (EntityDoorAnimation) iterator.next();
				if (door.isDead)
					iterator.remove();
				
				door.onUpdateForReal();
			}
		}
	}
	
	@SubscribeEvent
	public void chunkUnload(ChunkEvent.Unload event) {
		for (ClassInheritanceMultiMap<Entity> map : event.getChunk().getEntityLists()) {
			for (Entity entity : map) {
				if (entity instanceof EntityAnimation && ((EntityAnimation) entity).addedDoor) {
					((EntityAnimation) entity).addedDoor = false;
					openDoors.remove(entity);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void worldCollision(GetCollisionBoxesEvent event) {
		if (event.getWorld().isRemote != side.isClient())
			return;
		
		AxisAlignedBB box = event.getAabb();
		for (EntityAnimation<?> animation : findDoors(event.getWorld(), box)) {
			if (animation.noCollision)
				continue;
			
			OrientatedBoundingBox newAlignedBox = animation.origin.getOrientatedBox(box);
			for (OrientatedBoundingBox bb : animation.worldCollisionBoxes) {
				if (bb.intersects(newAlignedBox))
					event.getCollisionBoxesList().add(bb);
			}
		}
	}
	
	private static Field wasPushedByDoor = ReflectionHelper.findField(EntityPlayerMP.class, "wasPushedByDoor");
	
	public static void setPushedByDoor(EntityPlayerMP player) {
		try {
			wasPushedByDoor.setInt(player, 10);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkIfEmpty(List<AxisAlignedBB> boxes, EntityPlayerMP player) {
		try {
			if (wasPushedByDoor.getInt(player) > 0)
				return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return boxes.isEmpty();
	}
	
}
