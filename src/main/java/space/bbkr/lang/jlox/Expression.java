package space.bbkr.lang.jlox;

import java.util.List;

abstract class Expression {

	abstract <R> R accept(Visitor<R> visitor);

	interface Visitor<R> {
		R visitLiteralExpression(LiteralExpression expression);
		R visitVariableExpression(VariableExpression expression);
		R visitAssignExpression(AssignExpression expression);
		R visitGroupingExpression(GroupingExpression expression);
		R visitBlockExpression(BlockExpression expression);
		R visitUnaryExpression(UnaryExpression expression);
		R visitBinaryExpression(BinaryExpression expression);
		R visitTernaryExpression(TernaryExpression expression);
	}

	static class LiteralExpression extends Expression {
		 final Object value;

		LiteralExpression(Object value) {
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
}
