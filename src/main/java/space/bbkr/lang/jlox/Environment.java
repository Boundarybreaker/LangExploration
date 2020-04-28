package space.bbkr.lang.jlox;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class Environment {
	private final Map<String, Object> values = new HashMap<>();
	private final Map<String, Class<?>> types = new HashMap<>(); //TODO: however Lox classes will work

	@Nullable
	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

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
			if (types.containsKey(name.lexeme)) {
				Class<?> type = types.get(name.lexeme);
				if (value == null) {
					values.put(name.lexeme, null);
				} else if (type.equals(value.getClass())) {
					values.put(name.lexeme, value);
				} else {
					//TODO: Lox type naming so it can be better defined
					throw new RuntimeError("TypeError", name, "Variable of type '" + type.getName() +
							"' cannot be assigned a value of type '" + value.getClass().getName() + "'.");
				}
			} else {
				values.put(name.lexeme, value);
				if (value != null) types.put(name.lexeme, value.getClass());
			}
		}

		throw new RuntimeError("UndefinedError", name, "Undefined variable '" + name.lexeme + "'.");
	}
}
