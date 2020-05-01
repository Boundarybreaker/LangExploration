package space.bbkr.lang.jlox;

//TODO: improve once we have class heirarchy
class LoxType {
	static final LoxType UNKNOWN = new LoxType(TokenType.NIL, "unknown");
	static final LoxType ANY = new LoxType(TokenType.STAR, "any");
	static final LoxType NONE = new LoxType(TokenType.NIL, "none");
	static final LoxType NUMBER = new LoxType(TokenType.NUMBER, "number");
	static final LoxType BOOLEAN = new LoxType(TokenType.BOOLEAN, "boolean");
	static final LoxType STRING = new LoxType(TokenType.STRING, "string");
	static final LoxType FUNCTION = new LoxType(TokenType.FUN, "function");
	static LoxType CLASS(Token name)  {
		return new ClassLoxType(name);
	}
	static final LoxType INSTANCE(Token name) {
		return new InstanceLoxType(name);
	}

	final TokenType marker;
	final String lexeme;

	LoxType(TokenType marker, String lexeme) {
		this.marker = marker;
		this.lexeme = lexeme;
	}

	boolean matches(LoxType other) {
		return this.marker == other.marker
				|| this == ANY || other == ANY //wildcard for function args. TODO: keep?
				|| this == UNKNOWN || other == UNKNOWN; //TODO: remove once we have explicit typing for functions
	}

	boolean isCallable() {
		return this == FUNCTION || this == UNKNOWN; //TODO: remove once we have explicit typing for functions
	}
}
