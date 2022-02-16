package team.creative.littletiles.common.block.little.registry;

import java.util.Random;

import com.mojang.math.Vector3d;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.RenderProperties;
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

public class LittleMissingBlock implements LittleBlock {
    
    public final String blockName;
    
    public LittleMissingBlock(String name) {
        this.blockName = name;
    }
    
    @Override
    public String blockName() {
        return blockName;
    }
    
    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean isTranslucent() {
        return false;
    }
    
    @Override
    public boolean is(Block block) {
        return false;
    }
    
    @Override
    public boolean is(Tag<Block> tag) {
        return false;
    }
    
    @Override
    public BlockState getState() {
        return Blocks.AIR.defaultBlockState();
    }
    
    @Override
    public boolean canBeConvertedToVanilla() {
        return false;
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
        return SoundType.STONE;
    }
    
    @Override
    public float getExplosionResistance(LittleTile tile) {
        return 0;
    }
    
    @Override
    public void exploded(IParentCollection parent, LittleTile tile, Explosion explosion) {}
    
    @Override
    public void randomDisplayTick(IParentCollection parent, LittleTile tile, Random rand) {
        
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
    public int getLightValue() {
        return 0;
    }
    
    @Override
    public float getEnchantPowerBonus(IParentCollection parent, LittleTile tile) {
        return 0;
    }
    
    @Override
    public float getFriction(IParentCollection parent, LittleTile tile, Entity entity) {
        return 0;
    }
    
    @Override
    public boolean isMaterial(Material material) {
        return false;
    }
    
    @Override
    public boolean isLiquid() {
        return false;
    }
    
    @Override
    public Vector3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vector3d originalColor, float partialTicks) {
        return RenderProperties.get(tile.getState()).getFogColor(getState(), parent.getLevel(), parent.getPos(), entity, originalColor, partialTicks);
    }
    
    @Override
    public Vec3d modifyAcceleration(IParentCollection parent, LittleTile tile, Entity entity, Vec3d motion) {
        return motion;
    }
    
    @Override
    public LittleRenderBox getRenderBox(LittleGrid grid, RenderType layer, LittleBox box, LittleElement element) {
        return new LittleRenderBox(grid, box, element);
    }
    
    @Override
    public boolean canBeRenderCombined(LittleTile one, LittleTile two) {
        return two.getBlock() instanceof LittleMissingBlock;
    }
    
    @Override
    public boolean checkEntityCollision() {
        return false;
    }
    
    @Override
    public void entityCollided(IParentCollection parent, LittleTile tile, Entity entity) {}
    
    @Override
    public boolean is(ItemStack stack) {
        return false;
    }
    
    @Override
    public boolean noCollision() {
        return false;
    }
    
}
