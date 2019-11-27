package edu.brandeis.tgist.taxonomy;

import static edu.brandeis.tgist.taxonomy.Utils.blue;
import static edu.brandeis.tgist.taxonomy.Utils.red;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TermRelation implements Comparable {

	String document;
	String pred;
	Technology source, target;
	Context context;
	ArrayList<FeatureVector> vectors;

	TermRelation(
			String doc, String rel,
			Technology source, Technology target) {

		this.document = doc;
		this.pred = rel;
		this.source = source;
		this.target = target;
		this.context = new Context(this);
	}

	TermRelation(
			String doc, String rel,
			Technology source, Technology target,
			ArrayList<FeatureVector> vectors) {

		this.document = doc;
		this.pred = rel.replace("_", " ");
		this.source = source;
		this.target = target;
		this.context = new Context(this);
		this.vectors = vectors;
		buildContext();
	}

	public String getSignature() {
		return String.format("%s-%s-%s", this.pred, this.source.name, this.target.name);
	}

	@Override
	public String toString() {
		return String.format("<rel='%s' %s  %s>",
				this.pred, this.source.name, this.target.name);
	}

	@Override
	public int compareTo(Object o) {
		TermRelation rel = (TermRelation) o;
		return this.pred.compareTo(rel.pred);
	}

	private void buildContext() {
		FeatureVector v1 = this.vectors.get(0);
		FeatureVector v2 = this.vectors.get(1);
		String loc1 = v1.getFeature(FeatureVector.SENT_LOC);
		String loc2 = v2.getFeature(FeatureVector.SENT_LOC);
		String[] v1Prev3 = v1.getFeature(FeatureVector.PREV_N3).split("_");
		String[] v1Next3 = v1.getFeature(FeatureVector.NEXT_N3).split("_");
		String[] v2Prev3 = v2.getFeature(FeatureVector.PREV_N3).split("_");
		String[] v2Next3 = v2.getFeature(FeatureVector.NEXT_N3).split("_");
		this.context.addSpan(v1Prev3);
		this.context.addTerm(this.source);
		this.context.addPivot(endPos(loc1), v1Next3, v2Prev3, startPos(loc2));
		this.context.addTerm(this.target);
		this.context.addSpan(v2Next3);
	}

	private int startPos(String loc) {
		return Integer.parseInt(loc.split("-")[0]);
	}

	private int endPos(String loc) {
		return Integer.parseInt(loc.split("-")[1]);
	}

	public String contextAsTabSeparatedString() {
		return this.context.asTabSeparatedString();
	}

	public void addContextElement(String text) {
		this.context.addElement(text);
	}

	public void ppContext() {
		this.context.pp();
	}

}



class Context {

	TermRelation relation;
	List<Span> elements;
	boolean hasPred;
	boolean hasSource;
	boolean hasTarget;

	Context(TermRelation rel) {
		this.relation = rel;
		this.elements = new ArrayList();
		this.hasPred = false;
		this.hasSource = false;
		this.hasTarget = false;
	}

	@Override
	public String toString() {
		return "<Context>";
	}

	void addSpan(String[] v1Prev3) {
		List<String> toks = new ArrayList();
		for (String tok : v1Prev3) {
			if (! tok.equals("^")) toks.add(tok); }
		this.elements.add(new Span(String.join(" ", toks)));
	}

	void addTerm(Technology tech) {
		this.elements.add(new Term(tech.name));
	}

	void addPivot(int p1, String[] v1Next3, String[] v2Prev3, int p2) {

		// p1 is the offset after the first term and p2 the offset before the
		// second term so for the number of elements in the span we just need
		// to substract
		int span = p2 - p1;

		if (span == 0) {
			// nothing between the terms, so nothing to so
		} else if (span == 1) {
			String[] toks = { v1Next3[0] };
			addTokens(this.relation.pred, toks);
		} else if (span == 2) {
			String[] toks = { v1Next3[0], v1Next3[1] };
			addTokens(this.relation.pred, toks);
		} else if (span == 3) {
			String[] toks = { v1Next3[0], v1Next3[1], v1Next3[2] };
			addTokens(this.relation.pred, toks);
		} else if (span == 4) {
			String[] toks = {
				v1Next3[0], v1Next3[1], v1Next3[2], v2Prev3[2] };
			addTokens(this.relation.pred, toks);
		} else if (span == 5) {
			String[] toks = {
				v1Next3[0], v1Next3[1], v1Next3[2], v2Prev3[1], v2Prev3[2] };
			addTokens(this.relation.pred, toks);
		} else if (span == 6) {
			String[] toks = {
				v1Next3[0], v1Next3[1], v1Next3[2], v2Prev3[0], v2Prev3[1], v2Prev3[2] };
			addTokens(this.relation.pred, toks);
		} else {
			Logger.getLogger(TermRelation.class.getName()).log(
					Level.WARNING, "Unexpected distance between terms");
		}
	}

	private void addTokens(String rel, String[] toks) {
		rel = rel.replace("_", " ");
		String tokString = String.join(" ", toks);
		String pattern = "\\b" + rel + "\\b";
		Pattern regex = Pattern.compile(pattern);
		Matcher m = regex.matcher(tokString);
		if (m.find()) {
			String expanded = "\t" + tokString + "\t";
			this.hasPred = true;
			String matchingString = m.group(0);
			String[] contexts = expanded.split(matchingString);
			String left = contexts[0].trim();
			String right = contexts[1].trim();
			if (! left.isEmpty())
				this.elements.add(new Span(left.trim()));
			this.elements.add(new Pred(rel));
			if (! right.isEmpty())
				this.elements.add(new Span(right.trim()));
		} else {
			this.elements.add(new Span(tokString)); }
	}

	void pp() {
		for (Span element : this.elements)
			element.pp();
		System.out.println();
	}

	String asTabSeparatedString() {
		List<String> els = new ArrayList();
		for (Span e : this.elements) {
			if (! e.isEmpty()) els.add(e.asString()); }
		return String.join("\t", els);
	}

	void addElement(String text) {
		if (text.startsWith("Span"))
			this.elements.add(new Span(text.substring(5)));
		else if (text.startsWith("Pred"))
			this.elements.add(new Pred(text.substring(5)));
		else if (text.equals("Term")) {
			if (! this.hasSource) {
				this.hasSource = true;
				this.elements.add(new Term(this.relation.source.name)); }
			else {
				this.hasTarget = true;
				this.elements.add(new Term(this.relation.target.name)); }
		}
	}

}


class Span {

	String span;

	Span(String text) {
		this.span = text; }

	boolean isEmpty() {
		return this.span.equals(""); }

	void pp() {
		System.out.print(this.span + " "); }

	/**
	 * Return the span as a string for use in the TaxonomyWriter.
	 */
	String asString() {
		return String.format("%s %s", this.getClass().getSimpleName(), this.span); }
}


class Term extends Span {

	public Term(String text)
	{
		super(text);
	}
	
	@Override void pp()
	{
		System.out.print(blue(this.span) + ' ');
	}

	@Override String asString()
	{
		return "Term";
	}

}


class Pred extends Span {
	
	public Pred(String text)
	{
		super(text);
	}

	@Override void pp()
	{
		System.out.print(red(this.span) + ' ');
	}

}