package space.bbkr.lang.jlox;

import java.util.List;

import javax.annotation.Nullable;

abstract class Expression {

	abstract <R> R accept(Visitor<R> visitor);

	interface Visitor<R> {
		R visitAssignExpression(AssignExpression expression);
		R visitBlockExpression(BlockExpression expression);
		R visitTernaryExpression(TernaryExpression expression);
		R visitLogicalExpression(LogicalExpression expression);
		R visitBinaryExpression(BinaryExpression expression);
		R visitUnaryExpression(UnaryExpression expression);
		R visitCallExpression(CallExpression expression);
		R visitLiteralExpression(LiteralExpression expression);
		R visitVariableExpression(VariableExpression expression);
		R visitGroupingExpression(GroupingExpression expression);
		R visitFunctionExpression(FunctionExpression expression);
	}

	static class AssignExpression extends Expression {
		 final Token name;
		 final Expression value;

		AssignExpression(Token name, Expression value) {
			this.name = name;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpression(this);
		}
	}

	static class BlockExpression extends Expression {
		 final Expression left;
		 final Expression right;

		BlockExpression(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockExpression(this);
		}
	}

	static class TernaryExpression extends Expression {
		 final Token question;
		 final Expression condition;
		 final Expression positive;
		 final Expression negative;

		TernaryExpression(Token question, Expression condition, Expression positive, Expression negative) {
			this.question = question;
			this.condition = condition;
			this.positive = positive;
			this.negative = negative;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitTernaryExpression(this);
		}
	}

	static class LogicalExpression extends Expression {
		 final Expression left;
		 final Token operator;
		 final Expression right;

		LogicalExpression(Expression left, Token operator, Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpression(this);
		}
	}

	static class BinaryExpression extends Expression {
		 final Expression left;
		 final Token operator;
		 final Expression right;

		BinaryExpression(Expression left, Token operator, Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpression(this);
		}
	}

	static class UnaryExpression extends Expression {
		 final Token operator;
		 final Expression right;

		UnaryExpression(Token operator, Expression right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpression(this);
		}
	}

	static class CallExpression extends Expression {
		 final Expression callee;
		 final Token paren;
		 final List<Expression> arguments;

		CallExpression(Expression callee, Token paren, List<Expression> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpression(this);
		}
	}

	static class LiteralExpression extends Expression {
		 final @Nullable Object value;

		LiteralExpression(@Nullable Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpression(this);
		}
	}

	static class VariableExpression extends Expression {
		 final Token name;

		VariableExpression(Token name) {
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpression(this);
		}
	}

	static class GroupingExpression extends Expression {
		 final Expression expression;

		GroupingExpression(Expression expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpression(this);
		}
	}

	static class FunctionExpression extends Expression {
		 final Statement.FunctionStatement function;

		FunctionExpression(Statement.FunctionStatement function) {
			this.function = function;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionExpression(this);
		}
	}
}
