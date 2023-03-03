package team.creative.littletiles.common.packet.entity.animation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;

public class LittleAnimationBlocksPacket extends LittleEntityPacket<LittleAnimationEntity> {
    
    public List<LittleBlockChange> changes;
    
    public LittleAnimationBlocksPacket(LittleAnimationEntity entity, Iterable<BlockPos> changes) {
        super(entity);
        this.changes = new ArrayList<>();
        LittleSubLevel level = entity.getSubLevel();
        for (BlockPos pos : changes)
            this.changes.add(new LittleBlockChange(level, pos));
    }
    
    public LittleAnimationBlocksPacket(LittleAnimationEntity entity, List<LittleBlockChange> changes) {
        super(entity);
        this.changes = changes;
    }
    
    public LittleAnimationBlocksPacket() {}
    
    @Override
    public void execute(Player player, LittleAnimationEntity entity) {
        entity.applyChanges(changes);
    }
    
}
