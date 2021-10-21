package team.creative.littletiles.common.block.mc;

import net.minecraft.core.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.api.IFakeRenderingBlock;
import team.creative.littletiles.common.api.block.ILittleMCBlock;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class BlockFlowingLava extends Block implements ILittleMCBlock, IFakeRenderingBlock {
    
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    
    public final Block still;
    
    public BlockFlowingLava(Block still) {
        super(BlockBehaviour.Properties.of(Material.LAVA));
        this.still = still;
    }
    
    @Override
    public Block asBlock() {
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
    public boolean isMaterial(Material material) {
        return material == Material.LAVA;
    }
    
    @Override
    public boolean isLiquid() {
        return true;
    }
    
    @Override
    public boolean checkEntityCollision() {
        return true;
    }
    
    @Override
    public void entityCollided(IParentCollection parent, LittleTile tile, Entity entity) {
        AABB box = entity.getBoundingBox();
        LittleVec center = new LittleVec(parent.getGrid(), new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2)
                .subtract(Vec3.atLowerCornerOf(parent.getPos())));
        LittleBox testBox = new LittleBox(center, 1, 1, 1);
        if (tile.intersectsWith(testBox)) {
            double scale = 0.05;
            Vec3 vec = new Vec3(tile.getBlockState().getValue(DIRECTION).getDirectionVec()).normalize().scale(scale);
            entity.setDeltaMovement(entity.getDeltaMovement().add(vec));
        }
    }
    
    @Override
    public boolean canBeConvertedToVanilla() {
        return false;
    }
    
    @Override
    public BlockState getFakeState(BlockState state) {
        return Blocks.FLOWING_LAVA.getDefaultState();
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, InteractionHand hand, BlockHitResult result) {
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().getItem() instanceof BucketItem && LittleTiles.CONFIG.general.allowFlowingLava) {
            int meta = tile.getMeta() + 1;
            if (meta > EnumFacing.VALUES.length)
                tile.setBlock(LittleTiles.dyeableBlock, still.ordinal());
            else
                tile.setMeta(meta);
            parent.getTe().updateTiles();
            return InteractionResult.SUCCESS;
        }
        return ILittleMCBlock.super.use(parent, tile, box, player, hand, result);
    }
    
    @Override
    public Vec3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks) {
        return new Vec3d(0.6F, 0.1F, 0.0F);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canBeRenderCombined(LittleTile one, LittleTile two) {
        if (two.getBlock() == this)
            return true;
        if (two.getBlock() == LittleTiles.LAVA)
            return true;
        return false;
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
