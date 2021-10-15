package team.creative.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.action.tool.LittleActionSaw;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class ItemLittleSaw extends Item implements IItemTooltip {
    
    public ItemLittleSaw() {
        super(new Item.Properties().stacksTo(1).tab(LittleTiles.littleTab));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("rightclick to increase and");
        tooltip.add("shift+rightclick to decrease");
        tooltip.add("the size of a placed tile");
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof BETiles) {
            if (context.getLevel().isClientSide)
                new LittleActionSaw(world, pos, player, GuiScreen.isCtrlKeyDown(), LittleGridContext.get()).execute();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage() };
    }
    
}
