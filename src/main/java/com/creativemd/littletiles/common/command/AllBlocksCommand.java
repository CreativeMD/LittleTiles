package com.creativemd.littletiles.common.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.FMLCorePlugin;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.server.FMLServerHandler;

public class AllBlocksCommand extends CommandBase {

    @Override
    public String getName() {
        return "lt-allblocks";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.allblocks.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        File file = new File(server.getDataDirectory(), "littletiles-blocks.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (IBlockState blockState : Block.BLOCK_STATE_IDS) {
                if(!LittleAction.isBlockValid(blockState))
                    continue;
                int meta = blockState.getBlock().getMetaFromState(blockState);
                String name = blockState.getBlock().getRegistryName().toString() + (meta != 0 ? ":" + meta : "");
                writer.write(name + "§" + blockState.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
