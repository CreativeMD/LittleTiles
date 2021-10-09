package team.creative.littletiles.common.api.block;

import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.api.block.EntityPlayer;
import com.creativemd.littletiles.common.api.block.EnumFacing;
import com.creativemd.littletiles.common.api.block.EnumHand;
import com.creativemd.littletiles.common.api.block.IParentTileList;
import com.creativemd.littletiles.common.api.block.LittlePreview;
import com.creativemd.littletiles.common.api.block.SideOnly;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.material.Material;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.LittleTile;

public interface ILittleBlock {
    
    public boolean isMaterial(Material material);
    
    public boolean isLiquid();
    
    public boolean canBeConvertedToVanilla();
    
    public default LittleBox getCollisionBox(LittleTile tile, LittleBox defaultBox) {
        if (canWalkThrough(tile))
            return null;
        return defaultBox;
    }
    
    public default boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }
    
    public default void onTileExplodes(IParentTileList parent, LittleTile tile, Explosion explosion) {
        
    }
    
    public default void randomDisplayTick(IParentTileList parent, LittleTile tile, Random rand) {
        
    }
    
    public default boolean isMaterial(LittleTile tile, Material material) {
        return tile.getBlockState().getMaterial() == material;
    }
    
    public default boolean isLiquid(LittleTile tile) {
        return tile.getBlockState().getMaterial().isLiquid();
    }
    
    public default Vec3d modifyAcceleration(IParentTileList parent, LittleTile tile, Entity entityIn, Vec3d motion) {
        return null;
    }
    
    public default LittlePreview getPreview(LittleTile tile) {
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    public default boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
        return false;
    }
    
    public default Vec3d getFogColor(IParentTileList parent, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks) {
        return originalColor;
    }
    
    public default void flipPreview(Axis axis, LittlePreview preview, LittleVec doubledCenter) {
        
    }
    
    public default void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
        
    }
    
    public default boolean shouldCheckForCollision(LittleTile tile) {
        return false;
    }
    
    public default void onEntityCollidedWithBlock(IParentTileList parent, LittleTile tile, Entity entityIn) {
        
    }
    
}
