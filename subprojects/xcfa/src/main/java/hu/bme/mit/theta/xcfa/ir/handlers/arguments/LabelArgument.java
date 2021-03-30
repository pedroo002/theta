package hu.bme.mit.theta.xcfa.ir.handlers.arguments;

import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.Type;

import java.util.Map;

public class LabelArgument extends Argument{
    private final String name;

    LabelArgument(String type, String name) {
        this.name = name;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public Expr<?> getExpr(Map<String, Expr<?>> values) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }
}
