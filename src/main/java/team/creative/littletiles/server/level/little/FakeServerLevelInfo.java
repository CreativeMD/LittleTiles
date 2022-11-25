package team.creative.littletiles.server.level.little;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.border.WorldBorder.Settings;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import team.creative.littletiles.client.level.little.FakeClientLevelInfo;

public class FakeServerLevelInfo extends FakeClientLevelInfo implements ServerLevelData {
    
    public FakeServerLevelInfo(Difficulty difficulty, boolean hardcore, boolean isFlat) {
        super(difficulty, hardcore, isFlat);
    }
    
    @Override
    public String getLevelName() {
        return "fake";
    }
    
    @Override
    public void setThundering(boolean thundering) {}
    
    @Override
    public int getRainTime() {
        return 0;
    }
    
    @Override
    public void setRainTime(int time) {}
    
    @Override
    public void setThunderTime(int time) {}
    
    @Override
    public int getThunderTime() {
        return 0;
    }
    
    @Override
    public int getClearWeatherTime() {
        return 0;
    }
    
    @Override
    public void setClearWeatherTime(int time) {}
    
    @Override
    public int getWanderingTraderSpawnDelay() {
        return 0;
    }
    
    @Override
    public void setWanderingTraderSpawnDelay(int delay) {}
    
    @Override
    public int getWanderingTraderSpawnChance() {
        return 0;
    }
    
    @Override
    public void setWanderingTraderSpawnChance(int chance) {}
    
    @Override
    public UUID getWanderingTraderId() {
        return null;
    }
    
    @Override
    public void setWanderingTraderId(UUID uuid) {}
    
    @Override
    public GameType getGameType() {
        return GameType.SURVIVAL;
    }
    
    @Override
    public void setWorldBorder(Settings settings) {}
    
    @Override
    public Settings getWorldBorder() {
        return WorldBorder.DEFAULT_SETTINGS;
    }
    
    @Override
    public boolean isInitialized() {
        return true;
    }
    
    @Override
    public void setInitialized(boolean init) {}
    
    @Override
    public boolean getAllowCommands() {
        return false;
    }
    
    @Override
    public void setGameType(GameType type) {}
    
    @Override
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return null;
    }
    
}
