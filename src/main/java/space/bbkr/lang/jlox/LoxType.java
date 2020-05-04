package space.bbkr.lang.jlox;

import java.util.List;

import javax.annotation.Nullable;

class LoxType {
	static final LoxType UNKNOWN = new LoxType(TokenType.QUESTION, "unknown");
	static final LoxType NONE = new LoxType(TokenType.NIL, "none");
	static final LoxType NUMBER = new LoxType(TokenType.NUM, "number");
	static final LoxType BOOLEAN = new LoxType(TokenType.BOOL, "boolean");
	static final LoxType STRING = new LoxType(TokenType.STR, "string");

	final TokenType marker;
	final String lexeme;

	LoxType(TokenType marker, String lexeme) {
		this.marker = marker;
		this.lexeme = lexeme;
	}

	boolean matches(LoxType other) {
		return this.marker == other.marker
				|| this == UNKNOWN || other == UNKNOWN; //to prevent cascading errors only
	}

	boolean isCallable() {
		return false;
	}

	static class FunctionLoxType extends LoxType {
		final List<LoxType> paramTypes;
		final LoxType returnType;

		FunctionLoxType(List<LoxType> paramTypes, LoxType returnType) {
			super(TokenType.FUN, getIdentifier(paramTypes, returnType));
			this.paramTypes = paramTypes;
			this.returnType = returnType;
		}

		@Override
		boolean matches(LoxType other) {
			if (!super.matches(other)) return false;
			FunctionLoxType otherFun = (FunctionLoxType) other;
			if (paramTypes.size() != otherFun.paramTypes.size()) return false;
			return this.returnType.matches(otherFun.returnType);
		}

		@Override
		boolean isCallable() {
			return true;
		}

		private static String getIdentifier(List<LoxType> inputs, LoxType output) {
			StringBuilder builder = new StringBuilder("(");
			for (int i = 0; i < inputs.size(); i++) {
				builder.append(inputs.get(i).lexeme);
				if (i != inputs.size() - 1) {
					builder.append(", ");
				}
			}
			builder.append(")");
			if (output != LoxType.NONE) {
				builder.append("-> " + output.lexeme);
			}
			return builder.toString();
		}
	}

	static class InstanceLoxType extends LoxType {
		final Token name;
		final ClassLoxType type;

		InstanceLoxType(Token name, ClassLoxType type) {
			super(TokenType.IDENTIFIER, "instance<" + name.lexeme + ">");
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
		final ClassLoxType type;

		ClassLoxType(Token name, @Nullable ClassLoxType type) { //TODO: how to manage this better at parse time?
			super(TokenType.CLASS, "class<" + name.lexeme + ">");
			this.name = name;
			this.type = type;
		}

		String getRawTypeName() {
			return name.lexeme;
		}

		@Override
		boolean matches(LoxType other) {
			if (!super.matches(other)) return false;
			ClassLoxType classType = (ClassLoxType)other;
			if (lexeme.equals(other.lexeme)) return true; //shortcut
			return type != null && type.matches(classType.type);
		}

		@Override
		boolean isCallable() {
			return true;
		}
	}
}
