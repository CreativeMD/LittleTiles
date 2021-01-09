package com.creativemd.littletiles.common.command;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ToVanillaCommand extends CommandBase {
    
    @Override
    public String getName() {
        return "lt-tovanilla";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.tovanilla.usage";
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        List<TileEntityLittleTiles> blocks = new ArrayList<>();
        for (TileEntity te : world.loadedTileEntityList)
            if (te instanceof TileEntityLittleTiles)
                blocks.add((TileEntityLittleTiles) te);
        System.out.println("Attempting to convert " + blocks.size() + " blocks!");
        int converted = 0;
        int i = 0;
        for (TileEntityLittleTiles te : blocks) {
            if (te.convertBlockToVanilla())
                converted++;
            i++;
            if (i % 50 == 0)
                System.out.println("Processed " + i + "/" + blocks.size() + " and converted " + converted);
        }
        System.out.println("Converted " + converted + " blocks");
    }
    
}
