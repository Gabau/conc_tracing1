package conc_trace.instr.analysis.pta.model;

// represents a static definition
public class StaticDefinition extends Definition {

	public StaticDefinition() {
		super(-1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

}
