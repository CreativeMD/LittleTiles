package team.creative.littletiles.common.api.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.math.Vector3d;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.Tag;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public interface LittleBlock {
    
    public boolean isTranslucent();
    
    public boolean is(ItemStack stack);
    
    public boolean is(Block block);
    
    public boolean is(Tag<Block> tag);
    
    public boolean noCollision();
    
    public ItemStack getStack();
    
    public String blockName();
    
    public BlockState getState();
    
    public boolean canBeConvertedToVanilla();
    
    public BlockState mirror(BlockState state, Axis axis, LittleVec doubledCenter);
    
    public BlockState rotate(BlockState state, Rotation rotation, LittleVec doubledCenter);
    
    public SoundType getSoundType();
    
    public float getExplosionResistance(LittleTile tile);
    
    public void exploded(IParentCollection parent, LittleTile tile, Explosion explosion);
    
    public void randomDisplayTick(IParentCollection parent, LittleTile tile, Random rand);
    
    public boolean canInteract();
    
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result);
    
    public int getLightValue();
    
    public float getEnchantPowerBonus(IParentCollection parent, LittleTile tile);
    
    public float getFriction(IParentCollection parent, LittleTile tile, @Nullable Entity entity);
    
    public boolean isMaterial(Material material);
    
    public boolean isLiquid();
    
    public Vector3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vector3d originalColor, float partialTicks);
    
    public Vec3d modifyAcceleration(IParentCollection parent, LittleTile tile, Entity entity, Vec3d motion);
    
    public LittleRenderBox getRenderBox(LittleGrid grid, RenderType layer, LittleBox box, LittleElement element);
    
    public boolean canBeRenderCombined(LittleTile one, LittleTile two);
    
    public boolean checkEntityCollision();
    
    public void entityCollided(IParentCollection parent, LittleTile tile, Entity entity);
    
    @OnlyIn(Dist.CLIENT)
    public boolean canRenderInLayer(LittleTile tile, RenderType layer);
    
}
