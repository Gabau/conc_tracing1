package conc_trace.instr.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ReturnInstruction;

/**
 * Represents a minimal cfgnode
 * @author Gabau
 *
 */
public class JavaInstructionCFGNode {
	private List<Instruction> instructions;
	private InstructionHandle lastInstruction;
	private InstructionHandle firstInstruction;
	private List<JavaInstructionCFGNode> children = new LinkedList<>();
	private Collection<Integer> nextNodes;
	public JavaInstructionCFGNode(List<Instruction> instructions, InstructionHandle lastInstruction,
			InstructionHandle firstInstruction) {
		this.instructions = instructions;
		this.lastInstruction = lastInstruction;
		this.nextNodes = generateNextNodes();
		this.firstInstruction = firstInstruction;
	}
	
	public boolean isEnd() {
		return this.nextNodes.isEmpty();
	}
	
	/**
	 * The offset of the next instructions.
	 * @return
	 */
	private Collection<Integer> generateNextNodes() {
		if (!(lastInstruction.getInstruction() instanceof BranchInstruction)) {
			if (lastInstruction.getInstruction() instanceof ReturnInstruction) {
				return Collections.emptyList();
			}
			// return the next line
			LinkedList<Integer> result = new LinkedList<>();
			if (lastInstruction.getNext() == null) {
				return result;
			}
			result.add(lastInstruction.getNext().getPosition());
			return result;
		}

		List<Integer> result = new LinkedList<>();
		BranchInstruction branchInstruction = (BranchInstruction) lastInstruction.getInstruction();
		int currentPos = lastInstruction.getPosition();

		int branchNextPos = branchInstruction.getTarget().getPosition();
		if (branchInstruction instanceof IfInstruction) {
			int nextPos = lastInstruction.getNext().getPosition();
			result.add(nextPos);
			result.add(branchNextPos);
			return result;
		}
		// when it is a goto instruction 
		result.add(branchNextPos);
		return result;
	}
	
	/**
	 * Get the jump location of the last instruction if it is a branch, empty otherwise.
	 * @return
	 */
	public Collection<Integer> getNextNodes() {
		return this.nextNodes;

	}
	
	public List<Instruction> getInstructions() {
		return instructions;
	}
	
	public InstructionHandle getFirst() {
		return this.firstInstruction;
	}
	
	public InstructionHandle getLast() {
		return this.lastInstruction;
	}
	
	public List<JavaInstructionCFGNode> getChildren() {
		return this.children;
	}
	
	public List<JavaInstructionCFGNode> getMethodsCalled() {
		return null;
	}
	
	public void addChild(JavaInstructionCFGNode child) {
		this.children.add(child);
	}
}
