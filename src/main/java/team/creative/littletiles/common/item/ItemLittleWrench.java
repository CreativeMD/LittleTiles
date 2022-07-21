package team.creative.littletiles.common.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.gui.handler.LittleTileGuiCreator;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;

public class ItemLittleWrench extends Item {
    
    public static final LittleTileGuiCreator STRUCTURE_OVERVIEW = GuiCreator.register("structureoverview", new LittleTileGuiCreator((nbt, player, context) -> null));
    public static final LittleTileGuiCreator STRUCTURE_OVERVIEW2 = GuiCreator.register("structureoverview2", new LittleTileGuiCreator((nbt, player, context) -> null));
    
    public ItemLittleWrench() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof BETiles) {
            if (context.getLevel().isClientSide) {
                LittleTileContext result = LittleTileContext.selectFocused(context.getLevel(), context.getClickedPos(), context.getPlayer());
                if (context.getPlayer().isCrouching())
                    if (result.isComplete() && result.parent.isStructure())
                        STRUCTURE_OVERVIEW.open(context.getPlayer(), result);
                    else
                        LittleTiles.NETWORK.sendToServer(new BlockPacket(context.getLevel(), context.getClickedPos(), context.getPlayer(), BlockPacketAction.WRENCH));
                else
                    LittleTiles.NETWORK.sendToServer(new BlockPacket(context.getLevel(), context.getClickedPos(), context.getPlayer(), BlockPacketAction.WRENCH_INFO));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
