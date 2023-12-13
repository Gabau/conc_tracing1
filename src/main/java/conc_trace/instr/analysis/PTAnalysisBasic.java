package conc_trace.instr.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import conc_trace.instr.analysis.pta.FakeStack;
import conc_trace.instr.analysis.pta.PTAObject;

public class PTAnalysisBasic implements PTAnalysis {
	private HashMap<InstructionLocation, HashSet<InstructionLocation>> ptResult;
	// keep track of the number of times we reach a method
	private HashMap<String, Integer> methodsReached;
	
	// stores all the subclasses
	// used for virtual resolution.
	// map superclass -> Set(subclass)
	private HashMap<String, HashSet<String>> subClasses;
	
	private static final String TYPE_DELIM = ";";
	
	// resolve pointers -> virtual - generate CFG -> resolve pointers in new CFG
	// resolve virtual functions -> generate CFG etc...
	private void processMethod(HashMap<String, JavaInstructionCFG> currentCFG,
			JavaInstructionCFG methodCfg) {
		MethodGen mg = methodCfg.getMethod();
		// assumes that the entry point takes in the actual types
		// instead of some subclass
	 	Type[] argTypes = mg.getArgumentTypes();
	 	LinkedList<HashSet<String>> types = new LinkedList<>();
	 	if (!mg.isStatic()) {
	 		// contains reference to this object.
	 		HashSet<String> classSet = new HashSet<>();
	 		classSet.add(mg.getClassName());
	 		types.add(classSet);
	 	}
	 	// store all the types
	 	for (int i = 0; i < argTypes.length; ++i) {
	 		HashSet<String> typeSet = new HashSet<>();
	 		typeSet.add(argTypes[i].getSignature());
	 		types.add(typeSet);
	 	}
	 	processMethod(currentCFG, methodCfg, types);
		
	}
	
	
	/**
	 * Executes the given instructions, and generates the stack.
	 * @param instructions
	 * @param initialStack
	 * @param localVariables
	 * @return
	 */
	private FakeStack executeInstructions(List<Instruction> instructions,
			FakeStack initialStack,
			ArrayList<PTAObject> localVariables) {
		
		
		return null;
	}
	
	// returns the possible types that the return value can take.
	// using string as the directo type for conveinience
	// types is the semi-colon seperated string containing the individual type signatures
	private HashSet<String> processMethod(HashMap<String, JavaInstructionCFG> currentCFG,
			JavaInstructionCFG methodCfg, List<HashSet<String>> types) {
		// this variant does the processing with the type information
		// is used to process method calls with types. -> static or otherwise
		// initialise the local variabes with these types.
		ArrayList<HashSet<String>> localVariables = new ArrayList<>();
		FakeStack currentStack = new FakeStack();
		// optimisation to reduce n^2 performance
		localVariables.ensureCapacity(types.size());
		for (HashSet<String> type: types) {
			localVariables.add(type);
		}
		/**
		 * Worklist algorithm for points-to analysis. Aims at producing
		 * for each object creation, all the possible lines which has 
		 * access to said object.
		 * In-node definition -> set of all possible stack configurations on the stack before code block
		 * Out-node definition -> set of all possible stack configurations after code block
		 * 
		 * Base state:
		 * In-node: empty set
		 * Out-node: all the objects created in this code block and on the stack at the end of block
		 * 
		 * Recursive state:
		 * In-node: Union of predecessor out
		 * Out-node: Out-node_prev union new objects created in this code block- should not change
		 * 
		 * Algorithm will also have to compute within the codeblock, to determine
		 * the points to per instruction.
		 * 
		 * Problems:
		 * Type generation -> will have to be resolved after generating PTA
		 *  * Some objects will have an unknown type, because we cannot run the
		 *  function that creates them until the type has been resolved.
		 */
		
		
		// execute the invocations with the derived types
		
		
		return null;
	}
	
	@Override
	public HashMap<InstructionLocation, HashSet<InstructionLocation>> ptAnalysis(JavaClass entryClass,
			Method entryMethod) {
		ptResult = new HashMap<>();
		methodsReached = new HashMap<>();
		ClassGen cg = new ClassGen(entryClass);
		MethodGen mg = new MethodGen(entryMethod, cg.getClassName(), cg.getConstantPool());
		String entryPointHash = JavaInstructionCFG.generateHash(mg);
		HashMap<String, JavaInstructionCFG> cfg = JavaInstructionCFG.fromMethodRecursive(mg);
		processMethod(cfg, cfg.get(entryPointHash));
		return null;
	}

}
