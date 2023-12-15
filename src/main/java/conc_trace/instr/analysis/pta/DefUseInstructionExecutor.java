package conc_trace.instr.analysis.pta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.bcel.Const;
import org.apache.bcel.generic.*;

import conc_trace.instr.analysis.pta.model.Definition;
import conc_trace.instr.analysis.pta.model.IntValueDefinition;
import conc_trace.instr.analysis.pta.model.KillSet;
import conc_trace.instr.analysis.pta.model.MergedDefinitions;
import conc_trace.instr.analysis.pta.model.PrimitiveDefinition;
import conc_trace.instr.analysis.pta.model.StaticDefinition;

/**
 * Generates def use pair for instructions in a method.
 * Important instructions to take note of for generation of def use
 * All the new instructions
 * 
 * Treats static variables as a MergedDefinition.
 * Pass into other methods.
 * 
 * 
 * 
 * @author Gabau
 */
public class DefUseInstructionExecutor extends EmptyVisitor {
	
	private Stack<Definition> operandStack = new Stack<>();
	private ConstantPoolGen constantPoolGen;
	private ArrayList<Definition> localVariables;
	private int currentOffset;
	private ArrayList<Integer> invokations = new ArrayList<>();
	private HashMap<Integer, Definition> locDefinitions = new HashMap<>();
	private MergedDefinitions resultDefinitions = new MergedDefinitions();
	private StaticDefinition staticDefinition = new StaticDefinition();
	private boolean returnHit = false;
	
	public HashMap<Integer, Definition> getDefinitions() {
		return locDefinitions;
	}
	
	public void setCPG(ConstantPoolGen cpg) {
		this.constantPoolGen = cpg;
	}

	public boolean returnReached() {
		return returnHit;
	}
	
	public void setCurrentOffset(int value) {
		this.currentOffset = value;
	}
	
	public void initialiseParamSize(int size) {
		localVariables = new ArrayList<>();
		localVariables.ensureCapacity(size);
		for (int i = 0; i < size; ++i) {
			// reserve -5 -> -10 for param
			localVariables.add(Definition.createArgument(i));
		}
	}
	
	public KillSet getKillSet() {
		return null;
	}
	
	@Override
	public void visitACONST_NULL(ACONST_NULL obj) {
		// TODO Auto-generated method stub
		operandStack.push(PrimitiveDefinition.getPrimitive(Type.NULL));
	}


	@Override
	public void visitARRAYLENGTH(ARRAYLENGTH obj) {
		operandStack.push(PrimitiveDefinition.getPrimitive(Type.INT));
	}

	@Override
	public void visitATHROW(ATHROW obj) {
		// TODO: handle control flow after throw
	}

	@Override
	public void visitBIPUSH(BIPUSH obj) {
		
	}

	@Override
	public void visitBranchInstruction(BranchInstruction obj) {
		// end of node.
	}

	@Override
	public void visitBREAKPOINT(BREAKPOINT obj) {
		// Do nothing
	}

	@Override
	public void visitCASTORE(CASTORE obj) {
		// array store definition
		// the thing is this is a primitive array -> we can actually ignore those
		operandStack.pop();
		operandStack.pop();
		operandStack.pop();
	}

	@Override
	public void visitCHECKCAST(CHECKCAST obj) {
		// does nothing
	}

	@Override
	public void visitConstantPushInstruction(ConstantPushInstruction obj) {
		if (obj.getType(constantPoolGen).equals(Type.INT)) {
			operandStack.push(IntValueDefinition.getValue(obj.getValue().intValue()));
			return;
		}
		operandStack.push(PrimitiveDefinition.getPrimitive(obj.getType(constantPoolGen)));
	}

	@Override
	public void visitConversionInstruction(ConversionInstruction obj) {
		operandStack.pop();
		operandStack.push(PrimitiveDefinition.getPrimitive(obj.getType(constantPoolGen)));
	}


	@Override
	public void visitDASTORE(DASTORE obj) {
		// can ignore
		operandStack.pop();
		operandStack.pop();
		operandStack.pop();
	}

	@Override
	public void visitDCMPG(DCMPG obj) {
		operandStack.pop();
		operandStack.pop();
		operandStack.pop();
		operandStack.push(PrimitiveDefinition.getPrimitive(Type.INT));
	}

	@Override
	public void visitDCMPL(DCMPL obj) {
		operandStack.pop();
		operandStack.pop();
		operandStack.pop();
		operandStack.push(PrimitiveDefinition.getPrimitive(Type.INT));
	}


	@Override
	public void visitArithmeticInstruction(ArithmeticInstruction obj) {
		int opCode = obj.getOpcode();
		Definition a1 = null;
		Definition a2 = null;
		switch (opCode) {
		case Const.IADD:
		case Const.DADD:
		case Const.LADD:
		case Const.FADD:
			a1 = operandStack.pop();
			a2 = operandStack.pop();
			operandStack.push(a1.add(a2));
			return;
		case Const.ISUB:
		case Const.DSUB:
		case Const.FSUB:
		case Const.LSUB:
			a1 = operandStack.pop();
			a2 = operandStack.pop();
			operandStack.push(a2.subtract(a1));
			return;
		case Const.IDIV:
		case Const.DDIV:
		case Const.FDIV:
		case Const.LDIV:
			a1 = operandStack.pop();
			a2 = operandStack.pop();
			operandStack.push(a2.divide(a1));
			return;
		case Const.INEG:
		case Const.DNEG:
		case Const.FNEG:
		case Const.LNEG:
			operandStack.push(operandStack.pop().neg());
			return;
			// TODO: add other ops
		case Const.IOR:
			return;
		case Const.IAND:
			return;
		case Const.ISHR:
			return;
		case Const.ISHL:
			return;
		// and other ops.
		}
	}


	@Override
	public void visitDRETURN(DRETURN obj) {
		
	}


	@Override
	public void visitDSUB(DSUB obj) {
	 	Definition d1 = operandStack.pop();
		Definition d2 = operandStack.pop();
		operandStack.push(d1.subtract(d2));
	}

	@Override
	public void visitDUP(DUP obj) {
		Definition k = operandStack.peek();
		operandStack.push(k);
	}

	@Override
	public void visitDUP_X1(DUP_X1 obj) {
		// 2 1 -> 1 2 1
		Definition a1 = operandStack.pop();
		Definition a2 = operandStack.pop();
		operandStack.push(a1);
		operandStack.push(a2);
		operandStack.push(a1);
	}

	@Override
	public void visitDUP_X2(DUP_X2 obj) {
		// TODO Auto-generated method stub
		super.visitDUP_X2(obj);
	}

	@Override
	public void visitDUP2_X1(DUP2_X1 obj) {
		// TODO Auto-generated method stub
		super.visitDUP2_X1(obj);
	}

	@Override
	public void visitDUP2_X2(DUP2_X2 obj) {
		// TODO Auto-generated method stub
		super.visitDUP2_X2(obj);
	}

	@Override
	public void visitExceptionThrower(ExceptionThrower obj) {
		// TODO Auto-generated method stub
		super.visitExceptionThrower(obj);
	}



	@Override
	public void visitFASTORE(FASTORE obj) {
		// TODO Auto-generated method stub
		super.visitFASTORE(obj);
	}


	@Override
	public void visitFieldInstruction(FieldInstruction obj) {
		// TODO Auto-generated method stub
		super.visitFieldInstruction(obj);
	}

	@Override
	public void visitFieldOrMethod(FieldOrMethod obj) {
		// TODO Auto-generated method stub
		super.visitFieldOrMethod(obj);
	}


	@Override
	public void visitIASTORE(IASTORE obj) {
		// TODO Auto-generated method stub
		super.visitIASTORE(obj);
	}


	@Override
	public void visitIDIV(IDIV obj) {
		// TODO Auto-generated method stub
		super.visitIDIV(obj);
	}

	@Override
	public void visitIF_ACMPEQ(IF_ACMPEQ obj) {
		// TODO Auto-generated method stub
		super.visitIF_ACMPEQ(obj);
	}

	@Override
	public void visitIF_ACMPNE(IF_ACMPNE obj) {
		// TODO Auto-generated method stub
		super.visitIF_ACMPNE(obj);
	}

	@Override
	public void visitIF_ICMPEQ(IF_ICMPEQ obj) {
		// TODO Auto-generated method stub
		super.visitIF_ICMPEQ(obj);
	}

	@Override
	public void visitIF_ICMPGE(IF_ICMPGE obj) {
		// TODO Auto-generated method stub
		super.visitIF_ICMPGE(obj);
	}

	@Override
	public void visitIF_ICMPGT(IF_ICMPGT obj) {
		// TODO Auto-generated method stub
		super.visitIF_ICMPGT(obj);
	}

	@Override
	public void visitIF_ICMPLE(IF_ICMPLE obj) {
		// TODO Auto-generated method stub
		super.visitIF_ICMPLE(obj);
	}

	@Override
	public void visitIF_ICMPLT(IF_ICMPLT obj) {
		// TODO Auto-generated method stub
		super.visitIF_ICMPLT(obj);
	}

	@Override
	public void visitIF_ICMPNE(IF_ICMPNE obj) {
		// TODO Auto-generated method stub
		super.visitIF_ICMPNE(obj);
	}

	@Override
	public void visitIFEQ(IFEQ obj) {
		// TODO Auto-generated method stub
		super.visitIFEQ(obj);
	}

	@Override
	public void visitIFGE(IFGE obj) {
		// TODO Auto-generated method stub
		super.visitIFGE(obj);
	}

	@Override
	public void visitIFGT(IFGT obj) {
		// TODO Auto-generated method stub
		super.visitIFGT(obj);
	}


	@Override
	public void visitIFLE(IFLE obj) {
		// TODO Auto-generated method stub
		super.visitIFLE(obj);
	}

	@Override
	public void visitIFLT(IFLT obj) {
		// TODO Auto-generated method stub
		super.visitIFLT(obj);
	}

	@Override
	public void visitIFNE(IFNE obj) {
		// TODO Auto-generated method stub
		super.visitIFNE(obj);
	}

	@Override
	public void visitIFNONNULL(IFNONNULL obj) {
		// TODO Auto-generated method stub
		super.visitIFNONNULL(obj);
	}

	@Override
	public void visitIFNULL(IFNULL obj) {
		// TODO Auto-generated method stub
		super.visitIFNULL(obj);
	}

	@Override
	public void visitIINC(IINC obj) {
		// TODO Auto-generated method stub
		super.visitIINC(obj);
	}


	@Override
	public void visitIMPDEP1(IMPDEP1 obj) {
		// TODO Auto-generated method stub
		super.visitIMPDEP1(obj);
	}

	@Override
	public void visitIMPDEP2(IMPDEP2 obj) {
		// TODO Auto-generated method stub
		super.visitIMPDEP2(obj);
	}


	@Override
	public void visitINSTANCEOF(INSTANCEOF obj) {
		// TODO Auto-generated method stub
		super.visitINSTANCEOF(obj);
	}

	@Override
	public void visitINVOKEDYNAMIC(INVOKEDYNAMIC obj) {
		// TODO Auto-generated method stub
		super.visitINVOKEDYNAMIC(obj);
	}

	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		// TODO Auto-generated method stub
		super.visitINVOKEINTERFACE(obj);
	}

	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		// TODO Auto-generated method stub
		super.visitINVOKESPECIAL(obj);
	}

	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		// TODO Auto-generated method stub
		super.visitINVOKEVIRTUAL(obj);
	}


	@Override
	public void visitJSR(JSR obj) {
		// TODO Auto-generated method stub
		super.visitJSR(obj);
	}

	@Override
	public void visitJSR_W(JSR_W obj) {
		// TODO Auto-generated method stub
		super.visitJSR_W(obj);
	}

	@Override
	public void visitJsrInstruction(JsrInstruction obj) {
		// TODO Auto-generated method stub
		super.visitJsrInstruction(obj);
	}


	@Override
	public void visitLASTORE(LASTORE obj) {
		// TODO Auto-generated method stub
		super.visitLASTORE(obj);
	}

	@Override
	public void visitLCMP(LCMP obj) {
		// TODO Auto-generated method stub
		super.visitLCMP(obj);
	}



	@Override
	public void visitPopInstruction(PopInstruction obj) {
		// TODO Auto-generated method stub
		super.visitPopInstruction(obj);
	}


	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		// use the object to be put into this field
		Definition used = operandStack.pop();
		used.addUse(currentOffset);
		Definition objectRef = operandStack.pop();
		objectRef.putField(obj.getFieldName(constantPoolGen), used);
	}

	@Override
	public void visitSASTORE(SASTORE obj) {
		// TODO Auto-generated method stub
		super.visitSASTORE(obj);
	}

	
	@Override
	public void visitStoreInstruction(StoreInstruction obj) {
		Definition toStore = operandStack.pop();
		localVariables.set(obj.getIndex(), toStore);
	}

	// TODO handle array instructions
	@Override
	public void visitAALOAD(AALOAD obj) {
		// TODO Auto-generated method stub
		super.visitAALOAD(obj);
	}

	@Override
	public void visitALOAD(ALOAD obj) {
		// TODO Auto-generated method stub
		super.visitALOAD(obj);
	}

	@Override
	public void visitBALOAD(BALOAD obj) {
		// TODO Auto-generated method stub
		super.visitBALOAD(obj);
	}

	@Override
	public void visitCALOAD(CALOAD obj) {
		// TODO Auto-generated method stub
		super.visitCALOAD(obj);
	}

	@Override
	public void visitDALOAD(DALOAD obj) {
		// TODO Auto-generated method stub
		super.visitDALOAD(obj);
	}

	@Override
	public void visitFALOAD(FALOAD obj) {
		// TODO Auto-generated method stub
		super.visitFALOAD(obj);
	}

	@Override
	public void visitIALOAD(IALOAD obj) {
		// TODO Auto-generated method stub
		super.visitIALOAD(obj);
	}

	@Override
	public void visitLALOAD(LALOAD obj) {
		// TODO Auto-generated method stub
		super.visitLALOAD(obj);
	}

	@Override
	public void visitSALOAD(SALOAD obj) {
		Definition integerRef = operandStack.pop();
		Definition arrayRef = operandStack.pop();
		arrayRef.addUse(currentOffset, integerRef);
	}

	@Override
	public void visitAASTORE(AASTORE obj) {
		// TODO: upload value to array.
		Definition value = operandStack.pop();
		Definition integerRef = operandStack.pop();
		Definition arrayRef = operandStack.pop();
		// treat it like a field?
		arrayRef.addUse(currentOffset, integerRef);
		
	}
	
	@Override
	public void visitSWAP(SWAP obj) {
		Definition d1 = operandStack.pop();
		Definition d2 = operandStack.pop();
		
		operandStack.push(d1);
		operandStack.push(d2);
	}

	// should not be needed given the CFG nature of the execution
	public boolean hitReturn() {
		return returnHit;
	}
	
	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		// end of execution
		resultDefinitions.merge(operandStack.peek());
		returnHit = true;
	}

	@Override
	public void visitMONITORENTER(MONITORENTER obj) {
		operandStack.pop();
	}

	@Override
	public void visitMONITOREXIT(MONITOREXIT obj) {
		operandStack.pop();
	}

	@Override
	public void visitLDC(LDC obj) {
		operandStack.push(PrimitiveDefinition.getPrimitive(obj.getType(constantPoolGen)));
	}

	@Override
	public void visitLDC2_W(LDC2_W obj) {
		operandStack.push(PrimitiveDefinition.getPrimitive(obj.getType(constantPoolGen)));
	}

	@Override
	public void visitLoadInstruction(LoadInstruction obj) {
		operandStack.push(localVariables.get(obj.getIndex()));
	}

	private Definition generateDefinition() {
		if (!locDefinitions.containsKey(currentOffset)) {
			locDefinitions.put(currentOffset, new Definition(currentOffset));
		}
		return locDefinitions.get(currentOffset);
	}
	
	@Override
	public void visitInvokeInstruction(InvokeInstruction obj) {
		this.invokations.add(currentOffset);
	}

	@Override
	public void visitPOP(POP obj) {
		operandStack.pop();
	}

	@Override
	public void visitPOP2(POP2 obj) {
		Definition topObject = operandStack.peek();
		if (!topObject.isDoubleLength()) {
			operandStack.pop();
		}
	}
	
	

	@Override
	public void visitANEWARRAY(ANEWARRAY obj) {
		operandStack.pop();
		operandStack.push(generateDefinition());
	}

	@Override
	public void visitNEWARRAY(NEWARRAY obj) {
		operandStack.pop();
		operandStack.push(generateDefinition());
	}

	@Override
	public void visitGETFIELD(GETFIELD obj) {
		Definition objectRef = operandStack.peek();
		objectRef.addUse(currentOffset);
		operandStack.push(objectRef.getField(obj.getFieldName(constantPoolGen)));
	}

	
	@Override
	public void visitMULTIANEWARRAY(MULTIANEWARRAY obj) {
		int dim = obj.getDimensions();
		operandStack.pop();
		for (int i = 0; i < dim; ++i) {
			operandStack.pop();
		}
		Definition def = generateDefinition();
		operandStack.push(def);
	}
	

	@Override
	public void visitSelect(Select obj) {
		operandStack.pop();
	}

	// TODO: deal with Dups
	@Override
	public void visitDUP2(DUP2 obj) {
		// duplicate the top two words
		// depends on whether the top is a word or not
		Definition stackTop = operandStack.peek();
		if (stackTop.isDoubleLength()) {
			// dup only one.
			// might not need the instruction location tho...
			operandStack.push(stackTop);
			return;
		}
		
		// stackTop2, stackTop -> stackTop2, stackTop, stackTop2, stackTop
		operandStack.pop();
		Definition stackTop2 = operandStack.peek();
		operandStack.push(stackTop2);
		operandStack.push(stackTop);
		
	}


	@Override
	public void visitNEW(NEW obj) {
		// create the object to push to stack
		Definition objectCreated = Definition.ofNew(currentOffset);
		this.locDefinitions.put(currentOffset, objectCreated);
		operandStack.push(objectCreated);
	}
	
	
	
	
	// TODO: deal with static resolution for def-use generation
	@Override
	public void visitGETSTATIC(GETSTATIC obj) {
		// the static field might be modified though
		// TODO: resolve static ops
		Definition objObtained = staticDefinition.getField(obj.getSignature(constantPoolGen));
		objObtained.addUse(currentOffset);
	}

	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		// TODO Auto-generated method stub
		super.visitINVOKESTATIC(obj);
	}

	@Override
	public void visitPUTSTATIC(PUTSTATIC obj) {
		String signature = obj.getSignature(constantPoolGen);
		Definition toPut = operandStack.pop();
		staticDefinition.putField(signature, toPut);
		toPut.addUse(currentOffset);
	}


	
}
