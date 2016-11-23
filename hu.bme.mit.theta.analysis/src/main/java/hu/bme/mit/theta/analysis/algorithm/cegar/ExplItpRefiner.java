package hu.bme.mit.theta.analysis.algorithm.cegar;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import hu.bme.mit.theta.analysis.Trace;
import hu.bme.mit.theta.analysis.algorithm.ARG;
import hu.bme.mit.theta.analysis.algorithm.ArgNode;
import hu.bme.mit.theta.analysis.algorithm.ArgTrace;
import hu.bme.mit.theta.analysis.expl.ExplPrecision;
import hu.bme.mit.theta.analysis.expl.ExplState;
import hu.bme.mit.theta.analysis.expr.ExprAction;
import hu.bme.mit.theta.analysis.expr.ExprTraceChecker;
import hu.bme.mit.theta.analysis.expr.ExprTraceStatus2;
import hu.bme.mit.theta.analysis.expr.ItpRefutation;
import hu.bme.mit.theta.core.utils.impl.ExprUtils;

public class ExplItpRefiner<A extends ExprAction> implements Refiner<ExplState, A, ExplPrecision> {

	ExprTraceChecker<ItpRefutation> exprTraceChecker;

	private ExplItpRefiner(final ExprTraceChecker<ItpRefutation> exprTraceChecker) {
		this.exprTraceChecker = checkNotNull(exprTraceChecker);
	}

	public static <A extends ExprAction> ExplItpRefiner<A> create(
			final ExprTraceChecker<ItpRefutation> exprTraceChecker) {
		return new ExplItpRefiner<>(exprTraceChecker);
	}

	@Override
	public RefinerResult<ExplState, A, ExplPrecision> refine(final ARG<ExplState, A> arg,
			final ExplPrecision precision) {
		checkNotNull(arg);
		checkNotNull(precision);
		checkArgument(!arg.isSafe());

		final ArgTrace<ExplState, A> cexToConcretize = arg.getCexs().findFirst().get();
		final Trace<ExplState, A> traceToConcretize = cexToConcretize.toTrace();

		final ExprTraceStatus2<ItpRefutation> cexStatus = exprTraceChecker.check(traceToConcretize);

		if (cexStatus.isFeasible()) {
			return RefinerResult.unsafe(traceToConcretize);
		} else if (cexStatus.isInfeasible()) {
			final ItpRefutation interpolant = cexStatus.asInfeasible().getRefutation();

			final ExplPrecision refinedPrecision = precision.refine(ExprUtils.getVars(interpolant));
			final int pruneIndex = interpolant.getPruneIndex();
			checkState(0 <= pruneIndex && pruneIndex <= cexToConcretize.length());
			final ArgNode<ExplState, A> nodeToPrune = cexToConcretize.node(pruneIndex);
			arg.prune(nodeToPrune);
			return RefinerResult.spurious(refinedPrecision);
		} else {
			throw new IllegalStateException("Unknown status.");
		}
	}

}
