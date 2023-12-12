package classloading_test.classloading.analysis.pta;

import classloading_test.classloading.analysis.InstructionLocation;

/**
 * Represents an array ref.
 * @author Gabau
 *
 */
public class PTAObjectArrayRef extends PTAObject {
	public PTAObjectArrayRef(InstructionLocation location) {
		super(location);
		this.state = State.TYPE_DETERMINED;
	}
	
	@Override
	public boolean isArrayRef() {
		return true;
	}
}
