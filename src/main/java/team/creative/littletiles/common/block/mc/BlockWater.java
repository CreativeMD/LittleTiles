package team.creative.littletiles.common.block.mc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.block.ILittleMCBlock;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;

public class BlockWater extends Block implements ILittleMCBlock {
    
    public BlockWater(Properties properties) {
        super(properties);
    }
    
    @Override
    public Block asBlock() {
        return this;
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
    public boolean canBeConvertedToVanilla() {
        return false;
    }
    
    @Override
    public InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, InteractionHand hand, BlockHitResult result) {
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().getItem() instanceof BucketItem && LittleTiles.CONFIG.general.allowFlowingWater) {
            if (this == LittleTiles.WATER)
                tile.setState(LittleTiles.FLOWING_WATER.defaultBlockState());
            else
                tile.setState(LittleTiles.WHITE_FLOWING_WATER.defaultBlockState());
            parent.getBE().updateTiles();
            return InteractionResult.SUCCESS;
        }
        return ILittleMCBlock.super.use(parent, tile, box, player, hand, result);
    }
    
}
