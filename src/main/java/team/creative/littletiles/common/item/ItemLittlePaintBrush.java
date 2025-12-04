package team.creative.littletiles.common.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleShaper;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.shaper.LittleToolShaper;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.action.LittleActionColorBoxes;
import team.creative.littletiles.common.action.LittleActionColorBoxes.LittleActionColorBoxesFiltered;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiPaintBrush;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.packet.action.ChangedColorPacket;
import team.creative.littletiles.common.placement.PreviewMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.LittleShapeInstance;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class ItemLittlePaintBrush extends Item implements ILittleShaper, IItemTooltip {
    
    public ItemLittlePaintBrush() {
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LittleShapeInstance shape = getShape(stack);
        shape.appendInformation(stack, context, tooltip, flag);
        tooltip.add(Component.literal(TooltipUtils.printColor(stack.getOrDefault(LittleTilesRegistry.COLOR, ColorUtils.WHITE))));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND)
            return new InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand));
        if (!level.isClientSide)
            GuiCreator.ITEM_OPENER.open(player, hand);
        return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
    
    @Override
    public void shapeFinished(Level level, Player player, ItemStack stack, ShapeSelection selection, LittleBoxes boxes) {
        var filter = stack.get(LittleTilesRegistry.FILTER);
        int color = stack.getOrDefault(LittleTilesRegistry.COLOR, ColorUtils.WHITE);
        if (filter != null && filter.hasFilter())
            LittleTilesClient.ACTION_HANDLER.execute(new LittleActionColorBoxesFiltered(level, boxes, color, false, filter.getFilter()));
        else
            LittleTilesClient.ACTION_HANDLER.execute(new LittleActionColorBoxes(level, boxes, color, false));
    }
    
    @Override
    public boolean hasShape(Player player, ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleShape defaultShape() {
        return ShapeRegistry.TILE_SHAPE;
    }
    
    @Override
    public boolean previewInside(Player player, ItemStack stack) {
        return true;
    }
    
    @Override
    public PreviewMode previewMode(Player player, ItemStack stack) {
        return PreviewMode.LINES;
    }
    
    @Override
    public boolean selectLeftClick(Player player, ItemStack stack) {
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Iterable<LittleTool> tools(ItemStack stack) {
        return Arrays.asList(new LittleToolShaper(stack) {
            @Override
            public boolean onMouseWheelClickBlock(Level level, Player player, BlockHitResult result) {
                BlockState state = level.getBlockState(result.getBlockPos());
                
                if (state.getBlock() instanceof BlockTile) {
                    LittleTileContext context = LittleTileContext.selectFocused(level, result.getBlockPos(), player);
                    if (context.isComplete()) {
                        int color = ColorUtils.WHITE;
                        try {
                            if (context.parent.isStructure() && context.parent.getStructure() instanceof LittleStructure s && s.hasStructureColor())
                                color = s.getStructureColor();
                            else
                                color = context.tile.color;
                            LittleTiles.NETWORK.sendToServer(new ChangedColorPacket(color));
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    }
                    return true;
                }
                return super.onMouseWheelClickBlock(level, player, result);
            }
        });
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view, boolean secondary) {
        return new GuiPaintBrush(view);
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).translatable(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.KEY_MARK
                .getTranslatedKeyMessage(), LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage() };
    }
}
