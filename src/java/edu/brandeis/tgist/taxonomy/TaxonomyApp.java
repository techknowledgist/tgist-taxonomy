package edu.brandeis.tgist.taxonomy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaxonomyApp {

	static String DATA = "/DATA/techwatch/";
	static String CORPUS, TERMS, FEATS, TAXONOMY;

	public static void main(String[] args) {

		CORPUS = "SignalProcessing";
		//CORPUS = "SignalProcessingResolution";

		if (CORPUS.equals("SignalProcessing")) {
			TERMS = DATA + CORPUS + "/classify.MaxEnt.out.s4.scores.sum.az";
			FEATS = DATA + "SignalProcessing.txt.gz";

		} else if (CORPUS.equals("SignalProcessingResolution")) {
			TERMS = DATA + CORPUS + "/classify.MaxEnt.out.s4.scores.sum.az";
			FEATS = DATA + "SignalProcessingEmpty.txt.gz";
		}

		TAXONOMY = "taxonomy-" + CORPUS;

		// Initialization
		// createTaxonomy(CORPUS, TAXONOMY);

		// Data import
		// Taxonomy tax = openTaxonomy(TAXONOMY);
		// tax.importData(TERMS, FEATS);

		// Generating the hierarchy and adding relations
		Taxonomy taxonomy = openTaxonomy(TAXONOMY);
		taxonomy.rhhr();
		taxonomy.addRelations();
		taxonomy.userLoop();
	}

	private static void createTaxonomy(String name, String location) {
		try {
			Taxonomy tax = new Taxonomy(name, location);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static Taxonomy openTaxonomy(String name) {
		Taxonomy taxonomy = null;
		try {
			taxonomy = new Taxonomy(name);
			taxonomy.prettyPrint();
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
		return taxonomy;
	}

}
