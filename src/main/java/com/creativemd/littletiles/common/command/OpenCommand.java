package com.creativemd.littletiles.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OpenCommand extends CommandBase {
	
	@Override
	public String getName() {
		return "lt-open";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.open.usage";
	}
	
	protected boolean checkStructureName(LittleStructure structure, String[] args) {
		if (args.length > 3) {
			for (int i = 3; i < args.length; i++)
				if (structure.name != null && structure.name.equalsIgnoreCase(args[i]))
					return true;
			return false;
		}
		return true;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 3)
			throw new WrongUsageException("commands.open.usage", new Object[0]);
		
		BlockPos blockpos = parseBlockPos(sender, args, 0, false);
		World world = sender.getEntityWorld();
		
		List<LittleDoor> doors = new ArrayList<>();
		for (LittleDoor door : WorldAnimationHandler.getHandler(world).findAnimations(blockpos))
			if (checkStructureName(door, args))
				doors.add(door);
			
		TileEntity tileEntity = world.getTileEntity(blockpos);
		if (tileEntity instanceof TileEntityLittleTiles) {
			for (LittleTile tile : ((TileEntityLittleTiles) tileEntity)) {
				if (!tile.isConnectedToStructure())
					continue;
				
				LittleStructure structure = tile.connection.getStructure(tile.te.getWorld());
				if (structure instanceof LittleDoor) {
					structure = ((LittleDoor) structure).getParentDoor();
					if (checkStructureName(structure, args) && !doors.contains(structure)) {
						if (structure.load())
							doors.add((LittleDoor) structure);
						else
							notifyCommandListener(sender, this, "commands.open.notloaded");
					}
				}
			}
		}
		
		for (LittleDoor door : doors) {
			try {
				door.activate(null, null, null, true);
			} catch (LittleActionException e) {}
		}
		
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length > 0 && args.length <= 3)
			return getTabCompletionCoordinate(args, 0, targetPos);
		
		return Collections.emptyList();
	}
}
