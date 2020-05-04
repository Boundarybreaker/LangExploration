package space.bbkr.lang.jlox;

import java.util.ArrayList;
import java.util.List;

/**
 * A function! Stores its signature and code, the local env, and whether it initializes a class.
 */
class LoxFunction implements LoxCallable {
	private final Statement.FunctionStatement declaration;
	private final Environment closure;
	private final Boolean isInitializer;

	LoxFunction(Statement.FunctionStatement declaration, Environment closure, boolean isInitializer) {
		this.declaration = declaration;
		this.closure = closure;
		this.isInitializer = isInitializer;
	}

	@Override
	public int arity() {
		return declaration.params.size();
	}

	@Override
	public List<LoxType> getParamTypes() {
		List<LoxType> types = new ArrayList<>();
		for (Expression.ParameterExpression param : declaration.params) {
			types.add(param.type);
		}
		return types;
	}

	@Override
	public LoxType getReturnType() {
		return declaration.returnType;
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment environment = new Environment(closure);
		for (int i = 0; i < declaration.params.size(); i++) {
			environment.define(declaration.params.get(i).name.lexeme, arguments.get(i));
		}

		try {
			interpreter.executeBlock(declaration.body, environment);
		} catch (Return ret) {
			if (isInitializer) return closure.getAt(0, "this");
			return ret.value;
		}

		if (isInitializer) return closure.getAt(0, "this");
		return null;
	}

	public LoxFunction bind(LoxInstance instance) {
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		return new LoxFunction(declaration, environment, isInitializer);
	}

	@Override
	public String toString() {
		return declaration.name.type == TokenType.IDENTIFIER? "<fn " + declaration.name.lexeme + ">" : "<anonymous fn>";
	}
}
