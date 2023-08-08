package team.creative.littletiles.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.box.BoxesVoxelShape;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    
    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/shapes/Shapes;joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z"),
            method = "isPlayerCollidingWithAnythingNew", require = 1)
    private boolean joinIsNotEmptyCanBeIgnored(VoxelShape toAdd, VoxelShape shape, BooleanOp op) {
        if (toAdd instanceof BoxesVoxelShape)
            return true;
        return Shapes.joinIsNotEmpty(toAdd, shape, op);
    }
    
}
