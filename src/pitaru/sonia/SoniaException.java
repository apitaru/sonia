package pitaru.sonia;

public class SoniaException extends RuntimeException {

	public SoniaException(String s) {
		super(s);
	}

	public SoniaException(Throwable e) {
		super(e);
	}

	public SoniaException(String s, Throwable e) {
		super(s, e);
	}

	public SoniaException() {
		super();
	}
}
