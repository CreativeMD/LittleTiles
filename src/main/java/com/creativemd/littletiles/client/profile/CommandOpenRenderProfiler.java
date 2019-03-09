package com.creativemd.littletiles.client.profile;

import com.creativemd.creativecore.common.gui.opener.GuiHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class CommandOpenRenderProfiler extends CommandBase {
	
	@Override
	public String getName() {
		return "rendering-profiler";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "Opens a profiler gui";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		GuiHandler.openGui("rendering-handler", new NBTTagCompound());
	}
	
}
