package space.bbkr.lang.gen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Generator for the abstract syntax tree for Lox.
 * Because of this, I can't add any docs directly to Expression or Statement.
 * Run with arg `src/main/java/space/bbkr/lang/jlox` to make them generate in place
 * Entry format: <type name>: <comma-separated arguments>
 * support for @Nullable annotation is hardcoded, as strings are split on ' '
 * nodes are generally sorted by order of precedence - higher on list has higher precedence
 * each subclass has a pre-generated body so we only need to write one method to visit each type of class,
 * making it so that we don't need to worry about grain direction
 * (see <a href="https://craftinginterpreters.com/representing-code.html#the-expression-problem">The Expression Problem</a>)
 */
public class LoxAstGenerator {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(1);
		}
		String outputDir = args[0];
		//expressions - evaluated and a value is returned
		defineAst(outputDir, "Expression", Arrays.asList(
				"Assign: Token name, Expression value", //assign a value to a variable - `x = 5`
				"Ternary: Token question, Expression condition, Expression positive, Expression negative", //ternary operation - 5 == 5? true : false
				"Logical: Expression left, Token operator, Expression right", //boolean logic binary operation - true and false ('and' literal will be changed to '&&' later)
				"Binary: Expression left, Token operator, Expression right", //binary operation - +, -, *, /, <, <=, >, >=
				"Unary: Token operator, Expression right", //unary operation - !true, -5
				"Call: Expression callee, Token paren, List<Expression> arguments", //call a function or ctor (callee is the callable)
				"Get: Expression object, Token name", //get a property from an instance - object.property
				"Set: Expression object, Token name, Expression value", //set a property on an instance, object.property = 5
				"Literal: LoxType type, @Nullable Object value", //number, boolean, or string literal
				"Super: Token keyword, Token method", //call a method on superclass
				"This: Token keyword", //access a property or method on self
				"Variable: Token name", //reference a variable
				"Grouping: Expression expression", //do an operation inside of parentheses
				"Class: Statement.ClassStatement clazz", //define a class while inside an argument, for anonymous classes
				"Function: Statement.FunctionStatement function", //define a function inside of an argument, for anonymous functions
				"Parameter: Token name, LoxType type" //name and type for function param
		));

		defineAst(outputDir, "Statement", Arrays.asList(
				"If: Token keyword, Expression condition, Statement thenBranch, @Nullable Statement elseBranch", //if statement - if (true) print(5); else print(4);
				"Return: Token keyword, @Nullable Expression value, boolean hasType", //return
				"While: Token keyword, Expression condition, Statement body", //while loop - for loops are sugar
				"Break: Token keyword", //break a loop
				"Block: List<Statement> statements", //block of statements in curly brackets
				"Class: Token name, @Nullable Expression.VariableExpression superclass, List<Statement.FunctionStatement> methods", //class, with a name, optional superclass, and methods (properties can be added at any time)
				"Function: Token name, List<Expression.ParameterExpression> params, List<Statement> body, LoxType returnType", //function, with a name, params, a body, and a return type
				"Var: Token name, @Nullable Expression initializer", //variable with a name and optional initializer
				"Expression: Expression expression" //just an expression as a statement
		));
	}

	private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");

		//header
		writer.println("package space.bbkr.lang.jlox;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();
		writer.println("import javax.annotation.Nullable;");
		writer.println();
		writer.println("abstract class " + baseName + " {");
		writer.println();
		//base accept() method
		writer.println("\tabstract <R> R accept(Visitor<R> visitor);");
		writer.println();

		//visitor interface
		defineVisitor(writer, baseName, types);
		writer.println();

		//subclasses
		for (int i = 0; i < types.size(); i++) {
			String type = types.get(i);
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writer, baseName, className, fields);
			//don't print an extra blank line after the final type
			if (i != types.size() - 1) writer.println();
		}

		//footer
		writer.println("}");
		writer.close();
	}

	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("\tinterface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("\t\tR visit" + typeName + baseName + "(" + typeName + baseName + " " + baseName.toLowerCase() + ");");
		}

		writer.println("\t}");
	}

	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
		writer.println("\tstatic class " + className + baseName + " extends " + baseName + " {");
		String[] fields = fieldList.split(", ");
		for (String field : fields) {
			writer.println("\t\t final " + field + ";");
		}
		writer.println();
		writer.println("\t\t" + className + baseName + "(" + fieldList + ") {");
		for (String field : fields) {
			String[] split = field.split(" ");
			String name = split[0].equals("@Nullable")? split[2] : split[1];
			writer.println("\t\t\tthis." + name + " = " + name + ";");
		}
		writer.println("\t\t}");
		writer.println();
		writer.println("\t\t@Override");
		writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
		writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
		writer.println("\t\t}");
		writer.println("\t}");
	}
}
