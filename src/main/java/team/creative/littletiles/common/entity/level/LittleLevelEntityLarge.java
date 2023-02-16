package team.creative.littletiles.common.entity.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleLevelEntityLarge extends LittleLevelEntity {
    
    public LittleLevelEntityLarge(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleLevelEntityLarge(EntityType<?> type, Level level, LittleSubLevel subLevel, StructureAbsolute center, LocalStructureLocation location) {
        super(type, level, subLevel, center, location);
    }
    
    @Override
    public void saveLevelEntity(CompoundTag nbt) {}
    
    @Override
    public void onTick() {}
    
    @Override
    public void loadLevelEntity(CompoundTag nbt) {}
    
    @Override
    public void initialTick() {}
    
}
