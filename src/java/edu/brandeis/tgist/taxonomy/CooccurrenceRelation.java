package edu.brandeis.tgist.taxonomy;

public class CooccurrenceRelation implements Comparable {

	/**
	 * Class used to model the co-occurrence of two technologies in a document.
	 *
	 * A CooccurrenceRelation between two technologies t1 and t2 exist if the next
	 * technology in a document after t1 is t2.
	 */

	public static final String COOCCURENCE_RELATION = "coocc";

	int count;
	float mi;
	Technology source, target;

	CooccurrenceRelation(Technology source, Technology target) {
		this.count = 1;
		this.mi = 0f;
		this.source = source;
		this.target = target;
	}

	CooccurrenceRelation(int count, Technology source, Technology target) {
		this.count = count;
		this.mi = 0f;
		this.source = source;
		this.target = target;
	}

	CooccurrenceRelation(int count, float mi, Technology source, Technology target) {
		this.count = count;
		this.mi = mi;
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format("<CooccurrenceRelation (%d - %.2f) : '%s' ==> '%s'>",
				this.count, this.mi, this.source.name, this.target.name);
	}

	public Technology getTarget() {
		return this.target;
	}

	public String asTabSeparatedString() {
		return String.format("%s\t%s\n",
			this.source.name, this.target.name);
	}

	public String asTabSeparatedString(Technology technology) {
		if (technology.name.equals(source.name)) {
			return String.format("%d\t%.4f\t%s\n",
					this.count, this.mi, this.target.name);
		} else {
			return asTabSeparatedString();
		}
	}

	@Override
	public int compareTo(Object o) {
		CooccurrenceRelation rel = (CooccurrenceRelation) o;
		if (this.mi < rel.mi) return 1;
		else if (this.mi > rel.mi) return -1;
		return 0;
	}
}
