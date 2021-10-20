package team.creative.littletiles.common.action;

import com.creativemd.littletiles.common.event.LittleEventHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.util.EnumHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class LittleActionActivated extends LittleActionInteract {
    
    public LittleActionActivated(Level level, BlockPos blockPos, Player player) {
        super(level, blockPos, player);
    }
    
    public LittleActionActivated(Level level, BlockPos blockPos, Vec3 pos, Vec3 look, boolean secondMode) {
        super(level, blockPos, pos, look, secondMode);
    }
    
    public LittleActionActivated() {}
    
    @Override
    public boolean sendToServer() {
        return !preventInteraction;
    }
    
    public boolean preventInteraction = false;
    
    @Override
    protected boolean action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult hit, BlockPos pos, boolean secondMode) throws LittleActionException {
        if (parent.isStructure())
            return parent.getStructure().onBlockActivated(world, tile, pos, player, EnumHand.MAIN_HAND, player
                    .getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float) moving.hitVec.x, (float) moving.hitVec.y, (float) moving.hitVec.z, this);
        
        if (tile.onBlockActivated(parent, player, EnumHand.MAIN_HAND, player
                .getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float) moving.hitVec.x, (float) moving.hitVec.y, (float) moving.hitVec.z, this))
            return true;
        return false;
    }
    
    @Override
    public boolean action(Player player) throws LittleActionException {
        boolean result;
        try {
            result = super.action(player);
        } catch (LittleActionException e) {
            if (!player.level.isClientSide)
                LittleEventHandler.addBlockTilePrevent(player);
            throw e;
        }
        if (!player.level.isClientSide)
            LittleEventHandler.addBlockTilePrevent(player);
        return result;
    }
    
    @Override
    public boolean canBeReverted() {
        return false;
    }
    
    @Override
    public LittleAction revert(Player player) {
        return null;
    }
    
    @Override
    protected boolean isRightClick() {
        return true;
    }
    
    @Override
    public LittleAction mirror(Axis axis, LittleBoxAbsolute box) {
        return null;
    }
    
    @Override
    protected boolean requiresBreakEvent() {
        return false;
    }
    
}
