package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class BlockFilter extends TileFilter {
    
    public Block block;
    
    public BlockFilter(Block block) {
        this.block = block;
    }
    
    public BlockFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundTag nbt) {
        nbt.putString("block", block.getRegistryName().toString());
    }
    
    @Override
    protected void loadNBT(CompoundTag nbt) {
        block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
    }
    
    @Override
    public boolean is(IParentCollection parent, LittleTile tile) {
        return tile.block.is(block);
    }
}
