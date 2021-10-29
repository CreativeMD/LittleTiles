package team.creative.littletiles.common.item;

import com.creativemd.littletiles.common.action.tool.LittleActionSaw;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class ItemLittleSaw extends Item implements IItemTooltip {
    
    public ItemLittleSaw() {
        super(new Item.Properties().stacksTo(1).tab(LittleTiles.LITTLE_TAB));
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof BETiles) {
            if (context.getLevel().isClientSide)
                new LittleActionSaw(context.getLevel(), context.getClickedPos(), context.getClickedFace(), GuiScreen.isCtrlKeyDown(), LittleGridContext.get()).execute();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage() };
    }
    
}
