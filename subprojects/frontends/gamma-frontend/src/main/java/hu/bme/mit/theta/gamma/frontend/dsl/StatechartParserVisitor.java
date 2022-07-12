package hu.bme.mit.theta.gamma.frontend.dsl;

import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.gamma.frontend.dsl.gen.GammaBaseVisitor;
import hu.bme.mit.theta.gamma.frontend.dsl.gen.GammaParser;
import hu.bme.mit.theta.xcfa.model.XcfaLocation;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatechartParserVisitor extends GammaBaseVisitor<XcfaLocation> {

    private final Map<String, XcfaLocation> locationLut = new LinkedHashMap<>();

    @Override
    public XcfaLocation visitRuleSynchronousStatechartDefinition(GammaParser.RuleSynchronousStatechartDefinitionContext ctx) {
        return super.visitRuleSynchronousStatechartDefinition(ctx);
    }

    @Override
    public XcfaLocation visitRulePort(GammaParser.RulePortContext ctx) {
        return super.visitRulePort(ctx);
    }

    @Override
    public XcfaLocation visitRuleVariableDeclaration(GammaParser.RuleVariableDeclarationContext ctx) {
        return super.visitRuleVariableDeclaration(ctx);
    }

    @Override
    public XcfaLocation visitRuleRegion(GammaParser.RuleRegionContext ctx) {
        return super.visitRuleRegion(ctx);
    }

    @Override
    public XcfaLocation visitRuleState(GammaParser.RuleStateContext ctx) {
        String name = ctx.RULE_ID().getText();
        locationLut.putIfAbsent(name, XcfaLocation.create(name));
        XcfaLocation location = locationLut.get(name);
        return super.visitRuleState(ctx);
    }

    @Override
    public XcfaLocation visitRuleTransition(GammaParser.RuleTransitionContext ctx) {
        String from = ctx.RULE_ID(0).getText();
        locationLut.putIfAbsent(from, XcfaLocation.create(from));
        XcfaLocation locationFrom = locationLut.get(from);

        String to = ctx.RULE_ID(1).getText();
        locationLut.putIfAbsent(to, XcfaLocation.create(to));
        XcfaLocation locationTo = locationLut.get(to);

        EventParserVisitor eventParserVisitor = new EventParserVisitor();
        String eventName = checkNotNull(ctx.ruleTrigger().accept(eventParserVisitor));

        ExpressionParserVisitor expressionParserVisitor = new ExpressionParserVisitor(Map.of());
        Expr<?> guard = ctx.ruleExpression().accept(expressionParserVisitor);

        return super.visitRuleTransition(ctx);
    }
}
