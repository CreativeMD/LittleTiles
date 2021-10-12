package team.creative.littletiles.common.structure.type;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;

public class LittleLight extends LittleStructure {
    
    public int level;
    public boolean disableRightClick = false;
    
    public LittleLight(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(CompoundTag nbt) {
        level = nbt.getInt("level");
        disableRightClick = nbt.getBoolean("disableRightClick");
    }
    
    @Override
    protected void writeToNBTExtra(CompoundTag nbt) {
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
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
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
        @SideOnly(Side.CLIENT)
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            parent.addControl(new GuiSteppedSlider("level", 0, 0, 100, 12, structure instanceof LittleLight ? ((LittleLight) structure).level : 15, 0, 15));
            parent.addControl(new GuiCheckBox("rightclick", CoreControl
                    .translate("gui.door.rightclick"), 0, 20, structure instanceof LittleLight ? !((LittleLight) structure).disableRightClick : true));
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public LittleLight parseStructure(LittlePreviews previews) {
            LittleLight structure = createStructure(LittleLight.class, null);
            GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("level");
            GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
            structure.level = (int) slider.value;
            structure.disableRightClick = !rightclick.value;
            return structure;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleLight.class);
        }
    }
    
}
