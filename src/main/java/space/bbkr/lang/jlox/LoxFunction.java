package space.bbkr.lang.jlox;

import java.util.List;

class LoxFunction implements LoxCallable {
	private final Statement.FunctionStatement declaration;
	private final Environment closure;

	LoxFunction(Statement.FunctionStatement declaration, Environment closure) {
		this.declaration = declaration;
		this.closure = closure;
	}

	@Override
	public int arity() {
		return declaration.parms.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment environment = new Environment(closure);
		for (int i = 0; i < declaration.parms.size(); i++) {
			environment.define(declaration.parms.get(i).lexeme, arguments.get(i));
		}

		try {
			interpreter.executeBlock(declaration.body, environment);
		} catch (Return ret) {
			return ret.value;
		}
		return null;
	}

	@Override
	public String toString() {
		return declaration.name != null? "<fn " + declaration.name.lexeme + ">" : "<anonymous fn>";
	}
}
