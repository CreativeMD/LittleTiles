package team.creative.littletiles.common.level.little;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;
import net.neoforged.neoforge.common.CommonHooks;

public class FakeLevelInfo implements WritableLevelData {
    
    private final boolean hardcore;
    private final GameRules gameRules;
    private final boolean isFlat;
    private BlockPos spawnPos;
    private float spawnAngle;
    private long gameTime;
    private long dayTime;
    private boolean raining;
    private Difficulty difficulty;
    private boolean difficultyLocked;
    
    public FakeLevelInfo(Difficulty difficulty, boolean hardcore, boolean isFlat) {
        this.difficulty = difficulty;
        this.hardcore = hardcore;
        this.isFlat = isFlat;
        this.gameRules = new GameRules();
    }
    
    @Override
    public BlockPos getSpawnPos() {
        return spawnPos;
    }
    
    @Override
    public float getSpawnAngle() {
        return spawnAngle;
    }
    
    @Override
    public void setSpawn(BlockPos pos, float angle) {
        this.spawnPos = pos.immutable();
        this.spawnAngle = angle;
    }
    
    @Override
    public long getGameTime() {
        return this.gameTime;
    }
    
    @Override
    public long getDayTime() {
        return this.dayTime;
    }
    
    public void setGameTime(long p_239155_1_) {
        this.gameTime = p_239155_1_;
    }
    
    public void setDayTime(long p_239158_1_) {
        this.dayTime = p_239158_1_;
    }
    
    @Override
    public boolean isThundering() {
        return false;
    }
    
    @Override
    public boolean isRaining() {
        return this.raining;
    }
    
    @Override
    public void setRaining(boolean p_76084_1_) {
        this.raining = p_76084_1_;
    }
    
    @Override
    public boolean isHardcore() {
        return this.hardcore;
    }
    
    @Override
    public GameRules getGameRules() {
        return this.gameRules;
    }
    
    @Override
    public Difficulty getDifficulty() {
        return this.difficulty;
    }
    
    @Override
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        CommonHooks.onDifficultyChange(difficulty, this.difficulty);
        this.difficulty = difficulty;
    }
    
    public void setDifficultyLocked(boolean p_239157_1_) {
        this.difficultyLocked = p_239157_1_;
    }
    
    public double getHorizonHeight() {
        return this.isFlat ? 0.0D : 63.0D;
    }
    
    public double getClearColorScale() {
        return this.isFlat ? 1.0D : 0.03125D;
    }
}
