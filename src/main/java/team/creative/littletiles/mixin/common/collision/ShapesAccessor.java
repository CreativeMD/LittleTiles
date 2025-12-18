package team.creative.littletiles.mixin.common.collision;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Shapes.class)
public interface ShapesAccessor {
    
    @Invoker("collidedX")
    public static VoxelShape getCollidedX() {
        throw new UnsupportedOperationException();
    }
    
    @Invoker("collidedY")
    public static VoxelShape getCollidedY() {
        throw new UnsupportedOperationException();
    }
    
    @Invoker("collidedZ")
    public static VoxelShape getCollidedZ() {
        throw new UnsupportedOperationException();
    }
    
    @Invoker("collidedX")
    public static void setCollidedX(VoxelShape shape) {
        throw new UnsupportedOperationException();
    }
    
    @Invoker("collidedY")
    public static void setCollidedY(VoxelShape shape) {
        throw new UnsupportedOperationException();
    }
    
    @Invoker("collidedZ")
    public static void setCollidedZ(VoxelShape shape) {
        throw new UnsupportedOperationException();
    }
}
