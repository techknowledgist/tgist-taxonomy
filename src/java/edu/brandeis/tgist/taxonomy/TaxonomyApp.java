package edu.brandeis.tgist.taxonomy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaxonomyApp {

	static String DATA = "/DATA/techwatch/";
	static String CORPUS, TERMS, FEATS, TAXONOMY;

	public static void main(String[] args) {

		if (args.length > 0) {
			if (args.length == 3 && args[0].equals("--init"))
				initialize(args[1], args[2]);
			else if (args.length == 4 && args[0].equals("--import"))
				importData(args[1], args[2], args[3]);
			else if (args.length == 2 && args[0].equals("--build-hierarchy"))
				buildHierarchy(args[1]);
			else if (args.length == 2 && args[0].equals("--add-relations"))
				addRelations(args[1]);
			else if (args.length == 1 )
				userLoop(args[0]);
			else
				printUsage();
		} else {
			test(); }
	}

	private static void test() {
		// If there are no arguments then we are running this in develop mode
		// where we define what to do right here in the code.

		CORPUS = "SignalProcessing";
		CORPUS = "ComputerSciencePatents2002";
		CORPUS = "ComputerSciencePatents2007";
		CORPUS = "Thyme";
		
		TAXONOMY = "taxonomies/taxonomy-" + CORPUS;

		TERMS = DATA + CORPUS + "/classify.MaxEnt.out.s4.scores.sum.az";
		FEATS = DATA + CORPUS + ".txt.gz";

		boolean runInitialization = false;
		boolean runImport = false;
		boolean runBuildHierarchy = false;
		boolean runAddRelations = false;
		boolean runExport = false;
		boolean runLoop = true;

		if (runInitialization)
			initialize(CORPUS, TAXONOMY);

		if (runImport)
			importData(TAXONOMY, TERMS, FEATS);

		if (runBuildHierarchy)
			buildHierarchy(TAXONOMY);

		if (runAddRelations)
			addRelations(TAXONOMY);

		if (runExport)
			exportSQL(TAXONOMY);

		if (runLoop)
			userLoop(TAXONOMY);
	}

	private static void printUsage() {
		System.out.println(
				"\nUsage:\n\n" +
				"$ java -jar TGistTaxonomy.jar --init <TAXONOMY_NAME> <TAXONOMY_DIR>\n\n" +
				"    Initialize a taxonomy with name TAXONOMY_NAME in directory TAXONOMY_DIR\n\n" +
				"$ java -jar TGistTaxonomy.jar --import <TAXONOMY_DIR> <TERMS_FILE> <FEATURES_FILE>\n\n" +
				"    Import terms and features into the taxonomy in directory TAXONOMY_DIR\n\n" +
				"$ java -jar TGistTaxonomy.jar --build-hierarchy <TAXONOMY_DIR>\n\n" +
				"    Add ISA relations to the taxonomy in directory TAXONOMY_DIR\n\n" +
				"$ java -jar TGistTaxonomy.jar --add-relations <TAXONOMY_DIR>\n\n" +
				"    Add relations to the taxonomy in directory TAXONOMY_DIR\n\n" +
				"$ java -jar TGistTaxonomy.jar <TAXONOMY_DIR>\n\n" +
				"    Enter the user loop on the taxonomy in directory TAXONOMY_DIR\n\n");
	}

	private static void initialize(String name, String location) {
		try {
			Taxonomy tax = new Taxonomy(name, location);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void importData(String taxonomy, String terms, String features) {
		System.out.println(taxonomy);
		System.out.println(terms);
		System.out.println(features);
		Taxonomy tax = openTaxonomy(taxonomy);
		try {
			tax.importData(terms, features);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	private static void buildHierarchy(String taxonomyDir) {
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.rhhr();
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	private static void addRelations(String taxonomyDir) {
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.loadFeatures();
			System.out.println(taxonomy);
			//taxonomy.prettyPrint();
			taxonomy.addRelations();
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	private static void	exportSQL(String taxonomyDir) {
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.loadRelations();
			taxonomy.exportTables("exported_tables/" + taxonomy.name);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }}

	private static void userLoop(String taxonomyDir) {
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.loadRelations();
			taxonomy.prettyPrint();
			taxonomy.userLoop();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static Taxonomy openTaxonomy(String name) {
		Taxonomy taxonomy = null;
		try {
			taxonomy = new Taxonomy(name);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
		return taxonomy;
	}

}
