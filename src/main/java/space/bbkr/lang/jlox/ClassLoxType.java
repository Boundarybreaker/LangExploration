package space.bbkr.lang.jlox;

//TODO: class heirarchy
public class ClassLoxType extends LoxType {
	final Token name; //TODO: keep around?

	ClassLoxType(Token name) {
		super(TokenType.CLASS, name.lexeme);
		this.name = name;
	}

	@Override
	boolean matches(LoxType other) {
		if (!super.matches(other)) return false;
		return lexeme.equals(other.lexeme);
	}

	@Override
	boolean isCallable() {
		return true;
	}
}
