package edu.brandeis.tgist.taxonomy;

import static edu.brandeis.tgist.taxonomy.Utils.blue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TaxonomyApp {

	public static void main(String[] args) {

		if (args.length == 1 && args[0].equals("--help"))
			printUsage();
		else if (args.length == 3 && args[0].equals("--create"))
			create(args[1], args[2]);
		else if (args.length == 3 && args[0].equals("--init"))
			initialize(args[1], args[2]);
		else if (args.length == 2 && args[0].equals("--import"))
			importData(args[1]);
		else if (args.length == 2 && args[0].equals("--build-hierarchy"))
			buildHierarchy(args[1]);
		else if (args.length == 2 && args[0].equals("--add-relations"))
			addRelations(args[1]);
		else if (args.length == 2  && args[0].equals("--browse"))
			userLoop(args[1]);
		else
			test();
	}

	/**
	 * Method for running the code in development mode, used if no arguments
	 * are given.
	 */

	private static void test()
	{
		//String corpus = "Thyme";
		String corpus = "SignalProcessing";
		//String corpus = "Networking";
		String taxonomy = "/DATA/techwatch/taxonomies/" + corpus;
		String data = "/DATA/techwatch/data/" + corpus;

		boolean runCreate = false;
		boolean runInitialization = false;
		boolean runImport = false;
		boolean runBuildHierarchy = false;
		boolean runAddRelations = false;
		boolean runExport = false;
		boolean runLoop = true;

		System.out.println(taxonomy);
		if (runCreate) create(taxonomy, data);
		if (runInitialization) initialize(taxonomy, data);
		if (runImport) importData(taxonomy);
		if (runBuildHierarchy) buildHierarchy(taxonomy);
		if (runAddRelations) addRelations(taxonomy);
		if (runExport) exportSQL(taxonomy);
		if (runLoop) userLoop(taxonomy);
	}

	private static void printUsage() {
		System.out.println(usage()
				.replace("<b>", Node.BLUE)
				.replace("</b>", Node.END)
				.replace("<u>", Node.RED)
				.replace("</u>", Node.END));
	}

	private static String usage() {
		String command = "$ java -jar TGistTaxonomy.jar";
		return
			"\nUsage:\n\n" +
			command + " <b>--init</b> <u>taxonomy-dir</u> <u>data-dir</u>\n\n" +
			"    Initialize a taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--import</b> <u>taxonomy-dir</u>\n\n" +
			"    Import terms, roles and features into the taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--build-hierarchy</b> <u>taxonomy-dir</u>\n\n" +
			"    Add ISA relations to the taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--add-relations</b> <u>taxonomy-dir</u>\n\n" +
			"    Add relations to the taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--browse</b> <u>taxonomy-dir</u>\n\n" +
			"    Enter the user loop on the taxonomy in directory taxonomy-dir\n\n" +
			command + " <b>--help</b>\n\n" +
			"    Prints this message\n\n";
	}

	/**
	 * Create a taxonomy from scratch. Initializes, imports data, creates the
	 * hierarchy and adds relations.
	 *
	 * @param taxonomyName
	 * @param taxonomyLocation
	 */

	private static void create(String taxonomyLocation, String dataLocation)
	{
		printProgress(">>> Creating " + taxonomyLocation);
		initialize(taxonomyLocation, dataLocation);
		importData(taxonomyLocation);
		buildHierarchy(taxonomyLocation);
		addRelations(taxonomyLocation);
	}

	/**
	 * Initialize an empty taxonomy.
	 * @param taxonomyLocation path to the location of the taxonomy
	 * @param dataLocation path to the location of the data for the taxonomy
	 */

	private static void initialize(String taxonomyLocation, String dataLocation)
	{
		printProgress(">>> Initializing taxonomy in " + taxonomyLocation);
		try {
			Taxonomy tax = new Taxonomy(taxonomyLocation, dataLocation);
			printProgress(">>> Created " + tax);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Import corpus data for the taxonomy, using the settings in the taxonomy's
	 * properties file.
	 *
	 * @param taxonomy the path to the taxonomy
	 */

	private static void importData(String taxonomy)
	{
		try {
			Taxonomy tax = openTaxonomy(taxonomy);
			printProgress(">>> Importing data from " + tax.data);
			tax.importData();
			printProgress(">>> Updated " + tax);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}


	/**
	 * Build a hierarchy using the morphological rule and save it in the
	 * taxonomy.
	 *
	 * @param taxonomyDir path to the taxonomy
	 */

	private static void buildHierarchy(String taxonomyDir)
	{
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			printProgress(">>> Building hierarchy");
			taxonomy.rhhr();
			printProgress(">>> Updated " + taxonomy);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	/**
	 * Add relations to the taxonomy. The taxonomy is assumed to have imported
	 * all terms and feature vectors (that is, the contexts of the terms).
	 *
	 * @param taxonomyDir path to the taxonomy
	 */

	private static void addRelations(String taxonomyDir)
	{
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			printProgress(">>> Adding relations");
			taxonomy.loadFeatures();
			taxonomy.addRelations();
			printProgress(">>> Updated " + taxonomy);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	private static void	exportSQL(String taxonomyDir)
	{
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.exportTables();
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex); }
	}

	private static void userLoop(String taxonomyDir)
	{
		try {
			Taxonomy taxonomy = openTaxonomy(taxonomyDir);
			taxonomy.loadRelations();
			taxonomy.prettyPrint();
			UserLoop.run(taxonomy);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static Taxonomy openTaxonomy(String name)
	{
		Taxonomy taxonomy = null;
		try {
			taxonomy = new Taxonomy(name);
		} catch (IOException ex) {
			Logger.getLogger(TaxonomyApp.class.getName()).log(Level.SEVERE, null, ex);
		}
		return taxonomy;
	}

	private static void printProgress(String message)
	{
		System.out.println(blue(message));
	}

}
