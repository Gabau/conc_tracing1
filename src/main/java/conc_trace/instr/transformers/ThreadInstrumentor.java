package conc_trace.instr.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import conc_trace.instr.added.MasterRecorder;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;


// instrument thread creation
// Goal:
//  - add a unique id for each thread that is consistant to ordering
//  - idea -> each thread gets an array assigned to them for identifying
public class ThreadInstrumentor implements ClassFileTransformer {

	
	public static String thread_id_name = "current_thread_tree_id";
	
	public void instrumentStartMethod(Method method, ClassGen cg) {
		if (!method.getName().equals("start")) {
			return;
		}
		System.out.println("Succeeded in instrumenting thread");
		// set up the thread id, using the current threads id.
		// need to access the current threads
		ConstantPoolGen cpg = cg.getConstantPool();
		MethodGen mg = new MethodGen(method, cg.getClassName(), cpg);
		/**
		 * Method to add:
		 * MasterRecorder.addThreadId(this.getId());
		 */
		// get this
		ALOAD loadThis = new ALOAD(0);
		// add the current thread id
		// stack : this
		INVOKEVIRTUAL getId = new INVOKEVIRTUAL(cpg.addMethodref(
				"java/lang/Thread", "getId", "()J"));
		// stack : id
		INVOKESTATIC addThreadId = new INVOKESTATIC(cpg
				.addMethodref(MasterRecorder.class.getName().replace('.', '/'), 
						"addThreadId", "(J)V"));
		
		mg.getInstructionList().insert(addThreadId);
		mg.getInstructionList().insert(getId);
		mg.getInstructionList().insert(loadThis);

		cg.replaceMethod(method, mg.getMethod());
		
	}
	
	private void addImports(ClassGen cg) {
		cg.getConstantPool().addClass(ObjectType.getInstance(
				MasterRecorder.class.getName()));
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className.equals("java/lang/Thread")) {
			JavaClass jc = null;
			try {
				jc = Repository.lookupClass(classBeingRedefined);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ClassGen cg = new ClassGen(jc);
			addImports(cg);
			
			// add field for the id
			ConstantPoolGen cpg = cg.getConstantPool();
			Method[] methods = cg.getMethods();
			for (Method method: methods) {
				instrumentStartMethod(method, cg);
			}
			return cg.getJavaClass().getBytes();
		}
		
		return null;
	}
	
	
}
