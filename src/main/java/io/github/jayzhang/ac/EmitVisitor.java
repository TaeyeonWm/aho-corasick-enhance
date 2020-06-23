package io.github.jayzhang.ac;

public interface EmitVisitor {

	/**
	 * 
	 * @return true: continue  false: terminate
	 */
	public boolean visit(State state, Emit emit);
	
}
