package team.creative.littletiles.common.level.little;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;

public class FakeLevelInfo implements WritableLevelData {
    
    private final boolean hardcore;
    private final GameRules gameRules;
    private final boolean isFlat;
    private int xSpawn;
    private int ySpawn;
    private int zSpawn;
    private float spawnAngle;
    private long gameTime;
    private long dayTime;
    private boolean raining;
    private Difficulty difficulty;
    private boolean difficultyLocked;
    
    public FakeLevelInfo(Difficulty p_i232338_1_, boolean hardcore, boolean isFlat) {
        this.difficulty = p_i232338_1_;
        this.hardcore = hardcore;
        this.isFlat = isFlat;
        this.gameRules = new GameRules();
    }
    
    @Override
    public int getXSpawn() {
        return this.xSpawn;
    }
    
    @Override
    public int getYSpawn() {
        return this.ySpawn;
    }
    
    @Override
    public int getZSpawn() {
        return this.zSpawn;
    }
    
    @Override
    public float getSpawnAngle() {
        return this.spawnAngle;
    }
    
    @Override
    public long getGameTime() {
        return this.gameTime;
    }
    
    @Override
    public long getDayTime() {
        return this.dayTime;
    }
    
    @Override
    public void setXSpawn(int p_76058_1_) {
        this.xSpawn = p_76058_1_;
    }
    
    @Override
    public void setYSpawn(int p_76056_1_) {
        this.ySpawn = p_76056_1_;
    }
    
    @Override
    public void setZSpawn(int p_76087_1_) {
        this.zSpawn = p_76087_1_;
    }
    
    @Override
    public void setSpawnAngle(float p_241859_1_) {
        this.spawnAngle = p_241859_1_;
    }
    
    public void setGameTime(long p_239155_1_) {
        this.gameTime = p_239155_1_;
    }
    
    public void setDayTime(long p_239158_1_) {
        this.dayTime = p_239158_1_;
    }
    
    @Override
    public void setSpawn(BlockPos p_176143_1_, float p_176143_2_) {
        this.xSpawn = p_176143_1_.getX();
        this.ySpawn = p_176143_1_.getY();
        this.zSpawn = p_176143_1_.getZ();
        this.spawnAngle = p_176143_2_;
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
    
    public void setDifficulty(Difficulty p_239156_1_) {
        net.minecraftforge.common.ForgeHooks.onDifficultyChange(p_239156_1_, this.difficulty);
        this.difficulty = p_239156_1_;
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
