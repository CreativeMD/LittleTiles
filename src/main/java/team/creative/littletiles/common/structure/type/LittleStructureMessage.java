package team.creative.littletiles.common.structure.type;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
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
            player.displayClientMessage(Component.literal(text), true);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, context, pos, player, result);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        text = nbt.getString("text");
        allowRightClick = nbt.getBoolean("right");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.putString("text", text);
        nbt.putBoolean("right", allowRightClick);
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("message")) {
            Level level = getStructureLevel();
            if (level.isClientSide)
                return;
            
            final LevelChunk chunk = level.getChunkAt(getStructurePos());
            if (chunk != null)
                ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(x -> x.sendSystemMessage(Component.literal(text)));
        }
    }
    
}
