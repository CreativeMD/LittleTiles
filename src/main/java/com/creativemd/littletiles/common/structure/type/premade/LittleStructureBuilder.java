package com.creativemd.littletiles.common.structure.type.premade;

import java.util.HashMap;
import java.util.Set;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleStructureBuilder extends LittleStructurePremade {
    
    private static HashMap<String, LittleStructureBuilderType> types = new HashMap<>();
    
    public static void register(LittleStructureBuilderType type) {
        types.put(type.type.id, type);
    }
    
    public static Set<String> getNames() {
        return types.keySet();
    }
    
    public static LittleStructureBuilderType get(String id) {
        return types.get(id);
    }
    
    public InventoryBasic inventory = new InventoryBasic("recipe", false, 1);
    public int lastSizeX = 16;
    public int lastSizeY = 16;
    public int lastThickness = 1;
    public IBlockState lastBlockState;
    public String lastStructureType;
    public int lastGrid = 16;
    
    public LittleStructureBuilder(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        LittleStructureGuiHandler.openGui("structure_builder", new NBTTagCompound(), playerIn, this);
        return true;
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        if (nbt.hasKey("sizeX"))
            lastSizeX = nbt.getInteger("sizeX");
        else
            lastSizeX = 16;
        if (nbt.hasKey("sizeY"))
            lastSizeY = nbt.getInteger("sizeY");
        else
            lastSizeY = 16;
        if (nbt.hasKey("thickness"))
            lastThickness = nbt.getInteger("thickness");
        else
            lastThickness = 1;
        if (nbt.hasKey("grid"))
            lastGrid = nbt.getInteger("grid");
        else
            lastGrid = 16;
        lastStructureType = nbt.getString("type");
        if (nbt.hasKey("block")) {
            String[] parts = nbt.getString("block").split(":");
            Block block = Block.getBlockFromName(parts[0] + ":" + parts[1]);
            int meta;
            if (parts.length == 3)
                meta = Integer.parseInt(parts[2]);
            else
                meta = 0;
            lastBlockState = block.getStateFromMeta(meta);
        } else
            lastBlockState = Blocks.PLANKS.getDefaultState();
        inventory = InventoryUtils.loadInventoryBasic(nbt, 1);
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setInteger("sizeX", lastSizeX);
        nbt.setInteger("sizeY", lastSizeY);
        nbt.setInteger("thickness", lastThickness);
        InventoryUtils.saveInventoryBasic(inventory);
        Block block = lastBlockState.getBlock();
        int meta = block.getMetaFromState(lastBlockState);
        nbt.setString("block", block.getRegistryName().toString() + (meta != 0 ? ":" + meta : ""));
        nbt.setInteger("grid", lastGrid);
        nbt.setString("type", lastStructureType);
    }
    
    public static class LittleStructureBuilderType {
        
        public final LittleStructureType type;
        public final String frameVariableName;
        
        public LittleStructureBuilderType(LittleStructureType type, String frameVariableName) {
            this.type = type;
            this.frameVariableName = frameVariableName;
        }
        
        public LittlePreviews construct(LittleGridContext context, int width, int height, int thickness, NBTTagCompound tileData) {
            NBTTagCompound structureNBT = new NBTTagCompound();
            structureNBT.setString("id", type.id);
            structureNBT.setIntArray("topRight", new int[] { Float.floatToIntBits(0), Float.floatToIntBits(1), Float.floatToIntBits(1) });
            structureNBT.setIntArray(frameVariableName, new int[] { thickness, 0, 0, thickness + 1, height, width, context.size });
            LittlePreviews previews = new LittlePreviews(structureNBT, context);
            for (int x = 0; x < thickness; x += context.size)
                for (int y = 0; y < height; y += context.size)
                    for (int z = 0; z < width; z += context.size)
                        previews
                            .addWithoutCheckingPreview(new LittlePreview(new LittleBox(x, y, z, Math.min(x + 16, thickness), Math.min(y + 16, height), Math.min(z + 16, width)), tileData
                                .copy()));
            return previews;
        }
        
    }
    
}
