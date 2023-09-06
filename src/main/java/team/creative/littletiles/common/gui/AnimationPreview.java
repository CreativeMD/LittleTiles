package team.creative.littletiles.common.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;
import team.creative.littletiles.server.level.little.FakeServerLevel;

public class AnimationPreview {
    
    public final LittleAnimationEntity animation;
    public final LittleGroup previews;
    public final LittleBox entireBox;
    public final LittleGrid grid;
    public final AABB box;
    
    public AnimationPreview(LittleStructure structure, LittleGroup previews) throws LittleActionException {
        this.previews = previews;
        this.grid = previews.getGrid();
        BlockPos pos = new BlockPos(0, 0, 0);
        FakeClientLevel fakeWorld = FakeServerLevel.createClient("animationViewer");
        fakeWorld.setOrigin(new Vec3d());
        LittleAnimationLevel subLevel = new LittleAnimationLevel(fakeWorld);
        
        if (!previews.hasStructure()) {
            CompoundTag nbt = new CompoundTag();
            new LittleFixedStructure(LittleStructureRegistry.REGISTRY.get("fixed"), null).save(nbt);
            List<LittleGroup> newChildren = new ArrayList<>();
            for (LittleGroup group : previews.children.children())
                newChildren.add(group.copy());
            LittleGroup group = new LittleGroup(nbt, newChildren);
            final var oldPreviews = previews;
            group.addAll(grid, () -> new FunctionIterator<>(oldPreviews, x -> x.copy()));
            previews = group;
        }
        entireBox = previews.getSurroundingBox();
        box = entireBox.getBB(grid);
        StructureAbsolute absolute = structure != null ? structure.createAnimationCenter(pos, grid) : null;
        if (absolute == null)
            absolute = new StructureAbsolute(pos, entireBox, previews.getGrid());
        Placement placement = new Placement(null, subLevel, PlacementPreview.load((UUID) null, PlacementMode.ALL, new LittleGroupAbsolute(pos, previews), Facing.EAST));
        
        animation = new LittleAnimationEntity(fakeWorld, subLevel, absolute, placement);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void setupRendering(PoseStack pose) {
        animation.getOrigin().setupRendering(pose, 0, 0, 0, Minecraft.getInstance().getPartialTick());
    }
    
    public void unload() {
        animation.destroyAnimation();
    }
    
    public void set(PhysicalState state) {
        animation.physic.set(state);
    }
    
    public void tick() {
        animation.getOrigin().tick();
    }
    
    public void setCenter(StructureAbsolute center) {
        animation.setCenter(center);
    }
}
