package conc_trace.instr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.Iterator;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.InstructionFinder;

// only need to generate data, no need for any constraints
public class AddRecorderInstrumentationAgent implements ClassFileTransformer {

	// location used to store object special id
	// todo: add an option to change this name
	public static String SPECIAL_ID_LOC = "_special_id_reserved_222";
	
	// check if the instruction is a read instruction of a shared variable
	// in this case we just check if it is a read
	// aload, iload, fload, dload, lload -> local var (can be ignored)
	// getfield, getstatic -> should be instrumented
	public boolean isReadInstruction(Instruction instruction) {
		return instruction instanceof GETFIELD || instruction instanceof GETSTATIC;
	}
	
	/**
	 * Instrument the method for every instr that reads from a shared memory location
	 * to write to the event list.
	 * @param method
	 * @param cpg
	 * @return
	 */
	public MethodGen instrumentMethod(Method method, String className, ConstantPoolGen cpg) {
		MethodGen mg = new MethodGen(method, className, cpg);
		InstructionList instrs = mg.getInstructionList();
		InstructionList newInstrList = new InstructionList();
		InstructionFinder instrFinder = new InstructionFinder(instrs);
		Iterator<InstructionHandle[]> foundInstructions = instrFinder.search("NEW INVOKESPECIAL");
		while (foundInstructions.hasNext() ) {
			InstructionHandle[] handles = foundInstructions.next();
			NEW newInstr = (NEW) handles[0].getInstruction();

			// todo: handle classes which we cannot add field to
			ObjectType objType = newInstr.getLoadClassType(cpg);
			boolean shouldInstr = SharedMemoryChecker.shouldInstrument(objType.getClassName(), newInstr, mg, className, cpg);
			
			if (shouldInstr) {
				// update the id store.
				InstructionList additionalInstructions = new InstructionList();
				
				// get and increment the AddedClass
				GETSTATIC getThreadStore = new GETSTATIC(0);
			}
			
		}
		for (InstructionHandle instr: instrs) {
			newInstrList.append(instr.getInstruction());
			if (!isReadInstruction(instr.getInstruction())) {
				continue;
			}
			// add the tracing logic to the read instruction
			// todo: figure out how to replicate the shared memory
			// so that the recorder and replayer both can id the shared memory
			// cannot rely on consistent ordering tho
			
			
			// idea:
			// assumptions: deterministic thread creation
			// We can assume object creation order is the same within a thread
			// this means that we can use the object creation order within a thread
			// to id an object
			// to do so -> instrument each object to store a counter local to that thread
			// idea: add a concurrenthashmap for a global counter.
			
			
		}
		mg.setInstructionList(newInstrList);
		return mg;
	}
	
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
		FieldGen fieldGen = new FieldGen(Field.PUBLIC, Type.LONG, SPECIAL_ID_LOC, cpg);
		cg.addField(fieldGen.getField());
		Method[] methods = cg.getMethods();
		for (Method method: methods) {
			MethodGen mg = instrumentMethod(method, className, cpg);
			cg.removeMethod(mg.getMethod());
		}
		
		return cg.getJavaClass().getBytes();
	}

}
