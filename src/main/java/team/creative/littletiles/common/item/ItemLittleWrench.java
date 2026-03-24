package team.creative.littletiles.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.LittleToolWrench;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;

public class ItemLittleWrench extends Item implements ILittleTool, IItemTooltip {
    
    public ItemLittleWrench() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleTool tool(ItemStack stack) {
        return new LittleToolWrench(stack);
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage() };
    }
}
