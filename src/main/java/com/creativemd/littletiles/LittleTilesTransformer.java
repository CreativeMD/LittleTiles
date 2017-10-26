package com.creativemd.littletiles;

import java.util.Iterator;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.creativemd.creativecore.transformer.CreativeTransformer;
import com.creativemd.creativecore.transformer.Transformer;
import com.creativemd.creativecore.transformer.TransformerNames;

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
						m.instructions.insert(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "onReloadRenderers", patchDESC("(Lnet/minecraft/client/renderer/RenderGlobal;)V"), false));
						m.instructions.insert(node, new VarInsnNode(Opcodes.ALOAD, 0));
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
		addTransformer(new Transformer("net.minecraft.client.renderer.chunk.RenderChunk") {

			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "resortTransparency", "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
				
				m.instructions.clear();
				
				m.instructions.add(new LabelNode());
				
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
				m.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
				m.instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "resortTransparency", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V", false));
				
				m.instructions.add(new InsnNode(Opcodes.RETURN));
				m.instructions.add(new LabelNode());
				
			}
			
		});
		addTransformer(new Transformer("net.minecraft.client.renderer.VertexBuffer") {
			
			@Override
			public void transform(ClassNode node) {
				
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "littleTilesAdded", "Z", null, Boolean.FALSE));
				
				MethodNode m = findMethod(node, "reset", "()V");
				
				AbstractInsnNode start = m.instructions.getFirst();
				
				m.instructions.insertBefore(start, new LabelNode());
				m.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_0));
				m.instructions.insertBefore(start, new FieldInsnNode(Opcodes.PUTFIELD, patchClassName("net/minecraft/client/renderer/VertexBuffer"), "littleTilesAdded", "Z"));

				
			}
		});
		
		//Remove packet limits
		addTransformer(new Transformer("net.minecraft.network.NettyCompressionDecoder") {
			
			@Override
			public void transform(ClassNode node) {
				
				String descException = patchDESC("io/netty/handler/codec/DecoderException");
				
				String desc = patchDESC("(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V");
				String name = TransformerNames.patchMethodName("decode", desc, patchClassName("io/netty/handler/codec/ByteToMessageDecoder"));
				MethodNode m = findMethod(node, name, desc);
				
				ListIterator<AbstractInsnNode> iterator = m.instructions.iterator();
				
				int found = 0;
				while(iterator.hasNext())
				{
					AbstractInsnNode insn = iterator.next();
					if(insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.NEW && ((TypeInsnNode) insn).desc.equals(descException))
					{
						found++;
						if(found == 2)
							iterator.remove();
					}else if(found == 2){
						iterator.remove();
						if(insn instanceof InsnNode && insn.getOpcode() == Opcodes.ATHROW)
							break;
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.network.PacketBuffer") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "readCompoundTag", "()Lnet/minecraft/nbt/NBTTagCompound;");
				
				Iterator<AbstractInsnNode> iterator = m.instructions.iterator();
				while(iterator.hasNext())
				{
					AbstractInsnNode insn = iterator.next();
					if(insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Long)
					{
						m.instructions.remove(insn.getPrevious().getPrevious());
						m.instructions.remove(insn.getPrevious());
						m.instructions.remove(insn.getNext());
						
						m.instructions.insert(insn, new FieldInsnNode(Opcodes.GETSTATIC, patchDESC("net/minecraft/nbt/NBTSizeTracker"), TransformerNames.patchFieldName("INFINITE", patchClassName("net/minecraft/nbt/NBTSizeTracker")), patchDESC("Lnet/minecraft/nbt/NBTSizeTracker;")));
						m.instructions.remove(insn);
						break;
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.util.math.AxisAlignedBB") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "intersectsWith", "(Lnet/minecraft/util/math/AxisAlignedBB;)Z");
				
				String axisClassName = patchClassName("net/minecraft/util/math/AxisAlignedBB");
				String methodDesc = "(L" +  axisClassName + ";)Z";
				
				LabelNode label = (LabelNode) m.instructions.getFirst();
				m.instructions.insertBefore(label, new LabelNode());
				m.instructions.insertBefore(label, new VarInsnNode(Opcodes.ALOAD, 1));
				m.instructions.insertBefore(label, new TypeInsnNode(Opcodes.INSTANCEOF, "com/creativemd/creativecore/common/collision/CreativeAxisAlignedBB"));
				m.instructions.insertBefore(label, new JumpInsnNode(Opcodes.IFEQ, label));
				m.instructions.insertBefore(label, new LabelNode());
				m.instructions.insertBefore(label, new VarInsnNode(Opcodes.ALOAD, 1));
				m.instructions.insertBefore(label, new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.insertBefore(label, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, axisClassName, patchMethodName("intersectsWith", methodDesc), methodDesc, false));
				m.instructions.insertBefore(label, new InsnNode(Opcodes.IRETURN));
			}
		});
		addTransformer(new Transformer("net.minecraftforge.common.ForgeHooks") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "isLivingOnLadder", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityLivingBase;)Z");
				
				m.instructions.clear();
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/structure/LittleLadder", "isLivingOnLadder", patchDESC("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityLivingBase;)Z"), false));
				m.instructions.add(new InsnNode(Opcodes.IRETURN));
			}
		});
	}

}
