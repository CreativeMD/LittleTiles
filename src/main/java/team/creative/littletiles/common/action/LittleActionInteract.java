package team.creative.littletiles.common.action;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.network.CanBeNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.exception.AreaProtected;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.mod.sable.ISableContext;
import team.creative.littletiles.common.mod.sable.SableManager;

public abstract class LittleActionInteract<T> extends LittleAction<T> {
    
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
        this.pos = player.getEyePosition(TickUtils.getFrameTime(level));
        double reach = PlayerUtils.getReach(player);
        Vec3 look = player.getViewVector(TickUtils.getFrameTime(level));
        this.look = pos.add(look.x * reach, look.y * reach, look.z * reach);
        this.secondMode = LittleActionHandlerClient.isUsingSecondMode();
        if (level instanceof ISubLevel sub)
            uuid = sub.getHolder().getUUID();
    }
    
    public LittleActionInteract(Level level, BlockPos blockPos, Vec3 pos, Vec3 look, boolean secondMode) {
        super();
        this.blockPos = blockPos;
        this.pos = pos;
        this.look = look;
        this.secondMode = secondMode;
        if (level instanceof ISubLevel sub)
            uuid = sub.getHolder().getUUID();
    }
    
    public LittleActionInteract() {
        super();
    }
    
    protected abstract boolean requiresBreakEvent();
    
    protected abstract boolean isRightClick();
    
    protected abstract T action(Level level, BETiles te, LittleTileContext context, ItemStack stack, LittleActionSource source, BlockHitResult hit, BlockPos pos,
            boolean secondMode) throws LittleActionException;
    
    protected abstract T ignored();
    
    @Override
    public T action(LittleActionSource source) throws LittleActionException {
        
        Level level = source.getActionLevel();
        
        if (!transformedCoordinates) {
            transformedPos = this.pos;
            transformedLook = this.look;
        }
        
        if (uuid != null) {
            LittleEntity animation = LittleTiles.ANIMATION_HANDLERS.find(level.isClientSide, uuid);
            if (animation == null)
                onEntityNotFound();
            
            if (!isAllowedToInteract(source, animation, isRightClick()))
                return failed();
            
            level = (Level) animation.getSubLevel();
            if (!transformedCoordinates) {
                transformedPos = animation.getOrigin().transformPointToFakeWorld(transformedPos);
                transformedLook = animation.getOrigin().transformPointToFakeWorld(transformedLook);
                
                ISableContext sable = SableManager.context(level, blockPos);
                if (sable != null) {
                    pos = sable.toLocal(pos);
                    look = sable.toLocal(look);
                }
                
                transformedCoordinates = true;
            }
        }
        
        if (requiresBreakEvent() && !fireBlockBreakEvent(level, blockPos, source))
            throw new AreaProtected();
        
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BETiles be) {
            LittleTileContext context = be.getFocusedTile(transformedPos, transformedLook);
            
            if (!isAllowedToInteract(level, source, blockPos, isRightClick(), Facing.EAST)) {
                sendBlockResetToClient(level, source, be);
                return failed();
            }
            
            if (context.isComplete()) {
                ItemStack stack = source.getActionItem();
                BlockHitResult moving = rayTrace(be, context, transformedPos, transformedLook);
                if (moving != null)
                    return action(level, be, context, stack, source, moving, blockPos, secondMode);
            } else
                onTileNotFound();
        } else
            onBlockEntityNotFound();
        return ignored();
        
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
    
    protected void onBlockEntityNotFound() throws LittleActionException {
        throw new LittleActionException.BlockEntityNotFoundException();
    }
    
}
