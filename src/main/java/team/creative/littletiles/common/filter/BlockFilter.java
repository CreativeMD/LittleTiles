package team.creative.littletiles.common.filter;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentTileList;

public class BlockFilter extends TileFilter {
    
    public Block block;
    
    public BlockFilter(Block block) {
        this.block = block;
    }
    
    public BlockFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundNBT nbt) {
        nbt.putString("block", block.getRegistryName().toString());
    }
    
    @Override
    protected void loadNBT(CompoundNBT nbt) {
        block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
    }
    
    @Override
    public boolean is(IParentTileList parent, LittleTile tile) {
        return tile.block.is(block);
    }
}
