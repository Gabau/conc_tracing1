package classloading_test.classloading.analysis.pta;

import org.apache.commons.lang3.NotImplementedException;

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
	
	
	/**
	 * Function used to get a generic pointer object
	 * representing all possible objects on the array.
	 * @return
	 */
	public PTAObject getPtaObject() {
		throw new NotImplementedException();
	}
	
	/**
	 * Function used to get a generic pointer object reprenting an object on this location
	 * of the array.
	 * @param index
	 * @return
	 */
	public PTAObject getPtaObject(int index) {
		throw new NotImplementedException();
	}
}
