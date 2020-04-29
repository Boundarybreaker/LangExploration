package space.bbkr.lang.jlox;

import java.util.List;

abstract class Statement {

	abstract <R> R accept(Visitor<R> visitor);

	interface Visitor<R> {
		R visitBlockStatement(BlockStatement statement);
		R visitVarStatement(VarStatement statement);
		R visitExpressionStatement(ExpressionStatement statement);
		R visitPrintStatement(PrintStatement statement);
	}

	static class BlockStatement extends Statement {
		 final List<Statement> statements;

		BlockStatement(List<Statement> statements) {
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStatement(this);
		}
	}

	static class VarStatement extends Statement {
		 final Token name;
		 final Expression initializer;

		VarStatement(Token name, Expression initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStatement(this);
		}
	}

	static class ExpressionStatement extends Statement {
		 final Expression expression;

		ExpressionStatement(Expression expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStatement(this);
		}
	}

	static class PrintStatement extends Statement {
		 final Expression expression;

		PrintStatement(Expression expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStatement(this);
		}
	}
}
