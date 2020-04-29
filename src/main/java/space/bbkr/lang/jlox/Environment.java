package space.bbkr.lang.jlox;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class Environment {
	@Nullable
	final Environment enclosing;
	private final Map<String, Object> values = new HashMap<>();
	private final Map<String, Class<?>> types = new HashMap<>(); //TODO: however Lox classes will work

	Environment() {
		enclosing = null;
	}

	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	@Nullable
	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		if (enclosing != null) return enclosing.get(name);

		throw new RuntimeError("UndefinedError", name, "Undefined variable '" + name.lexeme + "'.");
	}

	void define(String name, @Nullable Object value) {
		values.put(name, value);
		if (value != null) {
			types.put(name, value.getClass());
		}
	}

	void assign(Token name, @Nullable Object value) {
		if (values.containsKey(name.lexeme)) {
			if (types.containsKey(name.lexeme)) { //check if type is defined yet
				Class<?> type = types.get(name.lexeme);
				if (value == null) {
					values.put(name.lexeme, null);
					return;
				} else if (type.equals(value.getClass())) {
					values.put(name.lexeme, value);
					return;
				} else {
					//TODO: Lox type naming so it can be better defined
					throw new RuntimeError("TypeError", name, "Variable of type '" + type.getName() +
							"' cannot be assigned a value of type '" + value.getClass().getName() + "'.");
				}
			} else {
				values.put(name.lexeme, value);
				if (value != null) types.put(name.lexeme, value.getClass());
				return;
			}
		}

		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}

		throw new RuntimeError("UndefinedError", name, "Undefined variable '" + name.lexeme + "'.");
	}
}
