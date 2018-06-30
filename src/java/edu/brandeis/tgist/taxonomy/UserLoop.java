package edu.brandeis.tgist.taxonomy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class UserLoop {

	static Taxonomy taxonomy;

	static void run(Taxonomy taxonomy) {

		UserLoop.taxonomy = taxonomy;

		String term = null;
		try (Scanner reader = new Scanner(System.in)) {
			Map<Integer, String> mappings = new HashMap<>();
			while (true) {
				if (term == null)
					mappings = printSummary();
				System.out.print("\n>>> ");
				term = reader.nextLine();
				if (term.equals("q"))
					break;
				if (term.equals("h")) {
					term = null;
					continue; }
				term = expand(term);
				if (term.matches("^\\d+$")) {
					int idx = Integer.parseInt(term);
					term = mappings.get(idx); }
				Technology tech = UserLoop.taxonomy.technologies.get(term);
				if (tech == null)
					System.out.println("Not in taxonomy");
				else
					mappings = printFragment(tech);
			}
		}
	}

	static HashMap<Integer, String> printSummary() {
		HashMap<Integer, String> mappings = new HashMap<>();
		int idx = 0;
		System.out.print(
				String.format("\n%s%s Corpus%s\n\n", Node.BOLD, UserLoop.taxonomy.name, Node.END));
		System.out.print("Most significant terms:\n\n");
		Object[] actTerms = UserLoop.taxonomy.getActTerms();
		for (Object technology : actTerms) {
			idx++;
			Technology t2 = (Technology) technology;
			mappings.put(idx, t2.name);
			System.out.print(String.format("    [%d] %s", idx, t2.name + "\n"));
		}
		return mappings;
	}

	/**
	 * Print a fragment of the ontology viewed from the technology given.
	 *
	 * @param tech
	 * @return
	 */
	static HashMap<Integer, String> printFragment(Technology tech) {

		HashMap<Integer, String> mappings = new HashMap<>();
		int idx = 0;
		String hyphens = "-------------------------------------------------";
		System.out.println("\n" + hyphens + "\n");
		System.out.println(Node.BOLD + tech.name.toUpperCase() + Node.END + "\n");
		System.out.println("Occurrences in dataset: " + tech.count + "\n");
		if (tech.hypernyms.isEmpty())
			System.out.println("Top");
		else {
			for (Technology hyper : tech.hypernyms) {
				idx++;
				mappings.put(idx, hyper.name);
				System.out.println(String.format("[%d] %s", idx, hyper.name)); }}
		System.out.println("  " + Node.BLUE + Node.BOLD + tech.name + Node.END);
		int hypoCount = 0;
		for (Technology hypo : tech.hyponyms) {
			idx++;
			mappings.put(idx, hypo.name);
			hypoCount++;
			//if (hypoCount > 10) break;
			System.out.println(String.format("    [%d] %s", idx, hypo.name));
		}

		idx = printCooccurrenceRelations(tech, mappings, idx);
		printTermRelations(tech, mappings, idx);
		System.out.println("\n" + hyphens);
		return mappings;
	}

	private static String expand(String term) {
		// some abbreviations for debugging
		if (term.equals("as")) return "analog summing";
		if (term.equals("ga")) return "genetic algorithm";
		if (term.equals("aga")) return "adaptive genetic algorithm";
		if (term.equals("gr")) return "greedy block coordinate descent algorithm";
		return term;
	}

	private static int printCooccurrenceRelations(
			Technology tech, HashMap<Integer, String> mappings, int idx) {

		Object[] sortedRelations = tech.relations.values().toArray();
		Arrays.sort(sortedRelations);
		if (sortedRelations.length == 0) return idx;
		System.out.println("\n" + Node.UNDER + "Related terms:" + Node.END + "\n");
		int relCount = 0;
		for (Object obj : sortedRelations) {
			idx++;
			relCount++;
			if (relCount > 20) break;
			CooccurrenceRelation rel = (CooccurrenceRelation) obj;
			String relTarget = rel.target.name;
			mappings.put(idx, rel.target.name);
			System.out.println(String.format("    [%d] %s", idx, rel.target.name));
		}
		return idx;
	}

	private static int printTermRelations(
			Technology tech, HashMap<Integer, String> mappings, int idx) {

		if (tech.termRelations.isEmpty()) return idx;

		Object[] sortedRelations = tech.termRelations.toArray();
		Arrays.sort(sortedRelations);

		Set<String> rels = new HashSet<>();

		System.out.println("\n" + Node.UNDER + "Relations:" + Node.END + "\n");
		for (Object obj : sortedRelations) {
			TermRelation rel = (TermRelation) obj;
			String sig = rel.getSignature();
			if (rels.contains(sig)) continue;
			idx++;
			rels.add(sig);
			String term1 = rel.source.name;
			String term2 = rel.target.name;
			String mapped_term = tech.name.equals(term1) ? term2 : term1;
			mappings.put(idx, mapped_term);
			System.out.println(String.format("    [%d]  %s : [%s] ==> [%s]", idx, rel.pred, term1, term2));
			System.out.print("          ");
			rel.ppContext();
		}
		return idx;
	}

}
