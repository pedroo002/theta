package hu.bme.mit.inf.ttmc.core.dsl;

import java.util.Optional;

public interface Scope {

	public Optional<Symbol> resolve(String name);

	public void declare(Symbol symbol);

	public Optional<Scope> getEnclosingScope();

}
