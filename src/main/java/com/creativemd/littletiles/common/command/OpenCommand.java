package com.creativemd.littletiles.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.structure.LittleDoorBase;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OpenCommand extends CommandBase {
	
	@Override
    public String getName()
    {
        return "lt-open";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    @Override
    public String getUsage(ICommandSender sender)
    {
        return "commands.open.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length != 3)
            throw new WrongUsageException("commands.open.usage", new Object[0]);
        
        BlockPos blockpos = parseBlockPos(sender, args, 0, false);
        World world = sender.getEntityWorld();
        
        TileEntity tileEntity = world.getTileEntity(blockpos);;
        if(tileEntity instanceof TileEntityLittleTiles)
        {
        	List<LittleDoorBase> doors = new ArrayList<>();
        	for (LittleTile tile : ((TileEntityLittleTiles) tileEntity).getTiles()) {
				if(tile.isStructureBlock && tile.isLoaded() && tile.structure instanceof LittleDoorBase && !doors.contains(tile.structure))
					if(tile.structure.hasLoaded())
						doors.add((LittleDoorBase) tile.structure);
					else
						notifyCommandListener(sender, this, "commands.open.notloaded");
			}
        	
        	for (LittleDoorBase door : doors) {
				door.activate(world, null, door.getDefaultRotation(), door.getMainTile().te.getPos());
			}
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length > 0 && args.length <= 3)
        	return getTabCompletionCoordinate(args, 0, targetPos);
        
        return Collections.emptyList();
    }
}
