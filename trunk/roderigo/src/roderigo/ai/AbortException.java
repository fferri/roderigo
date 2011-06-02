package roderigo.ai;

/**
 * Thrown if user aborts AI computation.
 * (Also used internally to quickly exit the minmax call stack)
 * 
 * @author Federico Ferri
 *
 */
public class AbortException extends Exception {
	private static final long serialVersionUID = -5608434305271667562L;

	public AbortException() {
		super("AIPlayer aborted");
	}
}
