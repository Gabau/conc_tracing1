package conc_trace.instr.analysis.pta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BALOAD;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.CALOAD;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.DALOAD;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.DUP2_X1;
import org.apache.bcel.generic.DUP2_X2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FALOAD;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.IALOAD;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IMUL;
import org.apache.bcel.generic.INEG;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.LALOAD;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.SALOAD;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.generic.SWAP;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang3.tuple.Pair;

import conc_trace.instr.analysis.InstructionLocation;
import conc_trace.instr.analysis.pta.exceptions.PTAInstructionExecutorException;

/**
 * List of instructions that this visitor needs to work with
 * not in class file: breakpoint, impdep1, impdep2
 * no name: reserved for future use
 * 
 * arrayref, index -> value: aaload, baload caload, daload, faload, iaload, laload, saload
 * [arg1, arg2, ...] -> result: invokedynamic, invokestatic (will have to get the num of args from signature)
 * [nochange]: goto, goto_w, iinc, nop, ret (these instructions don't really matter.
 * wide + opcode -> adopt corresponding opcode's pattern
 * (value1, value2) -> : pop2
 * (value2, value1) -> (value2, value1), (value2, value1) -> duplicate top two stack words, dup2
 * dup2_x2, also unique 
 * 
 * 
 * List of instructioons that need instruction location:
 * NEW
 * 
 * @author Gabau
 *
 */
public class PTAInstructionExecutor extends EmptyVisitor {
	private ArrayList<PTAObject> localVariables;
	private FakeStack currentStack = new FakeStack();
	private InstructionLocation currentInstructionLocation;
	private ConstantPoolGen cpg;
	// used to keep track of thread objects
	private ArrayList<PTAObject> threadObjects = new ArrayList<>();
	// use to store the classes which have had static  objects evaluated
	private HashSet<String> loadedClasses = new HashSet<>();
	
	/**
	 * Used to store static objects.
	 */
	private HashMap<String, PTAObject> staticObjects;
	
	// output variables
	private PTAObject output = null;
	// false iff the instruction cannot execute with the given state
	public PTAInstructionExecutor() {
		
	}
	
	public boolean hasOutput() {
		return output != null;
	}
	
	public void resetOutput() {
		this.output = null;
	}
	
	private void notValid() {
		throw new PTAInstructionExecutorException("No message", getCurrentInstructionLocation());
	}
	
	private void notValid(String message) {
		throw new PTAInstructionExecutorException(message, getCurrentInstructionLocation());
	}
	
	public void setCPG(ConstantPoolGen cpg) {
		this.cpg = cpg;
	}

	public void setLocalVariables(ArrayList<PTAObject> localVariables) {
		this.localVariables = localVariables;
	}
	
	public void setCurrentStack(FakeStack fakeStack) {
		this.currentStack = fakeStack;
	}
	
	public FakeStack getStack() {
		return this.currentStack;
	}
	
	public void setInstructionLocation(InstructionLocation currentInstructionLocation) {
		this.currentInstructionLocation = currentInstructionLocation;
	}
	
	// todo: reduce heap use, by simply setting instruction line,
	// so this instruction simply creates a new location
	public InstructionLocation getCurrentInstructionLocation() {
		return this.currentInstructionLocation;
	}
	
	
	private PTAObject getAndPopObject() {
		PTAObject result = currentStack.getObject();
		currentStack.pop();
		return result;
	}
	

	@Override
	public void visitIADD(IADD obj) {
		PTAObject currObject = currentStack.getObject();
		currentStack.pop();
		PTAObject nextObject = currentStack.getObject();
		currentStack.pop();
		if (currObject == null || nextObject == null) {
			this.notValid("Adding when null on stack");
		}
		currentStack.push(PTAObject.getUnknownIntObject());
	}

	@Override
	public void visitINEG(INEG obj) {
		PTAObject currObject = currentStack.getObject();
		currentStack.pop();
		if (currObject.isIntValue()) {
			int result = currObject.getValue();
			currentStack.push(PTAIntValue.fromValue(result));
			return;
		}
		currentStack.push(PTAObject.getUnknownIntObject());
	}

	// does not compute arithmetic, cause it will be too expensive espcially
	// during loops
	@Override
	public void visitIMUL(IMUL obj) {
		PTAObject currObject = currentStack.getObject();
		currentStack.pop();
		PTAObject nextObject = currentStack.getObject();
		currentStack.pop();
		if (nextObject == null || currObject == null) {
			notValid("Invalid state, attempt to INEG only one item on stack");
		}
		currentStack.push(PTAObject.getUnknownIntObject());
	}

	@Override
	public void visitArrayInstruction(ArrayInstruction obj) {
		// pop the top array ref and index
		// cannot get the object ref stored in this array ref though
		PTAObjectArrayRef arrayRef = (PTAObjectArrayRef) currentStack.getObject();
		currentStack.pop();
		PTAObject intVal = currentStack.getObject();
		currentStack.pop();
		if (!intVal.isType(Type.INT)) {
			this.notValid("Invalid array index");
		}
		PTAObject resultingRef = null;
		if (intVal.isIntValue()) {
			// when we do know the location
			resultingRef = arrayRef.getPtaObject(intVal.getValue());
		} else {
			// when we do not know the location of the object
			resultingRef = arrayRef.getPtaObject();
		}
		if (resultingRef == null) {
			this.notValid("Got null in array ref");
		}
		currentStack.push(resultingRef);
	}


	@Override
	public void visitSWAP(SWAP obj) {
		
		// pop two
		PTAObject k1 = null;
		PTAObject k2 = null;
		k1 = currentStack.getObject();
		currentStack.pop();
		
		k2 = currentStack.getObject();
		currentStack.pop();
		
		currentStack.push(k1);
		currentStack.push(k2);
		
		if (k1.isDoubleOrLong() || k2.isDoubleOrLong()) {
			this.notValid("Attempting to SWAP double or long");
		}
	}

	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		// send the return object to output, null if void
		if (obj.getOpcode() == Const.RETURN) {
			this.output = null;
			currentStack.clear();
			return;
		}
		this.output = currentStack.getObject();
		if (this.output == null) {
			this.notValid("Wrong return type");
		} else if (this.output != null &&
				obj.getOpcode() != Const.RETURN &&
				!this.output.isType(obj.getType())) {
			// deal with wrong type
			this.notValid("Wrong return type");
		}
		// clear the stack.
		currentStack.clear();	
	}
	
	
	
	
	@Override
	public void visitCHECKCAST(CHECKCAST obj) {
		if (currentStack.getObject() == null) {
			this.notValid("Nothign to do check cast on");
		}
	}

	@Override
	public void visitMONITORENTER(MONITORENTER obj) {
		currentStack.pop();
	}

	@Override
	public void visitMONITOREXIT(MONITOREXIT obj) {
		currentStack.pop();
	}

	// todo: consider generating a new type to contain the value
	// of the LDC - not needed for PTA tho
	@Override
	public void visitLDC(LDC obj) {
		Type type = obj.getType(cpg);
		currentStack.push(PTAObject.fromPrimitive(type));
	}
	@Override
	public void visitLDC2_W(LDC2_W obj) {
		Type type = obj.getType(cpg);
		currentStack.push(PTAObject.fromPrimitive(type));
	}
	@Override
	public void visitLoadInstruction(LoadInstruction obj) {
		currentStack.push(localVariables.get(obj.getIndex()));
	}
	
	
	
	@Override
	public void visitConstantPushInstruction(ConstantPushInstruction obj) {
		if (obj.getType(cpg).equals(Type.INT)) {
			currentStack.push(PTAIntValue.fromValue(obj.getValue().intValue()));
			return;
		}
		currentStack.push(PTAObject.fromPrimitive(obj.getType(cpg)));
	}
	
	@Override
	public void visitNEW(NEW obj) {
		// get the type
		ObjectType objType = obj.getLoadClassType(cpg);
		String objClassName = objType.getClassName();
		PTAObject createdObject = new PTAObject(getCurrentInstructionLocation());
		createdObject.addType(objType);
		try {
			JavaClass jc = Repository.lookupClass(objClassName);
			// lookup the objects superclass
			JavaClass[] superClasses = jc.getSuperClasses();
			for (int i = 0; i < superClasses.length; ++i) {
				createdObject.addSuperClass(superClasses[i].getClassName());
				if (superClasses[i].getClassName().equals(Thread.class.getName())) {
					threadObjects.add(createdObject);
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentStack.push(createdObject);
	}

	@Override
	public void visitNEWARRAY(NEWARRAY obj) {
		currentStack.pop();
		currentStack.push(new PTAObjectArrayRef(getCurrentInstructionLocation()));
	}

	@Override
	public void visitANEWARRAY(ANEWARRAY obj) {
		currentStack.pop();
		currentStack.push(new PTAObjectArrayRef(getCurrentInstructionLocation()));
	}

	@Override
	public void visitMULTIANEWARRAY(MULTIANEWARRAY obj) {
		int dim = obj.getDimensions();
		currentStack.pop();
		for (int i = 0; i < dim; ++i) {
			currentStack.pop();
		}
		// push Object representing arrayref
		PTAObject arrayRef = new PTAObjectArrayRef(getCurrentInstructionLocation());
		currentStack.push(arrayRef);
	}

	@Override
	public void visitSelect(Select obj) {
		currentStack.pop();
	}

	@Override
	public void visitPOP(POP obj) {
		currentStack.pop();
	}

	@Override
	public void visitPOP2(POP2 obj) {
		currentStack.popWords(2);
	}

	// Dup instructions
	@Override
	public void visitDUP(DUP obj) {
		currentStack.push(currentStack.getObject());
	}
	
	@Override
	public void visitDUP_X1(DUP_X1 obj) {
		PTAObject toDup = currentStack.getObject();
		currentStack.pop();
		PTAObject temp = currentStack.getObject();
		currentStack.pop();
		currentStack.push(toDup);
		currentStack.push(temp);
		currentStack.push(toDup);
	}
	
	@Override
	public void visitDUP_X2(DUP_X2 obj) {
		// TODO Auto-generated method stub
		super.visitDUP_X2(obj);
	}
	@Override
	public void visitDUP2(DUP2 obj) {
		// duplicate the top two words
		// depends on whether the top is a word or not
		PTAObject stackTop = currentStack.getObject();
		if (stackTop.isDoubleOrLong()) {
			// dup only one.
			// might not need the instruction location tho...
			currentStack = currentStack.push(stackTop);
			return;
		}
		
		// stackTop2, stackTop -> stackTop2, stackTop, stackTop2, stackTop
		FakeStack stackPrev = currentStack.pop();
		PTAObject stackTop2 = stackPrev.getObject();
		currentStack = currentStack.push(stackTop2);
		currentStack = currentStack.push(stackTop);
		
	}
	@Override
	public void visitDUP2_X1(DUP2_X1 obj) {
		FakeStack secondary = currentStack.popWords(2);
		PTAObject third = currentStack.getObject();
		currentStack.pop();
		LinkedList<PTAObject> nextValues = new LinkedList<>();
		while (secondary.hasValues()) {
			PTAObject temp = secondary.getObject();
			nextValues.add(temp);
			currentStack.push(temp);
			secondary.pop();
		}
		currentStack.push(third);
		for (PTAObject oldObjects: nextValues) {
			currentStack.push(oldObjects);
		}
	}
	@Override
	public void visitDUP2_X2(DUP2_X2 obj) {
		// the bottom should be duplicated
		// 1 2 3 4
		// secondary: 4 3
		// third: 2 1
		// goal -> 3 4 1 2 3 4
		FakeStack secondary = currentStack.popWords(2);
		FakeStack third = currentStack.popWords(2);
		List<PTAObject> secondaryValues = new LinkedList<>();
		
		// 4 3 -> 3 4, secondaryValues: 3 4
		// 
		while (secondary.hasValues()) {
			PTAObject currObject = secondary.getObject();
			currentStack.push(currObject);
			secondaryValues.add(currObject);
			secondary.pop();
		}
		currentStack.addStack(third);
		for (PTAObject prevObjects: secondaryValues) {
			currentStack.push(prevObjects);
		}
		
	}

	// todo: what to do during jsr
	@Override
	public void visitJsrInstruction(JsrInstruction obj) {
		// TODO Auto-generated method stub
		super.visitJsrInstruction(obj);
	}

	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		// update accesses -> current object field is accessed here
		
		PTAObject value = currentStack.getObject();
		currentStack.pop();
		PTAObject objectRef = currentStack.getObject();
		currentStack.pop();
		// read
		value.addAccessLocation(getCurrentInstructionLocation());
		// write
		objectRef.addAccessLocation(getCurrentInstructionLocation());
	}

	
	
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		// update accesses -> current object field is accessed here
		// TODO Auto-generated method stub
		// read
		PTAObject objectRef = this.getAndPopObject();
		String fieldName = obj.getFieldName(cpg);
		Type type = obj.getFieldType(cpg);
		PTAObject getResult = objectRef.getField(fieldName);
		objectRef.addAccessLocation(getCurrentInstructionLocation());
		getResult.addAccessLocation(getCurrentInstructionLocation());
		currentStack.push(getResult);
	}

	@Override
	public void visitGETSTATIC(GETSTATIC obj) {
		String sig = obj.getSignature(cpg);
		currentStack.push(staticObjects.get(sig));
	}

	@Override
	public void visitPUTSTATIC(PUTSTATIC obj) {
		PTAObject currObject = getAndPopObject();
		staticObjects.put(obj.getSignature(cpg), currObject);
	}

	// TODO: update access to object iff virtual
	// have to resolve the virtual function dynamically - need to check if the object has been initialised
	// if we do it in order -> the initialisation should have already been called,
	// so it's possible to have that as an assumption, that the actual class of this object can be resolved.
	@Override
	public void visitInvokeInstruction(InvokeInstruction obj) {
		// TODO Auto-generated method stub
		super.visitInvokeInstruction(obj);
	}
	
	
	
}
