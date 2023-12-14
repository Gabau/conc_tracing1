package conc_trace.instr.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2_X1;
import org.apache.bcel.generic.DUP2_X2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.junit.jupiter.api.Test;

import conc_trace.instr.analysis.pta.FakeStack;
import conc_trace.instr.analysis.pta.PTAInstructionExecutor;
import conc_trace.instr.analysis.pta.PTAObject;

public class PTAInstructionExecutorTest {
	/**
	 * Test classes used to inject
	 * @author Gabau
	 *
	 */
	private static class DummyObject extends PTAObject {
		private int value;
		public DummyObject(int value) {
			this.value = value;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DummyObject) {
				return ((DummyObject)obj).value == value && 
						((DummyObject)obj).isDoubleOrLong() == this.isDoubleOrLong();
			}
			return false;
		}
		
		@Override
		public String toString() {
			return this.value + "";
		}
	}
	
	private static class DummyLongObject extends DummyObject {

		public DummyLongObject(int value) {
			super(value);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean isDoubleOrLong() {
			// TODO Auto-generated method stub
			return true;
		}
		
		
		
	}
	
	private PTAInstructionExecutor toTest = new PTAInstructionExecutor();
	
	/**
	 * Add to stack [start, end)
	 */
	private void addToStackRange(FakeStack stack, int start, int end, boolean isLong) {
		int counter = 1;
		if (start > end) {
			counter = -1;
		}
		for (int i = start; i != end; i += counter) {
			if (!isLong) {
				stack.push(new DummyObject(i));
				continue;
			}
			stack.push(new DummyLongObject(i));
		}
	}
	
	
	// dup instruction testing
	// method for testing DUP2_X2 instruction
	// tests form 1
	@Test
	public void testDup2X2Instruction() {
		Instruction instruction = new DUP2_X2();
		// initialise the stack
		FakeStack testStack = new FakeStack();
		// 1 2 3 4 -> 3 4 1 2 3 4
		addToStackRange(testStack, 1, 5, false);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		// check on new status
		FakeStack expectedStack = new FakeStack();
		addToStackRange(expectedStack, 3, 5, false);
		addToStackRange(expectedStack, 1, 5, false);
		assertEquals(expectedStack, toTest.getStack());
	}
	
	// test form 4
	@Test
	public void testDup2X2InstructionLong() {

		Instruction instruction = new DUP2_X2();
		// initialise the stack
		FakeStack testStack = new FakeStack();
		// 1 2 3 4 -> 1 2 4 3 4
		addToStackRange(testStack, 1, 5, true);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		// expected values
		FakeStack expectedStack = new FakeStack();
		addToStackRange(expectedStack, 1, 3, true);
		addToStackRange(expectedStack, 4, 5, true);
		addToStackRange(expectedStack, 3, 5, true);
		

		assertEquals(expectedStack, toTest.getStack());	
	}
	
	// test form 2
	@Test
	public void testDup2X2InstructionLong2() {
		
		// 3 2 1 -> 1 3 2 1, 1 is double, 3 and 2 are not

		Instruction instruction = new DUP2_X2();
		// initialise the stack
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 3, 1, false);
		addToStackRange(testStack, 1, 0, true);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		// expected values
		FakeStack expectedStack = new FakeStack();
		addToStackRange(expectedStack, 1, 0, true);
		addToStackRange(expectedStack, 3, 1, false);
		addToStackRange(expectedStack, 1, 0, true);

		assertEquals(expectedStack, toTest.getStack());	
	}
	
	
	// test form 3
	@Test
	public void testDup2X2InstructionLong3() {
		
		// 3 2 1 -> 2 1 3 2 1, 1 and 2 are word sized, 3 is double

		Instruction instruction = new DUP2_X2();
		// initialise the stack
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 3, 2, true);
		addToStackRange(testStack, 2, 0, false);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		// expected values
		FakeStack expectedStack = new FakeStack();
		addToStackRange(expectedStack, 2, 0, false);
		addToStackRange(expectedStack, 3, 2, true);
		addToStackRange(expectedStack, 2, 0, false);

		assertEquals(expectedStack, toTest.getStack());	
	}
	
	
	@Test
	public void testDupInstruction() {
		Instruction instruction = new DUP();
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 1, 5, false);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		FakeStack expectedStack = new FakeStack();
		addToStackRange(expectedStack, 1, 5, false);
		addToStackRange(expectedStack, 4, 5, false);

		assertEquals(expectedStack, toTest.getStack());	
	}
	
	
	
	@Test
	public void testDup2_X1() {
		// 3 2 1 -> 2 1 3 2 1 (example from wiki)
		Instruction instruction = new DUP2_X1();
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 3, 0, false);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		FakeStack expectedFakeStack = new FakeStack();
		addToStackRange(expectedFakeStack, 2, 0, false);
		addToStackRange(expectedFakeStack, 3, 0, false);

		assertEquals(expectedFakeStack, toTest.getStack());	
	}
	
	@Test
	public void testDup2_X1_Long() {
		// 2 1 -> 1 2 1
		Instruction instruction = new DUP2_X1();
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 2, 1, false);
		addToStackRange(testStack, 1, 0, true);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		FakeStack expectedFakeStack = new FakeStack();
		addToStackRange(expectedFakeStack, 1, 2, true);
		addToStackRange(expectedFakeStack, 2, 3, false);
		addToStackRange(expectedFakeStack, 1, 2, true);

		assertEquals(expectedFakeStack, toTest.getStack());	
	}
	
	@Test
	public void testDup_X1() {
		Instruction instruction = new DUP_X1();
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 2, 0 , false);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		FakeStack expectedFakeStack = new FakeStack();
		addToStackRange(expectedFakeStack, 1, 3, false);
		addToStackRange(expectedFakeStack, 1, 0, false);

		assertEquals(expectedFakeStack, toTest.getStack());	
	}
	
	// test pop
	@Test
	public void testPop() {
		// expected stacks
		FakeStack expectedStack1 = new FakeStack();
		addToStackRange(expectedStack1, 1, 7, false);
		FakeStack expectedStack2 = new FakeStack();
		addToStackRange(expectedStack2, 1, 5, false);
		
		
		Instruction pop = new POP();
		Instruction pop2 = new POP2();
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 1, 8, false);
		toTest.setCurrentStack(testStack);
		pop.accept(toTest);
		assertEquals(expectedStack1, toTest.getStack());
		pop2.accept(toTest);
		assertEquals(expectedStack2, toTest.getStack());
	}
	
	@Test
	public void testPopLong() {
		Instruction instruction = new POP2();
		FakeStack testStack = new FakeStack();
		addToStackRange(testStack, 1, 19, true);
		toTest.setCurrentStack(testStack);
		instruction.accept(toTest);
		
		FakeStack expectedStack = new FakeStack();
		addToStackRange(expectedStack, 1, 18, true);
		
		assertEquals(expectedStack, toTest.getStack());
		
	}
	
	
	
	
	
}
