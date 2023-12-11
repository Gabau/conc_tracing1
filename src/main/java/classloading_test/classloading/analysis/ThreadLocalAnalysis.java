package classloading_test.classloading.analysis;

import java.util.List;

public interface ThreadLocalAnalysis {
	
	
	// a method to determine if a object is possibly shared or not
	// simply outputs two things -> className, Set<Set<Instruction>>
	// where the instruction is the location whhere this shared var is used
	/**
	 * a method to determine if a object is possibly shared or not
	 * where the instruction is the location whhere this shared var is used
	 * 
	 * @param entryClass The class where the entry point occurs
	 * @param entryMethod The method which we use to enter the program
	 * @return The list of shared memory locations.
	 */
	List<SharedMemoryLocation> sharedMemoryLocations(String entryClass, String entryMethod) throws ClassNotFoundException;
}
