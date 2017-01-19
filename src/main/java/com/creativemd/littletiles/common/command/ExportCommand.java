package com.creativemd.littletiles.common.command;

import com.creativemd.creativecore.gui.opener.GuiHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class ExportCommand extends CommandBase {

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		GuiHandler.openGui("lt-export", new NBTTagCompound(), (EntityPlayer) sender.getCommandSenderEntity());
	}

	@Override
	public String getName() {
		return "lt-export";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "export little structures";
	}

}
