package team.creative.littletiles.mixin.common.level;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.box.BoxesVoxelShape;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor, AutoCloseable {
    
    @Override
    public boolean noCollision(@Nullable Entity entity, AABB bb) {
        for (VoxelShape voxelshape : this.getBlockCollisions(entity, bb))
            if (voxelshape instanceof BoxesVoxelShape box) {
                if (box.intersectsWith(bb))
                    return false;
            } else if (!voxelshape.isEmpty())
                return false;
            
        if (!this.getEntityCollisions(entity, bb).isEmpty())
            return false;
        else if (entity == null)
            return true;
        
        VoxelShape voxelshape1 = this.borderCollision(entity, bb);
        return voxelshape1 == null || !Shapes.joinIsNotEmpty(voxelshape1, Shapes.create(bb), BooleanOp.AND);
    }
    
    @Nullable
    private VoxelShape borderCollision(Entity p_186441_, AABB p_186442_) {
        WorldBorder worldborder = this.getWorldBorder();
        return worldborder.isInsideCloseToBorder(p_186441_, p_186442_) ? worldborder.getCollisionShape() : null;
    }
    
    @Override
    public Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape shape, Vec3 vec, double x, double y, double z) {
        if (shape.isEmpty())
            return Optional.empty();
        AABB aabb = shape.bounds().inflate(x, y, z);
        
        List<VoxelShape> shapes = Lists.newArrayList(getBlockCollisions(entity, aabb));
        for (VoxelShape toTest : shapes)
            if (toTest instanceof BoxesVoxelShape box)
                box.onlyKeepIntersecting(aabb);
            
        VoxelShape voxelshape = shapes.stream().filter((toTest) -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(toTest.bounds())).flatMap((
                toTest) -> toTest.toAabbs().stream()).map((bb) -> bb.inflate(x / 2.0D, y / 2.0D, z / 2.0D)).map(Shapes::create).reduce(Shapes.empty(), Shapes::or);
        VoxelShape voxelshape1 = Shapes.join(shape, voxelshape, BooleanOp.ONLY_FIRST);
        return voxelshape1.closestPointTo(vec);
    }
    
}
