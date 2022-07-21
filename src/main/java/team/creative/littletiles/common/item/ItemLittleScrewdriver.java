package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiScrewdriver;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.packet.item.ScrewdriverSelectionPacket;
import team.creative.littletiles.common.placement.PlacementPosition;

public class ItemLittleScrewdriver extends Item implements ILittleTool, IItemTooltip {
    
    public ItemLittleScrewdriver() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    public void onClick(Player player, boolean rightClick, BlockPos pos, ItemStack stack) {
        if (rightClick) {
            stack.getOrCreateTag().putIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            if (!player.level.isClientSide)
                player.sendMessage(new TranslatableComponent("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()), Util.NIL_UUID);
        } else {
            stack.getOrCreateTag().putIntArray("pos1", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            if (!player.level.isClientSide)
                player.sendMessage(new TranslatableComponent("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()), Util.NIL_UUID);
        }
    }
    
    @Override
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        onClick(player, true, result.getBlockPos(), stack);
        LittleTiles.NETWORK.sendToServer(new ScrewdriverSelectionPacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        onClick(player, false, result.getBlockPos(), stack);
        LittleTiles.NETWORK.sendToServer(new ScrewdriverSelectionPacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiScrewdriver(view);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.getOrCreateTag().contains("pos1")) {
            int[] array = stack.getOrCreateTag().getIntArray("pos1");
            if (array.length == 3)
                tooltip.add(new TextComponent("1: " + array[0] + " " + array[1] + " " + array[2]));
        } else
            tooltip.add(new TextComponent("1: ").append(new TranslatableComponent("gui.click.left")));
        
        if (stack.getOrCreateTag().contains("pos2")) {
            int[] array = stack.getOrCreateTag().getIntArray("pos2");
            if (array.length == 3)
                tooltip.add(new TextComponent("2: " + array[0] + " " + array[1] + " " + array[2]));
        } else
            tooltip.add(new TextComponent("2: ").append(new TranslatableComponent("gui.click.right")));
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse
                .getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return LittleGrid.defaultGrid();
    }
    
    @Override
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {}
    
    @Override
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client) {}
    
}
