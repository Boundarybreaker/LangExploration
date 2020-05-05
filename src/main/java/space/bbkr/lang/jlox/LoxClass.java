package space.bbkr.lang.jlox;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A class in Lox! Stores the superclass and methods, along with the way to construct new instances.
 */
class LoxClass implements LoxCallable {
	final Token name;
	@Nullable
	final LoxClass superclass;
	final Map<String, LoxFunction> methods;

	LoxClass(Token name, @Nullable LoxClass superclass, Map<String, LoxFunction> methods) {
		this.name = name;
		this.superclass = superclass;
		this.methods = methods;
	}

	@Override
	public int arity() {
		LoxFunction initializer = findMethod("init");
		if (initializer == null) return 0;
		return initializer.arity();
	}

	@Override
	public List<LoxType> getParamTypes() {
		return null;
	}

	@Override
	public LoxType getReturnType() {
		return new LoxType.InstanceLoxType(name);
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);
		return instance;
	}

	boolean matches(LoxClass other) {
		LoxClass parent = superclass;
		while (parent.superclass != null) {
			parent = parent.superclass;
			if (parent.name.lexeme.equals(other.name.lexeme)) return true;
		}
		return false;
	}

	LoxFunction findMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		if (superclass != null) {
			return superclass.findMethod(name);
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
