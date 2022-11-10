package team.creative.littletiles.common.block.little.registry;

import com.mojang.math.Vector3d;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
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
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.mixin.BlockBehaviourAccessor;

public class LittleMCBlock implements LittleBlock {
    
    public final Block block;
    private final boolean translucent;
    
    public LittleMCBlock(Block block) {
        this.block = block;
        this.translucent = !block.defaultBlockState().getMaterial().isSolid() || !block.defaultBlockState().getMaterial().isSolid(); // Also depends on block model
    }
    
    @Override
    public boolean is(ItemStack stack) {
        return Block.byItem(stack.getItem()) == block;
    }
    
    @Override
    public ItemStack getStack() {
        return new ItemStack(block);
    }
    
    @Override
    public boolean canBeRenderCombined(LittleTile one, LittleTile two) {
        return one.getBlock() == two.getBlock() && one.color == two.color;
    }
    
    @Override
    public boolean noCollision() {
        return !((BlockBehaviourAccessor) block).getHasCollision();
    }
    
    @Override
    public boolean is(Block block) {
        return this.block == block;
    }
    
    @Override
    public boolean is(TagKey<Block> tag) {
        return block.builtInRegistryHolder().is(tag);
    }
    
    @Override
    public boolean isTranslucent() {
        return translucent;
    }
    
    @Override
    public boolean canBeConvertedToVanilla() {
        return true;
    }
    
    @Override
    public BlockState mirror(BlockState state, Axis axis, LittleVec doubledCenter) {
        return state;
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation, LittleVec doubledCenter) {
        return state;
    }
    
    @Override
    public SoundType getSoundType() {
        return block.getSoundType(getState());
    }
    
    @Override
    public float getExplosionResistance(LittleTile tile) {
        return block.getExplosionResistance();
    }
    
    @Override
    public void exploded(IParentCollection parent, LittleTile tile, Explosion explosion) {}
    
    @Override
    public void randomDisplayTick(IParentCollection parent, LittleTile tile, RandomSource rand) {}
    
    @Override
    public int getLightValue() {
        return getState().getLightEmission();
    }
    
    @Override
    public BlockState getState() {
        return block.defaultBlockState();
    }
    
    @Override
    public String blockName() {
        return block.builtInRegistryHolder().key().location().toString();
    }
    
    @Override
    public boolean canInteract() {
        return false;
    }
    
    @Override
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result) {
        return InteractionResult.PASS;
    }
    
    @Override
    public float getEnchantPowerBonus(IParentCollection parent, LittleTile tile) {
        float bonus = block.getEnchantPowerBonus(getState(), parent.getLevel(), parent.getPos());
        if (bonus > 0)
            return (float) (bonus * tile.getPercentVolume(parent.getGrid()));
        return 0;
    }
    
    @Override
    public float getFriction(IParentCollection parent, LittleTile tile, Entity entity) {
        return block.getFriction(getState(), parent.getLevel(), parent.getPos(), entity);
    }
    
    @Override
    public boolean isMaterial(Material material) {
        return getState().getMaterial() == material;
    }
    
    @Override
    public boolean isLiquid() {
        return getState().getMaterial().isLiquid();
    }
    
    @Override
    public LittleRenderBox getRenderBox(LittleGrid grid, RenderType layer, LittleBox box, LittleElement element) {
        return new LittleRenderBox(grid, box, element);
    }
    
    @Override
    public boolean checkEntityCollision() {
        return false;
    }
    
    @Override
    public void entityCollided(IParentCollection parent, LittleTile tile, Entity entity) {}
    
    @Override
    public Vector3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vector3d originalColor, float partialTicks) {
        return tile.getFogColor(parent, entity, originalColor, partialTicks);
    }
    
    @Override
    public Vec3d modifyAcceleration(IParentCollection parent, LittleTile tile, Entity entity, Vec3d motion) {
        return motion;
    }
    
    @Override
    public boolean shouldUseStateForRenderType() {
        return true;
    }
}
