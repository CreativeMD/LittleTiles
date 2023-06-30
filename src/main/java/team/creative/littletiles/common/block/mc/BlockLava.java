package team.creative.littletiles.common.block.mc;

import org.joml.Vector3d;

import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
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

public class BlockLava extends Block implements ILittleMCBlock, IFakeRenderingBlock {
    
    public BlockLava(Properties properties) {
        super(properties);
    }
    
    @Override
    public Block asBlock() {
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
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result) {
        if (player.getMainHandItem().getItem() instanceof BucketItem && LittleTiles.CONFIG.general.allowFlowingWater) {
            if (this == LittleTilesRegistry.LAVA.get())
                tile.setState(LittleTilesRegistry.FLOWING_LAVA.get().defaultBlockState().setValue(BlockFlowingWater.FACING, Direction.values()[0]));
            else
                tile.setState(LittleTilesRegistry.WHITE_FLOWING_LAVA.get().defaultBlockState().setValue(BlockFlowingWater.FACING, Direction.values()[0]));
            
            BlockState newState;
            if (this == LittleTilesRegistry.LAVA.get())
                newState = LittleTilesRegistry.FLOWING_LAVA.get().defaultBlockState().setValue(BlockFlowingWater.FACING, Direction.values()[0]);
            else
                newState = LittleTilesRegistry.WHITE_FLOWING_LAVA.get().defaultBlockState().setValue(BlockFlowingWater.FACING, Direction.values()[0]);
            parent.getBE().updateTiles(x -> {
                LittleTile newFlowing = new LittleTile(newState, ColorUtils.WHITE, box.copy());
                x.noneStructureTiles().remove(tile, box);
                x.noneStructureTiles().add(newFlowing);
            });
            return InteractionResult.SUCCESS;
        }
        return ILittleMCBlock.super.use(parent, tile, box, player, result);
    }
    
    @Override
    public BlockState getFakeState(BlockState state) {
        return Blocks.LAVA.defaultBlockState();
    }
    
    @Override
    public Vector3d getFogColor(IParentCollection parent, LittleTile tile, Entity entity, Vector3d originalColor, float partialTicks) {
        return new Vector3d(0.6F, 0.1F, 0.0F);
    }
    
}
