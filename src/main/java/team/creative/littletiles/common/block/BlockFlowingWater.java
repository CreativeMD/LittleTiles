package team.creative.littletiles.common.block;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.client.api.IFakeRenderingBlock;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import net.minecraft.core.Direction;
import net.minecraft.util.EnumFacing;
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
import net.minecraft.world.level.block.Rotation;
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
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.block.ILittleMCBlock;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class BlockFlowingWater extends Block implements ILittleMCBlock, IFakeRenderingBlock {
    
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    
    public final Block still;
    
    public BlockFlowingWater(Block still) {
        super(BlockBehaviour.Properties.of(Material.WATER));
        this.still = still;
    }
    
    @Override
    public Block asBlock() {
        return this;
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
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
        return material == Material.WATER;
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
    public Vec3d modifyAcceleration(IParentCollection parent, LittleTile tile, Entity entity, Vec3d motion) {
        AABB box = entity.getBoundingBox();
        LittleVec center = new LittleVec(parent.getGrid(), new Vec3((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2)
                .subtract(Vec3.atLowerCornerOf(parent.getPos())));
        LittleBox testBox = new LittleBox(center, 1, 1, 1);
        if (tile.intersectsWith(testBox)) {
            double scale = 0.01;
            Vec3 vec = new Vec3(tile.getBlockState().getValue(DIRECTION).getDirectionVec()).normalize().scale(scale);
            entity.setDeltaMovement(entity.getDeltaMovement().add(vec));
        }
        return new Vec3d(tile.getBlockState().getValue(DIRECTION).getDirectionVec());
    }
    
    @Override
    public boolean canBeConvertedToVanilla() {
        return false;
    }
    
    @Override
    public BlockState getFakeState(BlockState state) {
        return Blocks.FLOWING_WATER.getDefaultState();
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(IParentCollection parent, LittleBox box, Player player, InteractionHand hand, BlockHitResult result) {
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().getItem() instanceof BucketItem && LittleTiles.CONFIG.general.allowFlowingWater) {
            int meta = tile.getMeta() + 1;
            if (meta > EnumFacing.VALUES.length)
                tile.setBlock(LittleTiles.dyeableBlockTransparent, still.ordinal());
            else
                tile.setMeta(meta);
            parent.getTe().updateTiles();
            return InteractionResult.SUCCESS;
        }
        return ILittleMCBlock.super.use(parent, box, player, hand, result);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
        if (tile.getBlock() == this)
            return true;
        if (tile.getBlock() == LittleTiles.WATER)
            return true;
        return false;
    }
    
    @Override
    public Vec3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks) {
        float f12 = 0.0F;
        if (entity instanceof LivingEntity) {
            LivingEntity ent = (LivingEntity) entity;
            f12 = EnchantmentHelper.getRespiration(ent) * 0.2F;
            
            if (ent.hasEffect(MobEffects.WATER_BREATHING))
                f12 = f12 * 0.3F + 0.6F;
        }
        return new Vec3d(0.02F + f12, 0.02F + f12, 0.2F + f12);
    }
    
    @Override
    public void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
        preview.getTileData().setInteger("meta", RotationUtils.rotate(EnumFacing.getFront(preview.getMeta()), rotation).ordinal());
    }
    
    @Override
    public void flipPreview(Axis axis, LittlePreview preview, LittleVec doubledCenter) {
        EnumFacing facing = EnumFacing.getFront(preview.getMeta());
        if (facing.getAxis() == axis)
            facing = facing.getOpposite();
        preview.getTileData().setInteger("meta", facing.ordinal());
    }
    
}
