package hu.bme.mit.theta.gamma.frontend.dsl;

import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.stmt.Stmt;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.anytype.RefExpr;
import hu.bme.mit.theta.gamma.frontend.dsl.gen.GammaBaseVisitor;
import hu.bme.mit.theta.gamma.frontend.dsl.gen.GammaParser;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static hu.bme.mit.theta.core.stmt.Stmts.Assign;
import static hu.bme.mit.theta.core.utils.TypeUtils.cast;

public class StatementParserVisitor extends GammaBaseVisitor<Stmt> {

    private final Map<String, VarDecl<?>> varLut;

    public StatementParserVisitor(Map<String, VarDecl<?>> varLut) {
        this.varLut = varLut;
    }

    @Override
    public Stmt visitRuleAssignmentStatement(GammaParser.RuleAssignmentStatementContext ctx) {
        ExpressionParserVisitor expressionParserVisitor = new ExpressionParserVisitor(varLut);
        Expr<?> lhs = ctx.ruleAssignableAccessExpression().accept(expressionParserVisitor);
        Expr<?> rhs = ctx.ruleExpression().accept(expressionParserVisitor);
        checkState(lhs instanceof RefExpr<?>);
        VarDecl<?> decl = (VarDecl<?>) ((RefExpr<?>) lhs).getDecl();
        return Assign(cast(decl, decl.getType()), cast(rhs, decl.getType()));
    }
}
