package classloading_test.classloading.analysis.pta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.DUP2_X1;
import org.apache.bcel.generic.DUP2_X2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.Type;

import classloading_test.classloading.analysis.InstructionLocation;

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
	public PTAInstructionExecutor() {
		
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
	
	
	

	
	@Override
	public void visitLoadInstruction(LoadInstruction obj) {
		currentStack.push(localVariables.get(obj.getIndex()));
	}
	@Override
	public void visitICONST(ICONST obj) {
		// TODO Auto-generated method stub
		currentStack.push(new PTAObject(Type.INT));
	}
	
	@Override
	public void visitBIPUSH(BIPUSH obj) {
		currentStack.push(new PTAObject(Type.INT));
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
	
	
	
}
