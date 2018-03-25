package edu.brandeis.tgist.taxonomy;

import java.util.ArrayList;
import java.util.List;

public class Technology {

	String name;
	int count;
	float score;
	List<Technology> hypernyms;
	List<Technology> hyponyms;
	List<IsaRelation> isaRelations;
	List<Relation> relations;

	Technology(String term, float score, int count) {
		this.name = term;
		this.count = count;
		this.score = score;
		this.relations = new ArrayList<>();
		this.isaRelations = new ArrayList<>();
		this.hypernyms = new ArrayList<>();
		this.hyponyms = new ArrayList<>();
	}

	@Override
	public String toString() {
		return String.format(
				"<term='%s' count=%d score=%f>", this.name, this.count, this.score);
	}
}
