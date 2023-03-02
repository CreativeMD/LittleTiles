package team.creative.littletiles.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import team.creative.littletiles.common.action.LittleActionActivated;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.handler.LevelHandlers;
import team.creative.littletiles.common.level.handler.LittleAnimationHandler;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.math.vec.LittleHitResult;
import team.creative.littletiles.server.level.handler.LittleActionHandlerServer;
import team.creative.littletiles.server.level.util.NeighborUpdateOrganizer;

public class LittleTilesServer {
    
    public static final LittleAnimationHandlers ANIMATION_HANDLERS = new LittleAnimationHandlers();
    public static final LevelHandlers LEVEL_HANDLERS = new LevelHandlers(false);
    public static NeighborUpdateOrganizer NEIGHBOR;
    
    public static void init(FMLCommonSetupEvent event) {
        NEIGHBOR = new NeighborUpdateOrganizer();
    }
    
    /** activates blocks and animations from littletiles on server side
     * 
     * @param player
     *            entity performing the action
     * @param pos
     *            start of the player ray
     * @param look
     *            end of the player ray
     * @return whether something has been activated or not */
    public static boolean playerRightClickServer(ServerPlayer player, Vec3 pos, Vec3 look) {
        AABB box = new AABB(pos, look);
        Level level = player.level;
        
        LittleEntity pointedEntity = null;
        
        LittleAnimationHandler handler = LittleAnimationHandlers.get(level);
        
        BlockHitResult result = level.clip(new ClipContext(pos, look, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double distance = result != null ? pos.distanceTo(result.getLocation()) : 0;
        for (LittleEntity animation : handler.find(box)) {
            LittleHitResult tempResult = handler.getHit(pos, look, pos.distanceTo(look));
            if (!tempResult.isBlock())
                continue;
            double tempDistance = pos.distanceTo(animation.getOrigin().transformPointToWorld(tempResult.asBlockHit().getLocation()));
            if (result == null || tempDistance < distance) {
                result = tempResult.asBlockHit();
                distance = tempDistance;
                pointedEntity = animation;
            }
        }
        
        if (pointedEntity == null) {
            if (result instanceof BlockHitResult) {
                BlockState state = level.getBlockState(result.getBlockPos());
                if (state.getBlock() instanceof BlockTile)
                    return LittleActionHandlerServer.execute(player, new LittleActionActivated(level, result.getBlockPos(), pos, look, false)).consumesAction();
                return false;
            }
        } else
            return LittleActionHandlerServer.execute(player, new LittleActionActivated((Level) pointedEntity.getSubLevel(), result.getBlockPos(), pos, look, false))
                    .consumesAction();
        
        return false;
        
    }
    
}
