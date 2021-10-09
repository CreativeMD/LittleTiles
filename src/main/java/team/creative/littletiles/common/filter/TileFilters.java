package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.creativecore.common.util.CompoundSerializer;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.filter.BiFilterSerializer;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class TileFilters {
    
    public static final BiFilterSerializer<IParentCollection, LittleTile> SERIALIZER = new BiFilterSerializer<IParentCollection, LittleTile>().register("b", TileBlockFilter.class)
            .register("c", TileColorFilter.class).register("no", TileNoStructureFilter.class);
    
    public static BiFilter<IParentCollection, LittleTile> block(Block block) {
        return new TileBlockFilter(block);
    }
    
    public static BiFilter<IParentCollection, LittleTile> color(int color) {
        return new TileColorFilter(color);
    }
    
    public static BiFilter<IParentCollection, LittleTile> noStructure() {
        return new TileNoStructureFilter();
    }
    
    public static BiFilter<IParentCollection, LittleTile> and(BiFilter<IParentCollection, LittleTile>... filters) {
        return BiFilter.and(filters);
    }
    
    public static BiFilter<IParentCollection, LittleTile> or(BiFilter<IParentCollection, LittleTile>... filters) {
        return BiFilter.or(filters);
    }
    
    public static BiFilter<IParentCollection, LittleTile> not(BiFilter<IParentCollection, LittleTile> filter) {
        return BiFilter.not(filter);
    }
    
    public static class TileBlockFilter implements BiFilter<IParentCollection, LittleTile>, CompoundSerializer {
        
        public final Block block;
        
        public TileBlockFilter(Block block) {
            this.block = block;
        }
        
        public TileBlockFilter(CompoundTag nbt) {
            block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
        }
        
        @Override
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putString("block", block.getRegistryName().toString());
            return tag;
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return tile.block.is(block);
        }
    }
    
    public static class TileColorFilter implements BiFilter<IParentCollection, LittleTile>, CompoundSerializer {
        
        public final int color;
        
        public TileColorFilter(int color) {
            this.color = color;
        }
        
        public TileColorFilter(CompoundTag nbt) {
            color = nbt.getInt("color");
        }
        
        @Override
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("color", color);
            return tag;
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return tile.color == color;
        }
        
    }
    
    public static class TileNoStructureFilter implements BiFilter<IParentCollection, LittleTile>, CompoundSerializer {
        
        public TileNoStructureFilter() {
            
        }
        
        public TileNoStructureFilter(CompoundTag tag) {
            
        }
        
        @Override
        public CompoundTag write() {
            return new CompoundTag();
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return !parent.isStructure();
        }
        
    }
    
}
