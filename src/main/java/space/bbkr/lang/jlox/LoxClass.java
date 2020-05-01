package space.bbkr.lang.jlox;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {
	final Token name;
	final Map<String, LoxFunction> methods;

	LoxClass(Token name, Map<String, LoxFunction> methods) {
		this.name = name;
		this.methods = methods;
	}

	@Override
	public int arity() {
		LoxFunction initializer = findMethod("init");
		if (initializer == null) return 0;
		return initializer.arity();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);
		return instance;
	}

	LoxFunction findMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		return null;
	}

	@Override
	public String toString() {
		if (name.type == TokenType.CLASS) {
			return "<anonymous class>";
		}
		return "<class " + name.lexeme + ">";
	}
}
