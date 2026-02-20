package team.creative.littletiles.common.item;

import net.minecraft.client.Minecraft;
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
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class ItemLittleWrench extends Item implements ILittleTool, IItemTooltip {
    
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
                    try {
                        if (result.isComplete() && result.parent.isStructure())
                            if (result.parent.getStructure().wrenchInteract(context.getPlayer()))
                                LittleTiles.NETWORK.sendToServer(new BlockPacket(context.getLevel(), context.getClickedPos(), context.getPlayer(), BlockPacketAction.WRENCH_INFO));
                            else
                                LittleTilesGuiRegistry.STRUCTURE_SIGNAL.open(context.getPlayer(), result.parent.getStructure());
                        else
                            LittleTiles.NETWORK.sendToServer(new BlockPacket(context.getLevel(), context.getClickedPos(), context.getPlayer(), BlockPacketAction.WRENCH));
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    return InteractionResult.SUCCESS;
                }
            }
            
        }
        return InteractionResult.PASS;
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
