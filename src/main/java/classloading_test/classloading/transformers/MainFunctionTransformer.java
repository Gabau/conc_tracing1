package classloading_test.classloading.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;


public class MainFunctionTransformer implements ClassFileTransformer {

	// look for the main function of the input java code
	// puts the code to log the items at the end
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		return null;
	}

}
