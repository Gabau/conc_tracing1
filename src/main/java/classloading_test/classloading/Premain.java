package classloading_test.classloading;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import classloading_test.classloading.transformers.ThreadInstrumentor;

public class Premain {

	public static void premain(String agentArgs, Instrumentation instr) {
		BootstrapClasses.loadAdditionalClasses(instr);
		
		instr.addTransformer(new ThreadInstrumentor(), true);

		try {
			try {
				instr.retransformClasses(Class.forName("java.lang.Thread"));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnmodifiableClassException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to instrument");
		}
//		instr.addTransformer(new ThreadJAIInstrumentater());
	}
}
