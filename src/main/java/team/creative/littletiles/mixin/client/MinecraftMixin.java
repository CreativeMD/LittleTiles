package team.creative.littletiles.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.math.vec.LittleHitResult;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    
    @Unique
    public Minecraft asMinecraft() {
        return (Minecraft) (Object) this;
    }
    
    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), require = 1,
            cancellable = true)
    private void continueAttack(boolean holding, CallbackInfo info) {
        Minecraft mc = asMinecraft();
        if (holding && mc.hitResult instanceof LittleHitResult result && result.hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = result.asBlockHit();
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (!result.level.isEmptyBlock(blockpos)) {
                var inputEvent = ForgeHooksClient.onClickInput(0, mc.options.keyAttack, InteractionHand.MAIN_HAND);
                if (inputEvent.isCanceled()) {
                    if (inputEvent.shouldSwingHand()) {
                        mc.particleEngine.addBlockHitEffects(blockpos, blockhitresult);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }
                    info.cancel();
                    return;
                }
                Direction direction = blockhitresult.getDirection();
                if (LittleTilesClient.INTERACTION_HANDLER.continueDestroyBlock(result.level.asLevel(), blockpos, direction) && inputEvent.shouldSwingHand()) {
                    mc.particleEngine.addBlockHitEffects(blockpos, blockhitresult);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
            info.cancel();
        }
    }
    
    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"), require = 1)
    private void continueAttackStop(CallbackInfo info) {
        LittleTilesClient.INTERACTION_HANDLER.stopDestroyBlock();
    }
    
    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), require = 1,
            cancellable = true)
    private void startAttack(CallbackInfoReturnable<Boolean> info) {
        Minecraft mc = asMinecraft();
        if (mc.hitResult instanceof LittleHitResult hit) {
            if (hit.isEntity()) {
                LittleTilesClient.INTERACTION_HANDLER.attack(hit.level.asLevel(), mc.player, hit.asEntityHit().getEntity());
                info.setReturnValue(false);
            }
            
            BlockHitResult blockhitresult = hit.asBlockHit();
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (!hit.level.isEmptyBlock(blockpos)) {
                LittleTilesClient.INTERACTION_HANDLER.startDestroyBlock(hit.level.asLevel(), blockpos, blockhitresult.getDirection());
                info.setReturnValue(hit.level.getBlockState(blockpos).isAir());
            }
            
        }
    }
    
    @WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"), require = 1)
    private boolean isDestroying(MultiPlayerGameMode mode, Operation<Boolean> original) {
        return original.call(mode) || LittleTilesClient.INTERACTION_HANDLER.isDestroying();
    }
    
    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), require = 1,
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void startUseItem(CallbackInfo info, InteractionHand[] hands, int delay, int type, InteractionHand interactionhand, InteractionKeyMappingTriggered inputEvent, ItemStack itemstack) {
        Minecraft mc = asMinecraft();
        LocalPlayer player = mc.player;
        if (mc.hitResult instanceof LittleHitResult hit) {
            if (hit.isEntity()) {
                EntityHitResult entityhitresult = hit.asEntityHit();
                Entity entity = entityhitresult.getEntity();
                if (!hit.level.getWorldBorder().isWithinBounds(entity.blockPosition()))
                    return;
                
                //if (!player.canInteractWith(entityhitresult.getEntity(), 0)) Not there in 1.20 Forge 46.0.10
                //    return; //Forge: Entity may be traced via attack range, but the player may not have sufficient reach.  No padding in client code.
                InteractionResult interactionresult = LittleTilesClient.INTERACTION_HANDLER.interactAt(hit.level.asLevel(), player, entity, entityhitresult, interactionhand);
                if (!interactionresult.consumesAction())
                    interactionresult = LittleTilesClient.INTERACTION_HANDLER.interact(hit.level.asLevel(), player, entity, interactionhand);
                
                if (interactionresult.consumesAction()) {
                    if (interactionresult.shouldSwing() && inputEvent.shouldSwingHand())
                        player.swing(interactionhand);
                    info.cancel();
                }
                return;
            }
            
            BlockHitResult blockhitresult = hit.asBlockHit();
            int i = itemstack.getCount();
            InteractionResult interactionresult1 = LittleTilesClient.INTERACTION_HANDLER.useItemOn(hit.level.asLevel(), player, interactionhand, blockhitresult);
            if (interactionresult1.consumesAction()) {
                if (interactionresult1.shouldSwing() && inputEvent.shouldSwingHand()) {
                    player.swing(interactionhand);
                    if (!itemstack.isEmpty() && (itemstack.getCount() != i || mc.gameMode.hasInfiniteItems()))
                        mc.gameRenderer.itemInHandRenderer.itemUsed(interactionhand);
                }
                
                info.cancel();
                return;
            }
            
            if (interactionresult1 == InteractionResult.FAIL)
                info.cancel();
        }
    }
    
}
