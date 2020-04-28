package space.bbkr.lang.jlox;

import javax.annotation.Nullable;

class Token {
	final TokenType type;
	final String lexeme;
	@Nullable
	final Object literal;
	final int line;

	Token(TokenType type, String lexeme, @Nullable Object literal, int line) {
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}

	@Override
	public String toString() {
		return type + " " + lexeme + " " + literal;
	}
}
