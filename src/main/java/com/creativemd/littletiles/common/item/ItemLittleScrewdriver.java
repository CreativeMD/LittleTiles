package com.creativemd.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.gui.SubGuiScrewdriver;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.api.ILittlePlacer;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.packet.LittleScrewdriverSelectionPacket;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.tooltip.IItemTooltip;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleScrewdriver extends Item implements ILittlePlacer, IItemTooltip {
    
    public ItemLittleScrewdriver() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
        setMaxStackSize(1);
    }
    
    public void onClick(EntityPlayer player, boolean rightClick, BlockPos pos, ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        if (rightClick) {
            stack.getTagCompound().setIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            if (!player.world.isRemote)
                player.sendMessage(new TextComponentTranslation("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()));
        } else {
            stack.getTagCompound().setIntArray("pos1", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            if (!player.world.isRemote)
                player.sendMessage(new TextComponentTranslation("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()));
        }
    }
    
    @Override
    public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        onClick(player, true, result.getBlockPos(), stack);
        PacketHandler.sendPacketToServer(new LittleScrewdriverSelectionPacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    public boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        onClick(player, false, result.getBlockPos(), stack);
        PacketHandler.sendPacketToServer(new LittleScrewdriverSelectionPacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
        return new SubGuiScrewdriver(stack);
    }
    
    @Override
    public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 0F;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        if (stack.getTagCompound().hasKey("pos1")) {
            int[] array = stack.getTagCompound().getIntArray("pos1");
            if (array.length == 3)
                tooltip.add("1: " + array[0] + " " + array[1] + " " + array[2]);
        } else
            tooltip.add("1: left-click");
        
        if (stack.getTagCompound().hasKey("pos2")) {
            int[] array = stack.getTagCompound().getIntArray("pos2");
            if (array.length == 3)
                tooltip.add("2: " + array[0] + " " + array[1] + " " + array[2]);
        } else
            tooltip.add("2: right-click");
        
        tooltip.add("creative mode only");
    }
    
    @Override
    public boolean hasLittlePreview(ItemStack stack) {
        return false;
    }
    
    @Override
    public LittlePreviews getLittlePreview(ItemStack stack) {
        return null;
    }
    
    @Override
    public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
        
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName(), Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName(),
                LittleTilesClient.configure.getDisplayName() };
    }
    
}
