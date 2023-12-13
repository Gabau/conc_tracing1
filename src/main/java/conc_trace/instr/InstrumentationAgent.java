package conc_trace.instr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;

public class InstrumentationAgent implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// TODO Auto-generated method stub
		ClassGen cg = null;
		try {
			if (className.startsWith("java")) {
				return Repository.lookupClass(className).getBytes();
			}
			JavaClass javaClass = Repository.lookupClass(className);
			cg = new ClassGen(javaClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalClassFormatException();
		}
		ConstantPoolGen cpg = cg.getConstantPool();
		Method[] methods = cg.getMethods();
		for (int i = 0; i < methods.length; ++i) {
			MethodGen mg = new MethodGen(methods[i], className, cpg);
			InstructionList il = mg.getInstructionList();
			GETSTATIC getStatic = new GETSTATIC(
					cpg.addFieldref("java/lang/System", "out", "Ljava/io/PrintStream;"));
			PUSH push = new PUSH(cpg, "Hello world");
			INVOKEVIRTUAL invokeVirtual = new INVOKEVIRTUAL(
					cpg.addMethodref("java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
			il.insert(invokeVirtual);
			il.insert(push);
			il.insert(getStatic);
			cg.replaceMethod(methods[i], mg.getMethod());
		}
		return cg.getJavaClass().getBytes();
		
	}
}
