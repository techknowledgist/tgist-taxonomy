package edu.brandeis.tgist.taxonomy;

class IsaRelation {

	public static final String RHHR = "rhhr";

	public static int count = 0;

	String type;
	Technology source;
	Technology target;

	IsaRelation(String type, Technology source, Technology target) {
		count++;
		this.type = type;
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format(
				"<IsaRelation %s : '%s' ==> '%s'>",
				this.type, this.source.name, this.target.name);
	}

	public String asTabSeparatedString() {
		return String.format(
				"%s\t%s\t%s\n",
				this.type, this.source.name, this.target.name);
	}

	public String asTabSeparatedString(Technology tech) {
		if (tech.name.equals(source.name)) {
			return String.format("%s\t%s\n", this.type, this.target.name);
		} else {
			return asTabSeparatedString();
		}
	}

}
