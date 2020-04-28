package space.bbkr.lang.jlox;

import java.util.List;

abstract class Expression {

	abstract <R> R accept(Visitor<R> visitor);

	interface Visitor<R> {
		R visitLiteralExpression(Literal expression);
		R visitGroupingExpression(Grouping expression);
		R visitBlockExpression(Block expression);
		R visitUnaryExpression(Unary expression);
		R visitBinaryExpression(Binary expression);
		R visitTernaryExpression(Ternary expression);
	}

	static class Literal extends Expression {
		 final Object value;

		Literal(Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpression(this);
		}
	}

	static class Grouping extends Expression {
		 final Expression expression;

		Grouping(Expression expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpression(this);
		}
	}

	static class Block extends Expression {
		 final Expression left;
		 final Expression right;

		Block(Expression left, Expression right) {
			this.left = left;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockExpression(this);
		}
	}

	static class Unary extends Expression {
		 final Token operator;
		 final Expression right;

		Unary(Token operator, Expression right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpression(this);
		}
	}

	static class Binary extends Expression {
		 final Expression left;
		 final Token operator;
		 final Expression right;

		Binary(Expression left, Token operator, Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpression(this);
		}
	}

	static class Ternary extends Expression {
		 final Token question;
		 final Expression condition;
		 final Expression positive;
		 final Expression negative;

		Ternary(Token question, Expression condition, Expression positive, Expression negative) {
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
