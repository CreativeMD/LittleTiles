package team.creative.littletiles.common.packet.structure;

import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.type.animation.LittleStateStructure;

public class StructureStartAnimationPacket extends StructurePacket {
    
    public AnimationTimeline timeline;
    
    public StructureStartAnimationPacket() {}
    
    public StructureStartAnimationPacket(StructureLocation location, AnimationTimeline timeline) {
        super(location);
        this.timeline = timeline;
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        requiresClient(player);
        
        if (structure instanceof LittleStateStructure state)
            state.setClientTimeline(timeline);
    }
    
}
