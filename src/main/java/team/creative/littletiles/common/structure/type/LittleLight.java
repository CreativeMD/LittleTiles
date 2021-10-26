package team.creative.littletiles.common.structure.type;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleLight extends LittleStructure {
    
    public int level;
    public boolean disableRightClick = false;
    
    public LittleLight(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        level = nbt.getInt("level");
        disableRightClick = nbt.getBoolean("disableRightClick");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putInt("level", level);
        nbt.putBoolean("disableRightClick", disableRightClick);
    }
    
    @Override
    public int getLightValue(BlockPos pos) {
        return getOutput(0).getState()[0] ? level : 0;
    }
    
    @Override
    public boolean canInteract() {
        return !disableRightClick;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide && !disableRightClick)
            getOutput(0).toggle();
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("enabled")) {
            Level world = getLevel();
            try {
                tryAttributeChangeForBlocks();
                for (IStructureParentCollection list : blocksList()) {
                    BlockState state = world.getBlockState(list.getPos());
                    world.neighborChanged(list.getPos(), state.getBlock(), getPos());
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
    }
    
    @Override
    public int getAttribute() {
        if (getOutput(0).getState()[0])
            return super.getAttribute() | LittleStructureAttribute.EMISSIVE;
        return super.getAttribute();
    }
    
    public static class LittleLightStructureParser extends LittleStructureGuiParser {
        
        public LittleLightStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        public void createControls(LittleGroup previews, LittleStructure structure) {
            parent.add(new GuiSteppedSlider("level", structure instanceof LittleLight ? ((LittleLight) structure).level : 15, 0, 15));
            parent.add(new GuiCheckBox("rightclick", structure instanceof LittleLight ? !((LittleLight) structure).disableRightClick : true).setTranslate("gui.door.rightclick"));
        }
        
        @Override
        public LittleLight parseStructure(LittleGroup previews) {
            LittleLight structure = createStructure(LittleLight.class, null);
            GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("level");
            GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
            structure.level = (int) slider.value;
            structure.disableRightClick = !rightclick.value;
            return structure;
        }
        
        @Override
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleLight.class);
        }
    }
    
}
