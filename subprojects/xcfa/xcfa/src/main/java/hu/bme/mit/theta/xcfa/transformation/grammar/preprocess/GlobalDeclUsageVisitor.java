package hu.bme.mit.theta.xcfa.transformation.grammar.preprocess;

import hu.bme.mit.theta.xcfa.dsl.gen.CBaseVisitor;
import hu.bme.mit.theta.xcfa.dsl.gen.CParser;
import hu.bme.mit.theta.xcfa.transformation.grammar.type.DeclarationVisitor;
import hu.bme.mit.theta.xcfa.transformation.model.declaration.CDeclaration;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

public class GlobalDeclUsageVisitor extends CBaseVisitor<List<CDeclaration>> {
	public static final GlobalDeclUsageVisitor instance = new GlobalDeclUsageVisitor();

	private final Map<String, Set<String>> globalUsages = new LinkedHashMap<>();
	private final Map<String, CParser.ExternalDeclarationContext> usedContexts = new LinkedHashMap<>();
	private String current;

	@Override
	public List<CDeclaration> visitGlobalDeclaration(CParser.GlobalDeclarationContext ctx) {
		List<CDeclaration> declarations = DeclarationVisitor.instance.getDeclarations(ctx.declaration().declarationSpecifiers(), ctx.declaration().initDeclaratorList());
		for (CDeclaration declaration : declarations) {
			globalUsages.put(declaration.getName(), new LinkedHashSet<>());
			usedContexts.put(declaration.getName(), ctx);
			current = declaration.getName();
			super.visitGlobalDeclaration(ctx);
			current = null;
		}
		return null;
	}

	@Override
	public List<CDeclaration> visitExternalFunctionDefinition(CParser.ExternalFunctionDefinitionContext ctx) {
		CDeclaration funcDecl = ctx.functionDefinition().declarator().accept(DeclarationVisitor.instance);
		globalUsages.put(funcDecl.getName(), new LinkedHashSet<>());
		usedContexts.put(funcDecl.getName(), ctx);
		current = funcDecl.getName();
		super.visitExternalFunctionDefinition(ctx);
		current = null;
		return null;
	}

	@Override
	public List<CDeclaration> visitPrimaryExpressionId(CParser.PrimaryExpressionIdContext ctx) {
		globalUsages.get(current).add(ctx.getText());
		return null;
	}

	public List<CParser.ExternalDeclarationContext> getGlobalUsages(CParser.CompilationUnitContext ctx) {
		globalUsages.clear();
		usedContexts.clear();
		for (CParser.ExternalDeclarationContext externalDeclarationContext : ctx.translationUnit().externalDeclaration()) {
			externalDeclarationContext.accept(this);
		}
		checkState(globalUsages.containsKey("main"), "Main function not found!");
		Set<String> ret = new LinkedHashSet<>();
		Set<String> remaining = new LinkedHashSet<>();
		remaining.add("main");
		while(!remaining.isEmpty()) {
			Optional<String> remOpt = remaining.stream().findAny();
			String rem = remOpt.get();
			ret.add(rem);
			remaining.addAll(globalUsages.get(rem).stream().filter(globalUsages::containsKey).collect(Collectors.toSet()));
			remaining.removeAll(ret);
		}
		return usedContexts.entrySet().stream().filter(entry -> ret.contains(entry.getKey())).map(Map.Entry::getValue).distinct().collect(Collectors.toList());
	}
}
