package team.creative.littletiles.common.animation.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.level.FakeClientLevel;
import team.creative.creativecore.common.level.FakeServerLevel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.level.LittleRenderChunkSuppilier;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PlacementResult;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;

public class AnimationPreview {
    
    public final LittleLevelEntity animation;
    public final LittleGroup previews;
    public final LittleBox entireBox;
    public final LittleGrid grid;
    public final AABB box;
    
    @SuppressWarnings("deprecation")
    public AnimationPreview(LittleGroup previews) {
        this.previews = previews;
        BlockPos pos = new BlockPos(0, 75, 0);
        FakeClientLevel fakeWorld = FakeServerLevel.createClient("animationViewer");
        fakeWorld.renderChunkSupplier = new LittleRenderChunkSuppilier();
        
        if (!previews.hasStructure()) {
            CompoundTag nbt = new CompoundTag();
            new LittleFixedStructure(LittleStructureRegistry.getStructureType(LittleFixedStructure.class), null).save(nbt);
            List<LittleGroup> newChildren = new ArrayList<>();
            for (LittleGroup group : previews.children.children())
                newChildren.add(group.copy());
            LittleGroup group = new LittleGroup(nbt, grid, newChildren);
            for (LittleTile tile : previews)
                group.addDirectly(tile.copy());
        }
        
        Placement placement = new Placement(null, PlacementPreview.absolute(fakeWorld, PlacementMode.all, new LittleGroupAbsolute(pos, previews), Facing.EAST));
        PlacementResult result = null;
        try {
            result = placement.place();
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
        
        entireBox = previews.getSurroundingBox();
        grid = previews.getGrid();
        box = entireBox.getBB(grid);
        
        animation = new LittleLevelEntity(fakeWorld, fakeWorld, pos, UUID.randomUUID(), new StructureAbsolute(pos, entireBox, previews
                .getGrid()), result.parentStructure == null ? null : new LocalStructureLocation(result.parentStructure));
        
    }
    
}
