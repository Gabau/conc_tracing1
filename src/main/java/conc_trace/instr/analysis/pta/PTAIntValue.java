package conc_trace.instr.analysis.pta;

import org.apache.bcel.generic.Type;

public class PTAIntValue extends PTAObject {
	private int value;
	
	public static PTAIntValue fromValue(int value) {
		return new PTAIntValue(value);
	}
	
	public PTAIntValue(int value) {
		super(Type.INT);
		this.value = value;
	}

	@Override
	public boolean isIntValue() {
		return true;
	}

	@Override
	public int getValue() {
		return this.value;
	}
	
	
}
