package space.bbkr.lang.jlox;

public class AstPrinter implements Expression.Visitor<String> {

	String print(Expression expression) {
		return expression.accept(this);
	}

	@Override
	public String visitAssignExpression(Expression.AssignExpression expression) {
		return ("= " + expression.name.lexeme + print(expression.value));
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

	@Override
	public String visitLogicalExpression(Expression.LogicalExpression expression) {
		return parenthesize("and", expression.left, expression.right);
	}

	@Override
	public String visitBinaryExpression(Expression.BinaryExpression expression) {
		return parenthesize(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitUnaryExpression(Expression.UnaryExpression expression) {
		return parenthesize(expression.operator.lexeme, expression.right);
	}

	@Override
	public String visitCallExpression(Expression.CallExpression expression) {
		return parenthesize("call", expression.arguments.toArray(new Expression[0]));
	}

	@Override
	public String visitGetExpression(Expression.GetExpression expression) {
		return parenthesize("get", expression.object);
	}

	@Override
	public String visitSetExpression(Expression.SetExpression expression) {
		return parenthesize("set", expression.object, expression.value);
	}

	@Override
	public String visitLiteralExpression(Expression.LiteralExpression expression) {
		if (expression.value == null) return "nil";
		return expression.value.toString();
	}

	@Override
	public String visitSuperExpression(Expression.SuperExpression expression) {
		return parenthesize("super");
	}

	@Override
	public String visitThisExpression(Expression.ThisExpression expression) {
		return "this";
	}

	@Override
	public String visitVariableExpression(Expression.VariableExpression expression) {
		return "(var " + expression.name.lexeme +")";
	}

	@Override
	public String visitGroupingExpression(Expression.GroupingExpression expression) {
		return parenthesize("group", expression.expression);
	}

	@Override
	public String visitClassExpression(Expression.ClassExpression expression) {
		return parenthesize("<class>");
	}

	@Override
	public String visitFunctionExpression(Expression.FunctionExpression expression) {
		return parenthesize("<function>");
	}

	@Override
	public String visitParameterExpression(Expression.ParameterExpression expression) {
		return expression.name.lexeme + ": " + expression.type.lexeme;
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
