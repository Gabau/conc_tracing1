package classloading_test.classloading.analysis;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

/**
 * Interface representing the points to analysis strategy.
 * @author Gabau
 *
 */
@FunctionalInterface
public interface PTAnalysis {
	/**
	 * Get the object initialisation -> RW locations
	 */
	public HashMap<InstructionLocation, HashSet<InstructionLocation>> ptAnalysis(
			JavaClass entryClass,
			Method entryMethod
			);
}
