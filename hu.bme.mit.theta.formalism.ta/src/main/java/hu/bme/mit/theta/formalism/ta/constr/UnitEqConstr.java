package hu.bme.mit.theta.formalism.ta.constr;

import static hu.bme.mit.theta.core.expr.Exprs.Eq;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;

import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.expr.EqExpr;
import hu.bme.mit.theta.core.expr.RefExpr;
import hu.bme.mit.theta.core.type.RatType;

public final class UnitEqConstr extends UnitConstr {

	private static final int HASH_SEED = 5987;

	private static final String OPERATOR_LABEL = "=";

	private volatile EqExpr expr = null;

	UnitEqConstr(final VarDecl<RatType> clock, final int bound) {
		super(clock, bound);
	}

	@Override
	public EqExpr toExpr() {
		EqExpr result = expr;
		if (result == null) {
			final RefExpr<RatType> ref = getVar().getRef();
			result = Eq(ref, Int(getBound()));
			expr = result;
		}
		return result;
	}

	@Override
	public <P, R> R accept(final ClockConstrVisitor<? super P, ? extends R> visitor, final P param) {
		return visitor.visit(this, param);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof UnitEqConstr) {
			final UnitEqConstr that = (UnitEqConstr) obj;
			return this.getBound() == that.getBound() && this.getVar().equals(that.getVar());
		} else {
			return false;
		}
	}

	@Override
	protected int getHashSeed() {
		return HASH_SEED;
	}

	@Override
	protected String getOperatorLabel() {
		return OPERATOR_LABEL;
	}

}
