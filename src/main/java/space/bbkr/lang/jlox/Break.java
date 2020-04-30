package space.bbkr.lang.jlox;

class Break extends RuntimeException {
	Break() {
		super(null, null, false, false);
	}
}
