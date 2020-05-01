package space.bbkr.lang.jlox;

import java.util.List;

import javax.annotation.Nullable;

abstract class Statement {

	abstract <R> R accept(Visitor<R> visitor);

	interface Visitor<R> {
		R visitIfStatement(IfStatement statement);
		R visitReturnStatement(ReturnStatement statement);
		R visitWhileStatement(WhileStatement statement);
		R visitBreakStatement(BreakStatement statement);
		R visitBlockStatement(BlockStatement statement);
		R visitClassStatement(ClassStatement statement);
		R visitFunctionStatement(FunctionStatement statement);
		R visitVarStatement(VarStatement statement);
		R visitExpressionStatement(ExpressionStatement statement);
	}

	static class IfStatement extends Statement {
		 final Token keyword;
		 final Expression condition;
		 final Statement thenBranch;
		 final @Nullable Statement elseBranch;

		IfStatement(Token keyword, Expression condition, Statement thenBranch, @Nullable Statement elseBranch) {
			this.keyword = keyword;
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStatement(this);
		}
	}

	static class ReturnStatement extends Statement {
		 final Token keyword;
		 final @Nullable Expression value;
		 final boolean hasType;

		ReturnStatement(Token keyword, @Nullable Expression value, boolean hasType) {
			this.keyword = keyword;
			this.value = value;
			this.hasType = hasType;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStatement(this);
		}
	}

	static class WhileStatement extends Statement {
		 final Token keyword;
		 final Expression condition;
		 final Statement body;

		WhileStatement(Token keyword, Expression condition, Statement body) {
			this.keyword = keyword;
			this.condition = condition;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStatement(this);
		}
	}

	static class BreakStatement extends Statement {
		 final Token keyword;

		BreakStatement(Token keyword) {
			this.keyword = keyword;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStatement(this);
		}
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

	static class ClassStatement extends Statement {
		 final Token name;
		 final List<Statement.FunctionStatement> methods;

		ClassStatement(Token name, List<Statement.FunctionStatement> methods) {
			this.name = name;
			this.methods = methods;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStatement(this);
		}
	}

	static class FunctionStatement extends Statement {
		 final Token name;
		 final List<Token> parms;
		 final List<Statement> body;

		FunctionStatement(Token name, List<Token> parms, List<Statement> body) {
			this.name = name;
			this.parms = parms;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStatement(this);
		}
	}

	static class VarStatement extends Statement {
		 final Token name;
		 final @Nullable Expression initializer;

		VarStatement(Token name, @Nullable Expression initializer) {
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
}
