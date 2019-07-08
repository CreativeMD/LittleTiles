package com.creativemd.littletiles.common.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.client.render.entity.RenderAnimation;
import com.creativemd.littletiles.common.entity.EntityAnimation;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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
	
	protected List<EntityAnimation> toBeAdded = new ArrayList<>();
	protected boolean isTicking = false;
	public List<EntityAnimation> openDoors = new CopyOnWriteArrayList<>();
	
	public List<EntityAnimation> findDoors(World world, AxisAlignedBB bb) {
		if (openDoors.isEmpty())
			return Collections.emptyList();
		
		List<EntityAnimation> doors = new ArrayList<>();
		for (EntityAnimation door : openDoors) {
			if (door.getEntityBoundingBox().intersects(bb))
				doors.add(door);
		}
		return doors;
	}
	
	public List<EntityAnimation> findDoors(World world, BlockPos pos) {
		if (openDoors.isEmpty())
			return Collections.emptyList();
		
		AxisAlignedBB box = new AxisAlignedBB(pos);
		
		List<EntityAnimation> doors = new ArrayList<>();
		for (EntityAnimation door : openDoors) {
			if (door.world == world && door.getEntityBoundingBox().intersects(box))
				doors.add(door);
		}
		return doors;
	}
	
	public EntityAnimation findDoor(UUID uuid) {
		for (EntityAnimation animation : openDoors) {
			if (animation.getUniqueID().equals(uuid))
				return animation;
		}
		return null;
	}
	
	public void createDoor(EntityAnimation door) {
		if (isTicking)
			toBeAdded.add(door);
		else
			openDoors.add(door);
	}
	
	@SubscribeEvent
	public void tick(WorldTickEvent event) {
		if (event.side == side && event.phase == Phase.END) {
			World world = event.world;
			openDoors.addAll(toBeAdded);
			toBeAdded.clear();
			
			isTicking = true;
			for (EntityAnimation door : openDoors) {
				
				if (door.world instanceof CreativeWorld)
					continue;
				
				door.onUpdateForReal();
			}
			
			openDoors.removeIf((x) -> {
				if (x.isDead) {
					x.markRemoved();
					return true;
				}
				return false;
			});
			
			isTicking = false;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static RenderAnimation render;
	
	@SideOnly(Side.CLIENT)
	public static void renderTick() {
		if (render == null)
			render = new RenderAnimation(Minecraft.getMinecraft().getRenderManager());
		
		float partialTicks = TickUtils.getPartialTickTime();
		
		Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
		if (renderViewEntity == null || client.openDoors.isEmpty())
			return;
		
		double camX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * partialTicks;
		double camY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * partialTicks;
		double camZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * partialTicks;
		
		ICamera camera = new Frustum();
		camera.setPosition(camX, camY, camZ);
		
		for (EntityAnimation door : client.openDoors) {
			
			if (!render.shouldRender(door, camera, camX, camY, camZ) || door.isDead)
				continue;
			
			if (door.ticksExisted == 0) {
				door.lastTickPosX = door.posX;
				door.lastTickPosY = door.posY;
				door.lastTickPosZ = door.posZ;
			}
			
			double d0 = door.lastTickPosX + (door.posX - door.lastTickPosX) * partialTicks;
			double d1 = door.lastTickPosY + (door.posY - door.lastTickPosY) * partialTicks;
			double d2 = door.lastTickPosZ + (door.posZ - door.lastTickPosZ) * partialTicks;
			
			float f = door.prevRotationYaw + (door.rotationYaw - door.prevRotationYaw) * partialTicks;
			int i = door.getBrightnessForRender();
			
			if (door.isBurning()) {
				i = 15728880;
			}
			
			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			try {
				// render.setRenderOutlines(render.getRenderManager().renderOutlines);
				render.doRender(door, d0 - camX, d1 - camY, d2 - camZ, f, partialTicks);
			} catch (Throwable throwable1) {
				throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Rendering entity in world"));
			}
		}
	}
	
	@SubscribeEvent
	public void rightClick(PlayerInteractEvent event) {
		if (event instanceof RightClickBlock || event instanceof RightClickEmpty || event instanceof RightClickItem || event instanceof EntityInteractSpecific || event instanceof EntityInteract) {
			if (!event.getWorld().isRemote)
				return;
			
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer entity = event.getEntityPlayer();
			Vec3d vec3d = entity.getPositionEyes(TickUtils.getPartialTickTime());
			double d0 = mc.playerController.getBlockReachDistance();
			double d1 = d0;
			boolean flag = false;
			if (mc.playerController.extendedReach()) {
				d1 = 6.0D;
				d0 = d1;
			} else {
				if (d0 > 3.0D) {
					flag = true;
				}
			}
			
			if (mc.objectMouseOver != null) {
				d1 = mc.objectMouseOver.hitVec.distanceTo(vec3d);
			}
			
			Vec3d vec3d1 = entity.getLook(TickUtils.getPartialTickTime());
			Vec3d vec3d2 = vec3d.addVector(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
			EntityAnimation pointedEntity = null;
			Vec3d vec3d3 = null;
			float f = 1.0F;
			List<EntityAnimation> list = findDoors(entity.world, entity.getEntityBoundingBox().expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0D, 1.0D, 1.0D));
			double d2 = d1;
			
			for (int j = 0; j < list.size(); ++j) {
				EntityAnimation entity1 = list.get(j);
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);
				
				if (axisalignedbb.contains(vec3d)) {
					if (d2 >= 0.0D) {
						pointedEntity = entity1;
						vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
						d2 = 0.0D;
					}
				} else if (raytraceresult != null) {
					double d3 = vec3d.distanceTo(raytraceresult.hitVec);
					
					if (d3 < d2 || d2 == 0.0D) {
						if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity1.canRiderInteract()) {
							if (d2 == 0.0D) {
								pointedEntity = entity1;
								vec3d3 = raytraceresult.hitVec;
							}
						} else {
							pointedEntity = entity1;
							vec3d3 = raytraceresult.hitVec;
							d2 = d3;
						}
					}
				}
			}
			
			if (pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > 3.0D) {
				return;
			}
			
			Entity selectedEntity = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;
			if (event instanceof EntityInteractSpecific)
				selectedEntity = ((EntityInteractSpecific) event).getTarget();
			else if (event instanceof EntityInteract)
				selectedEntity = ((EntityInteract) event).getTarget();
			
			if (pointedEntity == null && selectedEntity instanceof EntityAnimation)
				pointedEntity = (EntityAnimation) selectedEntity;
			
			if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null || selectedEntity == pointedEntity)) {
				pointedEntity.onRightClick(entity, vec3d, vec3d2);
				
				if (event instanceof RightClickBlock)
					event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickClient(ClientTickEvent event) {
		if (event.side == side && event.phase == Phase.END && (!Minecraft.getMinecraft().isSingleplayer() || !Minecraft.getMinecraft().isGamePaused())) {
			for (EntityAnimation door : openDoors) {
				if (door.world instanceof CreativeWorld)
					continue;
				door.onUpdateForReal();
			}
			
			openDoors.removeIf((x) -> {
				if (x.isDead) {
					x.markRemoved();
					return true;
				}
				return false;
			});
		}
	}
	
	@SubscribeEvent
	public void chunkUnload(ChunkEvent.Unload event) {
		for (ClassInheritanceMultiMap<Entity> map : event.getChunk().getEntityLists()) {
			for (Entity entity : map) {
				if (entity instanceof EntityAnimation && ((EntityAnimation) entity).isDoorAdded()) {
					((EntityAnimation) entity).markRemoved();
				}
			}
		}
		
		openDoors.removeIf((x) -> x.isDead);
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {
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
		if (event.getWorld().isRemote != side.isClient())
			return;
		
		AxisAlignedBB box = event.getAabb();
		for (EntityAnimation animation : findDoors(event.getWorld(), box)) {
			if (animation.noCollision)
				continue;
			
			OrientatedBoundingBox newAlignedBox = animation.origin.getOrientatedBox(box);
			for (OrientatedBoundingBox bb : animation.worldCollisionBoxes) {
				if (bb.intersects(newAlignedBox))
					event.getCollisionBoxesList().add(bb);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	private EntityPlayer lastPlayerRayTraceResult;
	@SideOnly(Side.CLIENT)
	private RayTraceResult lastRayTraceResult;
	@SideOnly(Side.CLIENT)
	private CreativeWorld lastWorldRayTraceResult;
	
	@SideOnly(Side.CLIENT)
	public RayTraceResult getRayTraceResult(EntityPlayer player, float partialTicks, @Nullable RayTraceResult target) {
		if (lastPlayerRayTraceResult == player)
			return lastRayTraceResult;
		
		Vec3d pos = player.getPositionEyes(partialTicks);
		double d0 = target != null ? pos.distanceTo(target.hitVec) : (player.capabilities.isCreativeMode ? 5.0 : 4.5);
		Vec3d look = player.getLook(partialTicks);
		look = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		
		AxisAlignedBB box = new AxisAlignedBB(pos, target != null ? target.hitVec : look);
		World world = player.world;
		
		RayTraceResult result = target;
		double distance = result != null ? pos.distanceTo(result.hitVec) : 0;
		for (EntityAnimation animation : findDoors(world, box)) {
			RayTraceResult tempResult = getTarget(animation.fakeWorld, animation.origin.transformPointToFakeWorld(pos), animation.origin.transformPointToFakeWorld(look), pos, look);
			if (tempResult == null || tempResult.typeOfHit != RayTraceResult.Type.BLOCK)
				continue;
			double tempDistance = pos.distanceTo(tempResult.hitVec);
			if (result == null || tempDistance < distance) {
				result = tempResult;
				distance = tempDistance;
			}
		}
		
		lastPlayerRayTraceResult = player;
		if (result == target)
			result = null;
		lastRayTraceResult = result;
		lastWorldRayTraceResult = result != null ? (CreativeWorld) result.hitInfo : null;
		return result;
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderLast(RenderWorldLastEvent event) {
		
		if (side.isServer())
			return;
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if (mc.gameSettings.hideGUI)
			return;
		EntityPlayer player = mc.player;
		float partialTicks = event.getPartialTicks();
		
		lastPlayerRayTraceResult = null;
		lastRayTraceResult = null;
		lastWorldRayTraceResult = null;
		
		RayTraceResult result = getRayTraceResult(player, event.getPartialTicks(), (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK) ? mc.objectMouseOver : null);
		
		if (result == null)
			return;
		
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.enableRescaleNormal();
		
		BlockPos blockpos = result.getBlockPos();
		IBlockState iblockstate = lastWorldRayTraceResult.getBlockState(blockpos);
		
		if (iblockstate.getMaterial() != Material.AIR && lastWorldRayTraceResult.getWorldBorder().contains(blockpos)) {
			
			EntityAnimation entity = (EntityAnimation) lastWorldRayTraceResult.parent;
			GlStateManager.pushMatrix();
			
			entity.origin.setupRendering(entity, partialTicks);
			RenderGlobal.drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(lastWorldRayTraceResult, blockpos).grow(0.0020000000949949026D), 0.0F, 0.0F, 0.0F, 0.4F);
			GlStateManager.popMatrix();
		}
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event) {
		if (getRayTraceResult(lastPlayerRayTraceResult, event.getPartialTicks(), event.getTarget()) != null)
			event.setCanceled(true);
	}
	
	public static RayTraceResult getTarget(CreativeWorld world, Vec3d pos, Vec3d look, Vec3d originalPos, Vec3d originalLook) {
		RayTraceResult tempResult = world.rayTraceBlocks(pos, look);
		if (tempResult == null || tempResult.typeOfHit != RayTraceResult.Type.BLOCK)
			return null;
		tempResult.hitInfo = world;
		return tempResult;
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
