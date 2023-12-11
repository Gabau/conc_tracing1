package classloading_test.classloading.analysis;

import java.util.HashSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.NEW;

/**
 * A shared memory location
 * @author Gabau
 */
public class SharedMemoryLocation {
	private InstructionLocation creationLocation;
	private HashSet<InstructionLocation> readLocations;
	
	public SharedMemoryLocation(final InstructionLocation creationLocation, 
			final HashSet<InstructionLocation> readLocations) {
		this.creationLocation = creationLocation;
		this.readLocations = readLocations;
	}

}
