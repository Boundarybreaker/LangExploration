package space.bbkr.lang.jlox;

public class AstPrinter implements Expression.Visitor<String> {

	String print(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public String visitLiteralExpression(Expression.LiteralExpression expression) {
		if (expression.value == null) return "nil";
		return expression.value.toString();
	}

	@Override
	public String visitVariableExpression(Expression.VariableExpression expression) {
		return "(var " + expression.name.lexeme +")";
	}

	@Override
	public String visitAssignExpression(Expression.AssignExpression expression) {
		return ("= " + expression.name.lexeme + print(expression.value));
	}

	@Override
	public String visitGroupingExpression(Expression.GroupingExpression expression) {
		return parenthesize("group", expression.expression);
	}

	@Override
	public String visitBlockExpression(Expression.BlockExpression expression) {
		return parenthesize("block", expression.left, expression.right);
	}

	@Override
	public String visitUnaryExpression(Expression.UnaryExpression expression) {
		return parenthesize(expression.operator.lexeme, expression.right);
	}

	@Override
	public String visitBinaryExpression(Expression.BinaryExpression expression) {
		return parenthesize(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitTernaryExpression(Expression.TernaryExpression expression) {
		StringBuilder builder = new StringBuilder();
		builder.append("(? ");
		builder.append(print(expression.condition)).append(" ");
		builder.append(print(expression.positive)).append(" : ");
		builder.append(print(expression.negative));
		builder.append(")");
		return builder.toString();
	}

	private String parenthesize(String name, Expression... expressions) {
		StringBuilder builder = new StringBuilder();
		builder.append("(").append(name);
		for (Expression expression : expressions) {
			builder.append(" ");
			builder.append(expression.accept(this));
		}
		builder.append(")");
		return builder.toString();
	}
}
