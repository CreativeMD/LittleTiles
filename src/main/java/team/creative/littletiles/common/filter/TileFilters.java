package team.creative.littletiles.common.filter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.util.CompoundSerializer;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.registry.LittleMissingBlock;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;

public class TileFilters {
    
    static {
        BiFilter.SERIALIZER.register("b", TileBlockFilter.class).register("c", TileColorFilter.class).register("no", TileNoStructureFilter.class).register("t", TileTagFilter.class)
                .register("n", TileNameFilter.class).register("m", TileMissingFilter.class);
    }
    
    public static BiFilter<IParentCollection, LittleTile> block(Block block) {
        return new TileBlockFilter(block);
    }
    
    public static BiFilter<IParentCollection, LittleTile> tag(TagKey<Block> tag) {
        return new TileTagFilter(tag);
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
    
    public static BiFilter<IParentCollection, LittleTile> name(String name) {
        return new TileNameFilter(name);
    }
    
    public static BiFilter<IParentCollection, LittleTile> missing() {
        return new TileMissingFilter();
    }
    
    public static BiFilter<IParentCollection, LittleTile> of(LittleElement element) {
        return and(block(element.getState().getBlock()), color(element.color));
    }
    
    public static class TileBlockFilter implements BiFilter<IParentCollection, LittleTile>, CompoundSerializer {
        
        public final Block block;
        
        public TileBlockFilter(Block block) {
            this.block = block;
        }
        
        public TileBlockFilter(CompoundTag nbt) {
            block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbt.getString("block")));
        }
        
        @Override
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putString("block", block.builtInRegistryHolder().key().location().toString());
            return tag;
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return tile.getBlock().is(block);
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
        
        public TileNoStructureFilter() {}
        
        public TileNoStructureFilter(CompoundTag tag) {}
        
        @Override
        public CompoundTag write() {
            return new CompoundTag();
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return !parent.isStructure();
        }
        
    }
    
    public static class TileTagFilter implements BiFilter<IParentCollection, LittleTile>, CompoundSerializer {
        
        public final TagKey<Block> tag;
        
        public TileTagFilter(TagKey<Block> tag) {
            this.tag = tag;
        }
        
        public TileTagFilter(CompoundTag nbt) {
            tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(nbt.getString("tag")));
        }
        
        @Override
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putString("tag", this.tag.location().toString());
            return tag;
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return tile.getBlock().is(tag);
        }
    }
    
    public static class TileNameFilter implements BiFilter<IParentCollection, LittleTile>, CompoundSerializer {
        
        public final String name;
        
        public TileNameFilter(String name) {
            this.name = name;
        }
        
        public TileNameFilter(CompoundTag nbt) {
            name = nbt.getString("name");
        }
        
        @Override
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name);
            return tag;
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return tile.getBlock().blockName().contains(name);
        }
    }
    
    public static class TileMissingFilter implements BiFilter<IParentCollection, LittleTile>, CompoundSerializer {
        
        public TileMissingFilter() {}
        
        public TileMissingFilter(CompoundTag nbt) {}
        
        @Override
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            return tag;
        }
        
        @Override
        public boolean is(IParentCollection parent, LittleTile tile) {
            return tile.getBlock() instanceof LittleMissingBlock;
        }
    }
    
}
