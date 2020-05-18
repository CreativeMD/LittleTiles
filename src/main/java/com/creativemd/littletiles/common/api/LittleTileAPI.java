package com.creativemd.littletiles.common.api;

import com.creativemd.littletiles.client.world.LittleAnimationHandlerClient;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.LittleAnimationHandler;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LittleTileAPI {
	
	/** activates blocks and animations from littletiles on server side
	 * 
	 * @param player
	 *            entity performing the action
	 * @param pos
	 *            start of the player ray
	 * @param look
	 *            end of the player ray
	 * @return whether something has been activated or not */
	public static boolean playerRightClickServer(EntityPlayer player, Vec3d pos, Vec3d look) {
		AxisAlignedBB box = new AxisAlignedBB(pos, look);
		World world = player.world;
		
		EntityAnimation pointedEntity = null;
		
		LittleAnimationHandler handler = WorldAnimationHandler.getHandler(world);
		
		RayTraceResult result = world.rayTraceBlocks(pos, look);
		double distance = result != null ? pos.distanceTo(result.hitVec) : 0;
		for (EntityAnimation animation : handler.findAnimations(box)) {
			RayTraceResult tempResult = LittleAnimationHandlerClient.getTarget(animation.fakeWorld, animation.origin.transformPointToFakeWorld(pos), animation.origin.transformPointToFakeWorld(look), pos, look);
			if (tempResult == null || tempResult.typeOfHit != RayTraceResult.Type.BLOCK)
				continue;
			double tempDistance = pos.distanceTo(animation.origin.transformPointToWorld(tempResult.hitVec));
			if (result == null || tempDistance < distance) {
				result = tempResult;
				distance = tempDistance;
				pointedEntity = animation;
			}
		}
		
		if (pointedEntity == null) {
			if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
				IBlockState state = world.getBlockState(result.getBlockPos());
				if (state.getBlock() instanceof BlockTile)
					return new LittleActionActivated(world, result.getBlockPos(), pos, look, false).activateServer(player);
				return false;
			}
		} else
			return new LittleActionActivated(pointedEntity.world, result.getBlockPos(), pos, look, false).activateServer(player);
		
		return false;
		
	}
	
}
