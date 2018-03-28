package edu.brandeis.tgist.taxonomy;

public class Relation {

	public static final String COOCCURENCE_RELATION = "coocc";

	String type;
	int count;
	Technology source, target;

	// TODO: might have something here like the strength of the relation (that is,
	// the number of occurrences). So if t1 and t2 are in the same abstract 5 times
	// then the strength would be 5 (instead of raw count we could also use
	// relative count or mutual information or whatnot)

	// TODO: we now have a limited number of fields, should perhaps make sure that
	// when we print we do sth like count=3 so we can enter those into the righ
	// field once we get more fields.

	Relation(String type, Technology source, Technology target) {
		this.type = type;
		this.count = 1;
		this.source = source;
		this.target = target;
	}

	Relation(String relType, int count, Technology source, Technology target) {
		this.type = type;
		this.count = count;
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format(
				"<Relation %s : '%s' ==> '%s'>",
				this.type, this.source.name, this.target.name);
	}

	public String asTabSeparatedString() {
		return String.format(
			"%s\t%s\t%s\n",
			this.type, this.source.name, this.target.name);
	}

	public String asTabSeparatedString(Technology technology) {
		if (technology.name.equals(source.name)) {
			return String.format("%s\t%d\t%s\n", this.type, this.count, this.target.name);
		} else {
			return asTabSeparatedString();
		}
	}
}
