package team.creative.littletiles.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class ItemLittleGlove extends Item implements ILittlePlacer, IItemTooltip {
    
    public ItemLittleGlove() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public Iterable<LittleTool> tools(ItemStack stack) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        // TODO Auto-generated method stub
        return false;
    }
    
}
