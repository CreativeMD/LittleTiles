package com.creativemd.littletiles.common.blocks;

import com.creativemd.littletiles.LittleTiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.IBlockAccess;

public class BlockLTColored extends Block{

	public BlockLTColored() {
		super(Material.iron);
		this.setBlockTextureName(LittleTiles.modid + ":LTColored");
		setCreativeTab(CreativeTabs.tabTools);
	}

}
