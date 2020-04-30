package space.bbkr.lang.jlox;

import java.util.Objects;

//TODO: improve once we have class heirarchy
class LoxType {
	static final LoxType NUMBER = new LoxType(TokenType.NUM, "number");
	static final LoxType BOOLEAN = new LoxType(TokenType.BOOL, "boolean");
	static final LoxType STRING = new LoxType(TokenType.STR, "string");
	static final LoxType FUNCTION = new LoxType(TokenType.FUN, "function");
	static final LoxType CLASS = new LoxType(TokenType.CLASS, "class");

	final TokenType marker;
	final String lexeme;

	LoxType(TokenType marker, String lexeme) {
		this.marker = marker;
		this.lexeme = lexeme;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LoxType loxType = (LoxType) o;
		return marker == loxType.marker &&
				Objects.equals(lexeme, loxType.lexeme);
	}

	@Override
	public int hashCode() {
		return Objects.hash(marker, lexeme);
	}
}
