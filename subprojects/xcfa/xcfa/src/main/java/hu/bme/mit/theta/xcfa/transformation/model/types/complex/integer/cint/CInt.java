package hu.bme.mit.theta.xcfa.transformation.model.types.complex.integer.cint;

import hu.bme.mit.theta.xcfa.transformation.model.types.complex.integer.CInteger;
import hu.bme.mit.theta.xcfa.transformation.model.types.simple.CSimpleType;

public abstract class CInt extends CInteger {
	protected CInt(CSimpleType origin) {
		super(origin);
	}
}