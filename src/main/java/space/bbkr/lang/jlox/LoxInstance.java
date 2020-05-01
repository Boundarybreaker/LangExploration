package space.bbkr.lang.jlox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
	private LoxClass clazz;
	private final Map<String, Object> fields = new HashMap<>();

	LoxInstance(LoxClass clazz) {
		this.clazz = clazz;
	}

	Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}

		LoxFunction method = clazz.findMethod(name.lexeme);
		if (method != null) return method.bind(this);

		throw new RuntimeError("DefError", name, "Undefined property '" + name.lexeme + "'.");
	}

	void set(Token name, Object value) {
		//TODO: Fail if this property doesn't exist?
		fields.put(name.lexeme, value);
	}

	@Override
	public String toString() {
		return "<instance of " + clazz.toString() + ">";
	}
}
