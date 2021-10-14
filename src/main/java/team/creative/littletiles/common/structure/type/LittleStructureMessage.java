package team.creative.littletiles.common.structure.type;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleStructureMessage extends LittleStructure {
    
    public String text;
    public boolean allowRightClick = true;
    
    public LittleStructureMessage(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean canInteract() {
        return allowRightClick;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (allowRightClick) {
            player.sendMessage(new TextComponent(text), Util.NIL_UUID);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, tile, box, pos, player, hand, result);
    }
    
    @Override
    protected void loadFromNBTExtra(CompoundTag nbt) {
        text = nbt.getString("text");
        allowRightClick = nbt.getBoolean("right");
    }
    
    @Override
    protected void writeToNBTExtra(CompoundTag nbt) {
        nbt.putString("text", text);
        nbt.putBoolean("right", allowRightClick);
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("message")) {
            Level level = getLevel();
            if (level.isClientSide)
                return;
            
            final LevelChunk chunk = level.getChunkAt(getPos());
            if (chunk != null)
                ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false)
                        .forEach(x -> x.sendMessage(new TextComponent(text), Util.NIL_UUID));
        }
    }
    
    public static class LittleMessageStructureParser extends LittleStructureGuiParser {
        
        public LittleMessageStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            parent.addControl(new GuiTextfield("text", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).text : "Hello World!", 0, 0, 140, 14));
            parent.controls.add(new GuiCheckBox("rightclick", CoreControl
                    .translate("gui.door.rightclick"), 0, 20, structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).allowRightClick : true));
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public LittleStructureMessage parseStructure(LittlePreviews previews) {
            LittleStructureMessage structure = createStructure(LittleStructureMessage.class, null);
            GuiTextfield text = (GuiTextfield) parent.get("text");
            structure.text = text.text;
            GuiCheckBox box = (GuiCheckBox) parent.get("rightclick");
            structure.allowRightClick = box.value;
            return structure;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleStructureMessage.class);
        }
    }
    
}
