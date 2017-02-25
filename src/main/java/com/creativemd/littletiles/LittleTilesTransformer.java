package com.creativemd.littletiles;

import java.util.Iterator;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.creativemd.creativecore.transformer.CreativeTransformer;
import com.creativemd.creativecore.transformer.Transformer;
import com.creativemd.littletiles.common.structure.LittleBed;
import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.entity.player.EntityPlayer;

public class LittleTilesTransformer extends CreativeTransformer {

	public LittleTilesTransformer() {
		super("littletiles");
	}

	@Override
	protected void initTransformers() {
		addTransformer(new Transformer("net.minecraft.client.renderer.RenderGlobal") {
			
			@Override
			public void transform(ClassNode classNode) {
				MethodNode m = findMethod(classNode, "loadRenderers", "()V");
				String className = patchClassName("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher");
				boolean isNextLabel = false;
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode node = (AbstractInsnNode) iterator.next();
					if(isNextLabel && node instanceof LabelNode)
					{
						m.instructions.insert(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "onReloadRenderers", "()V", false));
						isNextLabel = false;
					}
					if(node instanceof TypeInsnNode && ((TypeInsnNode) node).getOpcode() == Opcodes.NEW && ((TypeInsnNode) node).desc.equals(className))
						((TypeInsnNode) node).desc = "com/creativemd/littletiles/client/render/LittleChunkDispatcher";
					if(node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals("<init>") && ((MethodInsnNode) node).desc.equals("()V") && ((MethodInsnNode) node).owner.equals(className))
					{
						MethodInsnNode method = (MethodInsnNode) node;
						method.owner = "com/creativemd/littletiles/client/render/LittleChunkDispatcher";
						isNextLabel = true;
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.entity.player.EntityPlayer") {
			
			@Override
			public void transform(ClassNode node) {
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "littleBed", "Lcom/creativemd/littletiles/common/structure/LittleStructure;", null, null));
				
				MethodNode m = findMethod(node, "getBedOrientationInDegrees", "()F");
				if(m != null)
				{
					m.instructions.clear();
					
					m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/structure/LittleBed", "getBedOrientationInDegrees", patchDESC("(Lnet/minecraft/entity/player/EntityPlayer;)F"), false));
					m.instructions.add(new InsnNode(Opcodes.FRETURN));
				}
			}
		});
		addTransformer(new Transformer("net.minecraftforge.client.ForgeHooksClient") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "orientBedCamera",  "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V");
				
				ListIterator<AbstractInsnNode> iterator = m.instructions.iterator();
				
				boolean nextLabel = false;
				while(iterator.hasNext())
				{
					AbstractInsnNode insn = iterator.next();
					if(nextLabel && insn instanceof LabelNode)
					{
						m.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/structure/LittleBed", "setBedDirection", patchDESC("(Lnet/minecraft/entity/Entity;)V"), false));
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ALOAD, 3));
						break;
					}
					if(insn instanceof MethodInsnNode && ((MethodInsnNode) insn).owner.equals(patchDESC("org/lwjgl/opengl/GL11")) && ((MethodInsnNode) insn).name.equals(patchFieldName("glRotatef")))
						nextLabel = true;
				}
			}
		});
	}

}
