package conc_trace.instr.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

/**
 * Represents the CFG for a given bytecode method
 * @author Gabau
 *
 */
public class JavaInstructionCFG {
	private MethodGen method;
	private JavaInstructionCFGNode root;
	private List<JavaInstructionCFGNode> exitList = new LinkedList<>();
	private List<JavaInstructionCFGNode> nodeList = new LinkedList<>();
	private List<InstructionHandle> virtualInvokers;
	
	private JavaInstructionCFG(MethodGen method) {
		this.method = method;
	}
	
	public JavaInstructionCFGNode getRoot() {
		return this.root;
	}
	
	public MethodGen getMethod() {
		return this.method;
	}
	
	public ConstantPoolGen getCPG() {
		return this.method.getConstantPool();
	}
	
	public static String generateHash(MethodGen method) {
		return String.format("%s:%s:%s", method.getClassName(), method.getName(), method.getSignature());
	}
	
	/**
	 * Construct the cfg for each java instruction
	 * @param method
	 * @return
	 */
	public static HashMap<String, JavaInstructionCFG> fromMethodRecursive(MethodGen method) {
		return fromMethodRecursive(method, new HashMap<>());
	}
	
	public static HashMap<String, JavaInstructionCFG> fromMethodRecursive(MethodGen method, 
			HashMap<String, JavaInstructionCFG>  result) {
		if (method == null || method.getInstructionList() == null) {
			return result;
		}
		List<MethodGen> toProcess = new LinkedList<>();
		String hash = generateHash(method);
		if (result.containsKey(hash)) {
			return result;
		}
		JavaInstructionCFG mainInstructionCFG = new JavaInstructionCFG(method);
		result.put(hash, mainInstructionCFG);
		// generate the possible nodes first
		HashMap<Integer, JavaInstructionCFGNode> nodeMap = new HashMap<>();
		

		InstructionList insList = method.getInstructionList();
		int[] instructionPositions = insList.getInstructionPositions();
		HashMap<Integer, Integer> inversePositionMap = new HashMap<>();
		for (int i = 0; i < instructionPositions.length; ++i) {
			inversePositionMap.put(instructionPositions[i], i);
		}
		
		LinkedList<InstructionHandle> virtualFunctionCalls = new LinkedList<>();
		mainInstructionCFG.virtualInvokers = virtualFunctionCalls;
		// generate the blocks for the current method.
		// the goto list for branch instructions
		// sorted in ascending order.
		SortedSet<Integer> gotoList = generateGotoList(method, result, inversePositionMap, true, 
				virtualFunctionCalls);
		int temp = gotoList.first();
		// add the end of the list.
		generateCFGBlocks(method, nodeMap, gotoList);
		// put the first one as root.
		mainInstructionCFG.root = nodeMap.get(temp);
		
		// populate the objects in the graph, so we can release nodeMap
		for (Integer index : nodeMap.keySet()) {
			JavaInstructionCFGNode node = nodeMap.get(index);
			mainInstructionCFG.nodeList.add(node);
			if (node.isEnd()) {
				mainInstructionCFG.exitList.add(node);
			}
			for (Integer nextNodeIndex: node.getNextNodes()) {
				node.addChild(nodeMap.get(inversePositionMap.get(nextNodeIndex)));
			}
		}
		return result;
		
	}

	/**
	 * Method is used to generate the list of positions which
	 * a branch can reach. Will split the code into these positions
	 * in one pass first.
	 * This indexing is based of instruction indexing.
	 */
	private static SortedSet<Integer> generateGotoList(MethodGen method, 
			HashMap<String, JavaInstructionCFG> result,
			HashMap<Integer, Integer> inversePositionMap,
			boolean doRecursive,
			LinkedList<InstructionHandle> virtualInstructions) {
		int ctr = 0;
		SortedSet<Integer> gotoList = new TreeSet<>();
		// the start of the set.
		gotoList.add(0);

		InstructionList insList = method.getInstructionList();
		if (insList == null) {
			return gotoList;
		}
		// generate inverse map
		for (InstructionHandle instr : insList) {
			// check if instr is branch
			if (instr.getInstruction() instanceof BranchInstruction) {
				BranchInstruction branchInstruction = (BranchInstruction) instr.getInstruction();
				int location = inversePositionMap.get(branchInstruction.getTarget().getPosition());
				if (branchInstruction instanceof IfInstruction) {
					// will generate two branches
					gotoList.add(ctr + 1);
					gotoList.add(location);
				} else {
					// will generate only one branch
					gotoList.add(location);
				}				
			}
			if (instr.getInstruction() instanceof InvokeInstruction) {
				InvokeInstruction invoke = (InvokeInstruction) instr.getInstruction();
				String methodName = invoke.getMethodName(method.getConstantPool());
				String methodClassName = invoke.getClassName(method.getConstantPool());
				String methodSignature = invoke.getSignature(method.getConstantPool());
				try {
					JavaClass methodJavaClass = Repository.lookupClass(methodClassName);
					ClassGen cg = new ClassGen(methodJavaClass);
					MethodGen selectedMethod = null;
					for (Method innerMethod : methodJavaClass.getMethods()) {
						if (innerMethod.getSignature().equals(methodSignature)
								&& innerMethod.getName().equals(methodName)) {
							selectedMethod = new MethodGen(innerMethod, methodClassName, cg.getConstantPool());
							break;
						}
					}
					// do this for static
					if (doRecursive && instr.getInstruction() instanceof INVOKESTATIC) {
						JavaInstructionCFG.fromMethodRecursive(selectedMethod, result);
					} else if (doRecursive && instr.getInstruction() instanceof INVOKEVIRTUAL) {
						// add to the list of virtual calls in this CFG
						// will be resolved during Points to analysis, as we can determine
						// the possible types that this object can be.
						virtualInstructions.add(instr);
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ctr++;
		}
		return gotoList;
	}

	private static void generateCFGBlocks(MethodGen method, 
			HashMap<Integer, JavaInstructionCFGNode> nodeMap,
			SortedSet<Integer> gotoList) {
		gotoList.add(method.getInstructionList().size());
		InstructionHandle[] methodInstructionHandles = method.getInstructionList().getInstructionHandles();
		Instruction[] instructions = method.getInstructionList().getInstructions();
		// generate the List of nodes from the set of intervals
		while (!gotoList.isEmpty()) {
			int firstIndex = gotoList.first();
			List<Instruction> handles = new LinkedList<>();
			gotoList.remove(firstIndex);
			int curIndex = firstIndex;
			// deal with when the curIndex is the last one
			if (gotoList.isEmpty()) {
				break;
			}
			while (curIndex != gotoList.first() && curIndex < instructions.length) {
				handles.add(instructions[curIndex]);
				curIndex++;
			}
			// add the block to the set
			nodeMap.put(firstIndex, new JavaInstructionCFGNode(handles, 
					methodInstructionHandles[curIndex - 1],
					methodInstructionHandles[firstIndex]));
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}
	
	
	
}
