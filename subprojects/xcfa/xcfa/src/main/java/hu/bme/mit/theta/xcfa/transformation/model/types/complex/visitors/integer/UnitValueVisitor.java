package hu.bme.mit.theta.xcfa.transformation.model.types.complex.visitors.integer;

import hu.bme.mit.theta.core.type.LitExpr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.arraytype.ArrayLitExpr;
import hu.bme.mit.theta.core.type.arraytype.ArrayType;
import hu.bme.mit.theta.xcfa.transformation.model.types.complex.CComplexType;
import hu.bme.mit.theta.xcfa.transformation.model.types.complex.compound.CArray;
import hu.bme.mit.theta.xcfa.transformation.model.types.complex.integer.CInteger;

import java.util.List;

import static hu.bme.mit.theta.core.type.arraytype.ArrayExprs.Array;
import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;
import static hu.bme.mit.theta.core.utils.TypeUtils.cast;

public class UnitValueVisitor extends CComplexType.CComplexTypeVisitor<Void, LitExpr<?>> {
	public static final UnitValueVisitor instance = new UnitValueVisitor();
	@Override
	public LitExpr<?> visit(CInteger type, Void param) {
		return Int(1);
	}

	@Override
	public LitExpr<?> visit(CArray type, Void param) {
		return getExpr(type);
	}

	private <IndexType extends Type, ElemType extends Type> ArrayLitExpr<IndexType, ElemType> getExpr(CArray type) {
		//noinspection unchecked
		ArrayType<IndexType, ElemType> smtType = (ArrayType<IndexType, ElemType>) type.getSmtType();
		return Array(List.of(), cast(type.getEmbeddedType().getUnitValue(), smtType.getElemType()), smtType);
	}
}
