package edu.brandeis.tgist.taxonomy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Math.log;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core class where taxonomy manipulations and taxonomy access occurs.
 *
 * A taxonomy has a name but is really defined defined by its location. It knows
 * where it has stored its main elements: taxonomy meta data, technologies, features,
 * hierarchy and relations. Technologies and features are imported from the
 * outside (created by tgist-features and tgist-classifiers) , the hierarchy
 * and the term relations are created by this class.
 *
 * The current implementation is static. You import technologies and features once
 * and you generate hierarchy and relations once. There is not yet any incremental
 * updating.
 */

public class Taxonomy {

	/** The name of the file that stores the properties. */
	public static final String PROPERTIES_FILE = "properties.txt";

	/** The name of the file that stores the feature vectors. */
	public static final String FEATURES_FILE = "features.txt";

	/** The name of the file that stores the technology terms. */
	public static final String TECHNOLOGIES_FILE = "technologies.txt";

	/** The name of the file that stores the ACT terms. */
	public static final String ACT_FILE = "act.txt";

	/** The name of the file that stores the hierarchical relations. */
	public static final String HIERARCHY_FILE = "hierarchy.txt";

	/** The name of the file that stores relations between technologies. */
	public static final String RELATIONS_FILE = "relations.txt";

	/** The minimum technology score required for a term to be included. */
	public static float TECHSCORE = 0.5f;

	/** The minimum term count required for a term to be included. */
	public static int MINCOUNT = 2;

	// TODO: allow changing TECHSCORE and MINCOUNT in the calling method and add
	// the values chosen to the properties file

	/** Number of terms to display on the splash screen. */
	public static int ACT_TERMS = 25;

	public String name;
	public String location;
	public HashMap<String, Technology> technologies;
	public List<Technology> acts;
	public List<FeatureVector> features;

	/**
	 * Create a new taxonomy. Creates a new directory and initializes the taxonomy,
	 * which includes writing a properties file.
	 *
	 * The technologies and features are created externally to the taxonomy code
	 * so they need to be imported after technology initialization:
	 *
	 *		Taxonomy tax = new Taxonomy(taxonomyName, taxonomyLocation);
	 *		tax.importData();
	 *
	 * This needs to be done only once since both technologies and features are
	 * available internally after inportData()..
	 *
	 * @param taxonomyName Name of the taxonomy.
	 * @param taxonomyLocation Location of the taxonomy.
	 * @throws java.io.IOException
	 */

	public Taxonomy(String taxonomyName, String taxonomyLocation)
			throws IOException {

		if (Files.exists(Paths.get(taxonomyLocation))) {
			Logger.getLogger(Taxonomy.class.getName())
					.log(Level.WARNING,
							"a file or directory named \"{0}\" allready exists",
							taxonomyLocation);
			System.exit(0);
		}

		this.name = taxonomyName;
		this.location = taxonomyLocation;

		new File(taxonomyLocation).mkdirs();
		ArrayList<String> lines = new ArrayList<>();
		lines.add("name = " + taxonomyName);
		lines.add("location = " + taxonomyLocation);
		String pFile = taxonomyLocation + File.separator + PROPERTIES_FILE;
		TaxonomyWriter.writeProperties(pFile, lines);
	}

	/**
	 * Open an existing taxonomy and load it into memory. This includes
	 * technologies, ACT terms and the hierarchy. Features and the relations
	 * are not loaded by this constructor.
	 *
	 * @param taxonomyLocation The location of the taxonomy.
	 * @throws java.io.FileNotFoundException
	 */

	public Taxonomy(String taxonomyLocation)
			throws FileNotFoundException, IOException {

		String pFile = taxonomyLocation +  File.separator + PROPERTIES_FILE;
		Properties properties = TaxonomyLoader.loadProperties(pFile);

		this.name = properties.getProperty("name");
		this.location = taxonomyLocation;
		this.technologies = new HashMap<>();
		this.acts = new ArrayList<>();
		this.features = new ArrayList<>();

		String tFile = this.location + File.separator + TECHNOLOGIES_FILE;
		String aFile = this.location + File.separator + ACT_FILE;
		String hFile = this.location + File.separator + HIERARCHY_FILE;
		TaxonomyLoader.loadTechnologies(tFile, this);
		TaxonomyLoader.loadACT(aFile, this);
		TaxonomyLoader.loadHierarchy(hFile, this);
	}

	/**
	 * Load relations into memory. This is done separately from initialization
	 * because relations are not always needed and they can take a lot of time
	 * to load.
	 *
	 * @throws FileNotFoundException
	 */

	public void loadRelations() throws FileNotFoundException {
		String rFile = this.location + File.separator + RELATIONS_FILE;
		TaxonomyLoader.loadRelations(rFile, this);
	}

	public Object[] getActTerms() {
		List<Technology> terms = this.acts;
		Object[] terms2 = terms.toArray();
		Arrays.sort(terms2);
		return terms2.length <= ACT_TERMS ? terms2 : Arrays.copyOfRange(terms2, 0, ACT_TERMS);
	}

	@Override
	public String toString() {
		return String.format("<taxonomy.Taxonomy %s terms=%d relations=%d>",
				this.name, this.technologies.size(), countRelations());
	}

	public void prettyPrint() {
		System.out.println(this);
		for (int i = 0; i < 5 && i < this.features.size(); i++)
			System.out.println("   " + this.features.get(i));
	}

	/**
	 * Print the taxonomy's tree. Now the taxonomy is not yet a tree, but a forest
	 * and this method is rather dumb because it ends up printing mini trees starting
	 * from each technology (introducing a lot of duplicate fragments). Later it
	 * will start printing at the top, at least, it will if we have single inheritance.
	 *
	 * This current version is here because I wanted to find a fragment with some
	 * depth. One of the deepest I found was "greedy block coordinate descent algorithm".

	 * @throws FileNotFoundException
	 */

	public void printHierarchyTree() throws FileNotFoundException, IOException {
		TaxonomyWriter.writeHierarchyTree("hierachyTree.txt", this);
	}

	/**
	 * Add data to a taxonomy.
	 *
	 * This is really just for the initialization and adding subsequent data will
	 * probably be done in another way. The terms file is a file as generated by
	 * the tgist-classifiers code and the features file is a file generated by the
	 * tgist-features code (more specifically, by the extract_features.py script).
	 *
	 * @param termsFile
	 * @param externalFeaturesFile
	 * @throws IOException
	 */

	public void importData(String termsFile, String actsFile, String externalFeaturesFile)
			throws IOException {

		File tFile = new File(this.location + File.separator + TECHNOLOGIES_FILE);
		File aFile = new File(this.location + File.separator + ACT_FILE);
		File vFile = new File(this.location + File.separator + FEATURES_FILE);

		CheckPoint cp = new CheckPoint(true);
		TaxonomyLoader.importTechnologies(termsFile, this, TECHSCORE, MINCOUNT);
		TaxonomyLoader.importACTS(actsFile, this);
		TaxonomyLoader.importFeatures(externalFeaturesFile, this);
		//cp.report("importData");
		TaxonomyWriter.writeTechnologies(tFile, this.technologies);
		TaxonomyWriter.writeACT(aFile, this.acts);
		TaxonomyWriter.writeFeatures(vFile, this.features);
	}

	/**
	 * Load the features.
	 *
	 * @throws FileNotFoundException
	 */

	void loadFeatures() throws FileNotFoundException {
		String vFile = this.location + File.separator + FEATURES_FILE;
		TaxonomyLoader.loadFeatures(vFile, this);
	}

	/**
	 * Apply the righthand head rule to elements in the taxonomy. As a result,
	 * instances of IsaRelation are added to technologies such that if isa(t1,t2)
	 * appears as a IsaRelation on both t1 and t1. In addition, if isa(t1,t2) then
	 * t1 is added as a hypernym to t2 and t2 is added as a hyponym to t1.
	 */

	void rhhr() throws IOException {
		Node top = new Node("Top");
		int c = 0;
		// The way this works is by creating a tree where nodes are inserted
		// depending on the tokens in the term. The top node Node("Top") has no
		// tokens, a node Node("door") would be added as a direct child of Top
		// and a node Node("iron door") would be added as an immediate child of
		// Node("door"). Nodes can be associated with an instance of Technology.
		for (Technology tech : this.technologies.values()) {
			c++;
			//if (c > 100) break;
			//System.out.println("\n" + tech.name);
			String[] tokens = tech.name.split(" ");
			top.insert(tech, tokens, tokens.length);
		}
		//top.prettyPrint();
		top.addIsaRelations(null);
		System.out.println(String.format("Created %d isa relations", IsaRelation.count));
		File hFile = new File(this.location + File.separator + HIERARCHY_FILE);
		TaxonomyWriter.writeHierarchy(hFile, this);
	}

	/**
	 * Add relations to technologies in the ontology. Creates a sliding window
	 * over the terms and stipulate that there is a cooccurrence relation if terms
	 * cooccur in that window.
	 *
	 * This code will be put in its own class.
	 */

	void addRelations() throws IOException {

		String relType = Relation.COOCCURENCE_RELATION;
		int relationCount = 0;
		System.out.println();
		CooccurrenceWindow window = new CooccurrenceWindow();
		for (FeatureVector vector : this.features) {
			window.update(vector);
			for (String[] p : window.cooccurrencePairs()) {
				Technology t1 = this.technologies.get(p[0]);
				Technology t2 = this.technologies.get(p[1]);
				relationCount++;
				// These are stored on both source and target technologies
				// (as opposed to isa relations, which go only one way)
				t1.addRelation(relType, t2);
				t2.addRelation(relType, t1); }
		}
		System.out.println(String.format(
				"Occurence relations created (tokens) %,12d", relationCount));
		System.out.println(String.format(
				"Occurence relations created (types)  %,12d", countRelations()));
		//filterRelations();
		//System.out.println(String.format(
		//		"Types after filtering                %,12d", countRelations()));
		File rFile = new File(this.location + File.separator + RELATIONS_FILE);
		calculateMutualInformation();
		TaxonomyWriter.writeRelations(rFile, this);
	}

	/**
	 * Collect all technologies and group them by document name. This allows us
	 * to collect relations between technologies on a document by document basis.
	 *
	 * @return A Map with Strings as keys and lists of Technology instances
	 * as values.
	 */

	private	Map<String, List<Technology>> groupTechnologiesByDocument() {
		System.out.println("\nGrouping technologies");
		CheckPoint checkpoint = new CheckPoint();
		//checkpoint.showFootPrint("this.features", this.features);
		checkpoint.reset();
		Map<String, List<Technology>> groupedTechnologies;
		groupedTechnologies = new HashMap<>();
		int c = 0;
		for (FeatureVector vector : this.features) {
			//System.out.println(vector);
			c++;
			if ((c % 100000) == 0) {
				System.out.println(c);
				System.out.flush(); }
			// NOTE: a technology might occur multiple times in a document
			// TODO: should probably group the vectors and not the technologies
			groupedTechnologies
					.putIfAbsent(vector.fileName, new ArrayList<>());
			groupedTechnologies
					.get(vector.fileName)
					.add(this.technologies.get(vector.term));
		}
		checkpoint.report("groupTechnologiesByDocument");
		//checkpoint.showFootPrint("groupedTechnologies", groupedTechnologies);
		//System.exit(0);
		return groupedTechnologies;
	}

	void filterRelations() {
		for (Technology technology : this.technologies.values()) {
			technology.filterRelations(); }
	}

	int countRelations() {
		int count = 0;
		for (Technology technology : this.technologies.values())
			count += technology.relations.size();
		return count;
	}

	void userLoop() throws FileNotFoundException {
		String term = null;
		try (Scanner reader = new Scanner(System.in)) {
			Map<Integer, String> mappings = new HashMap<>();
			while (true) {
				if (term == null) {
					mappings = printFlashScreen();
				}
				System.out.print("\n>>> ");
				term = reader.nextLine();
				if (term.equals("q"))
					break;
				if (term.equals("h")) {
					term = null;
					continue; }
				// some abbreviations for debugging
				if (term.equals("ga")) term = "genetic algorithm";
				if (term.equals("aga")) term = "adaptive genetic algorithm";
				if (term.equals("gr")) term = "greedy block coordinate descent algorithm";
				if (term.matches("^\\d+$")) {
					int idx = Integer.parseInt(term);
					term = mappings.get(idx);
				}
				//System.out.println(String.format("[%s]", term));
				Technology tech = this.technologies.get(term);
				if (tech == null)
					System.out.println("Not in taxonomy");
				else
					mappings = printFragment(tech);
			}
		}
	}

	HashMap<Integer, String> printFlashScreen() {
		HashMap<Integer, String> mappings = new HashMap<>();
		int idx = 0;
		System.out.print(
				String.format("\n%s%s Corpus%s\n\n", Node.BOLD, this.name, Node.END));
		Object[] actTerms = this.getActTerms();
		for (Object technology : actTerms) {
			idx++;
			Technology t2 = (Technology) technology;
			mappings.put(idx, t2.name);
			System.out.print(String.format("[%d] %s", idx, t2.name + "\n"));
		}
		return mappings;
	}

	HashMap<Integer, String> printFragment(Technology tech) {
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
		System.out.println("\n" + Node.UNDER + "Related terms:" + Node.END + "\n");

		Collection<Relation> relations = tech.relations.values();
		Object[] sortedRelations = relations.toArray();
		Arrays.sort(sortedRelations);

		int relCount = 0;
		for (Object obj : sortedRelations) {
			idx++;
			relCount++;
			if (relCount > 20) break;
			Relation rel = (Relation) obj;
			String relTarget = rel.target.name;
			mappings.put(idx, rel.target.name);
			System.out.println(String.format("    [%d] %s", idx, rel.target.name));
		}

		System.out.println("\n" + hyphens);
		return mappings;
	}

	void exportTables(String outputDir) throws IOException {
		new File(outputDir).mkdirs();
		TaxonomyWriter.writeTermsAsTable(
				txtFile(this.location, TECHNOLOGIES_FILE),
				sqlFile(outputDir, TECHNOLOGIES_FILE));
		TaxonomyWriter.writeHierarchyAsTable(
				txtFile(this.location, HIERARCHY_FILE),
				sqlFile(outputDir, HIERARCHY_FILE));
		TaxonomyWriter.writeRelationsAsTable(
				txtFile(this.location, RELATIONS_FILE),
				sqlFile(outputDir, RELATIONS_FILE));
	}

	String txtFile(String directory, String filename) {
		return directory + File.separator + filename;
	}

	String sqlFile(String directory, String filename) {
		String filebase = filename.substring(0, filename.lastIndexOf('.'));
		return directory + File.separator + filebase + ".sql";
	}

	private void calculateMutualInformation() {
		int n = this.technologies.size();
		int c = 0;
		for (Technology t1 : this.technologies.values()) {
			c++;
			if (c < 10) {
				System.out.println();
				System.out.println(t1); }
			int c2 = 0;
			for (String t2_name : t1.relations.keySet()) {
				c2++;
				Technology t2 = this.technologies.get(t2_name);
				Relation rel = t1.relations.get(t2_name);
				float mi = mutualInformation(n, t1, t2, rel);
				rel.mi = mi;
				if (c < 10) {
					System.out.println(
					String.format(
							"   %f %d %d %d %s",
							mi, rel.count, t1.count, t2.count, t2.name));
				}
			}
		}
	}

	private float mutualInformation(int n, Technology t1, Technology t2, Relation rel) {
		float p_x_y = rel.count * 2 / (float) n;
		float p_x = t1.count / (float) n;
		float p_y = t2.count / (float) n;
		return (float) log(p_x_y / (p_x * p_y));
	}

}
