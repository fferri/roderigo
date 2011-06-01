package roderigo.ai;

public class AbortException extends Exception {
	private static final long serialVersionUID = -5608434305271667562L;

	public AbortException() {
		super("AIPlayer aborted");
	}
}
