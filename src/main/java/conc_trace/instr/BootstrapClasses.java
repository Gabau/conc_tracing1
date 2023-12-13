package conc_trace.instr;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

// class used to modify the class loader
public interface BootstrapClasses {
	// gets the location of the current jar
	public static String getBootStrapJarLoc() {
		return BootstrapClasses.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}
	public static void loadAdditionalClasses(Instrumentation instr) {
		try {
			JarFile jarFile = new JarFile(new File(getBootStrapJarLoc()));
			instr.appendToBootstrapClassLoaderSearch(jarFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
