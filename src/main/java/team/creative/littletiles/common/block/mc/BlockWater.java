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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.client.IFakeRenderingBlock;
import team.creative.littletiles.api.common.block.ILittleMCBlock;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;

public class BlockWater extends Block implements ILittleMCBlock, IFakeRenderingBlock {
    
    public BlockWater(Properties properties) {
        super(properties);
    }
    
    @Override
    public Block asVanillaBlock() {
        return this;
    }
    
    @Override
    public boolean isFluid(TagKey<Fluid> fluid) {
        return fluid.equals(FluidTags.LAVA);
    }
    
    @Override
    public boolean canBeConvertedToVanilla() {
        return false;
    }
    
    @Override
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result, InteractionHand hand) {
        if (player.getMainHandItem().getItem() instanceof BucketItem && !parent.isStructure() && LittleTiles.CONFIG.general.allowFlowingWater) {
            BlockState newState = LittleTilesRegistry.FLOWING_WATER.get().defaultBlockState().setValue(BlockFlowingWater.FACING, Direction.values()[0]);
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
    public BlockState getFakeState(BlockState state) {
        return Blocks.WATER.defaultBlockState();
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
    
}
