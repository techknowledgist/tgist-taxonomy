package edu.brandeis.tgist.taxonomy;

public class Technology {

	String name;
	int count;
	float score;

	Technology(String term, float score, int count) {
		this.name = term;	 
		this.count = count;
		this.score = score;
	}
	
	@Override
	public String toString() {
		return String.format(
				"<term='%s' count=%d score=%f", this.name, this.count, this.score);
	}
}
