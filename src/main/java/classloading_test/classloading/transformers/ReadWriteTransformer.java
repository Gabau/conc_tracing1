package classloading_test.classloading.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;

import classloading_test.classloading.filter.ClassLoadingClassFilter;
import classloading_test.classloading.masterRecorderFunctions.MasterRunnerFunctions;

/**
 * Inserts instrumentation on read and write
 * @author Gabau
 *
 */
public class ReadWriteTransformer implements ClassFileTransformer {

	private ClassLoadingClassFilter filter = new ClassLoadingClassFilter();
	
	/*
	 * Adds the field ref to constant pool as a string.
	 */
	private int addFieldRefToConstantPool(ConstantFieldref fieldRef, ConstantPoolGen cpg) {
		ConstantNameAndType cnat = (ConstantNameAndType) cpg.getConstant(fieldRef.getNameAndTypeIndex());
		ConstantUtf8 name = (ConstantUtf8) cpg.getConstant(cnat.getNameIndex());
		ConstantUtf8 type = (ConstantUtf8) cpg.getConstant(cnat.getSignatureIndex());
		// store the name and type as strings
		
		ConstantClass cc = (ConstantClass) cpg.getConstant(fieldRef.getClassIndex());
		int className = cc.getNameIndex();
		ConstantUtf8 classNameUTF = (ConstantUtf8) cpg.getConstant(className);
		int loc =
				cpg.addString(String.format("%s:%s:%s", name.getBytes(), type.getBytes(), classNameUTF.getBytes()));
		return loc;
	}
	
	// plan is to send the read object to the global mem
	// only store the object hashcode tho
	public InstructionList onRead(MethodGen method) {
		/**
		 * Idea:
		 * Object | primitive o = read();
		 * logging_op(o);
		 * write_to = o;
		 * 
		 * In bytecode
		 * 
		 * - current stack: objectref | empty, indexbyte1, indexbyte2
		 * 
		 * 
		 * GETFIELD | GETSTATIC
		 * - current stack: value
		 * INVOKE
		 * 
		 */
 		for (InstructionHandle instr: method.getInstructionList()) {
			short op = instr.getInstruction().getOpcode();
			switch (op) {
			
			case Const.GETFIELD:
				GETFIELD readInstrField = (GETFIELD) instr.getInstruction();
				InstructionHandle previous = instr.getPrev();
				// push the object to read onto the stack
				// instructions to add before the read

				int cpIndex = readInstrField.getIndex(); // the string value referenced
				DUP toAdd = new DUP(); // objRef, objRef -> value
				ConstantFieldref fieldRef = 
						(ConstantFieldref) method.getConstantPool().getConstant(cpIndex);
				int fieldRefStringLoc = this.addFieldRefToConstantPool(fieldRef, method.getConstantPool());
				// push this to 
				LDC fieldRefLDC = new LDC(fieldRefStringLoc);
				// invoke the function.
				InstructionList masterRunnerInvokation = MasterRunnerFunctions.logReadField(method.getConstantPool());
				masterRunnerInvokation.insert(fieldRefLDC);
				masterRunnerInvokation.insert(toAdd); // DUP then ldc
				
				break;
			case Const.GETSTATIC:
				GETSTATIC readInstrStatic = (GETSTATIC) instr.getInstruction();
				
				// log the constant pool index
//				int cpIndex = readInstrStatic.getIndex();
				break;
			default:
			}
		}
		return null;
	}
	
	
	// send the write object to global mem
	/**
	 * Idea:
	 * 
	 * @param method
	 * @return
	 */
	public InstructionList onWrite(MethodGen method) {
		return null;
	}
	
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// TODO Auto-generated method stub
		if (!filter.test(className)) return null;
		// these two should be done at the same time.
		// instrument the getField and getStatic instructions.
		// might not be an issue if the additional instructions do not actually do
		// GETFIELD
		// can simpley do it separately
		JavaClass jc = null;
		try {
			jc = Repository.lookupClass(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClassGen cg = new ClassGen(jc);
		Method[] methods = cg.getMethods();
		for (Method method: methods) {
			MethodGen mg = new MethodGen(method, className, cg.getConstantPool());
			// instrument GETFIELD, GETSTATIC
			onRead(mg);
			// instrument PUTFIELD, PUTSTATIC
			
		}
		return null;
	}

}
