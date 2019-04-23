package edu.brandeis.tgist.taxonomy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TaxonomyApp {

	static String DATA = "/DATA/techwatch/";
	static String CORPUS, TERMS, ACTS, FEATS, TAXONOMY;

	public static void main(String[] args) {

		System.out.println(args.length);
		if (args.length > 0) {
			if (args.length == 3 && args[0].equals("--init"))
				initialize(args[1], args[2]);
			else if (args.length == 4 && args[0].equals("--import"))
				importData(args[1], args[2], args[3], args[4]);
			else if (args.length == 2 && args[0].equals("--build-hierarchy"))
				buildHierarchy(args[1]);
			else if (args.length == 2 && args[0].equals("--add-relations"))
				addRelations(args[1]);
			else if (args.length == 1 && args[0].equals("--help"))
				printUsage();
			else if (args.length == 1 )
				userLoop(args[0]);
			else if (args.length == 6 && args[0].equals("--create"))
				create(args[1], args[2], args[3], args[4], args[5]);
			else
				printUsage();
		} else {
			test(); }
	}

	private static String option(String text) {
		return Node.RED + text + Node.END;
	}

	private static String value(String text) {
		return Node.BLUE + text + Node.END;
	}

	private static void test() {
		// If there are no arguments then we are running this in develop mode
		// where we define what to do right here in the code.
		String message = String.format(
				"$ java -jar TGISTTaxonomy.jar %s %s %s",
				option(" --init"),
				value("taxonomy-name"),
				value("taxonomy-directory"));
		System.out.println(message);
		//printUsage();

		CORPUS = "SignalProcessing";
		//CORPUS = "Thyme";

		TAXONOMY = "taxonomies/" + CORPUS;

		TERMS = DATA + CORPUS + "/classify.MaxEnt.out.s4.scores.sum.az";
		FEATS = DATA + CORPUS + ".txt.gz";
		ACTS = DATA + CORPUS + "/NB.IG50.test1.woc.9999.results.classes";

		boolean runCreate = false;
		boolean runInitialization = false;
		boolean runImport = false;
		boolean runBuildHierarchy = false;
		boolean runAddRelations = false;
		boolean runExport = false;
		boolean runLoop = true;

		if (runCreate)
			create(CORPUS, TAXONOMY, TERMS, ACTS, FEATS);

		if (runInitialization)
			initialize(CORPUS, TAXONOMY);

		if (runImport)
			importData(TAXONOMY, TERMS, ACTS, FEATS);

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
		System.out.println(usage()
				.replace("<b>", Node.BOLD)
				.replace("</b>", Node.END)
				.replace("<u>", Node.UNDER)
				.replace("</u>", Node.END));
	}

	private static String usage() {
		String command = "$ java -jar TGistTaxonomy.jar";
		return
			"\nUsage:\n\n" +
			command + " <b>--init</b> <u>taxonomy-name</u> <u>taxonomy-dir</u>\n\n" +
			"    Initialize a taxonomy with name taxonomy-name in directory taxonomy-name\n\n" +
			command + " <b>--import</b> <u>taxonomy-dir</u> <u>terms-file</u> <u>roles-file</u> <u>features-file</u>\n\n" +
			"    Import terms, roles and features into the taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--build-hierarchy</b> <u>taxonomy-dir</u>\n\n" +
			"    Add ISA relations to the taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--add-relations</b> <u>taxonomy-dir</u>\n\n" +
			"    Add relations to the taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--help</b>\n\n" +
			"    Prints this message\n\n" +
			command + " <u>taxonomy-dir</u>\n\n" +
			"    Enter the user loop on the taxonomy in directory taxonomy-dir\n\n";
	}

	/**
	 * Create a taxonomy from scratch. Initializes, imports data, creates the
	 * hierarchy and adds relations.
	 *
	 * @param taxonomyName
	 * @param taxonomyLocation
	 * @param terms
	 * @param acts
	 * @param features
	 */
	private static void create(
			String taxonomyName,
			String taxonomyLocation,
			String terms,
			String acts,
			String features) {
		System.out.println("Creating " + taxonomyLocation);
		initialize(taxonomyName, taxonomyLocation);
		importData(taxonomyLocation, terms, acts, features);
		buildHierarchy(taxonomyLocation);
		addRelations(taxonomyLocation);
	}


	/**
	 * Initialize an empty taxonomy.
	 * @param name the name of the taxonomy
	 * @param location path to the location of the taxonomy
	 */
	private static void initialize(String name, String location) {
		try {
			Taxonomy tax = new Taxonomy(name, location);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Import data into the taxonomy from a corpus.
	 *
	 * Three kinds of data are imported. (1) A list of all terms with technology
	 * scores; this is almost always a file named classify.MaxEnt.out.s4.scores.sum.az
	 * that was created by the tgist-classifiers code. (2) A list of terms with scores
	 * for all three roles, created by the https://github.com/techknowledgist/act code.
	 * (3) A list with all context features of all terms, created by the code in
	 * tgist-features.
	 *
	 * @param taxonomy path to the taxonomy
	 * @param terms path to the list of terms
	 * @param acts path to the of terms with domain roles
	 * @param features path the context features of all terms
	 */
	private static void importData(String taxonomy, String terms, String acts, String features) {
		System.out.println(taxonomy);
		System.out.println(terms);
		System.out.println(acts);
		System.out.println(features);
		Taxonomy tax = openTaxonomy(taxonomy);
		try {
			tax.importData(terms, acts, features);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	/**
	 * Build a hierarchy using the morphological rule and save it in the
	 * taxonomy.
	 *
	 * @param taxonomyDir path to the taxonomy
	 */
	private static void buildHierarchy(String taxonomyDir) {
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.rhhr();
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	/**
	 * Add relations to the taxonomy. The taxonomy is assumed to have imported
	 * all technologies and feature vectors (that is, the contexts of the terms).
	 *
	 * @param taxonomyDir path to the taxonomy
	 */
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
			taxonomy.exportTables();
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }}

	private static void userLoop(String taxonomyDir) {
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.loadRelations();
			taxonomy.prettyPrint();
			UserLoop.run(taxonomy);
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
