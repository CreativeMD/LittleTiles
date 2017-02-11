package com.creativemd.littletiles;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.creativemd.creativecore.transformer.CreativeTransformer;
import com.creativemd.creativecore.transformer.Transformer;

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
		/*addTransformer(new Transformer("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "uploadChunk", patchDESC("(Lnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator$Type;Lnet/minecraft/util/BlockRenderLayer;Lnet/minecraft/client/renderer/VertexBuffer;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/client/renderer/chunk/CompiledChunk;D)Lcom/google/common/util/concurrent/ListenableFuture;"), patchDESC("(Lnet/minecraft/util/BlockRenderLayer;Lnet/minecraft/client/renderer/VertexBuffer;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/client/renderer/chunk/CompiledChunk;D)Lcom/google/common/util/concurrent/ListenableFuture<Ljava/lang/Object;>;"), null);
				m.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
				m.instructions.add(new InsnNode(Opcodes.ARETURN));
				m.maxLocals = 8;
				m.maxStack = 1;
				node.methods.add(m);
			}
		});
		addTransformer(new Transformer("net.minecraft.client.renderer.chunk.ChunkRenderWorker") {
			
			@Override
			public void transform(ClassNode clazz) {
				MethodNode m = findMethod(clazz, "processTask", "(Lnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode node = (AbstractInsnNode) iterator.next();
					if(node instanceof FieldInsnNode)
					{
						FieldInsnNode field = (FieldInsnNode) node;
						if(field.getOpcode() == Opcodes.GETFIELD && field.name.equals(patchFieldName("chunkRenderDispatcher")) && field.owner.equals(patchClassName("net/minecraft/client/renderer/chunk/ChunkRenderWorker")) && field.desc.equals(patchDESC("Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;")))
						{
							m.instructions.insert(field, new VarInsnNode(Opcodes.ALOAD, 6));
						}
					}
					if(node instanceof MethodInsnNode)
					{
						MethodInsnNode method = (MethodInsnNode) node;
						String desc = patchDESC("(Lnet/minecraft/util/BlockRenderLayer;Lnet/minecraft/client/renderer/VertexBuffer;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/client/renderer/chunk/CompiledChunk;D)Lcom/google/common/util/concurrent/ListenableFuture;");
						if(method.getOpcode() == Opcodes.INVOKEVIRTUAL && method.name.equals(patchMethodName("uploadChunk", desc)) && method.desc.equals(desc) && method.owner.equals(patchClassName("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher")))
						{
							method.name = "uploadChunk";
							method.desc = patchDESC("(Lnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator$Type;Lnet/minecraft/util/BlockRenderLayer;Lnet/minecraft/client/renderer/VertexBuffer;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/client/renderer/chunk/CompiledChunk;D)Lcom/google/common/util/concurrent/ListenableFuture;");
						}
					}
				}
			}
		});*/
	}

}
