package team.creative.littletiles.common.item;

import java.util.Arrays;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.LittleToolWrench;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;

public class ItemLittleWrench extends Item implements ILittleTool {
    
    public ItemLittleWrench() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof BETiles) {
            if (context.getLevel().isClientSide) {
                LittleTileContext result = LittleTileContext.selectFocused(context.getLevel(), context.getClickedPos(), context.getPlayer());
                if (context.getPlayer().isCrouching()) {
                    if (result.isComplete() && result.parent.isStructure())
                        LittleTilesGuiRegistry.STRUCTURE_OVERVIEW.open(context.getPlayer(), result);
                    else
                        LittleTiles.NETWORK.sendToServer(new BlockPacket(context.getLevel(), context.getClickedPos(), context.getPlayer(), BlockPacketAction.WRENCH));
                    return InteractionResult.SUCCESS;
                }
            }
            
        }
        return InteractionResult.PASS;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Iterable<LittleTool> tools(ItemStack stack) {
        return Arrays.asList(new LittleToolWrench(stack));
    }
}
