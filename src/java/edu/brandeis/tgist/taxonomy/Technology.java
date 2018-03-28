package edu.brandeis.tgist.taxonomy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Technology {

	String name;
	int count;
	float score;
	List<Technology> hypernyms;
	List<Technology> hyponyms;
	List<IsaRelation> isaRelations;
	Map<String, Relation> relations;

	Technology(String term, float score, int count) {
		this.name = term;
		this.count = count;
		this.score = score;
		this.relations = new HashMap<>();
		this.isaRelations = new ArrayList<>();
		this.hypernyms = new ArrayList<>();
		this.hyponyms = new ArrayList<>();
	}

	@Override
	public String toString() {
		return String.format(
				"<term='%s' count=%d score=%f>", this.name, this.count, this.score);
	}

	public void addRelation(String relType, Technology tech) {
		if (this.relations.containsKey(tech.name)) {
			this.relations.get(tech.name).count++;
		} else {
			this.relations.put(tech.name, new Relation(relType, this, tech)); }
	}

	public void addRelation(String relType, int count, Technology tech) {
		if (this.relations.containsKey(tech.name)) {
			this.relations.get(tech.name).count++;
		} else {
			this.relations.put(tech.name, new Relation(relType, count, this, tech)); }
	}

	public String asTabSeparatedFields() {
		return String.format("%s\t%f\t%d\n", this.name, this.score, this.count);
	}

}
