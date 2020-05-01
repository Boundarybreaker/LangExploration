package space.bbkr.lang.jlox;

import javax.annotation.Nullable;

//TODO: improve once we have class heirarchy
class LoxType {
	static final LoxType UNKNOWN = new LoxType(TokenType.QUESTION, "unknown");
	static final LoxType ANY = new LoxType(TokenType.STAR, "any");
	static final LoxType NONE = new LoxType(TokenType.NIL, "none");
	static final LoxType NUMBER = new LoxType(TokenType.NUMBER, "number");
	static final LoxType BOOLEAN = new LoxType(TokenType.BOOLEAN, "boolean");
	static final LoxType STRING = new LoxType(TokenType.STRING, "string");
	static LoxType.FunctionLoxType FUNCTION(LoxType returnType) {
		return new FunctionLoxType(returnType);
	}
	static LoxType.ClassLoxType CLASS(Token name, ClassLoxType supertype)  {
		return new ClassLoxType(name, supertype);
	}
	static LoxType.InstanceLoxType INSTANCE(Token name, ClassLoxType type) {
		return new InstanceLoxType(name, type);
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
		return this == UNKNOWN; //TODO: remove once we have explicit typing for functions
	}

	static class FunctionLoxType extends LoxType {
		final LoxType returnType;

		FunctionLoxType(LoxType returnType) {
			super(TokenType.FUN, "function<" + returnType.lexeme + ">");
			this.returnType = returnType;
		}

		@Override
		boolean matches(LoxType other) {
			if (!super.matches(other)) return false;
			return this.returnType.matches(((FunctionLoxType)other).returnType);
		}

		@Override
		boolean isCallable() {
			return true;
		}
	}

	static class InstanceLoxType extends LoxType {
		final Token name;
		final ClassLoxType type;

		InstanceLoxType(Token name, ClassLoxType type) {
			super(TokenType.INSTANCE, "instance<" + name.lexeme + ">");
			this.name = name;
			this.type = type;
		}

		String getRawTypeName() {
			return type.getRawTypeName();
		}

		@Override
		boolean matches(LoxType other) {
			if (!super.matches(other)) return false;
			return type.matches(((InstanceLoxType)other).type);
		}
	}

	static class ClassLoxType extends LoxType {
		final Token name;
		final ClassLoxType supertype;

		ClassLoxType(Token name, @Nullable ClassLoxType supertype) {
			super(TokenType.CLASS, "class<" + name.lexeme + ">");
			this.name = name;
			this.supertype = supertype;
		}

		String getRawTypeName() {
			return name.lexeme;
		}

		@Override
		boolean matches(LoxType other) {
			if (!super.matches(other)) return false;
			if (lexeme.equals(other.lexeme)) return true;
			return supertype != null && supertype.matches(other);
		}

		@Override
		boolean isCallable() {
			return true;
		}
	}
}
