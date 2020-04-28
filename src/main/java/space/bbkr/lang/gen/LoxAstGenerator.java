package space.bbkr.lang.gen;

import java.io.IOException;

public class LoxAstGenerator {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(1);
		}
	}
}
