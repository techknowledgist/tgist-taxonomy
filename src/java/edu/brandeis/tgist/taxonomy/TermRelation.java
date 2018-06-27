package edu.brandeis.tgist.taxonomy;

public class TermRelation {

	String document;
	String relation;
	Technology source, target;

	TermRelation(String doc, String rel, Technology source, Technology target) {
		this.document = doc;
		this.relation = rel;
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format(
				"<rel='%s' %s  %s>",
				this.relation, this.source.name, this.target.name);
	}
}
