package team.creative.littletiles.server;

import com.creativemd.littletiles.client.world.LittleAnimationHandlerClient;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.common.api.RayTraceResult;
import team.creative.littletiles.common.api.World;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.EntityAnimation;
import team.creative.littletiles.common.level.LittleAnimationHandler;
import team.creative.littletiles.common.level.WorldAnimationHandler;

public class LittleTilesServer {
    
    public static NeighborUpdateOrganizer NEIGHBOR;
    
    /** activates blocks and animations from littletiles on server side
     * 
     * @param player
     *            entity performing the action
     * @param pos
     *            start of the player ray
     * @param look
     *            end of the player ray
     * @return whether something has been activated or not */
    public static boolean playerRightClickServer(Player player, Vec3 pos, Vec3 look) {
        AABB box = new AABB(pos, look);
        World world = player.world;
        
        EntityAnimation pointedEntity = null;
        
        LittleAnimationHandler handler = WorldAnimationHandler.getHandler(world);
        
        RayTraceResult result = world.rayTraceBlocks(pos, look);
        double distance = result != null ? pos.distanceTo(result.hitVec) : 0;
        for (EntityAnimation animation : handler.findAnimations(box)) {
            RayTraceResult tempResult = LittleAnimationHandlerClient
                    .getTarget(animation.fakeWorld, animation.origin.transformPointToFakeWorld(pos), animation.origin.transformPointToFakeWorld(look), pos, look);
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
                BlockState state = world.getBlockState(result.getBlockPos());
                if (state.getBlock() instanceof BlockTile)
                    return new LittleActionActivated(world, result.getBlockPos(), pos, look, false).activateServer(player);
                return false;
            }
        } else
            return new LittleActionActivated(pointedEntity.fakeWorld, result.getBlockPos(), pos, look, false).activateServer(player);
        
        return false;
        
    }
    
}
