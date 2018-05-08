package edu.brandeis.tgist.taxonomy;

public class Relation implements Comparable {

	public static final String COOCCURENCE_RELATION = "coocc";

	String relType;
	int count;
	float mi;
	Technology source, target;

	Relation(String type, Technology source, Technology target) {
		this.relType = type;
		this.count = 1;
		this.mi = 0f;
		this.source = source;
		this.target = target;
	}

	Relation(String relType, int count, Technology source, Technology target) {
		this.relType = relType;
		this.count = count;
		this.mi = 0f;
		this.source = source;
		this.target = target;
	}

	Relation(String relType, int count, float mi, Technology source, Technology target) {
		this.relType = relType;
		this.count = count;
		this.mi = mi;
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format("<Relation %s (%d - %.2f) : '%s' ==> '%s'>",
				this.relType, this.count, this.mi, this.source.name, this.target.name);
	}

	public Technology getTarget() {
		return this.target;
	}

	public String asTabSeparatedString() {
		return String.format("%s\t%s\t%s\n",
			this.relType, this.source.name, this.target.name);
	}

	public String asTabSeparatedString(Technology technology) {
		if (technology.name.equals(source.name)) {
			return String.format("%s\t%d\t%.4f\t%s\n",
					this.relType, this.count, this.mi, this.target.name);
		} else {
			return asTabSeparatedString();
		}
	}

	@Override
	public int compareTo(Object o) {
		Relation rel = (Relation) o;
		if (this.mi < rel.mi) return 1;
		else if (this.mi > rel.mi) return -1;
		return 0;
	}
}
