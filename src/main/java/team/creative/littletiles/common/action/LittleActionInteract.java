package team.creative.littletiles.common.action;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.level.WorldAnimationHandler;
import team.creative.littletiles.common.structure.type.LittleBedEventHandler;

public abstract class LittleActionInteract extends LittleAction {
    
    public BlockPos blockPos;
    public Vec3 pos;
    public Vec3 look;
    
    public Vec3 transformedPos;
    public Vec3 transformedLook;
    
    public boolean secondMode;
    @CanBeNull
    public UUID uuid;
    
    public boolean transformedCoordinates = false;
    
    @OnlyIn(Dist.CLIENT)
    public LittleActionInteract(Level level, BlockPos blockPos, Player player) {
        super();
        this.blockPos = blockPos;
        this.pos = player.getEyePosition(TickUtils.getDeltaFrameTime(level));
        double reach = PlayerUtils.getReach(player);
        Vec3 look = player.getViewVector(TickUtils.getDeltaFrameTime(level));
        this.look = pos.add(look.x * reach, look.y * reach, look.z * reach);
        this.secondMode = LittleActionHandlerClient.isUsingSecondMode(player);
        if (level instanceof CreativeLevel)
            uuid = ((CreativeLevel) level).parent.getUUID();
    }
    
    public LittleActionInteract(Level level, BlockPos blockPos, Vec3 pos, Vec3 look, boolean secondMode) {
        super();
        this.blockPos = blockPos;
        this.pos = pos;
        this.look = look;
        this.secondMode = secondMode;
        if (level instanceof CreativeLevel)
            uuid = ((CreativeLevel) level).parent.getUUID();
    }
    
    public LittleActionInteract() {
        super();
    }
    
    protected abstract boolean requiresBreakEvent();
    
    protected abstract boolean isRightClick();
    
    protected abstract boolean action(Level level, BETiles te, LittleTileContext context, ItemStack stack, Player player, BlockHitResult hit, BlockPos pos, boolean secondMode) throws LittleActionException;
    
    @Override
    public boolean action(Player player) throws LittleActionException {
        
        if (isRightClick() && LittleBedEventHandler.consumeBlockTilePrevent(player, InteractionHand.MAIN_HAND))
            return false;
        
        Level level = player.level;
        
        transformedPos = this.pos;
        transformedLook = this.look;
        
        if (uuid != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(player.level.isClientSide, uuid);
            if (animation == null)
                onEntityNotFound();
            
            if (!isAllowedToInteract(player, animation, isRightClick())) {
                sendEntityResetToClient(player, animation);
                return false;
            }
            
            level = animation.fakeWorld;
            if (!transformedCoordinates) {
                transformedPos = animation.origin.transformPointToFakeWorld(transformedPos);
                transformedLook = animation.origin.transformPointToFakeWorld(transformedLook);
            }
        }
        
        if (requiresBreakEvent())
            fireBlockBreakEvent(level, blockPos, player);
        
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BETiles) {
            BETiles be = (BETiles) blockEntity;
            LittleTileContext context = be.getFocusedTile(transformedPos, transformedLook);
            
            if (!isAllowedToInteract(level, player, blockPos, isRightClick(), Facing.EAST)) {
                sendBlockResetToClient(level, player, be);
                return false;
            }
            
            if (context.isComplete()) {
                ItemStack stack = player.getMainHandItem();
                BlockHitResult moving = rayTrace(be, context, transformedPos, transformedLook);
                if (moving != null)
                    return action(level, be, context, stack, player, moving, blockPos, secondMode);
            } else
                onTileNotFound();
        } else
            onTileEntityNotFound();
        return false;
        
    }
    
    public BlockHitResult rayTrace(BETiles be, LittleTileContext tile, Vec3 pos, Vec3 look) {
        return be.rayTrace(pos, look);
    }
    
    protected void onEntityNotFound() throws LittleActionException {
        throw new LittleActionException.EntityNotFoundException();
    }
    
    protected void onTileNotFound() throws LittleActionException {
        throw new LittleActionException.TileNotFoundException();
    }
    
    protected void onTileEntityNotFound() throws LittleActionException {
        throw new LittleActionException.TileEntityNotFoundException();
    }
    
}
