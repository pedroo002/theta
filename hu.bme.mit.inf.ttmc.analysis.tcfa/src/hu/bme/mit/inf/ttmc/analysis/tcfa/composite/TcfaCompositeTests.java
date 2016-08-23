package hu.bme.mit.inf.ttmc.analysis.tcfa.composite;

import static hu.bme.mit.inf.ttmc.core.type.impl.Types.Int;
import static hu.bme.mit.inf.ttmc.formalism.common.decl.impl.Decls2.Var;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import hu.bme.mit.inf.ttmc.analysis.algorithm.Abstractor;
import hu.bme.mit.inf.ttmc.analysis.algorithm.ArgPrinter;
import hu.bme.mit.inf.ttmc.analysis.algorithm.impl.AbstractorImpl;
import hu.bme.mit.inf.ttmc.analysis.composite.CompositeAnalysis;
import hu.bme.mit.inf.ttmc.analysis.composite.CompositePrecision;
import hu.bme.mit.inf.ttmc.analysis.composite.CompositeState;
import hu.bme.mit.inf.ttmc.analysis.expl.ExplPrecision;
import hu.bme.mit.inf.ttmc.analysis.expl.ExplState;
import hu.bme.mit.inf.ttmc.analysis.expl.GlobalExplPrecision;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TcfaAction;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TcfaAnalyis;
import hu.bme.mit.inf.ttmc.analysis.tcfa.TcfaState;
import hu.bme.mit.inf.ttmc.analysis.tcfa.expl.TcfaExplAnalysis;
import hu.bme.mit.inf.ttmc.analysis.tcfa.zone.TcfaZoneAnalysis;
import hu.bme.mit.inf.ttmc.analysis.zone.ZonePrecision;
import hu.bme.mit.inf.ttmc.analysis.zone.ZoneState;
import hu.bme.mit.inf.ttmc.core.type.IntType;
import hu.bme.mit.inf.ttmc.formalism.common.decl.ClockDecl;
import hu.bme.mit.inf.ttmc.formalism.common.decl.VarDecl;
import hu.bme.mit.inf.ttmc.formalism.tcfa.instances.FischerTCFA;
import hu.bme.mit.inf.ttmc.solver.Solver;
import hu.bme.mit.inf.ttmc.solver.SolverManager;
import hu.bme.mit.inf.ttmc.solver.z3.Z3SolverManager;

public class TcfaCompositeTests {

	@Test
	public void testExplicit() {
		final VarDecl<IntType> vlock = Var("lock", Int());
		final FischerTCFA fischer = new FischerTCFA(1, 1, 2, vlock);

		final SolverManager manager = new Z3SolverManager();
		final Solver solver = manager.createSolver(true, true);

		final TcfaAnalyis<CompositeState<ZoneState, ExplState>, CompositePrecision<ZonePrecision, ExplPrecision>> analysis = new TcfaAnalyis<>(
				fischer.getInitial(),
				new CompositeAnalysis<>(TcfaZoneAnalysis.getInstance(), new TcfaExplAnalysis(solver)));

		final HashMap<ClockDecl, Integer> ceilings = new HashMap<>();
		ceilings.put(fischer.getClock(), 2);

		final CompositePrecision<ZonePrecision, ExplPrecision> precision = CompositePrecision
				.create(new ZonePrecision(ceilings), GlobalExplPrecision.create(Collections.singleton(vlock)));

		final Abstractor<TcfaState<CompositeState<ZoneState, ExplState>>, TcfaAction, CompositePrecision<ZonePrecision, ExplPrecision>> abstractor = new AbstractorImpl<>(
				analysis, s -> s.getLoc().equals(fischer.getCritical()));

		abstractor.init(precision);
		abstractor.check(precision);

		System.out.println(ArgPrinter.toGraphvizString(abstractor.getARG()));
	}

}