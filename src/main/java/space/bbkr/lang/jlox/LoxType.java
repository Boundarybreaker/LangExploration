package space.bbkr.lang.jlox;

import java.util.List;

import javax.annotation.Nullable;

/**
 * The different types of value that can exist!
 */
class LoxType {
	/**
	 * No way to define this in-script, currently. Used as a wildcard for print and for preventing cascading errors if one type is bad.
	 */
	static final LoxType UNKNOWN = new LoxType(TokenType.QUESTION, "unknown");
	/**
	 * Used for marking a function as not returning anything. Represented by not specifying a return type.
	 */
	static final LoxType NONE = new LoxType(TokenType.NIL, "none");
	/**
	 * A 64-bit, double-precision floating point number. Just a double.
	 */
	static final LoxType NUMBER = new LoxType(TokenType.NUM, "number");
	/**
	 * A boolean. Nothing special.
	 */
	static final LoxType BOOLEAN = new LoxType(TokenType.BOOL, "boolean");
	/**
	 * A string! Saved using Java string literals, so what they really are is Complicated(tm).
	 */
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

	/**
	 * A function, with specific inputs and output.
	 */
	static class FunctionLoxType extends LoxType {
		final List<LoxType> paramTypes;
		final LoxType returnType;

		FunctionLoxType(List<LoxType> paramTypes, LoxType returnType) {
			super(TokenType.FUN, getIdentifier(paramTypes, returnType));
			this.paramTypes = paramTypes;
			this.returnType = returnType;
		}

		//TODO: match Unknown wildcard?
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

	/**
	 * An instance of a class, with a specific class type.
	 */
	static class InstanceLoxType extends LoxType {
		final Token name;

		InstanceLoxType(Token name) {
			super(TokenType.IDENTIFIER, "instance<" + name.lexeme + ">");
			this.name = name;
		}

		@Override
		boolean matches(LoxType other) {
			if (!super.matches(other)) return false;
			return lexeme.equals(other.lexeme);
		}
	}

	/**
	 * A class, with a specific constructor and superclass.
	 */
	static class ClassLoxType extends FunctionLoxType {
		final Token name;
		final FunctionLoxType constructor;

		ClassLoxType(Token name, FunctionLoxType constructor) { //TODO: should class lox types have more stuff?
			super(constructor.paramTypes, constructor.returnType);
			this.name = name;
			this.constructor = constructor;
		}

		@Override
		boolean matches(LoxType other) {
			if (!super.matches(other)) return false;
			if (other instanceof ClassLoxType) {
				ClassLoxType classType = (ClassLoxType) other;
				if (lexeme.equals(other.lexeme)) return true; //shortcut
				return constructor.matches(classType.constructor); //TODO: what we want for inheritance?
			}
			return true;
		}

		@Override
		boolean isCallable() {
			return true;
		}
	}
}
