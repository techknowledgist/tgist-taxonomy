package edu.brandeis.tgist.taxonomy;

class IsaRelation {

	String type;
	Technology source;
	Technology target;
	
	IsaRelation(String type, Technology source, Technology target) {
		this.type = type;
		this.source = source;
		this.target = target;
	}
}
