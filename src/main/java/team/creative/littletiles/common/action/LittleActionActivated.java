package team.creative.littletiles.common.action;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class LittleActionActivated extends LittleActionInteract<InteractionResult> {
    
    public LittleActionActivated(Level level, BlockPos blockPos, Player player) {
        super(level, blockPos, player);
    }
    
    public LittleActionActivated(Level level, BlockPos blockPos, Vec3 pos, Vec3 look, boolean secondMode) {
        super(level, blockPos, pos, look, secondMode);
    }
    
    public LittleActionActivated() {}
    
    @Override
    protected InteractionResult action(Level level, BETiles be, LittleTileContext context, ItemStack stack, Player player, BlockHitResult hit, BlockPos pos, boolean secondMode) throws LittleActionException {
        if (context.parent.isStructure())
            return context.parent.getStructure().use(level, context, pos, player, hit);
        
        InteractionResult result = context.tile.use(context.parent, context.box, pos, player, hit);
        if (result.consumesAction())
            return result;
        return InteractionResult.PASS;
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
    
    @Override
    protected InteractionResult ignored() {
        return InteractionResult.PASS;
    }
    
    @Override
    public boolean wasSuccessful(InteractionResult result) {
        return InteractionResult.PASS != result;
    }
    
    @Override
    public InteractionResult failed() {
        return InteractionResult.FAIL;
    }
    
}
