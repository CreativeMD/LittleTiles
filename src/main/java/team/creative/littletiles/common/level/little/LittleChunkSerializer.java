package team.creative.littletiles.common.level.little;

import java.util.EnumSet;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkDataEvent;
import team.creative.littletiles.mixin.server.level.ChunkSerializerAccessor;
import team.creative.littletiles.server.level.little.LittleServerLevel;

public class LittleChunkSerializer {
    
    public static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = ChunkSerializerAccessor.getBLOCK_STATE_CODEC();
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static LevelChunk read(LittleLevel level, CompoundTag nbt) {
        ChunkPos chunkpos = new ChunkPos(nbt.getInt("xPos"), nbt.getInt("zPos"));
        
        ListTag listtag = nbt.getList("sections", 10);
        int i = level.getSectionsCount();
        LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
        boolean chunkSkyLight = level.dimensionType().hasSkyLight();
        ChunkSource chunksource = level.getChunkSource();
        LevelLightEngine levellightengine = chunksource.getLightEngine();
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        boolean retained = false;
        
        for (int j = 0; j < listtag.size(); ++j) {
            CompoundTag compoundtag = listtag.getCompound(j);
            int y = compoundtag.getByte("Y");
            int l = level.getSectionIndexFromSectionY(y);
            if (l >= 0 && l < alevelchunksection.length) {
                PalettedContainer<BlockState> states;
                if (compoundtag.contains("block_states", 10))
                    states = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundtag.getCompound("block_states")).promotePartial(error -> logErrors(chunkpos, y, error))
                            .getOrThrow(false, LOGGER::error);
                else
                    states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                
                LevelChunkSection levelchunksection = new LevelChunkSection(y, states, new PalettedContainer<>(registry.asHolderIdMap(), registry
                        .getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES));
                alevelchunksection[l] = levelchunksection;
                //poiManager.checkConsistencyWithBlocks(pos, levelchunksection);
            }
            
            boolean hasBlockLight = compoundtag.contains("BlockLight", 7);
            boolean hasSkyLight = chunkSkyLight && compoundtag.contains("SkyLight", 7);
            if (hasBlockLight || hasSkyLight) {
                if (!retained) {
                    levellightengine.retainData(chunkpos, true);
                    retained = true;
                }
                
                if (hasBlockLight)
                    levellightengine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkpos, y), new DataLayer(compoundtag.getByteArray("BlockLight")), true);
                
                if (hasSkyLight)
                    levellightengine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkpos, y), new DataLayer(compoundtag.getByteArray("SkyLight")), true);
            }
        }
        
        LevelChunkTicks<Block> blockTicks = LevelChunkTicks
                .load(nbt.getList("block_ticks", 10), (x) -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(x)), chunkpos);
        LevelChunkTicks<Fluid> fluidTicks = LevelChunkTicks
                .load(nbt.getList("fluid_ticks", 10), (x) -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(x)), chunkpos);
        
        LevelChunk chunk = new LevelChunk(level.asLevel(), chunkpos, UpgradeData.EMPTY, blockTicks, fluidTicks, nbt
                .getLong("InhabitedTime"), alevelchunksection, postLoadChunk(level, nbt), null);
        
        if (nbt.contains("ForgeCaps"))
            chunk.readCapsFromNBT(nbt.getCompound("ForgeCaps"));
        
        chunk.setLightCorrect(nbt.getBoolean("isLightOn"));
        
        CompoundTag heightmaps = nbt.getCompound("Heightmaps");
        EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);
        
        for (Heightmap.Types type : chunk.getStatus().heightmapsAfter()) {
            String s = type.getSerializationKey();
            if (heightmaps.contains(s, 12))
                chunk.setHeightmap(type, heightmaps.getLongArray(s));
            else
                enumset.add(type);
        }
        
        Heightmap.primeHeightmaps(chunk, enumset);
        
        if (nbt.getBoolean("shouldSave"))
            chunk.setUnsaved(true);
        
        ListTag blockEntities = nbt.getList("block_entities", 10);
        
        for (int j = 0; j < blockEntities.size(); ++j)
            chunk.setBlockEntityNbt(blockEntities.getCompound(j));
        
        MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, nbt, ChunkStatus.ChunkType.LEVELCHUNK));
        return chunk;
    }
    
    private static void logErrors(ChunkPos pos, int y, String error) {
        LOGGER.error("Recoverable errors when loading section [" + pos.x + ", " + y + ", " + pos.z + "]: " + error);
    }
    
    public static CompoundTag write(LittleServerLevel level, ChunkAccess chunk) {
        ChunkPos chunkpos = chunk.getPos();
        CompoundTag nbt = NbtUtils.addCurrentDataVersion(new CompoundTag());
        nbt.putInt("xPos", chunkpos.x);
        nbt.putInt("yPos", chunk.getMinSection());
        nbt.putInt("zPos", chunkpos.z);
        nbt.putLong("LastUpdate", level.getGameTime());
        nbt.putLong("InhabitedTime", chunk.getInhabitedTime());
        
        LevelChunkSection[] alevelchunksection = chunk.getSections();
        ListTag sections = new ListTag();
        LevelLightEngine levellightengine = level.getChunkSource().getLightEngine();
        
        for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
            int j = chunk.getSectionIndexFromSectionY(i);
            boolean flag1 = j >= 0 && j < alevelchunksection.length;
            DataLayer blockLight = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
            DataLayer skyLight = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
            if (flag1 || blockLight != null || skyLight != null) {
                CompoundTag sectionTag = new CompoundTag();
                if (flag1) {
                    LevelChunkSection levelchunksection = alevelchunksection[j];
                    sectionTag.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelchunksection.getStates()).getOrThrow(false, LOGGER::error));
                }
                
                if (blockLight != null && !blockLight.isEmpty())
                    sectionTag.putByteArray("BlockLight", blockLight.getData());
                
                if (skyLight != null && !skyLight.isEmpty())
                    sectionTag.putByteArray("SkyLight", skyLight.getData());
                
                if (!sectionTag.isEmpty()) {
                    sectionTag.putByte("Y", (byte) i);
                    sections.add(sectionTag);
                }
            }
        }
        nbt.put("sections", sections);
        
        if (chunk.isLightCorrect())
            nbt.putBoolean("isLightOn", true);
        
        ListTag blockEntities = new ListTag();
        for (BlockPos blockpos : chunk.getBlockEntitiesPos()) {
            CompoundTag blockEntityTag = chunk.getBlockEntityNbtForSaving(blockpos);
            if (blockEntityTag != null)
                blockEntities.add(blockEntityTag);
        }
        nbt.put("block_entities", blockEntities);
        
        LevelChunk levelChunk = (LevelChunk) chunk;
        try {
            final CompoundTag capTag = levelChunk.writeCapsToNBT();
            if (capTag != null)
                nbt.put("ForgeCaps", capTag);
        } catch (Exception exception) {
            LOGGER.error("A capability provider has thrown an exception trying to write state. It will not persist. Report this to the mod author", exception);
        }
        
        saveTicks(level, nbt, chunk.getTicksForSerialization());
        
        CompoundTag heightmaps = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps())
            if (chunk.getStatus().heightmapsAfter().contains(entry.getKey()))
                heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        nbt.put("Heightmaps", heightmaps);
        
        MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, level, nbt));
        return nbt;
    }
    
    private static void saveTicks(Level level, CompoundTag nbt, ChunkAccess.TicksToSave ticks) {
        long i = level.getLevelData().getGameTime();
        nbt.put("block_ticks", ticks.blocks().save(i, x -> BuiltInRegistries.BLOCK.getKey(x).toString()));
        nbt.put("fluid_ticks", ticks.fluids().save(i, (x) -> BuiltInRegistries.FLUID.getKey(x).toString()));
    }
    
    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(LittleLevel level, CompoundTag nbt) {
        ListTag entities = getListOfCompoundsOrNull(nbt, "entities");
        ListTag blockEntities = getListOfCompoundsOrNull(nbt, "block_entities");
        return entities == null && blockEntities == null ? null : (x) -> {
            if (entities != null && level instanceof LittleServerLevel sLevel)
                sLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(entities, level.asLevel()));
            
            if (blockEntities != null)
                for (int i = 0; i < blockEntities.size(); ++i) {
                    CompoundTag blockTag = blockEntities.getCompound(i);
                    if (blockTag.getBoolean("keepPacked"))
                        x.setBlockEntityNbt(blockTag);
                    else {
                        BlockPos blockpos = BlockEntity.getPosFromTag(blockTag);
                        BlockEntity blockentity = BlockEntity.loadStatic(blockpos, x.getBlockState(blockpos), blockTag);
                        if (blockentity != null)
                            x.setBlockEntity(blockentity);
                    }
                }
        };
    }
    
    @Nullable
    private static ListTag getListOfCompoundsOrNull(CompoundTag nbt, String key) {
        ListTag listtag = nbt.getList(key, 10);
        return listtag.isEmpty() ? null : listtag;
    }
}
