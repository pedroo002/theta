package hu.bme.mit.theta.xsts.analysis.initprec;

import hu.bme.mit.theta.analysis.expl.ExplPrec;
import hu.bme.mit.theta.analysis.pred.PredPrec;
import hu.bme.mit.theta.analysis.prod2.Prod2Prec;
import hu.bme.mit.theta.analysis.prod2.prod2explpred.dynamic.DynamicPrec;
import hu.bme.mit.theta.common.container.Containers;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.utils.ExprUtils;
import hu.bme.mit.theta.core.utils.StmtAtomCollector;
import hu.bme.mit.theta.xsts.XSTS;

import java.util.Set;
import java.util.stream.Collectors;

public class XstsAllVarsInitPrec implements XstsInitPrec {
	@Override
	public ExplPrec createExpl(XSTS xsts) {
		return ExplPrec.of(xsts.getVars());
	}

	@Override
	public PredPrec createPred(XSTS xsts) {
		return PredPrec.of();
	}

	@Override
	public Prod2Prec<ExplPrec, PredPrec> createProd2ExplPred(XSTS xsts) {
		return Prod2Prec.of(ExplPrec.of(xsts.getVars()), PredPrec.of());
	}

	@Override
	public DynamicPrec createDynamic(XSTS xsts) {
		final Set<Expr<BoolType>> atoms = Containers.createSet();
		atoms.addAll(StmtAtomCollector.collectAtoms(xsts.getEnv()));
		atoms.addAll(StmtAtomCollector.collectAtoms(xsts.getInit()));
		atoms.addAll(StmtAtomCollector.collectAtoms(xsts.getTran()));
		atoms.addAll(ExprUtils.getAtoms(xsts.getProp()));
		atoms.addAll(ExprUtils.getAtoms(xsts.getInitFormula()));

		final Set<Expr<BoolType>> canonicalAtoms = atoms.stream()
				.map(ExprUtils::canonize)
				.flatMap(atom -> ExprUtils.getAtoms(atom).stream())
				.collect(Collectors.toSet());
		return DynamicPrec.of(ExplPrec.of(xsts.getVars()), PredPrec.of(canonicalAtoms));
	}
}
