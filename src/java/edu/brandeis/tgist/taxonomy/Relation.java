package edu.brandeis.tgist.taxonomy;

public class Relation {

	public static final String COOCCURENCE_RELATION = "cooccurrence";

	Technology source, target;

	// TODO: might have something here like the strength of the relation, that is
	// the number of occurrences. So if t1 and t2 are in the same abstract 5 times
	// then the strength would be 5 (instead of raw count we could also use
	// relative count or mutual information or whatnot)

	Relation(String type, Technology source, Technology target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format(
				"<Relation '%s' ==> '%s'",
				this.source.name, this.target.name);
	}
}
