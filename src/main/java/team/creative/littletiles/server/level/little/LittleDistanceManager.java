package team.creative.littletiles.server.level.little;

import java.util.concurrent.Executor;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public class LittleDistanceManager extends DistanceManager {
    
    protected LittleDistanceManager(Executor exe, Executor exe2) {
        super(exe, exe2);
    }
    
    @Override
    protected boolean isChunkToRemove(long pos) {
        return false;
    }
    
    @Override
    protected ChunkHolder getChunk(long pos) {
        return null;
    }
    
    @Override
    protected ChunkHolder updateChunkScheduling(long pos, int oldLevel, ChunkHolder holder, int newLevel) {
        return null;
    }
    
    @Override
    public boolean runAllUpdates(ChunkMap map) {
        return true;
    }
    
    @Override
    public <T> void addTicket(TicketType<T> type, ChunkPos pos, int level, T key) {}
    
    @Override
    public <T> void removeTicket(TicketType<T> type, ChunkPos pos, int level, T key) {}
    
    @Override
    public <T> void addRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key) {}
    
    @Override
    public <T> void addRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key, boolean forceTicks) {}
    
    @Override
    public <T> void removeRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key) {}
    
    @Override
    public <T> void removeRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key, boolean forceTicks) {}
    
    @Override
    public void addPlayer(SectionPos pos, ServerPlayer player) {}
    
    @Override
    public void removePlayer(SectionPos pos, ServerPlayer player) {
        
    }
    
    @Override
    public boolean inEntityTickingRange(long pos) {
        return true;
    }
    
    @Override
    public boolean inBlockTickingRange(long pos) {
        return true;
    }
    
    @Override
    public void updateSimulationDistance(int distance) {}
    
    @Override
    public int getNaturalSpawnChunkCount() {
        return 0;
    }
    
    @Override
    public boolean hasPlayersNearby(long pos) {
        return false;
    }
    
    @Override
    public String getDebugStatus() {
        return "";
    }
    
    @Override
    public boolean shouldForceTicks(long chunkPos) {
        return false;
    }
    
    @Override
    public void removeTicketsOnClosing() {}
    
    @Override
    public boolean hasTickets() {
        return false;
    }
    
    @Override
    public void purgeStaleTickets() {}
    
    @Override
    public void updateChunkForced(ChunkPos pos, boolean added) {}
}
