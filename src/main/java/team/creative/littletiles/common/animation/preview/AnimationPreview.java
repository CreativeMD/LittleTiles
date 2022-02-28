package team.creative.littletiles.common.animation.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.creativemd.littletiles.common.tile.place.PlacePreview;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.level.FakeClientLevel;
import team.creative.creativecore.common.level.FakeServerLevel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.level.LittleRenderChunkSuppilier;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.EntityAnimationController;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.math.transformation.LittleTransformation;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PlacementResult;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;

public class AnimationPreview {
    
    public final EntityAnimation animation;
    public final LittleGroup previews;
    public final LittleBox entireBox;
    public final LittleGrid grid;
    public final AABB box;
    
    public AnimationPreview(LittleGroup previews) {
        this.previews = previews;
        BlockPos pos = new BlockPos(0, 75, 0);
        FakeClientLevel fakeWorld = FakeServerLevel.createFakeLevel("animationViewer", true);
        fakeWorld.renderChunkSupplier = new LittleRenderChunkSuppilier();
        
        if (!previews.hasStructure()) {
            CompoundTag nbt = new CompoundTag();
            new LittleFixedStructure(LittleStructureRegistry.getStructureType(LittleFixedStructure.class), null).save(nbt);
            previews = new LittleGroup(nbt, previews);
        }
        
        Placement placement = new Placement(null, PlacementPreview.absolute(fakeWorld, null, new LittleGroupAbsolute(pos, previews), Facing.EAST));
        List<PlacePreview> placePreviews = new ArrayList<>();
        PlacementResult result = null;
        try {
            result = placement.place();
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
        
        entireBox = previews.getSurroundingBox();
        grid = previews.getGrid();
        box = entireBox.getBB(grid);
        
        animation = new EntityAnimation(fakeWorld, fakeWorld, (EntityAnimationController) new EntityAnimationController() {
            
            @Override
            public void transform(LittleTransformation transformation) {}
            
            @Override
            protected void saveExtra(CompoundTag nbt) {}
            
            @Override
            protected void load(CompoundTag nbt) {}
            
        }.addStateAndSelect("nothing", new AnimationState()), pos, UUID.randomUUID(), new StructureAbsolute(pos, entireBox, previews
                .getGrid()), result.parentStructure == null ? null : new LocalStructureLocation(result.parentStructure));
        
    }
    
}
