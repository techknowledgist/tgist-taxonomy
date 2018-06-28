package edu.brandeis.tgist.taxonomy;

public class TermRelation implements Comparable {

	String document;
	String relation;
	Technology source, target;

	TermRelation(String doc, String rel, Technology source, Technology target) {
		this.document = doc;
		this.relation = rel;
		this.source = source;
		this.target = target;
	}

	public String getSignature() {
		return String.format(
				"%s-%s-%s", this.relation, this.source.name, this.target.name);
	}

	@Override
	public String toString() {
		return String.format(
				"<rel='%s' %s  %s>",
				this.relation, this.source.name, this.target.name);
	}

	@Override
	public int compareTo(Object o) {
		TermRelation rel = (TermRelation) o;
		return this.relation.compareTo(rel.relation);
		//if (this.mi < rel.mi) return 1;
		//else if (this.mi > rel.mi) return -1;
		//return 0;
	}
}
