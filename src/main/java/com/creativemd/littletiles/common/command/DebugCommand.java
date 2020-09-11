package com.creativemd.littletiles.common.command;

import com.creativemd.littletiles.client.render.overlay.LittleTilesProfilerOverlay;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class DebugCommand extends CommandBase {
	
	@Override
	public String getName() {
		return "lt-debug";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "/lt-debug shows special debugging for LT";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (LittleTilesProfilerOverlay.isActive())
			LittleTilesProfilerOverlay.stop();
		else
			LittleTilesProfilerOverlay.start();
	}
	
}
