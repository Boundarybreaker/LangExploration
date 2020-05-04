package space.bbkr.lang.jlox;

/**
 * We don't have a call stack in the interpreter, so no real easy way to back out of nested statements.
 * What's a quick, easy way to back out of nested execution in java? throwing!
 * We don't generate a stacktrace as to keep performance,
 * and catch this in the nearest while loop block.
 */
class Break extends RuntimeException {
	Break() {
		super(null, null, false, false);
	}
}
