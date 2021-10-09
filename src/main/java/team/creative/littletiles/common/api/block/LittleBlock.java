package team.creative.littletiles.common.api.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public abstract class LittleBlock {
    
    public abstract boolean isTranslucent();
    
    public abstract boolean is(ItemStack stack);
    
    public abstract boolean is(Block block);
    
    public abstract boolean noCollision();
    
    public abstract ItemStack getStack();
    
    public abstract BlockState getState();
    
    public abstract boolean canBeConvertedToVanilla();
    
    public abstract String blockName();
    
    public abstract void mirror(LittleTile tile, Axis axis, LittleVec doubledCenter);
    
    public abstract void rotate(LittleTile tile, Rotation rotation, LittleVec doubledCenter);
    
    public abstract SoundType getSoundType();
    
    public abstract float getExplosionResistance(LittleTile tile);
    
    public abstract void exploded(IParentCollection parent, LittleTile tile, Explosion explosion);
    
    public abstract void randomDisplayTick(IParentCollection parent, LittleTile tile, Random rand);
    
    public abstract boolean canInteract(LittleTile tile);
    
    public abstract InteractionResult use(IParentCollection parent, LittleTile tile, Player player, InteractionHand hand, BlockHitResult result);
    
    public abstract int getLightValue();
    
    public abstract float getEnchantPowerBonus(IParentCollection parent, LittleTile tile);
    
    public abstract float getFriction(IParentCollection parent, LittleTile tile, @Nullable Entity entity);
    
    public abstract boolean isMaterial(Material material);
    
    public abstract boolean isLiquid();
    
    public abstract Vec3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks);
    
    public abstract Vec3d modifyAcceleration(IParentCollection parent, LittleTile tile, Entity entity, Vec3d motion);
    
    public abstract boolean canRenderInLayer(RenderType layer);
    
    public abstract LittleRenderBox getRenderBox(LittleGrid grid, RenderType layer, LittleBox box, int color);
    
    public abstract boolean canBeRenderCombined(LittleBlock tile);
    
    public abstract boolean checkEntityCollision();
    
    public abstract void entityCollided(IParentCollection parent, LittleTile tile, Entity entity);
    
    public abstract VoxelShape getSelectionShape(IParentCollection parent, LittleTile tile);
    
    public abstract VoxelShape getOcclusionShape(IParentCollection parent, LittleTile tile);
    
    public VoxelShape getCollisionShape(IParentCollection parent, CollisionContext context, LittleTile tile) {
        if (noCollision())
            return null;
    }
    
}
