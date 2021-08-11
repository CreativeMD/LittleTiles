package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructureBuilder;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.item.ItemLittleRecipeAdvanced;

public class SubContainerBuilder extends SubContainer {
    
    public LittleStructureBuilder builder;
    
    public SubContainerBuilder(EntityPlayer player, LittleStructureBuilder builder) {
        super(player);
        this.builder = builder;
    }
    
    @Override
    public void createControls() {
        addSlotToContainer(new Slot(builder.inventory, 0, 152, 61));
        addPlayerSlotsToContainer(player);
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        if ((player.isCreative() && builder.inventory.getStackInSlot(0).isEmpty()) || ItemLittleRecipeAdvanced.isRecipe(builder.inventory.getStackInSlot(0).getItem())) {
            int width = nbt.getInteger("width");
            int height = nbt.getInteger("height");
            int thickness = nbt.getInteger("thickness");
            String[] parts = nbt.getString("block").split(":");
            Block block = Block.getBlockFromName(parts[0] + ":" + parts[1]);
            int meta;
            if (parts.length == 3)
                meta = Integer.parseInt(parts[2]);
            else
                meta = 0;
            IBlockState state = block.getStateFromMeta(meta);
            LittleGridContext grid = LittleGridContext.get(nbt.getInteger("grid"));
            
            builder.lastBlockState = state;
            builder.lastSizeX = width;
            builder.lastSizeY = height;
            builder.lastThickness = thickness;
            builder.lastGrid = grid.size;
            builder.lastStructureType = nbt.getString("type");
            builder.updateStructure();
            
            LittleStructureBuilderType type = LittleStructureBuilder.get(builder.lastStructureType);
            if (type != null) {
                ItemStack stack = builder.inventory.getStackInSlot(0);
                if (stack.isEmpty()) {
                    stack = new ItemStack(LittleTiles.recipeAdvanced);
                    builder.inventory.setInventorySlotContents(0, stack);
                }
                NBTTagCompound tileData = new NBTTagCompound();
                tileData.setString("block", nbt.getString("block"));
                LittlePreview.savePreview(type.construct(grid, width, height, thickness, tileData), stack);
            }
        }
    }
    
}
