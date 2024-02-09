package team.creative.littletiles.common.block.mc;

import org.joml.Vector3d;

import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.client.IFakeRenderingBlock;
import team.creative.littletiles.api.common.block.ILittleMCBlock;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class BlockFlowingWater extends Block implements ILittleMCBlock, IFakeRenderingBlock {
    
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    
    public final Block still;
    
    public BlockFlowingWater(Block still) {
        super(BlockBehaviour.Properties.of().liquid());
        this.still = still;
    }
    
    @Override
    public Block asVanillaBlock() {
        return this;
    }
    
    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }
    
    @Override
    public boolean noCollision() {
        return true;
    }
    
    @Override
    public boolean isFluid(TagKey<Fluid> fluid) {
        return fluid.equals(FluidTags.WATER);
    }
    
    @Override
    public boolean checkEntityCollision() {
        return true;
    }
    
    @Override
    public boolean canBeConvertedToVanilla() {
        return false;
    }
    
    @Override
    public BlockState getFakeState(BlockState state) {
        return Blocks.WATER.defaultBlockState();
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result, InteractionHand hand) {
        if (player.getMainHandItem().getItem() instanceof BucketItem && LittleTiles.CONFIG.general.allowFlowingWater) {
            BlockState newState;
            Direction facing = tile.getState().getValue(BlockStateProperties.FACING);
            int index = facing.ordinal() + 1;
            if (index >= Direction.values().length)
                newState = LittleTilesRegistry.WATER.get().defaultBlockState();
            else
                newState = tile.getState().setValue(BlockStateProperties.FACING, Direction.values()[index]);
            parent.getBE().updateTiles(x -> {
                LittleTile newFlowing = new LittleTile(newState, ColorUtils.WHITE, box.copy());
                x.noneStructureTiles().remove(tile, box);
                x.noneStructureTiles().add(newFlowing);
            });
            return InteractionResult.SUCCESS;
        }
        return ILittleMCBlock.super.use(parent, tile, box, player, result, hand);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
        if (tile.getBlock() == this)
            return true;
        if (tile.getBlock() == LittleTilesRegistry.WATER.get())
            return true;
        return false;
    }
    
    @Override
    public Vector3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vector3d originalColor, float partialTicks) {
        float f12 = 0.0F;
        if (entity instanceof LivingEntity living) {
            f12 = EnchantmentHelper.getRespiration(living) * 0.2F;
            
            if (living.hasEffect(MobEffects.WATER_BREATHING))
                f12 = f12 * 0.3F + 0.6F;
        }
        return new Vector3d(0.02F + f12, 0.02F + f12, 0.2F + f12);
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation, LittleVec doubledCenter) {
        return state.setValue(FACING, rotation.rotate(Facing.get(state.getValue(FACING))).toVanilla());
    }
    
    @Override
    public BlockState mirror(BlockState state, Axis axis, LittleVec doubledCenter) {
        return state.setValue(FACING, axis.mirror(state.getValue(FACING)));
    }
    
}
