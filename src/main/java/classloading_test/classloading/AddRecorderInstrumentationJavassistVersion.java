package classloading_test.classloading;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;

// only need to generate data, no need for any constraints
public class AddRecorderInstrumentationJavassistVersion implements ClassFileTransformer {

	// location used to store object special id
	// todo: add an option to change this name
	public static String SPECIAL_ID_LOC = "_special_id_reserved_222";


	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
            ctClass.addField(new CtField(CtClass.longType, SPECIAL_ID_LOC, ctClass));
            // Add code to all constructors
            addCodeToConstructors(ctClass, String.format("this.%s = AddedClass;", SPECIAL_ID_LOC));

            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null to indicate no transformation was applied
        }
	}
	
    private void addCodeToConstructors(CtClass ctClass, String code) {
        CtConstructor[] constructors = ctClass.getConstructors();
        for (CtConstructor constructor : constructors) {
            // Add code at the beginning of each constructor
            try {
				constructor.insertBefore(code);
			} catch (CannotCompileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }


}
