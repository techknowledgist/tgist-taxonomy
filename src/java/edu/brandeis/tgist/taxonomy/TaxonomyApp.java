package edu.brandeis.tgist.taxonomy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaxonomyApp {

	static final String CLASSIFICATION = "/DATA/techwatch/SignalProcessing";
	static final String TERMS = "/DATA/techwatch/SignalProcessing/classify.MaxEnt.out.s4.scores.sum.az";
	static final String FEATURES = "/DATA/techwatch/SignalProcessing.txt.gz";
	
	public static void main(String[] args) {
		//createTaxonomy("test", "test");
		openTaxonomy("test");
		//addData("test");
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
			System.out.println(taxonomy);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
		return taxonomy;
	}

	private static void addData(String taxonomyLocation) {
		Taxonomy tax;
		try {
			tax = new Taxonomy(taxonomyLocation);
			tax.addData(TERMS, FEATURES);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
