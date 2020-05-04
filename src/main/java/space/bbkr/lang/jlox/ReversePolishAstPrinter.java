package space.bbkr.lang.jlox;

/**
 * Debug class that presents a representation of the abstract syntax tree with reverse polish notation. Might be removed.
 */
class ReversePolishAstPrinter implements Expression.Visitor<String> {

	String print(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public String visitAssignExpression(Expression.AssignExpression expression) {
		return expression.name.lexeme + print(expression.value) + " =";
	}

	@Override
	public String visitTernaryExpression(Expression.TernaryExpression expression) {
		StringBuilder builder = new StringBuilder();
		builder.append(print(expression.positive)).append(" ");
		builder.append(print(expression.negative)).append(" ");
		builder.append(print(expression.condition)).append(" ?");
		return builder.toString();
	}

	@Override
	public String visitLogicalExpression(Expression.LogicalExpression expression) {
		return print(expression.left) + " " + print(expression.right) + " " + expression.operator.lexeme;
	}

	@Override
	public String visitBinaryExpression(Expression.BinaryExpression expression) {
		return stack(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitUnaryExpression(Expression.UnaryExpression expression) {
		return stack(expression.operator.lexeme, expression.right);
	}

	@Override
	public String visitCallExpression(Expression.CallExpression expression) {
		return stack("call", expression.arguments.toArray(new Expression[0]));
	}

	@Override
	public String visitGetExpression(Expression.GetExpression expression) {
		return stack("get", expression.object);
	}

	@Override
	public String visitSetExpression(Expression.SetExpression expression) {
		return stack("set", expression.object, expression.value);
	}

	@Override
	public String visitLiteralExpression(Expression.LiteralExpression expression) {
		if (expression.value == null) return "nil";
		return expression.value.toString();
	}

	@Override
	public String visitSuperExpression(Expression.SuperExpression expression) {
		return stack("super");
	}

	@Override
	public String visitThisExpression(Expression.ThisExpression expression) {
		return "this";
	}

	@Override
	public String visitVariableExpression(Expression.VariableExpression expression) {
		return expression.name.lexeme;
	}

	@Override
	public String visitGroupingExpression(Expression.GroupingExpression expression) {
		return stack("group", expression.expression);
	}

	@Override
	public String visitClassExpression(Expression.ClassExpression expression) {
		return stack("<class>");
	}

	@Override
	public String visitFunctionExpression(Expression.FunctionExpression expression) {
		return stack("<function>");
	}

	@Override
	public String visitParameterExpression(Expression.ParameterExpression expression) {
		return expression.name.lexeme + ": " + expression.type.lexeme;
	}

	private String stack(String name, Expression... expressions) {
		StringBuilder builder = new StringBuilder();
		for (Expression expression : expressions) {
			builder.append(expression.accept(this));
			builder.append(" ");
		}
		builder.append(name);
		return builder.toString();
	}
}
