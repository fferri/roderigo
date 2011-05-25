package roderigo.ai;

public class AlphaBeta {
	private int alpha;
	private int beta;
	
	public AlphaBeta(int a, int b) {
		alpha = a;
		beta = b;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getBeta() {
		return beta;
	}

	public void setBeta(int beta) {
		this.beta = beta;
	}
	
	public AlphaBeta clone() {
		return new AlphaBeta(alpha, beta);
	}
}
