package team.creative.littletiles.server.level.little;

import java.util.BitSet;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleVanillaPacket;

public class LittleChunkHolder {
    
    public final LevelChunk chunk;
    private boolean hasChangedSections;
    private final ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter = new BitSet();
    private final BitSet skyChangedLightSectionFilter = new BitSet();
    private final LevelLightEngine lightEngine;
    
    public LittleChunkHolder(ServerLevel level, ChunkPos pos, LevelLightEngine lightEngine) {
        this(new LevelChunk(level, pos), lightEngine);
    }
    
    public LittleChunkHolder(LevelChunk chunk, LevelLightEngine lightEngine) {
        this.chunk = chunk;
        this.changedBlocksPerSection = new ShortSet[chunk.getLevel().getSectionsCount()];
        this.lightEngine = lightEngine;
    }
    
    public void blockChanged(BlockPos pos) {
        int i = this.chunk.getLevel().getSectionIndex(pos.getY());
        if (this.changedBlocksPerSection[i] == null) {
            this.hasChangedSections = true;
            this.changedBlocksPerSection[i] = new ShortOpenHashSet();
        }
        
        this.changedBlocksPerSection[i].add(SectionPos.sectionRelativePos(pos));
    }
    
    public void sectionLightChanged(LightLayer layer, int y) {
        int i = this.lightEngine.getMinLightSection();
        int j = this.lightEngine.getMaxLightSection();
        if (y >= i && y <= j) {
            int k = y - i;
            if (layer == LightLayer.SKY)
                this.skyChangedLightSectionFilter.set(k);
            else
                this.blockChangedLightSectionFilter.set(k);
        }
    }
    
    public void broadcastChanges() {
        if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            Level level = chunk.getLevel();
            
            if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
                ClientboundLightUpdatePacket clientboundlightupdatepacket = new ClientboundLightUpdatePacket(chunk
                        .getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter);
                this.broadcast(clientboundlightupdatepacket);
                
                this.skyChangedLightSectionFilter.clear();
                this.blockChangedLightSectionFilter.clear();
            }
            
            if (this.hasChangedSections) {
                for (int j = 0; j < this.changedBlocksPerSection.length; ++j) {
                    ShortSet shortset = this.changedBlocksPerSection[j];
                    if (shortset != null) {
                        this.changedBlocksPerSection[j] = null;
                        int i = level.getSectionYFromSectionIndex(j);
                        SectionPos sectionpos = SectionPos.of(chunk.getPos(), i);
                        if (shortset.size() == 1) {
                            BlockPos blockpos = sectionpos.relativeToBlockPos(shortset.iterator().nextShort());
                            BlockState blockstate = level.getBlockState(blockpos);
                            this.broadcast(new ClientboundBlockUpdatePacket(blockpos, blockstate));
                            this.broadcastBlockEntityIfNeeded(level, blockpos, blockstate);
                        } else {
                            LevelChunkSection levelchunksection = chunk.getSection(j);
                            ClientboundSectionBlocksUpdatePacket clientboundsectionblocksupdatepacket = new ClientboundSectionBlocksUpdatePacket(sectionpos, shortset, levelchunksection);
                            this.broadcast(clientboundsectionblocksupdatepacket);
                            clientboundsectionblocksupdatepacket.runUpdates((pos, state) -> this.broadcastBlockEntityIfNeeded(level, pos, state));
                        }
                    }
                }
                
                this.hasChangedSections = false;
            }
        }
    }
    
    private void broadcastBlockEntityIfNeeded(Level level, BlockPos pos, BlockState state) {
        if (state.hasBlockEntity())
            this.broadcastBlockEntity(level, pos);
    }
    
    private void broadcastBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity != null) {
            Packet<?> packet = blockentity.getUpdatePacket();
            if (packet != null)
                this.broadcast(packet);
        }
    }
    
    private void broadcast(Packet<?> packet) {
        LittleTiles.NETWORK.sendToClientTracking(new LittleVanillaPacket(((LittleLevel) chunk.getLevel()), packet), ((LittleLevel) chunk.getLevel()).getHolder());
    }
    
}
