package team.creative.littletiles.common.structure.type;

import java.util.HashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.SignalState;

public class LittleNoClipStructure extends LittleStructure {
    
    public HashSet<Entity> entities = new HashSet<>();
    
    public boolean web = true;
    
    public LittleNoClipStructure(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        web = nbt.getBoolean("web");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putBoolean("web", web);
    }
    
    @Override
    public void onEntityCollidedWithBlock(Level level, IStructureParentCollection parent, BlockPos pos, Entity entityIn) {
        if (web)
            entityIn.makeStuckInBlock(Blocks.COBWEB.defaultBlockState(), new Vec3(0.25D, 0.05F, 0.25D));
        if (level.isClientSide)
            return;
        
        boolean intersected = false;
        for (LittleTile tile : parent) {
            for (LittleBox box : tile)
                if (box.getBB(parent.getGrid(), pos).intersects(entityIn.getBoundingBox())) {
                    intersected = true;
                    break;
                }
            
        }
        
        if (intersected)
            entities.add(entityIn);
        
        queueForNextTick();
    }
    
    @Override
    public boolean queueTick() {
        int players = 0;
        for (Entity entity : entities)
            if (entity instanceof Player)
                players++;
        getInput(0).updateState(SignalState.of(players));
        getInput(1).updateState(SignalState.of(entities.size()));
        boolean wasEmpty = entities.isEmpty();
        entities.clear();
        return !wasEmpty;
    }
    
    public static class LittleNoClipStructureParser extends LittleStructureGuiParser {
        
        public LittleNoClipStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        public void createControls(LittleGroup previews, LittleStructure structure) {
            boolean slowness = true;
            if (structure instanceof LittleNoClipStructure)
                slowness = ((LittleNoClipStructure) structure).web;
            parent.add(new GuiCheckBox("web", slowness).setTranslate("gui.noclip.slowness"));
        }
        
        @Override
        public LittleNoClipStructure parseStructure(LittleGroup previews) {
            LittleNoClipStructure structure = createStructure(LittleNoClipStructure.class, null);
            structure.web = ((GuiCheckBox) parent.get("web")).value;
            return structure;
        }
        
        @Override
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleNoClipStructure.class);
        }
    }
    
}
