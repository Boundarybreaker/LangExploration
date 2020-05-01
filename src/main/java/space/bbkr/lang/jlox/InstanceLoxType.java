package space.bbkr.lang.jlox;

//TODO: class heirarchy
public class InstanceLoxType extends LoxType {
	final Token name; //TODO: keep around?

	InstanceLoxType(Token name) {
		super(TokenType.INSTANCE, name.lexeme);
		this.name = name;
	}

	@Override
	boolean matches(LoxType other) {
		if (!super.matches(other)) return false;
		return lexeme.equals(other.lexeme);
	}
}
