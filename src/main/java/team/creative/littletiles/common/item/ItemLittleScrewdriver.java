package team.creative.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiScrewdriver;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.packet.action.ChangedPosPacket;

public class ItemLittleScrewdriver extends Item implements ILittleTool, IItemTooltip {
    
    public ItemLittleScrewdriver() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view, boolean secondary) {
        return new GuiScrewdriver(view);
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
        var pos = stack.get(LittleTilesRegistry.FIRST_POS);
        if (pos != null)
            tooltip.add(Component.literal("1: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        else
            tooltip.add(Component.literal("1: ").append(Component.translatable("gui.click.left")));
        
        pos = stack.get(LittleTilesRegistry.SECOND_POS);
        if (pos != null)
            tooltip.add(Component.literal("2: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()));
        else
            tooltip.add(Component.literal("2: ").append(Component.translatable("gui.click.right")));
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse
                .getTranslatedKeyMessage(), LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage() };
    }
    
    @Override
    public LittleGrid getPositionGrid(Player player, ItemStack stack) {
        return LittleGrid.overallDefault();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleTool tool(ItemStack stack) {
        return new LittleTool(stack) {
            
            @Override
            public void tick(Level level, Player player, BlockHitResult blockHit) {}
            
            @Override
            public void render(Level level, Player player, PoseStack pose, Vec3 cam, boolean lines) {}
            
            @Override
            public boolean keyPressed(Level level, Player player, KeyMapping key) {
                return false;
            }
            
            @Override
            public void removed() {}
            
            @Override
            public boolean onRightClick(Level level, Player player, @Nullable BlockHitResult result) {
                if (result == null)
                    return false;
                LittleTiles.NETWORK.sendToServer(new ChangedPosPacket(true, result.getBlockPos()));
                return true;
            }
            
            @Override
            public boolean onLeftClick(Level level, Player player, @Nullable BlockHitResult result) {
                if (result == null)
                    return false;
                LittleTiles.NETWORK.sendToServer(new ChangedPosPacket(false, result.getBlockPos()));
                return true;
            }
            
        };
    }
    
}
