package team.creative.littletiles.mixin.common.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.littletiles.common.level.little.LittleLevel;

@Mixin(BlockPlaceContext.class)
public abstract class BlockPlaceContextMixin extends UseOnContext {
    
    private BlockPlaceContextMixin(Player player, InteractionHand hand, BlockHitResult hit) {
        super(player, hand, hit);
    }
    
    @Inject(method = "canPlace()Z", at = @At("HEAD"), cancellable = true)
    public void canPlace(CallbackInfoReturnable<Boolean> info) {
        if (getLevel() instanceof LittleLevel level && !level.allowPlacement())
            info.setReturnValue(false);
    }
    
}
