package team.creative.littletiles.common.structure.type;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
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
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (allowRightClick) {
            player.sendSystemMessage(Component.literal(text));
            return InteractionResult.SUCCESS;
        }
        return super.use(level, context, pos, player, result);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        text = nbt.getString("text");
        allowRightClick = nbt.getBoolean("right");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
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
                ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(x -> x.sendSystemMessage(Component.literal(text)));
        }
    }
    
    public static class LittleMessageStructureParser extends LittleStructureGuiParser {
        
        public LittleMessageStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        public void createControls(LittleGroup previews, LittleStructure structure) {
            parent.add(new GuiTextfield("text", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).text : "Hello World!"));
            parent.add(new GuiCheckBox("rightclick", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).allowRightClick : true)
                    .setTranslate("gui.door.rightclick"));
        }
        
        @Override
        public LittleStructureMessage parseStructure(LittleGroup previews) {
            LittleStructureMessage structure = createStructure(LittleStructureMessage.class, null);
            GuiTextfield text = (GuiTextfield) parent.get("text");
            structure.text = text.getText();
            GuiCheckBox box = (GuiCheckBox) parent.get("rightclick");
            structure.allowRightClick = box.value;
            return structure;
        }
        
        @Override
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleStructureMessage.class);
        }
    }
    
}
