package team.creative.littletiles.common.structure.type;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleStructureMessage extends LittleStructure {
    
    public String text;
    public boolean allowRightClick = true;
    public boolean status = false;
    
    public LittleStructureMessage(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean canInteract() {
        return allowRightClick;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result, InteractionHand hand) {
        if (allowRightClick) {
            if (!level.isClientSide)
                player.displayClientMessage(message(), status);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, context, pos, player, result, hand);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        text = StringUtils.truncate(nbt.getString("text"), LittleTiles.CONFIG.general.messageStructureLength);
        allowRightClick = nbt.getBoolean("right");
        status = nbt.getBoolean("status");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putString("text", text);
        nbt.putBoolean("right", allowRightClick);
        nbt.putBoolean("status", status);
    }
    
    public Component message() {
        try {
            return Component.Serializer.fromJson(text);
        } catch (Exception exception) {
            return Component.literal(text);
        }
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("message")) {
            Level level = getStructureLevel();
            if (level.isClientSide)
                return;
            
            final LevelChunk chunk = level.getChunkAt(getStructurePos());
            if (chunk != null)
                ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(x -> x.sendSystemMessage(message(), status));
        }
    }
    
}
