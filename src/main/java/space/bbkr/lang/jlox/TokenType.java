package space.bbkr.lang.jlox;

enum TokenType {
	//single-char tokens
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COLON, COMMA, DOT, MINUS, PLUS, QUESTION, SEMICOLON, SLASH, STAR,

	//1-2 char tokens
	ARROW, BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

	//literals
	IDENTIFIER, NUMBER, STRING,

	//types
	BOOL, NUM, STR,

	//keywords
	//TODO: convert `and` and `or` to `&&` and `||`, maybe add bitwise logic?
	//TODO: `val` not yet implemented
	AND, BREAK, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, RETURN, SUPER, THIS, TRUE, VAL, VAR, WHILE,

	//eof
	EOF
}
