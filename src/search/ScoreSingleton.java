package search;


public class ScoreSingleton {

private float bullScore = 0f;
private float bearScore = 0f;
private static ScoreSingleton instance = null;	

	protected ScoreSingleton() {
	}
	
	public static ScoreSingleton getInstance() {
		if (instance == null) {
			instance = new ScoreSingleton();
		}
		return instance;
	}
	
	public float getBullScore() {
		return bullScore;
	}

	public void setBullScore(float bullScore) {
		this.bullScore = bullScore;
	}

	public float getBearScore() {
		return bearScore;
	}

	public void setBearScore(float bearScore) {
		this.bearScore = bearScore;
	}
}