package com.creativemd.littletiles.common.blocks;

import java.util.List;

import com.creativemd.littletiles.LittleTiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockLTColored extends Block{

	public BlockLTColored() {
		super(Material.rock);
		this.setBlockTextureName(LittleTiles.modid + ":LTColored0");
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	public static final String[] subBlocks = new String[]{"clean", "floor", "grainybig", "grainy", "grainylow", "brick", "bordered", "brickBig", "structured", "brokenBrickBig", "clay"};
	
	@SideOnly(Side.CLIENT)
	public IIcon[] textures;
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
		for (int i = 0; i < subBlocks.length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
    }
	
	@Override
	public int damageDropped(int meta)
	{
		return meta;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister registry)
    {
        super.registerBlockIcons(registry);
        textures = new IIcon[subBlocks.length];
        for (int i = 0; i < subBlocks.length; i++) {
        	 textures[i] = registry.registerIcon(LittleTiles.modid + ":LTColored" + i);
		}
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
       if(meta > 0 && meta < subBlocks.length)
    	   return textures[meta];
       return super.getIcon(side, meta);
    }

}
