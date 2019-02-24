package exception;

public abstract class MelpodyException extends Exception {

	private static final long serialVersionUID = 1L;

	protected MelpodyException(String message) {
		super(message);
	}
	
	protected MelpodyException() {
		super();
	}
}
