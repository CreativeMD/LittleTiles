package com.creativemd.littletiles.client.render;

import com.creativemd.creativecore.client.rendering.ExtendedRenderBlocks;
import com.creativemd.littletiles.client.render.LittleBlockVertex.LittleVertex;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockWall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.src.FMLRenderAccessLibrary;
import net.minecraft.util.IIcon;

public class LittleThreadedRenderer extends ExtendedRenderBlocks{

	public LittleThreadedRenderer(RenderBlocks renderer) {
		super(renderer);
		//aoBrightnessXYNN = renderer.aoBrightnessXYNN
	}
	
	@Override
	public boolean renderBlockByRenderType(Block p_147805_1_, int p_147805_2_, int p_147805_3_, int p_147805_4_)
    {
        int l = p_147805_1_.getRenderType();

        if (l == -1)
        {
            return false;
        }
        else
        {
            p_147805_1_.setBlockBoundsBasedOnState(this.blockAccess, p_147805_2_, p_147805_3_, p_147805_4_);
            this.setRenderBoundsFromBlock(p_147805_1_);

            switch (l)
            {
            //regex: ' : \(l == ([\d]+) \?' replace: ';\ncase \1: return' ::: IMPORTANT: REMEMBER THIS ON FIRST line!
            case 0 : return this.renderStandardBlock(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 4: return this.renderBlockLiquid(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 31: return this.renderBlockLog(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 1: return this.renderCrossedSquares(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 40: return this.renderBlockDoublePlant((BlockDoublePlant)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 2: return this.renderBlockTorch(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 20: return this.renderBlockVine(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 11: return this.renderBlockFence((BlockFence)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 39: return this.renderBlockQuartz(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 5: return this.renderBlockRedstoneWire(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 13: return this.renderBlockCactus(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 9: return this.renderBlockMinecartTrack((BlockRailBase)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 19: return this.renderBlockStem(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 23: return this.renderBlockLilyPad(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 6: return this.renderBlockCrops(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 3: return this.renderBlockFire((BlockFire)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 8: return this.renderBlockLadder(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 7: return this.renderBlockDoor(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 10: return this.renderBlockStairs((BlockStairs)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 27: return this.renderBlockDragonEgg((BlockDragonEgg)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 32: return this.renderBlockWall((BlockWall)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 12: return this.renderBlockLever(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 29: return this.renderBlockTripWireSource(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 30: return this.renderBlockTripWire(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 14: return this.renderBlockBed(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 15: return this.renderBlockRepeater((BlockRedstoneRepeater)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 36: return this.renderBlockRedstoneDiode((BlockRedstoneDiode)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 37: return this.renderBlockRedstoneComparator((BlockRedstoneComparator)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 16: return this.renderPistonBase(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_, false) ;
            case 17: return this.renderPistonExtension(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_, true) ;
            case 18: return this.renderBlockPane((BlockPane)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 41: return this.renderBlockStainedGlassPane(p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 21: return this.renderBlockFenceGate((BlockFenceGate)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 24: return this.renderBlockCauldron((BlockCauldron)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 33: return this.renderBlockFlowerpot((BlockFlowerPot)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 35: return this.renderBlockAnvil((BlockAnvil)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 25: return this.renderBlockBrewingStand((BlockBrewingStand)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 26: return this.renderBlockEndPortalFrame((BlockEndPortalFrame)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 28: return this.renderBlockCocoa((BlockCocoa)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 34: return this.renderBlockBeacon((BlockBeacon)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_) ;
            case 38: return this.renderBlockHopper((BlockHopper)p_147805_1_, p_147805_2_, p_147805_3_, p_147805_4_);
            default: return FMLRenderAccessLibrary.renderWorldBlock(this, blockAccess, p_147805_2_, p_147805_3_, p_147805_4_, p_147805_1_, l);
            }
        }
    }
	
	/*@Override
	public boolean renderStandardBlock(Block p_147784_1_, int p_147784_2_, int p_147784_3_, int p_147784_4_)
    {
        int l = p_147784_1_.colorMultiplier(this.blockAccess, p_147784_2_, p_147784_3_, p_147784_4_);
        float f = (float)(l >> 16 & 255) / 255.0F;
        float f1 = (float)(l >> 8 & 255) / 255.0F;
        float f2 = (float)(l & 255) / 255.0F;

        if (EntityRenderer.anaglyphEnable)
        {
            float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
            float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
            float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
            f = f3;
            f1 = f4;
            f2 = f5;
        }

        return Minecraft.isAmbientOcclusionEnabled() && p_147784_1_.getLightValue() == 0 ? (this.partialRenderBounds ? this.renderStandardBlockWithAmbientOcclusionPartial(p_147784_1_, p_147784_2_, p_147784_3_, p_147784_4_, f, f1, f2) : this.renderStandardBlockWithAmbientOcclusion(p_147784_1_, p_147784_2_, p_147784_3_, p_147784_4_, f, f1, f2)) : this.renderStandardBlockWithColorMultiplier(p_147784_1_, p_147784_2_, p_147784_3_, p_147784_4_, f, f1, f2);
    }*/
	
	@Override
	public boolean renderStandardBlockWithAmbientOcclusion(Block p_147751_1_, int p_147751_2_, int p_147751_3_, int p_147751_4_, float p_147751_5_, float p_147751_6_, float p_147751_7_)
    {
        this.enableAO = true;
        boolean flag = false;
        float f3 = 0.0F;
        float f4 = 0.0F;
        float f5 = 0.0F;
        float f6 = 0.0F;
        boolean flag1 = true;
        int l = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_);
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(983055);

        if (this.getBlockIcon(p_147751_1_).getIconName().equals("grass_top"))
        {
            flag1 = false;
        }
        else if (this.hasOverrideBlockTexture())
        {
            flag1 = false;
        }

        boolean flag2;
        boolean flag3;
        boolean flag4;
        boolean flag5;
        int i1;
        float f7;

        if (this.renderAllFaces || p_147751_1_.shouldSideBeRendered(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_, 0))
        {
            if (this.renderMinY <= 0.0D)
            {
                --p_147751_3_;
            }

            this.aoBrightnessXYNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_);
            this.aoBrightnessYZNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ - 1);
            this.aoBrightnessYZNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ + 1);
            this.aoBrightnessXYPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_);
            this.aoLightValueScratchXYNN = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYPN = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            flag2 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ - 1, p_147751_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ - 1, p_147751_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ - 1).getCanBlockGrass();

            if (!flag5 && !flag3)
            {
                this.aoLightValueScratchXYZNNN = this.aoLightValueScratchXYNN;
                this.aoBrightnessXYZNNN = this.aoBrightnessXYNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNN = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_ - 1);
            }

            if (!flag4 && !flag3)
            {
                this.aoLightValueScratchXYZNNP = this.aoLightValueScratchXYNN;
                this.aoBrightnessXYZNNP = this.aoBrightnessXYNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNP = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_ + 1);
            }

            if (!flag5 && !flag2)
            {
                this.aoLightValueScratchXYZPNN = this.aoLightValueScratchXYPN;
                this.aoBrightnessXYZPNN = this.aoBrightnessXYPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNN = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_ - 1);
            }

            if (!flag4 && !flag2)
            {
                this.aoLightValueScratchXYZPNP = this.aoLightValueScratchXYPN;
                this.aoBrightnessXYZPNP = this.aoBrightnessXYPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNP = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_ + 1);
            }

            if (this.renderMinY <= 0.0D)
            {
                ++p_147751_3_;
            }

            i1 = l;

            if (this.renderMinY <= 0.0D || !this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_).isOpaqueCube())
            {
                i1 = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_);
            }

            f7 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
            f3 = (this.aoLightValueScratchXYZNNP + this.aoLightValueScratchXYNN + this.aoLightValueScratchYZNP + f7) / 4.0F;
            f6 = (this.aoLightValueScratchYZNP + f7 + this.aoLightValueScratchXYZPNP + this.aoLightValueScratchXYPN) / 4.0F;
            f5 = (f7 + this.aoLightValueScratchYZNN + this.aoLightValueScratchXYPN + this.aoLightValueScratchXYZPNN) / 4.0F;
            f4 = (this.aoLightValueScratchXYNN + this.aoLightValueScratchXYZNNN + f7 + this.aoLightValueScratchYZNN) / 4.0F;
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessXYZNNP, this.aoBrightnessXYNN, this.aoBrightnessYZNP, i1);
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessYZNP, this.aoBrightnessXYZPNP, this.aoBrightnessXYPN, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessYZNN, this.aoBrightnessXYPN, this.aoBrightnessXYZPNN, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessXYNN, this.aoBrightnessXYZNNN, this.aoBrightnessYZNN, i1);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147751_5_ * 0.5F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147751_6_ * 0.5F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147751_7_ * 0.5F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.5F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.5F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.5F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            this.renderFaceYNeg(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, this.getBlockIcon(p_147751_1_, this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_, 0));
            flag = true;
        }

        if (this.renderAllFaces || p_147751_1_.shouldSideBeRendered(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_, 1))
        {
            if (this.renderMaxY >= 1.0D)
            {
                ++p_147751_3_;
            }

            this.aoBrightnessXYNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_);
            this.aoBrightnessXYPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_);
            this.aoBrightnessYZPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ - 1);
            this.aoBrightnessYZPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ + 1);
            this.aoLightValueScratchXYNP = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYPP = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
            flag2 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ + 1, p_147751_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ + 1, p_147751_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ - 1).getCanBlockGrass();

            if (!flag5 && !flag3)
            {
                this.aoLightValueScratchXYZNPN = this.aoLightValueScratchXYNP;
                this.aoBrightnessXYZNPN = this.aoBrightnessXYNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPN = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_ - 1);
            }

            if (!flag5 && !flag2)
            {
                this.aoLightValueScratchXYZPPN = this.aoLightValueScratchXYPP;
                this.aoBrightnessXYZPPN = this.aoBrightnessXYPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPN = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_ - 1);
            }

            if (!flag4 && !flag3)
            {
                this.aoLightValueScratchXYZNPP = this.aoLightValueScratchXYNP;
                this.aoBrightnessXYZNPP = this.aoBrightnessXYNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPP = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_ + 1);
            }

            if (!flag4 && !flag2)
            {
                this.aoLightValueScratchXYZPPP = this.aoLightValueScratchXYPP;
                this.aoBrightnessXYZPPP = this.aoBrightnessXYPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPP = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_ + 1);
            }

            if (this.renderMaxY >= 1.0D)
            {
                --p_147751_3_;
            }

            i1 = l;

            if (this.renderMaxY >= 1.0D || !this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_).isOpaqueCube())
            {
                i1 = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_);
            }

            f7 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
            f6 = (this.aoLightValueScratchXYZNPP + this.aoLightValueScratchXYNP + this.aoLightValueScratchYZPP + f7) / 4.0F;
            f3 = (this.aoLightValueScratchYZPP + f7 + this.aoLightValueScratchXYZPPP + this.aoLightValueScratchXYPP) / 4.0F;
            f4 = (f7 + this.aoLightValueScratchYZPN + this.aoLightValueScratchXYPP + this.aoLightValueScratchXYZPPN) / 4.0F;
            f5 = (this.aoLightValueScratchXYNP + this.aoLightValueScratchXYZNPN + f7 + this.aoLightValueScratchYZPN) / 4.0F;
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessXYZNPP, this.aoBrightnessXYNP, this.aoBrightnessYZPP, i1);
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessYZPP, this.aoBrightnessXYZPPP, this.aoBrightnessXYPP, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessYZPN, this.aoBrightnessXYPP, this.aoBrightnessXYZPPN, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessXYNP, this.aoBrightnessXYZNPN, this.aoBrightnessYZPN, i1);
            this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147751_5_;
            this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147751_6_;
            this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147751_7_;
            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            this.renderFaceYPos(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, this.getBlockIcon(p_147751_1_, this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_, 1));
            flag = true;
        }

        IIcon iicon;

        if (this.renderAllFaces || p_147751_1_.shouldSideBeRendered(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ - 1, 2))
        {
            if (this.renderMinZ <= 0.0D)
            {
                --p_147751_4_;
            }

            this.aoLightValueScratchXZNN = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPN = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXZNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_);
            this.aoBrightnessYZNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_);
            this.aoBrightnessYZPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_);
            this.aoBrightnessXZPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_);
            flag2 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ - 1).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ - 1).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ - 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ - 1).getCanBlockGrass();

            if (!flag3 && !flag5)
            {
                this.aoLightValueScratchXYZNNN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNNN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNN = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_ - 1, p_147751_4_);
            }

            if (!flag3 && !flag4)
            {
                this.aoLightValueScratchXYZNPN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNPN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNPN = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_ + 1, p_147751_4_);
            }

            if (!flag2 && !flag5)
            {
                this.aoLightValueScratchXYZPNN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPNN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNN = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_ - 1, p_147751_4_);
            }

            if (!flag2 && !flag4)
            {
                this.aoLightValueScratchXYZPPN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPPN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPPN = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_ + 1, p_147751_4_);
            }

            if (this.renderMinZ <= 0.0D)
            {
                ++p_147751_4_;
            }

            i1 = l;

            if (this.renderMinZ <= 0.0D || !this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ - 1).isOpaqueCube())
            {
                i1 = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ - 1);
            }

            f7 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
            f3 = (this.aoLightValueScratchXZNN + this.aoLightValueScratchXYZNPN + f7 + this.aoLightValueScratchYZPN) / 4.0F;
            f4 = (f7 + this.aoLightValueScratchYZPN + this.aoLightValueScratchXZPN + this.aoLightValueScratchXYZPPN) / 4.0F;
            f5 = (this.aoLightValueScratchYZNN + f7 + this.aoLightValueScratchXYZPNN + this.aoLightValueScratchXZPN) / 4.0F;
            f6 = (this.aoLightValueScratchXYZNNN + this.aoLightValueScratchXZNN + this.aoLightValueScratchYZNN + f7) / 4.0F;
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessXZNN, this.aoBrightnessXYZNPN, this.aoBrightnessYZPN, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessYZPN, this.aoBrightnessXZPN, this.aoBrightnessXYZPPN, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessYZNN, this.aoBrightnessXYZPNN, this.aoBrightnessXZPN, i1);
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessXYZNNN, this.aoBrightnessXZNN, this.aoBrightnessYZNN, i1);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147751_5_ * 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147751_6_ * 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147751_7_ * 0.8F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.8F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147751_1_, this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_, 2);
            this.renderFaceZNeg(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, iicon);

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147751_5_;
                this.colorRedBottomLeft *= p_147751_5_;
                this.colorRedBottomRight *= p_147751_5_;
                this.colorRedTopRight *= p_147751_5_;
                this.colorGreenTopLeft *= p_147751_6_;
                this.colorGreenBottomLeft *= p_147751_6_;
                this.colorGreenBottomRight *= p_147751_6_;
                this.colorGreenTopRight *= p_147751_6_;
                this.colorBlueTopLeft *= p_147751_7_;
                this.colorBlueBottomLeft *= p_147751_7_;
                this.colorBlueBottomRight *= p_147751_7_;
                this.colorBlueTopRight *= p_147751_7_;
                this.renderFaceZNeg(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (this.renderAllFaces || p_147751_1_.shouldSideBeRendered(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ + 1, 3))
        {
            if (this.renderMaxZ >= 1.0D)
            {
                ++p_147751_4_;
            }

            this.aoLightValueScratchXZNP = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPP = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXZNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_);
            this.aoBrightnessXZPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_);
            this.aoBrightnessYZNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_);
            this.aoBrightnessYZPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_);
            flag2 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ + 1).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ + 1).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ + 1).getCanBlockGrass();

            if (!flag3 && !flag5)
            {
                this.aoLightValueScratchXYZNNP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNNP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNNP = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_ - 1, p_147751_4_);
            }

            if (!flag3 && !flag4)
            {
                this.aoLightValueScratchXYZNPP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNPP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPP = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_ + 1, p_147751_4_);
            }

            if (!flag2 && !flag5)
            {
                this.aoLightValueScratchXYZPNP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPNP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPNP = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_ - 1, p_147751_4_);
            }

            if (!flag2 && !flag4)
            {
                this.aoLightValueScratchXYZPPP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPPP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPP = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_ + 1, p_147751_4_);
            }

            if (this.renderMaxZ >= 1.0D)
            {
                --p_147751_4_;
            }

            i1 = l;

            if (this.renderMaxZ >= 1.0D || !this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ + 1).isOpaqueCube())
            {
                i1 = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ + 1);
            }

            f7 = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
            f3 = (this.aoLightValueScratchXZNP + this.aoLightValueScratchXYZNPP + f7 + this.aoLightValueScratchYZPP) / 4.0F;
            f6 = (f7 + this.aoLightValueScratchYZPP + this.aoLightValueScratchXZPP + this.aoLightValueScratchXYZPPP) / 4.0F;
            f5 = (this.aoLightValueScratchYZNP + f7 + this.aoLightValueScratchXYZPNP + this.aoLightValueScratchXZPP) / 4.0F;
            f4 = (this.aoLightValueScratchXYZNNP + this.aoLightValueScratchXZNP + this.aoLightValueScratchYZNP + f7) / 4.0F;
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessXZNP, this.aoBrightnessXYZNPP, this.aoBrightnessYZPP, i1);
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessYZPP, this.aoBrightnessXZPP, this.aoBrightnessXYZPPP, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessYZNP, this.aoBrightnessXYZPNP, this.aoBrightnessXZPP, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessXYZNNP, this.aoBrightnessXZNP, this.aoBrightnessYZNP, i1);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147751_5_ * 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147751_6_ * 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147751_7_ * 0.8F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.8F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147751_1_, this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_, 3);
            this.renderFaceZPos(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, this.getBlockIcon(p_147751_1_, this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_, 3));

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147751_5_;
                this.colorRedBottomLeft *= p_147751_5_;
                this.colorRedBottomRight *= p_147751_5_;
                this.colorRedTopRight *= p_147751_5_;
                this.colorGreenTopLeft *= p_147751_6_;
                this.colorGreenBottomLeft *= p_147751_6_;
                this.colorGreenBottomRight *= p_147751_6_;
                this.colorGreenTopRight *= p_147751_6_;
                this.colorBlueTopLeft *= p_147751_7_;
                this.colorBlueBottomLeft *= p_147751_7_;
                this.colorBlueBottomRight *= p_147751_7_;
                this.colorBlueTopRight *= p_147751_7_;
                this.renderFaceZPos(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (this.renderAllFaces || p_147751_1_.shouldSideBeRendered(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_, 4))
        {
            if (this.renderMinX <= 0.0D)
            {
                --p_147751_2_;
            }

            this.aoLightValueScratchXYNN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZNN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZNP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYNP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXYNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_);
            this.aoBrightnessXZNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ - 1);
            this.aoBrightnessXZNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ + 1);
            this.aoBrightnessXYNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_);
            flag2 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ + 1, p_147751_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_ - 1, p_147751_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ - 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_ + 1).getCanBlockGrass();

            if (!flag4 && !flag3)
            {
                this.aoLightValueScratchXYZNNN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNNN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_ - 1);
            }

            if (!flag5 && !flag3)
            {
                this.aoLightValueScratchXYZNNP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNNP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNNP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_ + 1);
            }

            if (!flag4 && !flag2)
            {
                this.aoLightValueScratchXYZNPN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNPN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNPN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_ - 1);
            }

            if (!flag5 && !flag2)
            {
                this.aoLightValueScratchXYZNPP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNPP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_ + 1);
            }

            if (this.renderMinX <= 0.0D)
            {
                ++p_147751_2_;
            }

            i1 = l;

            if (this.renderMinX <= 0.0D || !this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_).isOpaqueCube())
            {
                i1 = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ - 1, p_147751_3_, p_147751_4_);
            }

            f7 = this.blockAccess.getBlock(p_147751_2_ - 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            f6 = (this.aoLightValueScratchXYNN + this.aoLightValueScratchXYZNNP + f7 + this.aoLightValueScratchXZNP) / 4.0F;
            f3 = (f7 + this.aoLightValueScratchXZNP + this.aoLightValueScratchXYNP + this.aoLightValueScratchXYZNPP) / 4.0F;
            f4 = (this.aoLightValueScratchXZNN + f7 + this.aoLightValueScratchXYZNPN + this.aoLightValueScratchXYNP) / 4.0F;
            f5 = (this.aoLightValueScratchXYZNNN + this.aoLightValueScratchXYNN + this.aoLightValueScratchXZNN + f7) / 4.0F;
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessXYNN, this.aoBrightnessXYZNNP, this.aoBrightnessXZNP, i1);
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessXZNP, this.aoBrightnessXYNP, this.aoBrightnessXYZNPP, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessXZNN, this.aoBrightnessXYZNPN, this.aoBrightnessXYNP, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessXYZNNN, this.aoBrightnessXYNN, this.aoBrightnessXZNN, i1);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147751_5_ * 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147751_6_ * 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147751_7_ * 0.6F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.6F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147751_1_, this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_, 4);
            this.renderFaceXNeg(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, iicon);

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147751_5_;
                this.colorRedBottomLeft *= p_147751_5_;
                this.colorRedBottomRight *= p_147751_5_;
                this.colorRedTopRight *= p_147751_5_;
                this.colorGreenTopLeft *= p_147751_6_;
                this.colorGreenBottomLeft *= p_147751_6_;
                this.colorGreenBottomRight *= p_147751_6_;
                this.colorGreenTopRight *= p_147751_6_;
                this.colorBlueTopLeft *= p_147751_7_;
                this.colorBlueBottomLeft *= p_147751_7_;
                this.colorBlueBottomRight *= p_147751_7_;
                this.colorBlueTopRight *= p_147751_7_;
                this.renderFaceXNeg(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (this.renderAllFaces || p_147751_1_.shouldSideBeRendered(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_, 5))
        {
            if (this.renderMaxX >= 1.0D)
            {
                ++p_147751_2_;
            }

            this.aoLightValueScratchXYPN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_, p_147751_4_ + 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYPP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXYPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_);
            this.aoBrightnessXZPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ - 1);
            this.aoBrightnessXZPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_ + 1);
            this.aoBrightnessXYPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_);
            flag2 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ + 1, p_147751_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_ - 1, p_147751_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_ - 1).getCanBlockGrass();

            if (!flag3 && !flag5)
            {
                this.aoLightValueScratchXYZPNN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPNN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_ - 1);
            }

            if (!flag3 && !flag4)
            {
                this.aoLightValueScratchXYZPNP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPNP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPNP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ - 1, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ - 1, p_147751_4_ + 1);
            }

            if (!flag2 && !flag5)
            {
                this.aoLightValueScratchXYZPPN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPPN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPPN = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPN = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_ - 1);
            }

            if (!flag2 && !flag4)
            {
                this.aoLightValueScratchXYZPPP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPPP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPP = this.blockAccess.getBlock(p_147751_2_, p_147751_3_ + 1, p_147751_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPP = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_, p_147751_3_ + 1, p_147751_4_ + 1);
            }

            if (this.renderMaxX >= 1.0D)
            {
                --p_147751_2_;
            }

            i1 = l;

            if (this.renderMaxX >= 1.0D || !this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_).isOpaqueCube())
            {
                i1 = p_147751_1_.getMixedBrightnessForBlock(this.blockAccess, p_147751_2_ + 1, p_147751_3_, p_147751_4_);
            }

            f7 = this.blockAccess.getBlock(p_147751_2_ + 1, p_147751_3_, p_147751_4_).getAmbientOcclusionLightValue();
            f3 = (this.aoLightValueScratchXYPN + this.aoLightValueScratchXYZPNP + f7 + this.aoLightValueScratchXZPP) / 4.0F;
            f4 = (this.aoLightValueScratchXYZPNN + this.aoLightValueScratchXYPN + this.aoLightValueScratchXZPN + f7) / 4.0F;
            f5 = (this.aoLightValueScratchXZPN + f7 + this.aoLightValueScratchXYZPPN + this.aoLightValueScratchXYPP) / 4.0F;
            f6 = (f7 + this.aoLightValueScratchXZPP + this.aoLightValueScratchXYPP + this.aoLightValueScratchXYZPPP) / 4.0F;
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessXYPN, this.aoBrightnessXYZPNP, this.aoBrightnessXZPP, i1);
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessXZPP, this.aoBrightnessXYPP, this.aoBrightnessXYZPPP, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessXZPN, this.aoBrightnessXYZPPN, this.aoBrightnessXYPP, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessXYZPNN, this.aoBrightnessXYPN, this.aoBrightnessXZPN, i1);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147751_5_ * 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147751_6_ * 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147751_7_ * 0.6F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.6F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147751_1_, this.blockAccess, p_147751_2_, p_147751_3_, p_147751_4_, 5);
            this.renderFaceXPos(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, iicon);

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147751_5_;
                this.colorRedBottomLeft *= p_147751_5_;
                this.colorRedBottomRight *= p_147751_5_;
                this.colorRedTopRight *= p_147751_5_;
                this.colorGreenTopLeft *= p_147751_6_;
                this.colorGreenBottomLeft *= p_147751_6_;
                this.colorGreenBottomRight *= p_147751_6_;
                this.colorGreenTopRight *= p_147751_6_;
                this.colorBlueTopLeft *= p_147751_7_;
                this.colorBlueBottomLeft *= p_147751_7_;
                this.colorBlueBottomRight *= p_147751_7_;
                this.colorBlueTopRight *= p_147751_7_;
                this.renderFaceXPos(p_147751_1_, (double)p_147751_2_, (double)p_147751_3_, (double)p_147751_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        this.enableAO = false;
        return flag;
    }
	
	@Override
	public boolean renderStandardBlockWithAmbientOcclusionPartial(Block p_147808_1_, int p_147808_2_, int p_147808_3_, int p_147808_4_, float p_147808_5_, float p_147808_6_, float p_147808_7_)
    {
        this.enableAO = true;
        boolean flag = false;
        float f3 = 0.0F;
        float f4 = 0.0F;
        float f5 = 0.0F;
        float f6 = 0.0F;
        boolean flag1 = true;
        int l = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_);
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(983055);

        if (this.getBlockIcon(p_147808_1_).getIconName().equals("grass_top"))
        {
            flag1 = false;
        }
        else if (this.hasOverrideBlockTexture())
        {
            flag1 = false;
        }

        boolean flag2;
        boolean flag3;
        boolean flag4;
        boolean flag5;
        int i1;
        float f7;

        if (this.renderAllFaces || p_147808_1_.shouldSideBeRendered(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_, 0))
        {
            if (this.renderMinY <= 0.0D)
            {
                --p_147808_3_;
            }

            this.aoBrightnessXYNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_);
            this.aoBrightnessYZNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ - 1);
            this.aoBrightnessYZNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ + 1);
            this.aoBrightnessXYPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_);
            this.aoLightValueScratchXYNN = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYPN = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            flag2 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ - 1, p_147808_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ - 1, p_147808_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ - 1).getCanBlockGrass();

            if (!flag5 && !flag3)
            {
                this.aoLightValueScratchXYZNNN = this.aoLightValueScratchXYNN;
                this.aoBrightnessXYZNNN = this.aoBrightnessXYNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNN = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_ - 1);
            }

            if (!flag4 && !flag3)
            {
                this.aoLightValueScratchXYZNNP = this.aoLightValueScratchXYNN;
                this.aoBrightnessXYZNNP = this.aoBrightnessXYNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNP = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_ + 1);
            }

            if (!flag5 && !flag2)
            {
                this.aoLightValueScratchXYZPNN = this.aoLightValueScratchXYPN;
                this.aoBrightnessXYZPNN = this.aoBrightnessXYPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNN = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_ - 1);
            }

            if (!flag4 && !flag2)
            {
                this.aoLightValueScratchXYZPNP = this.aoLightValueScratchXYPN;
                this.aoBrightnessXYZPNP = this.aoBrightnessXYPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNP = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_ + 1);
            }

            if (this.renderMinY <= 0.0D)
            {
                ++p_147808_3_;
            }

            i1 = l;

            if (this.renderMinY <= 0.0D || !this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_).isOpaqueCube())
            {
                i1 = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_);
            }

            f7 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
            f3 = (this.aoLightValueScratchXYZNNP + this.aoLightValueScratchXYNN + this.aoLightValueScratchYZNP + f7) / 4.0F;
            f6 = (this.aoLightValueScratchYZNP + f7 + this.aoLightValueScratchXYZPNP + this.aoLightValueScratchXYPN) / 4.0F;
            f5 = (f7 + this.aoLightValueScratchYZNN + this.aoLightValueScratchXYPN + this.aoLightValueScratchXYZPNN) / 4.0F;
            f4 = (this.aoLightValueScratchXYNN + this.aoLightValueScratchXYZNNN + f7 + this.aoLightValueScratchYZNN) / 4.0F;
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessXYZNNP, this.aoBrightnessXYNN, this.aoBrightnessYZNP, i1);
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessYZNP, this.aoBrightnessXYZPNP, this.aoBrightnessXYPN, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessYZNN, this.aoBrightnessXYPN, this.aoBrightnessXYZPNN, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessXYNN, this.aoBrightnessXYZNNN, this.aoBrightnessYZNN, i1);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147808_5_ * 0.5F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147808_6_ * 0.5F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147808_7_ * 0.5F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.5F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.5F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.5F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            this.renderFaceYNeg(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, this.getBlockIcon(p_147808_1_, this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_, 0));
            flag = true;
        }

        if (this.renderAllFaces || p_147808_1_.shouldSideBeRendered(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_, 1))
        {
            if (this.renderMaxY >= 1.0D)
            {
                ++p_147808_3_;
            }

            this.aoBrightnessXYNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_);
            this.aoBrightnessXYPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_);
            this.aoBrightnessYZPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ - 1);
            this.aoBrightnessYZPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ + 1);
            this.aoLightValueScratchXYNP = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYPP = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
            flag2 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ + 1, p_147808_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ + 1, p_147808_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ - 1).getCanBlockGrass();

            if (!flag5 && !flag3)
            {
                this.aoLightValueScratchXYZNPN = this.aoLightValueScratchXYNP;
                this.aoBrightnessXYZNPN = this.aoBrightnessXYNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPN = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_ - 1);
            }

            if (!flag5 && !flag2)
            {
                this.aoLightValueScratchXYZPPN = this.aoLightValueScratchXYPP;
                this.aoBrightnessXYZPPN = this.aoBrightnessXYPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPN = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_ - 1);
            }

            if (!flag4 && !flag3)
            {
                this.aoLightValueScratchXYZNPP = this.aoLightValueScratchXYNP;
                this.aoBrightnessXYZNPP = this.aoBrightnessXYNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPP = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_ + 1);
            }

            if (!flag4 && !flag2)
            {
                this.aoLightValueScratchXYZPPP = this.aoLightValueScratchXYPP;
                this.aoBrightnessXYZPPP = this.aoBrightnessXYPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPP = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_ + 1);
            }

            if (this.renderMaxY >= 1.0D)
            {
                --p_147808_3_;
            }

            i1 = l;

            if (this.renderMaxY >= 1.0D || !this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_).isOpaqueCube())
            {
                i1 = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_);
            }

            f7 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
            f6 = (this.aoLightValueScratchXYZNPP + this.aoLightValueScratchXYNP + this.aoLightValueScratchYZPP + f7) / 4.0F;
            f3 = (this.aoLightValueScratchYZPP + f7 + this.aoLightValueScratchXYZPPP + this.aoLightValueScratchXYPP) / 4.0F;
            f4 = (f7 + this.aoLightValueScratchYZPN + this.aoLightValueScratchXYPP + this.aoLightValueScratchXYZPPN) / 4.0F;
            f5 = (this.aoLightValueScratchXYNP + this.aoLightValueScratchXYZNPN + f7 + this.aoLightValueScratchYZPN) / 4.0F;
            this.brightnessTopRight = this.getAoBrightness(this.aoBrightnessXYZNPP, this.aoBrightnessXYNP, this.aoBrightnessYZPP, i1);
            this.brightnessTopLeft = this.getAoBrightness(this.aoBrightnessYZPP, this.aoBrightnessXYZPPP, this.aoBrightnessXYPP, i1);
            this.brightnessBottomLeft = this.getAoBrightness(this.aoBrightnessYZPN, this.aoBrightnessXYPP, this.aoBrightnessXYZPPN, i1);
            this.brightnessBottomRight = this.getAoBrightness(this.aoBrightnessXYNP, this.aoBrightnessXYZNPN, this.aoBrightnessYZPN, i1);
            this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147808_5_;
            this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147808_6_;
            this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147808_7_;
            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            this.renderFaceYPos(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, this.getBlockIcon(p_147808_1_, this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_, 1));
            flag = true;
        }

        float f8;
        float f9;
        float f10;
        float f11;
        int j1;
        int k1;
        int l1;
        int i2;
        IIcon iicon;

        if (this.renderAllFaces || p_147808_1_.shouldSideBeRendered(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ - 1, 2))
        {
            if (this.renderMinZ <= 0.0D)
            {
                --p_147808_4_;
            }

            this.aoLightValueScratchXZNN = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPN = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXZNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_);
            this.aoBrightnessYZNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_);
            this.aoBrightnessYZPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_);
            this.aoBrightnessXZPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_);
            flag2 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ - 1).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ - 1).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ - 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ - 1).getCanBlockGrass();

            if (!flag3 && !flag5)
            {
                this.aoLightValueScratchXYZNNN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNNN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNN = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_ - 1, p_147808_4_);
            }

            if (!flag3 && !flag4)
            {
                this.aoLightValueScratchXYZNPN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNPN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNPN = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_ + 1, p_147808_4_);
            }

            if (!flag2 && !flag5)
            {
                this.aoLightValueScratchXYZPNN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPNN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNN = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_ - 1, p_147808_4_);
            }

            if (!flag2 && !flag4)
            {
                this.aoLightValueScratchXYZPPN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPPN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPPN = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_ + 1, p_147808_4_);
            }

            if (this.renderMinZ <= 0.0D)
            {
                ++p_147808_4_;
            }

            i1 = l;

            if (this.renderMinZ <= 0.0D || !this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ - 1).isOpaqueCube())
            {
                i1 = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ - 1);
            }

            f7 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
            f8 = (this.aoLightValueScratchXZNN + this.aoLightValueScratchXYZNPN + f7 + this.aoLightValueScratchYZPN) / 4.0F;
            f9 = (f7 + this.aoLightValueScratchYZPN + this.aoLightValueScratchXZPN + this.aoLightValueScratchXYZPPN) / 4.0F;
            f10 = (this.aoLightValueScratchYZNN + f7 + this.aoLightValueScratchXYZPNN + this.aoLightValueScratchXZPN) / 4.0F;
            f11 = (this.aoLightValueScratchXYZNNN + this.aoLightValueScratchXZNN + this.aoLightValueScratchYZNN + f7) / 4.0F;
            f3 = (float)((double)f8 * this.renderMaxY * (1.0D - this.renderMinX) + (double)f9 * this.renderMaxY * this.renderMinX + (double)f10 * (1.0D - this.renderMaxY) * this.renderMinX + (double)f11 * (1.0D - this.renderMaxY) * (1.0D - this.renderMinX));
            f4 = (float)((double)f8 * this.renderMaxY * (1.0D - this.renderMaxX) + (double)f9 * this.renderMaxY * this.renderMaxX + (double)f10 * (1.0D - this.renderMaxY) * this.renderMaxX + (double)f11 * (1.0D - this.renderMaxY) * (1.0D - this.renderMaxX));
            f5 = (float)((double)f8 * this.renderMinY * (1.0D - this.renderMaxX) + (double)f9 * this.renderMinY * this.renderMaxX + (double)f10 * (1.0D - this.renderMinY) * this.renderMaxX + (double)f11 * (1.0D - this.renderMinY) * (1.0D - this.renderMaxX));
            f6 = (float)((double)f8 * this.renderMinY * (1.0D - this.renderMinX) + (double)f9 * this.renderMinY * this.renderMinX + (double)f10 * (1.0D - this.renderMinY) * this.renderMinX + (double)f11 * (1.0D - this.renderMinY) * (1.0D - this.renderMinX));
            j1 = this.getAoBrightness(this.aoBrightnessXZNN, this.aoBrightnessXYZNPN, this.aoBrightnessYZPN, i1);
            k1 = this.getAoBrightness(this.aoBrightnessYZPN, this.aoBrightnessXZPN, this.aoBrightnessXYZPPN, i1);
            l1 = this.getAoBrightness(this.aoBrightnessYZNN, this.aoBrightnessXYZPNN, this.aoBrightnessXZPN, i1);
            i2 = this.getAoBrightness(this.aoBrightnessXYZNNN, this.aoBrightnessXZNN, this.aoBrightnessYZNN, i1);
            this.brightnessTopLeft = this.mixAoBrightness(j1, k1, l1, i2, this.renderMaxY * (1.0D - this.renderMinX), this.renderMaxY * this.renderMinX, (1.0D - this.renderMaxY) * this.renderMinX, (1.0D - this.renderMaxY) * (1.0D - this.renderMinX));
            this.brightnessBottomLeft = this.mixAoBrightness(j1, k1, l1, i2, this.renderMaxY * (1.0D - this.renderMaxX), this.renderMaxY * this.renderMaxX, (1.0D - this.renderMaxY) * this.renderMaxX, (1.0D - this.renderMaxY) * (1.0D - this.renderMaxX));
            this.brightnessBottomRight = this.mixAoBrightness(j1, k1, l1, i2, this.renderMinY * (1.0D - this.renderMaxX), this.renderMinY * this.renderMaxX, (1.0D - this.renderMinY) * this.renderMaxX, (1.0D - this.renderMinY) * (1.0D - this.renderMaxX));
            this.brightnessTopRight = this.mixAoBrightness(j1, k1, l1, i2, this.renderMinY * (1.0D - this.renderMinX), this.renderMinY * this.renderMinX, (1.0D - this.renderMinY) * this.renderMinX, (1.0D - this.renderMinY) * (1.0D - this.renderMinX));

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147808_5_ * 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147808_6_ * 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147808_7_ * 0.8F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.8F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147808_1_, this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_, 2);
            this.renderFaceZNeg(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, iicon);

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147808_5_;
                this.colorRedBottomLeft *= p_147808_5_;
                this.colorRedBottomRight *= p_147808_5_;
                this.colorRedTopRight *= p_147808_5_;
                this.colorGreenTopLeft *= p_147808_6_;
                this.colorGreenBottomLeft *= p_147808_6_;
                this.colorGreenBottomRight *= p_147808_6_;
                this.colorGreenTopRight *= p_147808_6_;
                this.colorBlueTopLeft *= p_147808_7_;
                this.colorBlueBottomLeft *= p_147808_7_;
                this.colorBlueBottomRight *= p_147808_7_;
                this.colorBlueTopRight *= p_147808_7_;
                this.renderFaceZNeg(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (this.renderAllFaces || p_147808_1_.shouldSideBeRendered(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ + 1, 3))
        {
            if (this.renderMaxZ >= 1.0D)
            {
                ++p_147808_4_;
            }

            this.aoLightValueScratchXZNP = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPP = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZNP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchYZPP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXZNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_);
            this.aoBrightnessXZPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_);
            this.aoBrightnessYZNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_);
            this.aoBrightnessYZPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_);
            flag2 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ + 1).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ + 1).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ + 1).getCanBlockGrass();

            if (!flag3 && !flag5)
            {
                this.aoLightValueScratchXYZNNP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNNP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNNP = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_ - 1, p_147808_4_);
            }

            if (!flag3 && !flag4)
            {
                this.aoLightValueScratchXYZNPP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNPP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPP = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_ + 1, p_147808_4_);
            }

            if (!flag2 && !flag5)
            {
                this.aoLightValueScratchXYZPNP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPNP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPNP = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_ - 1, p_147808_4_);
            }

            if (!flag2 && !flag4)
            {
                this.aoLightValueScratchXYZPPP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPPP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPP = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_ + 1, p_147808_4_);
            }

            if (this.renderMaxZ >= 1.0D)
            {
                --p_147808_4_;
            }

            i1 = l;

            if (this.renderMaxZ >= 1.0D || !this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ + 1).isOpaqueCube())
            {
                i1 = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ + 1);
            }

            f7 = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
            f8 = (this.aoLightValueScratchXZNP + this.aoLightValueScratchXYZNPP + f7 + this.aoLightValueScratchYZPP) / 4.0F;
            f9 = (f7 + this.aoLightValueScratchYZPP + this.aoLightValueScratchXZPP + this.aoLightValueScratchXYZPPP) / 4.0F;
            f10 = (this.aoLightValueScratchYZNP + f7 + this.aoLightValueScratchXYZPNP + this.aoLightValueScratchXZPP) / 4.0F;
            f11 = (this.aoLightValueScratchXYZNNP + this.aoLightValueScratchXZNP + this.aoLightValueScratchYZNP + f7) / 4.0F;
            f3 = (float)((double)f8 * this.renderMaxY * (1.0D - this.renderMinX) + (double)f9 * this.renderMaxY * this.renderMinX + (double)f10 * (1.0D - this.renderMaxY) * this.renderMinX + (double)f11 * (1.0D - this.renderMaxY) * (1.0D - this.renderMinX));
            f4 = (float)((double)f8 * this.renderMinY * (1.0D - this.renderMinX) + (double)f9 * this.renderMinY * this.renderMinX + (double)f10 * (1.0D - this.renderMinY) * this.renderMinX + (double)f11 * (1.0D - this.renderMinY) * (1.0D - this.renderMinX));
            f5 = (float)((double)f8 * this.renderMinY * (1.0D - this.renderMaxX) + (double)f9 * this.renderMinY * this.renderMaxX + (double)f10 * (1.0D - this.renderMinY) * this.renderMaxX + (double)f11 * (1.0D - this.renderMinY) * (1.0D - this.renderMaxX));
            f6 = (float)((double)f8 * this.renderMaxY * (1.0D - this.renderMaxX) + (double)f9 * this.renderMaxY * this.renderMaxX + (double)f10 * (1.0D - this.renderMaxY) * this.renderMaxX + (double)f11 * (1.0D - this.renderMaxY) * (1.0D - this.renderMaxX));
            j1 = this.getAoBrightness(this.aoBrightnessXZNP, this.aoBrightnessXYZNPP, this.aoBrightnessYZPP, i1);
            k1 = this.getAoBrightness(this.aoBrightnessYZPP, this.aoBrightnessXZPP, this.aoBrightnessXYZPPP, i1);
            l1 = this.getAoBrightness(this.aoBrightnessYZNP, this.aoBrightnessXYZPNP, this.aoBrightnessXZPP, i1);
            i2 = this.getAoBrightness(this.aoBrightnessXYZNNP, this.aoBrightnessXZNP, this.aoBrightnessYZNP, i1);
            this.brightnessTopLeft = this.mixAoBrightness(j1, i2, l1, k1, this.renderMaxY * (1.0D - this.renderMinX), (1.0D - this.renderMaxY) * (1.0D - this.renderMinX), (1.0D - this.renderMaxY) * this.renderMinX, this.renderMaxY * this.renderMinX);
            this.brightnessBottomLeft = this.mixAoBrightness(j1, i2, l1, k1, this.renderMinY * (1.0D - this.renderMinX), (1.0D - this.renderMinY) * (1.0D - this.renderMinX), (1.0D - this.renderMinY) * this.renderMinX, this.renderMinY * this.renderMinX);
            this.brightnessBottomRight = this.mixAoBrightness(j1, i2, l1, k1, this.renderMinY * (1.0D - this.renderMaxX), (1.0D - this.renderMinY) * (1.0D - this.renderMaxX), (1.0D - this.renderMinY) * this.renderMaxX, this.renderMinY * this.renderMaxX);
            this.brightnessTopRight = this.mixAoBrightness(j1, i2, l1, k1, this.renderMaxY * (1.0D - this.renderMaxX), (1.0D - this.renderMaxY) * (1.0D - this.renderMaxX), (1.0D - this.renderMaxY) * this.renderMaxX, this.renderMaxY * this.renderMaxX);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147808_5_ * 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147808_6_ * 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147808_7_ * 0.8F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.8F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.8F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.8F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147808_1_, this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_, 3);
            this.renderFaceZPos(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, iicon);

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147808_5_;
                this.colorRedBottomLeft *= p_147808_5_;
                this.colorRedBottomRight *= p_147808_5_;
                this.colorRedTopRight *= p_147808_5_;
                this.colorGreenTopLeft *= p_147808_6_;
                this.colorGreenBottomLeft *= p_147808_6_;
                this.colorGreenBottomRight *= p_147808_6_;
                this.colorGreenTopRight *= p_147808_6_;
                this.colorBlueTopLeft *= p_147808_7_;
                this.colorBlueBottomLeft *= p_147808_7_;
                this.colorBlueBottomRight *= p_147808_7_;
                this.colorBlueTopRight *= p_147808_7_;
                this.renderFaceZPos(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (this.renderAllFaces || p_147808_1_.shouldSideBeRendered(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_, 4))
        {
            if (this.renderMinX <= 0.0D)
            {
                --p_147808_2_;
            }

            this.aoLightValueScratchXYNN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZNN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZNP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYNP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXYNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_);
            this.aoBrightnessXZNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ - 1);
            this.aoBrightnessXZNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ + 1);
            this.aoBrightnessXYNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_);
            flag2 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ + 1, p_147808_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_ - 1, p_147808_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ - 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_ + 1).getCanBlockGrass();

            if (!flag4 && !flag3)
            {
                this.aoLightValueScratchXYZNNN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNNN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNNN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_ - 1);
            }

            if (!flag5 && !flag3)
            {
                this.aoLightValueScratchXYZNNP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNNP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNNP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_ + 1);
            }

            if (!flag4 && !flag2)
            {
                this.aoLightValueScratchXYZNPN = this.aoLightValueScratchXZNN;
                this.aoBrightnessXYZNPN = this.aoBrightnessXZNN;
            }
            else
            {
                this.aoLightValueScratchXYZNPN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_ - 1);
            }

            if (!flag5 && !flag2)
            {
                this.aoLightValueScratchXYZNPP = this.aoLightValueScratchXZNP;
                this.aoBrightnessXYZNPP = this.aoBrightnessXZNP;
            }
            else
            {
                this.aoLightValueScratchXYZNPP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZNPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_ + 1);
            }

            if (this.renderMinX <= 0.0D)
            {
                ++p_147808_2_;
            }

            i1 = l;

            if (this.renderMinX <= 0.0D || !this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_).isOpaqueCube())
            {
                i1 = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ - 1, p_147808_3_, p_147808_4_);
            }

            f7 = this.blockAccess.getBlock(p_147808_2_ - 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            f8 = (this.aoLightValueScratchXYNN + this.aoLightValueScratchXYZNNP + f7 + this.aoLightValueScratchXZNP) / 4.0F;
            f9 = (f7 + this.aoLightValueScratchXZNP + this.aoLightValueScratchXYNP + this.aoLightValueScratchXYZNPP) / 4.0F;
            f10 = (this.aoLightValueScratchXZNN + f7 + this.aoLightValueScratchXYZNPN + this.aoLightValueScratchXYNP) / 4.0F;
            f11 = (this.aoLightValueScratchXYZNNN + this.aoLightValueScratchXYNN + this.aoLightValueScratchXZNN + f7) / 4.0F;
            f3 = (float)((double)f9 * this.renderMaxY * this.renderMaxZ + (double)f10 * this.renderMaxY * (1.0D - this.renderMaxZ) + (double)f11 * (1.0D - this.renderMaxY) * (1.0D - this.renderMaxZ) + (double)f8 * (1.0D - this.renderMaxY) * this.renderMaxZ);
            f4 = (float)((double)f9 * this.renderMaxY * this.renderMinZ + (double)f10 * this.renderMaxY * (1.0D - this.renderMinZ) + (double)f11 * (1.0D - this.renderMaxY) * (1.0D - this.renderMinZ) + (double)f8 * (1.0D - this.renderMaxY) * this.renderMinZ);
            f5 = (float)((double)f9 * this.renderMinY * this.renderMinZ + (double)f10 * this.renderMinY * (1.0D - this.renderMinZ) + (double)f11 * (1.0D - this.renderMinY) * (1.0D - this.renderMinZ) + (double)f8 * (1.0D - this.renderMinY) * this.renderMinZ);
            f6 = (float)((double)f9 * this.renderMinY * this.renderMaxZ + (double)f10 * this.renderMinY * (1.0D - this.renderMaxZ) + (double)f11 * (1.0D - this.renderMinY) * (1.0D - this.renderMaxZ) + (double)f8 * (1.0D - this.renderMinY) * this.renderMaxZ);
            j1 = this.getAoBrightness(this.aoBrightnessXYNN, this.aoBrightnessXYZNNP, this.aoBrightnessXZNP, i1);
            k1 = this.getAoBrightness(this.aoBrightnessXZNP, this.aoBrightnessXYNP, this.aoBrightnessXYZNPP, i1);
            l1 = this.getAoBrightness(this.aoBrightnessXZNN, this.aoBrightnessXYZNPN, this.aoBrightnessXYNP, i1);
            i2 = this.getAoBrightness(this.aoBrightnessXYZNNN, this.aoBrightnessXYNN, this.aoBrightnessXZNN, i1);
            this.brightnessTopLeft = this.mixAoBrightness(k1, l1, i2, j1, this.renderMaxY * this.renderMaxZ, this.renderMaxY * (1.0D - this.renderMaxZ), (1.0D - this.renderMaxY) * (1.0D - this.renderMaxZ), (1.0D - this.renderMaxY) * this.renderMaxZ);
            this.brightnessBottomLeft = this.mixAoBrightness(k1, l1, i2, j1, this.renderMaxY * this.renderMinZ, this.renderMaxY * (1.0D - this.renderMinZ), (1.0D - this.renderMaxY) * (1.0D - this.renderMinZ), (1.0D - this.renderMaxY) * this.renderMinZ);
            this.brightnessBottomRight = this.mixAoBrightness(k1, l1, i2, j1, this.renderMinY * this.renderMinZ, this.renderMinY * (1.0D - this.renderMinZ), (1.0D - this.renderMinY) * (1.0D - this.renderMinZ), (1.0D - this.renderMinY) * this.renderMinZ);
            this.brightnessTopRight = this.mixAoBrightness(k1, l1, i2, j1, this.renderMinY * this.renderMaxZ, this.renderMinY * (1.0D - this.renderMaxZ), (1.0D - this.renderMinY) * (1.0D - this.renderMaxZ), (1.0D - this.renderMinY) * this.renderMaxZ);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147808_5_ * 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147808_6_ * 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147808_7_ * 0.6F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.6F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147808_1_, this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_, 4);
            this.renderFaceXNeg(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, iicon);

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147808_5_;
                this.colorRedBottomLeft *= p_147808_5_;
                this.colorRedBottomRight *= p_147808_5_;
                this.colorRedTopRight *= p_147808_5_;
                this.colorGreenTopLeft *= p_147808_6_;
                this.colorGreenBottomLeft *= p_147808_6_;
                this.colorGreenBottomRight *= p_147808_6_;
                this.colorGreenTopRight *= p_147808_6_;
                this.colorBlueTopLeft *= p_147808_7_;
                this.colorBlueBottomLeft *= p_147808_7_;
                this.colorBlueBottomRight *= p_147808_7_;
                this.colorBlueTopRight *= p_147808_7_;
                this.renderFaceXNeg(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        if (this.renderAllFaces || p_147808_1_.shouldSideBeRendered(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_, 5))
        {
            if (this.renderMaxX >= 1.0D)
            {
                ++p_147808_2_;
            }

            this.aoLightValueScratchXYPN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ - 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXZPP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_, p_147808_4_ + 1).getAmbientOcclusionLightValue();
            this.aoLightValueScratchXYPP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_).getAmbientOcclusionLightValue();
            this.aoBrightnessXYPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_);
            this.aoBrightnessXZPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ - 1);
            this.aoBrightnessXZPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_ + 1);
            this.aoBrightnessXYPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_);
            flag2 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ + 1, p_147808_4_).getCanBlockGrass();
            flag3 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_ - 1, p_147808_4_).getCanBlockGrass();
            flag4 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ + 1).getCanBlockGrass();
            flag5 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_ - 1).getCanBlockGrass();

            if (!flag3 && !flag5)
            {
                this.aoLightValueScratchXYZPNN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPNN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPNN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_ - 1);
            }

            if (!flag3 && !flag4)
            {
                this.aoLightValueScratchXYZPNP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPNP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPNP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ - 1, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPNP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ - 1, p_147808_4_ + 1);
            }

            if (!flag2 && !flag5)
            {
                this.aoLightValueScratchXYZPPN = this.aoLightValueScratchXZPN;
                this.aoBrightnessXYZPPN = this.aoBrightnessXZPN;
            }
            else
            {
                this.aoLightValueScratchXYZPPN = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ - 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPN = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_ - 1);
            }

            if (!flag2 && !flag4)
            {
                this.aoLightValueScratchXYZPPP = this.aoLightValueScratchXZPP;
                this.aoBrightnessXYZPPP = this.aoBrightnessXZPP;
            }
            else
            {
                this.aoLightValueScratchXYZPPP = this.blockAccess.getBlock(p_147808_2_, p_147808_3_ + 1, p_147808_4_ + 1).getAmbientOcclusionLightValue();
                this.aoBrightnessXYZPPP = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_, p_147808_3_ + 1, p_147808_4_ + 1);
            }

            if (this.renderMaxX >= 1.0D)
            {
                --p_147808_2_;
            }

            i1 = l;

            if (this.renderMaxX >= 1.0D || !this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_).isOpaqueCube())
            {
                i1 = p_147808_1_.getMixedBrightnessForBlock(this.blockAccess, p_147808_2_ + 1, p_147808_3_, p_147808_4_);
            }

            f7 = this.blockAccess.getBlock(p_147808_2_ + 1, p_147808_3_, p_147808_4_).getAmbientOcclusionLightValue();
            f8 = (this.aoLightValueScratchXYPN + this.aoLightValueScratchXYZPNP + f7 + this.aoLightValueScratchXZPP) / 4.0F;
            f9 = (this.aoLightValueScratchXYZPNN + this.aoLightValueScratchXYPN + this.aoLightValueScratchXZPN + f7) / 4.0F;
            f10 = (this.aoLightValueScratchXZPN + f7 + this.aoLightValueScratchXYZPPN + this.aoLightValueScratchXYPP) / 4.0F;
            f11 = (f7 + this.aoLightValueScratchXZPP + this.aoLightValueScratchXYPP + this.aoLightValueScratchXYZPPP) / 4.0F;
            f3 = (float)((double)f8 * (1.0D - this.renderMinY) * this.renderMaxZ + (double)f9 * (1.0D - this.renderMinY) * (1.0D - this.renderMaxZ) + (double)f10 * this.renderMinY * (1.0D - this.renderMaxZ) + (double)f11 * this.renderMinY * this.renderMaxZ);
            f4 = (float)((double)f8 * (1.0D - this.renderMinY) * this.renderMinZ + (double)f9 * (1.0D - this.renderMinY) * (1.0D - this.renderMinZ) + (double)f10 * this.renderMinY * (1.0D - this.renderMinZ) + (double)f11 * this.renderMinY * this.renderMinZ);
            f5 = (float)((double)f8 * (1.0D - this.renderMaxY) * this.renderMinZ + (double)f9 * (1.0D - this.renderMaxY) * (1.0D - this.renderMinZ) + (double)f10 * this.renderMaxY * (1.0D - this.renderMinZ) + (double)f11 * this.renderMaxY * this.renderMinZ);
            f6 = (float)((double)f8 * (1.0D - this.renderMaxY) * this.renderMaxZ + (double)f9 * (1.0D - this.renderMaxY) * (1.0D - this.renderMaxZ) + (double)f10 * this.renderMaxY * (1.0D - this.renderMaxZ) + (double)f11 * this.renderMaxY * this.renderMaxZ);
            j1 = this.getAoBrightness(this.aoBrightnessXYPN, this.aoBrightnessXYZPNP, this.aoBrightnessXZPP, i1);
            k1 = this.getAoBrightness(this.aoBrightnessXZPP, this.aoBrightnessXYPP, this.aoBrightnessXYZPPP, i1);
            l1 = this.getAoBrightness(this.aoBrightnessXZPN, this.aoBrightnessXYZPPN, this.aoBrightnessXYPP, i1);
            i2 = this.getAoBrightness(this.aoBrightnessXYZPNN, this.aoBrightnessXYPN, this.aoBrightnessXZPN, i1);
            this.brightnessTopLeft = this.mixAoBrightness(j1, i2, l1, k1, (1.0D - this.renderMinY) * this.renderMaxZ, (1.0D - this.renderMinY) * (1.0D - this.renderMaxZ), this.renderMinY * (1.0D - this.renderMaxZ), this.renderMinY * this.renderMaxZ);
            this.brightnessBottomLeft = this.mixAoBrightness(j1, i2, l1, k1, (1.0D - this.renderMinY) * this.renderMinZ, (1.0D - this.renderMinY) * (1.0D - this.renderMinZ), this.renderMinY * (1.0D - this.renderMinZ), this.renderMinY * this.renderMinZ);
            this.brightnessBottomRight = this.mixAoBrightness(j1, i2, l1, k1, (1.0D - this.renderMaxY) * this.renderMinZ, (1.0D - this.renderMaxY) * (1.0D - this.renderMinZ), this.renderMaxY * (1.0D - this.renderMinZ), this.renderMaxY * this.renderMinZ);
            this.brightnessTopRight = this.mixAoBrightness(j1, i2, l1, k1, (1.0D - this.renderMaxY) * this.renderMaxZ, (1.0D - this.renderMaxY) * (1.0D - this.renderMaxZ), this.renderMaxY * (1.0D - this.renderMaxZ), this.renderMaxY * this.renderMaxZ);

            if (flag1)
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = p_147808_5_ * 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = p_147808_6_ * 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = p_147808_7_ * 0.6F;
            }
            else
            {
                this.colorRedTopLeft = this.colorRedBottomLeft = this.colorRedBottomRight = this.colorRedTopRight = 0.6F;
                this.colorGreenTopLeft = this.colorGreenBottomLeft = this.colorGreenBottomRight = this.colorGreenTopRight = 0.6F;
                this.colorBlueTopLeft = this.colorBlueBottomLeft = this.colorBlueBottomRight = this.colorBlueTopRight = 0.6F;
            }

            this.colorRedTopLeft *= f3;
            this.colorGreenTopLeft *= f3;
            this.colorBlueTopLeft *= f3;
            this.colorRedBottomLeft *= f4;
            this.colorGreenBottomLeft *= f4;
            this.colorBlueBottomLeft *= f4;
            this.colorRedBottomRight *= f5;
            this.colorGreenBottomRight *= f5;
            this.colorBlueBottomRight *= f5;
            this.colorRedTopRight *= f6;
            this.colorGreenTopRight *= f6;
            this.colorBlueTopRight *= f6;
            iicon = this.getBlockIcon(p_147808_1_, this.blockAccess, p_147808_2_, p_147808_3_, p_147808_4_, 5);
            this.renderFaceXPos(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, iicon);

            if (fancyGrass && iicon.getIconName().equals("grass_side") && !this.hasOverrideBlockTexture())
            {
                this.colorRedTopLeft *= p_147808_5_;
                this.colorRedBottomLeft *= p_147808_5_;
                this.colorRedBottomRight *= p_147808_5_;
                this.colorRedTopRight *= p_147808_5_;
                this.colorGreenTopLeft *= p_147808_6_;
                this.colorGreenBottomLeft *= p_147808_6_;
                this.colorGreenBottomRight *= p_147808_6_;
                this.colorGreenTopRight *= p_147808_6_;
                this.colorBlueTopLeft *= p_147808_7_;
                this.colorBlueBottomLeft *= p_147808_7_;
                this.colorBlueBottomRight *= p_147808_7_;
                this.colorBlueTopRight *= p_147808_7_;
                this.renderFaceXPos(p_147808_1_, (double)p_147808_2_, (double)p_147808_3_, (double)p_147808_4_, BlockGrass.getIconSideOverlay());
            }

            flag = true;
        }

        this.enableAO = false;
        return flag;
    }
	
	public LittleBlockVertex blockVertex = null;
	
	/**
     * Renders the given texture to the bottom face of the block. Args: block, x, y, z, texture
     */
	@Override
    public void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon)
    {
        //Tessellator tessellator = Tessellator.instance;

        if (this.hasOverrideBlockTexture())
        {
            icon = this.overrideBlockTexture;
        }

        double d3 = (double)icon.getInterpolatedU(this.renderMinX * 16.0D);
        double d4 = (double)icon.getInterpolatedU(this.renderMaxX * 16.0D);
        double d5 = (double)icon.getInterpolatedV(this.renderMinZ * 16.0D);
        double d6 = (double)icon.getInterpolatedV(this.renderMaxZ * 16.0D);

        if (this.renderMinX < 0.0D || this.renderMaxX > 1.0D)
        {
            d3 = (double)icon.getMinU();
            d4 = (double)icon.getMaxU();
        }

        if (this.renderMinZ < 0.0D || this.renderMaxZ > 1.0D)
        {
            d5 = (double)icon.getMinV();
            d6 = (double)icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;

        if (this.uvRotateBottom == 2)
        {
            d3 = (double)icon.getInterpolatedU(this.renderMinZ * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxX * 16.0D);
            d4 = (double)icon.getInterpolatedU(this.renderMaxZ * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinX * 16.0D);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d9;
        }
        else if (this.uvRotateBottom == 1)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMaxZ * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMinZ * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMaxX * 16.0D);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d8;
            d9 = d6;
            d10 = d5;
        }
        else if (this.uvRotateBottom == 3)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMaxX * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMinZ * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMaxZ * 16.0D);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = x + this.renderMinX;
        double d12 = x + this.renderMaxX;
        double d13 = y + this.renderMinY;
        double d14 = z + this.renderMinZ;
        double d15 = z + this.renderMaxZ;

        if (this.renderFromInside)
        {
            d11 = x + this.renderMaxX;
            d12 = x + this.renderMinX;
        }
        
        LittleVertex vertex = new LittleVertex();
        vertex.coords = new double[][]{
        		new double[]{d11, d13, d15, d8, d10},
                new double[]{d11, d13, d14, d3, d5},
                new double[]{d12, d13, d14, d7, d9},
                new double[]{d12, d13, d15, d4, d6}
        };
        
        if (this.enableAO)
        {
        	blockVertex.enableAO = true;
        	 vertex.colorRedTopLeft = colorRedTopLeft;
             vertex.colorGreenTopLeft = colorGreenTopLeft;
             vertex.colorBlueTopLeft = colorBlueTopLeft;
             
             vertex.colorRedBottomLeft = colorRedBottomLeft;
             vertex.colorGreenBottomLeft = colorGreenBottomLeft;
             vertex.colorBlueBottomLeft = colorBlueBottomLeft;
             
             vertex.colorRedBottomRight = colorRedBottomRight;
             vertex.colorGreenBottomRight = colorGreenBottomRight;
             vertex.colorBlueBottomRight = colorBlueBottomRight;

             vertex.colorRedTopRight = colorRedTopRight;
             vertex.colorGreenTopRight = colorGreenTopRight;
             vertex.colorBlueTopRight = colorBlueTopRight;
             
             vertex.brightnessTopLeft = brightnessTopLeft;
             vertex.brightnessBottomLeft = brightnessBottomLeft;
             vertex.brightnessBottomRight = brightnessBottomRight;
             vertex.brightnessTopRight = brightnessTopRight;
        }else{
        	Tessellator tessellator = Tessellator.instance;
        	vertex.color = ReflectionHelper.getPrivateValue(Tessellator.class, tessellator, "color", "field_78402_m");
        	vertex.brightnessBottomLeft = ReflectionHelper.getPrivateValue(Tessellator.class, tessellator, "brightness", "field_78401_l");
        }
        blockVertex.YNeg = vertex;
    }

    /**
     * Renders the given texture to the top face of the block. Args: block, x, y, z, texture
     */
	@Override
    public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon)
    {
        //Tessellator tessellator = Tessellator.instance;

        if (this.hasOverrideBlockTexture())
        {
            icon = this.overrideBlockTexture;
        }

        double d3 = (double)icon.getInterpolatedU(this.renderMinX * 16.0D);
        double d4 = (double)icon.getInterpolatedU(this.renderMaxX * 16.0D);
        double d5 = (double)icon.getInterpolatedV(this.renderMinZ * 16.0D);
        double d6 = (double)icon.getInterpolatedV(this.renderMaxZ * 16.0D);

        if (this.renderMinX < 0.0D || this.renderMaxX > 1.0D)
        {
            d3 = (double)icon.getMinU();
            d4 = (double)icon.getMaxU();
        }

        if (this.renderMinZ < 0.0D || this.renderMaxZ > 1.0D)
        {
            d5 = (double)icon.getMinV();
            d6 = (double)icon.getMaxV();
        }

        double d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;

        if (this.uvRotateTop == 1)
        {
            d3 = (double)icon.getInterpolatedU(this.renderMinZ * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxX * 16.0D);
            d4 = (double)icon.getInterpolatedU(this.renderMaxZ * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinX * 16.0D);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d9;
        }
        else if (this.uvRotateTop == 2)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMaxZ * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMinZ * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMaxX * 16.0D);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d8;
            d9 = d6;
            d10 = d5;
        }
        else if (this.uvRotateTop == 3)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMaxX * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMinZ * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMaxZ * 16.0D);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = x + this.renderMinX;
        double d12 = x + this.renderMaxX;
        double d13 = y + this.renderMaxY;
        double d14 = z + this.renderMinZ;
        double d15 = z + this.renderMaxZ;

        if (this.renderFromInside)
        {
            d11 = x + this.renderMaxX;
            d12 = x + this.renderMinX;
        }

        LittleVertex vertex = new LittleVertex();
        vertex.coords = new double[][]{
        	new double[]{d12, d13, d15, d4, d6},
        	new double[]{d12, d13, d14, d7, d9},
        	new double[]{d11, d13, d14, d3, d5},
        	new double[]{d11, d13, d15, d8, d10}
        };
        
        if (this.enableAO)
        {
        	blockVertex.enableAO = true;
        	 vertex.colorRedTopLeft = colorRedTopLeft;
             vertex.colorGreenTopLeft = colorGreenTopLeft;
             vertex.colorBlueTopLeft = colorBlueTopLeft;
             
             vertex.colorRedBottomLeft = colorRedBottomLeft;
             vertex.colorGreenBottomLeft = colorGreenBottomLeft;
             vertex.colorBlueBottomLeft = colorBlueBottomLeft;
             
             vertex.colorRedBottomRight = colorRedBottomRight;
             vertex.colorGreenBottomRight = colorGreenBottomRight;
             vertex.colorBlueBottomRight = colorBlueBottomRight;

             vertex.colorRedTopRight = colorRedTopRight;
             vertex.colorGreenTopRight = colorGreenTopRight;
             vertex.colorBlueTopRight = colorBlueTopRight;
             
             vertex.brightnessTopLeft = brightnessTopLeft;
             vertex.brightnessBottomLeft = brightnessBottomLeft;
             vertex.brightnessBottomRight = brightnessBottomRight;
             vertex.brightnessTopRight = brightnessTopRight;
        }
        
        blockVertex.YPos = vertex;
    }

    /**
     * Renders the given texture to the north (z-negative) face of the block.  Args: block, x, y, z, texture
     */
	@Override
    public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon)
    {
        //Tessellator tessellator = Tessellator.instance;

        if (this.hasOverrideBlockTexture())
        {
            icon = this.overrideBlockTexture;
        }

        double d3 = (double)icon.getInterpolatedU(this.renderMinX * 16.0D);
        double d4 = (double)icon.getInterpolatedU(this.renderMaxX * 16.0D);

        if (this.field_152631_f)
        {
            d4 = (double)icon.getInterpolatedU((1.0D - this.renderMinX) * 16.0D);
            d3 = (double)icon.getInterpolatedU((1.0D - this.renderMaxX) * 16.0D);
        }

        double d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxY * 16.0D);
        double d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinY * 16.0D);
        double d7;

        if (this.flipTexture)
        {
            d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (this.renderMinX < 0.0D || this.renderMaxX > 1.0D)
        {
            d3 = (double)icon.getMinU();
            d4 = (double)icon.getMaxU();
        }

        if (this.renderMinY < 0.0D || this.renderMaxY > 1.0D)
        {
            d5 = (double)icon.getMinV();
            d6 = (double)icon.getMaxV();
        }

        d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;

        if (this.uvRotateEast == 2)
        {
            d3 = (double)icon.getInterpolatedU(this.renderMinY * 16.0D);
            d4 = (double)icon.getInterpolatedU(this.renderMaxY * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMinX * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMaxX * 16.0D);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d9;
        }
        else if (this.uvRotateEast == 1)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMaxY * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMinY * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMaxX * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMinX * 16.0D);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d8;
            d9 = d6;
            d10 = d5;
        }
        else if (this.uvRotateEast == 3)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMaxX * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMaxY * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMinY * 16.0D);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = x + this.renderMinX;
        double d12 = x + this.renderMaxX;
        double d13 = y + this.renderMinY;
        double d14 = y + this.renderMaxY;
        double d15 = z + this.renderMinZ;

        if (this.renderFromInside)
        {
            d11 = x + this.renderMaxX;
            d12 = x + this.renderMinX;
        }
        
        LittleVertex vertex = new LittleVertex();
        vertex.coords = new double[][]{
        	new double[]{d11, d14, d15, d7, d9},
        	new double[]{d12, d14, d15, d3, d5},
        	new double[]{d12, d13, d15, d8, d10},
        	new double[]{d11, d13, d15, d4, d6}
        };
        
        if (this.enableAO)
        {
        	blockVertex.enableAO = true;
        	 vertex.colorRedTopLeft = colorRedTopLeft;
             vertex.colorGreenTopLeft = colorGreenTopLeft;
             vertex.colorBlueTopLeft = colorBlueTopLeft;
             
             vertex.colorRedBottomLeft = colorRedBottomLeft;
             vertex.colorGreenBottomLeft = colorGreenBottomLeft;
             vertex.colorBlueBottomLeft = colorBlueBottomLeft;
             
             vertex.colorRedBottomRight = colorRedBottomRight;
             vertex.colorGreenBottomRight = colorGreenBottomRight;
             vertex.colorBlueBottomRight = colorBlueBottomRight;

             vertex.colorRedTopRight = colorRedTopRight;
             vertex.colorGreenTopRight = colorGreenTopRight;
             vertex.colorBlueTopRight = colorBlueTopRight;
             
             vertex.brightnessTopLeft = brightnessTopLeft;
             vertex.brightnessBottomLeft = brightnessBottomLeft;
             vertex.brightnessBottomRight = brightnessBottomRight;
             vertex.brightnessTopRight = brightnessTopRight;
        }
        
        blockVertex.ZNeg = vertex;
    }

    /**
     * Renders the given texture to the south (z-positive) face of the block.  Args: block, x, y, z, texture
     */
	@Override
    public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon)
    {
        //Tessellator tessellator = Tessellator.instance;

        if (this.hasOverrideBlockTexture())
        {
            icon = this.overrideBlockTexture;
        }

        double d3 = (double)icon.getInterpolatedU(this.renderMinX * 16.0D);
        double d4 = (double)icon.getInterpolatedU(this.renderMaxX * 16.0D);
        double d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxY * 16.0D);
        double d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinY * 16.0D);
        double d7;

        if (this.flipTexture)
        {
            d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (this.renderMinX < 0.0D || this.renderMaxX > 1.0D)
        {
            d3 = (double)icon.getMinU();
            d4 = (double)icon.getMaxU();
        }

        if (this.renderMinY < 0.0D || this.renderMaxY > 1.0D)
        {
            d5 = (double)icon.getMinV();
            d6 = (double)icon.getMaxV();
        }

        d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;

        if (this.uvRotateWest == 1)
        {
            d3 = (double)icon.getInterpolatedU(this.renderMinY * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(this.renderMaxY * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxX * 16.0D);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d9;
        }
        else if (this.uvRotateWest == 2)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMaxY * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMinY * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMaxX * 16.0D);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d8;
            d9 = d6;
            d10 = d5;
        }
        else if (this.uvRotateWest == 3)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMinX * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMaxX * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMaxY * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMinY * 16.0D);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = x + this.renderMinX;
        double d12 = x + this.renderMaxX;
        double d13 = y + this.renderMinY;
        double d14 = y + this.renderMaxY;
        double d15 = z + this.renderMaxZ;

        if (this.renderFromInside)
        {
            d11 = x + this.renderMaxX;
            d12 = x + this.renderMinX;
        }
        
        LittleVertex vertex = new LittleVertex();
        vertex.coords = new double[][]{
        	new double[]{d11, d14, d15, d3, d5},
        	new double[]{d11, d13, d15, d8, d10},
        	new double[]{d12, d13, d15, d4, d6},
        	new double[]{d12, d14, d15, d7, d9}
        };
        
        if (this.enableAO)
        {
        	blockVertex.enableAO = true;
        	 vertex.colorRedTopLeft = colorRedTopLeft;
             vertex.colorGreenTopLeft = colorGreenTopLeft;
             vertex.colorBlueTopLeft = colorBlueTopLeft;
             
             vertex.colorRedBottomLeft = colorRedBottomLeft;
             vertex.colorGreenBottomLeft = colorGreenBottomLeft;
             vertex.colorBlueBottomLeft = colorBlueBottomLeft;
             
             vertex.colorRedBottomRight = colorRedBottomRight;
             vertex.colorGreenBottomRight = colorGreenBottomRight;
             vertex.colorBlueBottomRight = colorBlueBottomRight;

             vertex.colorRedTopRight = colorRedTopRight;
             vertex.colorGreenTopRight = colorGreenTopRight;
             vertex.colorBlueTopRight = colorBlueTopRight;
             
             vertex.brightnessTopLeft = brightnessTopLeft;
             vertex.brightnessBottomLeft = brightnessBottomLeft;
             vertex.brightnessBottomRight = brightnessBottomRight;
             vertex.brightnessTopRight = brightnessTopRight;
        }
        
        blockVertex.ZPos = vertex;
    }

    /**
     * Renders the given texture to the west (x-negative) face of the block.  Args: block, x, y, z, texture
     */
	@Override
    public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon)
    {
        //Tessellator tessellator = Tessellator.instance;

        if (this.hasOverrideBlockTexture())
        {
            icon = this.overrideBlockTexture;
        }

        double d3 = (double)icon.getInterpolatedU(this.renderMinZ * 16.0D);
        double d4 = (double)icon.getInterpolatedU(this.renderMaxZ * 16.0D);
        double d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxY * 16.0D);
        double d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinY * 16.0D);
        double d7;

        if (this.flipTexture)
        {
            d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (this.renderMinZ < 0.0D || this.renderMaxZ > 1.0D)
        {
            d3 = (double)icon.getMinU();
            d4 = (double)icon.getMaxU();
        }

        if (this.renderMinY < 0.0D || this.renderMaxY > 1.0D)
        {
            d5 = (double)icon.getMinV();
            d6 = (double)icon.getMaxV();
        }

        d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;

        if (this.uvRotateNorth == 1)
        {
            d3 = (double)icon.getInterpolatedU(this.renderMinY * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxZ * 16.0D);
            d4 = (double)icon.getInterpolatedU(this.renderMaxY * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinZ * 16.0D);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d9;
        }
        else if (this.uvRotateNorth == 2)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMaxY * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMinZ * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMinY * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMaxZ * 16.0D);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d8;
            d9 = d6;
            d10 = d5;
        }
        else if (this.uvRotateNorth == 3)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMinZ * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMaxZ * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMaxY * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMinY * 16.0D);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = x + this.renderMinX;
        double d12 = y + this.renderMinY;
        double d13 = y + this.renderMaxY;
        double d14 = z + this.renderMinZ;
        double d15 = z + this.renderMaxZ;

        if (this.renderFromInside)
        {
            d14 = z + this.renderMaxZ;
            d15 = z + this.renderMinZ;
        }
        
        LittleVertex vertex = new LittleVertex();
        vertex.coords = new double[][]{
        	new double[]{d11, d13, d15, d7, d9},
        	new double[]{d11, d13, d14, d3, d5},
        	new double[]{d11, d12, d14, d8, d10},
        	new double[]{d11, d12, d15, d4, d6}
        };
        
        if (this.enableAO)
        {
        	blockVertex.enableAO = true;
        	 vertex.colorRedTopLeft = colorRedTopLeft;
             vertex.colorGreenTopLeft = colorGreenTopLeft;
             vertex.colorBlueTopLeft = colorBlueTopLeft;
             
             vertex.colorRedBottomLeft = colorRedBottomLeft;
             vertex.colorGreenBottomLeft = colorGreenBottomLeft;
             vertex.colorBlueBottomLeft = colorBlueBottomLeft;
             
             vertex.colorRedBottomRight = colorRedBottomRight;
             vertex.colorGreenBottomRight = colorGreenBottomRight;
             vertex.colorBlueBottomRight = colorBlueBottomRight;

             vertex.colorRedTopRight = colorRedTopRight;
             vertex.colorGreenTopRight = colorGreenTopRight;
             vertex.colorBlueTopRight = colorBlueTopRight;
             
             vertex.brightnessTopLeft = brightnessTopLeft;
             vertex.brightnessBottomLeft = brightnessBottomLeft;
             vertex.brightnessBottomRight = brightnessBottomRight;
             vertex.brightnessTopRight = brightnessTopRight;
        }
        
        blockVertex.XNeg = vertex;
    }

    /**
     * Renders the given texture to the east (x-positive) face of the block.  Args: block, x, y, z, texture
     */
	@Override
    public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon)
    {
        //Tessellator tessellator = Tessellator.instance;

        if (this.hasOverrideBlockTexture())
        {
            icon = this.overrideBlockTexture;
        }

        double d3 = (double)icon.getInterpolatedU(this.renderMinZ * 16.0D);
        double d4 = (double)icon.getInterpolatedU(this.renderMaxZ * 16.0D);

        if (this.field_152631_f)
        {
            d4 = (double)icon.getInterpolatedU((1.0D - this.renderMinZ) * 16.0D);
            d3 = (double)icon.getInterpolatedU((1.0D - this.renderMaxZ) * 16.0D);
        }

        double d5 = (double)icon.getInterpolatedV(16.0D - this.renderMaxY * 16.0D);
        double d6 = (double)icon.getInterpolatedV(16.0D - this.renderMinY * 16.0D);
        double d7;

        if (this.flipTexture)
        {
            d7 = d3;
            d3 = d4;
            d4 = d7;
        }

        if (this.renderMinZ < 0.0D || this.renderMaxZ > 1.0D)
        {
            d3 = (double)icon.getMinU();
            d4 = (double)icon.getMaxU();
        }

        if (this.renderMinY < 0.0D || this.renderMaxY > 1.0D)
        {
            d5 = (double)icon.getMinV();
            d6 = (double)icon.getMaxV();
        }

        d7 = d4;
        double d8 = d3;
        double d9 = d5;
        double d10 = d6;

        if (this.uvRotateSouth == 2)
        {
            d3 = (double)icon.getInterpolatedU(this.renderMinY * 16.0D);
            d5 = (double)icon.getInterpolatedV(16.0D - this.renderMinZ * 16.0D);
            d4 = (double)icon.getInterpolatedU(this.renderMaxY * 16.0D);
            d6 = (double)icon.getInterpolatedV(16.0D - this.renderMaxZ * 16.0D);
            d9 = d5;
            d10 = d6;
            d7 = d3;
            d8 = d4;
            d5 = d6;
            d6 = d9;
        }
        else if (this.uvRotateSouth == 1)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMaxY * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMaxZ * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMinY * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMinZ * 16.0D);
            d7 = d4;
            d8 = d3;
            d3 = d4;
            d4 = d8;
            d9 = d6;
            d10 = d5;
        }
        else if (this.uvRotateSouth == 3)
        {
            d3 = (double)icon.getInterpolatedU(16.0D - this.renderMinZ * 16.0D);
            d4 = (double)icon.getInterpolatedU(16.0D - this.renderMaxZ * 16.0D);
            d5 = (double)icon.getInterpolatedV(this.renderMaxY * 16.0D);
            d6 = (double)icon.getInterpolatedV(this.renderMinY * 16.0D);
            d7 = d4;
            d8 = d3;
            d9 = d5;
            d10 = d6;
        }

        double d11 = x + this.renderMaxX;
        double d12 = y + this.renderMinY;
        double d13 = y + this.renderMaxY;
        double d14 = z + this.renderMinZ;
        double d15 = z + this.renderMaxZ;

        if (this.renderFromInside)
        {
            d14 = z + this.renderMaxZ;
            d15 = z + this.renderMinZ;
        }
        
        LittleVertex vertex = new LittleVertex();
        vertex.coords = new double[][]{
        	new double[]{d11, d12, d15, d8, d10},
        	new double[]{d11, d12, d14, d4, d6},
        	new double[]{d11, d13, d14, d7, d9},
        	new double[]{d11, d13, d15, d3, d5}
        };
        
        if (this.enableAO)
        {
        	blockVertex.enableAO = true;
        	 vertex.colorRedTopLeft = colorRedTopLeft;
             vertex.colorGreenTopLeft = colorGreenTopLeft;
             vertex.colorBlueTopLeft = colorBlueTopLeft;
             
             vertex.colorRedBottomLeft = colorRedBottomLeft;
             vertex.colorGreenBottomLeft = colorGreenBottomLeft;
             vertex.colorBlueBottomLeft = colorBlueBottomLeft;
             
             vertex.colorRedBottomRight = colorRedBottomRight;
             vertex.colorGreenBottomRight = colorGreenBottomRight;
             vertex.colorBlueBottomRight = colorBlueBottomRight;

             vertex.colorRedTopRight = colorRedTopRight;
             vertex.colorGreenTopRight = colorGreenTopRight;
             vertex.colorBlueTopRight = colorBlueTopRight;
             
             vertex.brightnessTopLeft = brightnessTopLeft;
             vertex.brightnessBottomLeft = brightnessBottomLeft;
             vertex.brightnessBottomRight = brightnessBottomRight;
             vertex.brightnessTopRight = brightnessTopRight;
        }
        
        blockVertex.XPos = vertex;
    }

}
