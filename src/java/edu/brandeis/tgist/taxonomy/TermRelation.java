package edu.brandeis.tgist.taxonomy;

import java.util.ArrayList;
import java.util.List;

public class TermRelation implements Comparable {

	String document;
	String relation;
	Technology source, target;
	List<String> tokens;
	boolean hasPred;

	TermRelation(
			String doc, String rel,
			Technology source, Technology target) {

		this.document = doc;
		this.relation = rel;
		this.source = source;
		this.target = target;
	}

	TermRelation(
			String doc, String rel,
			Technology source, Technology target,
			ArrayList<FeatureVector> vectors) {

		this.document = doc;
		this.relation = rel;
		this.source = source;
		this.target = target;
		this.tokens = new ArrayList();
		this.hasPred = false;

		FeatureVector v1 = vectors.get(0);
		FeatureVector v2 = vectors.get(1);
		buildTokens(v1, v2);

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
	}

	private void buildTokens(FeatureVector v1, FeatureVector v2) {

		//ArrayList tokens = new ArrayList(<String>);

		String loc1 = v1.getFeature(FeatureVector.SENT_LOC);
		String loc2 = v2.getFeature(FeatureVector.SENT_LOC);
		int p2 = endPos(loc1);
		int p3 = startPos(loc2);
		String[] v1Prev3 = v1.getFeature(FeatureVector.PREV_N3).split("_");
		String[] v1Next3 = v1.getFeature(FeatureVector.NEXT_N3).split("_");
		String[] v2Prev3 = v2.getFeature(FeatureVector.PREV_N3).split("_");
		String[] v2Next3 = v2.getFeature(FeatureVector.NEXT_N3).split("_");

		for (String tok : v1Prev3) {
			if (! tok.equals("^"))
				this.tokens.add(tok); }
		this.tokens.add("[");
		for (String tok : this.source.name.split(" "))
			this.tokens.add(tok);
		this.tokens.add("]");

		if (p3 - p2 == 1) {}

		else if (p3 - p2 == 2) {
			String[] toks = { v1Next3[0] };
			addTokens(this.tokens, this.relation, toks); }
		else if (p3 - p2 == 3) {
			String[] toks = { v1Next3[0], v1Next3[1] };
			addTokens(this.tokens, this.relation, toks); }
		else if (p3 - p2 == 4) {
			String[] toks = { v1Next3[0], v1Next3[1], v1Next3[2] };
			addTokens(this.tokens, this.relation, toks); }
		else if (p3 - p2 == 5) {
			String[] toks = {
				v1Next3[0], v1Next3[1], v1Next3[2], v2Prev3[2] };
			addTokens(this.tokens, this.relation, toks); }
		else if (p3 - p2 == 6) {
			String[] toks = {
				v1Next3[0], v1Next3[1], v1Next3[2], v2Prev3[1], v2Prev3[2] };
			addTokens(this.tokens, this.relation, toks); }
		else if (p3 - p2 == 7) {
			String[] toks = {
				v1Next3[0], v1Next3[1], v1Next3[2], v2Prev3[0], v2Prev3[1], v2Prev3[2] };
			addTokens(this.tokens, this.relation, toks); }
		else
			this.tokens.add("___");

		this.tokens.add("[");
		for (String tok : this.target.name.split(" "))
			this.tokens.add(tok);
		this.tokens.add("]");
		for (String tok : v2Next3) {
			if (! tok.equals("^"))
				this.tokens.add(tok); }

		//System.out.println(this.relation + " " + loc1 + " " + loc2);
		//System.out.println(String.join(" ", this.tokens));
	}


	private void addTokens(List<String> tokens, String rel, String[] toks) {
		for (String tok : toks) {
			if (tok.equals(rel)) {
				this.hasPred = true;
				tokens.add("<");
				tokens.add(tok);
				tokens.add(">");
			} else {
				tokens.add(tok); }
		}
	}

	private int startPos(String loc) {
		return Integer.parseInt(loc.split("-")[0]);
	}

	private int endPos(String loc) {
		return Integer.parseInt(loc.split("-")[1]);
	}

}
