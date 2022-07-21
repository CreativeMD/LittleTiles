package team.creative.littletiles.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class ItemLittleSaw extends Item implements IItemTooltip {
    
    public ItemLittleSaw() {
        super(new Item.Properties().stacksTo(1).tab(LittleTiles.LITTLE_TAB));
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
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage() };
    }
    
}
