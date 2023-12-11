package classloading_test.classloading.filter;

import java.util.HashSet;
import java.util.function.Predicate;

import classloading_test.classloading.masterRecorderFunctions.MasterRunnerFunctions;

/**
 * Filters the classes that should be instrumented by this read write instrumentation
 * @author Gabau
 *
 */
public class ClassLoadingClassFilter implements Predicate<String> {

	private HashSet<String> toExclude = new HashSet<>();
	public ClassLoadingClassFilter() {
		toExclude.add(MasterRunnerFunctions.class.getName().replace('.', '/'));
		
	}
	
	
	/**
	 * Returns true if t is something we should include.
	 */
	@Override
	public boolean test(String t) {
		// exclude all classes in this package.
		if (t.startsWith("classloading_test")) return false;
		return !toExclude.contains(t);
	}

}
