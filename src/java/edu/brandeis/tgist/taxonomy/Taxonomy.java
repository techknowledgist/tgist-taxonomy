package edu.brandeis.tgist.taxonomy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Math.log;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
	public static final String TERMS_FILE = "terms.txt";

	/** The name of the file that stores the ACT terms. */
	public static final String ROLES_FILE = "roles.txt";

	/** The name of the file that stores the hierarchical relations. */
	public static final String HIERARCHY_FILE = "hierarchy.txt";

	/** The name of the file that stores cooccurrence relations between technologies. */
	public static final String RELATIONS_FILE = "relations-cooc.txt";

	/** The name of the file that stores term relations between technologies. */
	public static final String TERM_RELATIONS_FILE = "relations-term.txt";

	/** The name of the file with input terms. */
	public static final String INPUT_TERMS = "classify.MaxEnt.out.s4.scores.sum.az";

	/** The name of the file with input features. */
	public static final String INPUT_FEATURES = "features.txt.gz";

	/** The name of the file with input term roles. */
	public static final String INPUT_ROLES = "NB.IG50.test1.woc.9999.results.classes";

	/** The minimum technology score required for a term to be included. */
	public static float TECHSCORE = 0.5f;

	/** The minimum term count required for a term to be included. */
	public static int MINCOUNT = 2;

	// TODO: allow changing TECHSCORE and MINCOUNT in the calling method and add
	// TODO: ... the values chosen to the properties file

	/** Number of terms to display on the splash screen. */
	public static int NUMBER_OF_TERMS = 25;

	public String name;
	public String location;
	public String data;
	public HashMap<String, Technology> technologies;
	public List<Technology> roles;
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
	 * available internally after inportData().
	 *
	 * @param taxonomyLocation path to the taxonomy.
	 * @param dataLocation path to the input data from the corpus
	 * @throws java.io.IOException
	 */

	public Taxonomy(String taxonomyLocation, String dataLocation)
			throws IOException
	{
		checkTaxonomyExistence(taxonomyLocation);
		File taxonomy = new File(taxonomyLocation);
		this.name = taxonomy.getName();
		this.data = dataLocation;
		initializeData();
		taxonomy.mkdirs();
		ArrayList<String> lines = new ArrayList<>();
		lines.add("name = " + this.name);
		lines.add("data = " + this.data);
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
			throws FileNotFoundException, IOException
	{
		String pFile = taxonomyLocation +  File.separator + PROPERTIES_FILE;
		Properties properties = TaxonomyLoader.loadProperties(pFile);
		this.name = properties.getProperty("name");
		this.data = properties.getProperty("data");
		this.location = taxonomyLocation;
		initializeData();
		String tFile = this.location + File.separator + TERMS_FILE;
		String rFile = this.location + File.separator + ROLES_FILE;
		String hFile = this.location + File.separator + HIERARCHY_FILE;
		TaxonomyLoader.loadTechnologies(tFile, this);
		TaxonomyLoader.loadRoles(rFile, this);
		TaxonomyLoader.loadHierarchy(hFile, this);
	}

	/**
	 * Initialize technologies, roles and features to empty maps and lists.
	 */
	private void initializeData()
	{
		this.technologies = new HashMap<>();
		this.roles = new ArrayList<>();
		this.features = new ArrayList<>();
	}

	/**
	 * Check existence of taxonomy. Exit with a warning if the taxonomy already
	 * exists.
	 * @param taxonomyLocation path to the taxonomy
	 */
	private void checkTaxonomyExistence(String taxonomyLocation)
	{
		if (Files.exists(Paths.get(taxonomyLocation))) {
			Logger.getLogger(Taxonomy.class.getName())
					.log(Level.WARNING,
							"a file or directory named \"{0}\" allready exists",
							taxonomyLocation);
			System.exit(0); }
	}

	/**
	 * Load relations into memory. This is done separately from initialization
	 * because relations are not always needed and they can take a lot of time
	 * to load.
	 *
	 * @throws FileNotFoundException
	 */

	public void loadRelations() throws FileNotFoundException
	{
		String crFile = this.location + File.separator + RELATIONS_FILE;
		String trFile = this.location + File.separator + TERM_RELATIONS_FILE;
		TaxonomyLoader.loadCooccurrenceRelations(crFile, this);
		TaxonomyLoader.loadTermRelations(trFile, this);
	}

	/**
	 * Returns the most significant terms.
	 *
	 * This is now rather unsatisfactory. The idea was that the we would return
	 * terms with the task role, but most roles could not be linked to a term so
	 * we now take the most frequent ones of those terms that do have roles. It is
	 * probably better to just get the most frequent terms.
	 *
	 * @return
	 */
	public Object[] getMostSignificantTerms()
	{
		// TODO: Seriously reconsider this, could use:
		// TODO: Object[] terms = this.technologies.values().toArray();
		Object[] terms = this.roles.toArray();
		Arrays.sort(terms);
		return terms.length <= NUMBER_OF_TERMS
				? terms
				: Arrays.copyOfRange(terms, 0, NUMBER_OF_TERMS);
	}

	@Override
	public String toString()
	{
		return String.format("<taxonomy.Taxonomy %s terms=%d relations=%d>",
				this.name,
				this.technologies.size(),
				countRelations());
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

	public void printHierarchyTree()
			throws FileNotFoundException, IOException
	{
		TaxonomyWriter.writeHierarchyTree("hierachyTree.txt", this);
	}

	/**
	 * Import data into the taxonomy.
	 *
	 * This is to add data created by other modules like the feature extraction,
	 * classification and ACT modules. This needs to be only once, after which
	 * those data will be stored locally to the taxonomy in a more compact format.
	 *
	 * Three kinds of data are imported.
	 *
	 * 1- A list of all terms with technology scores; this is almost always a file
	 *    named classify.MaxEnt.out.s4.scores.sum.az that was created by the
	 *    tgist-classifiers code.
	 *
	 * 2- A list of terms with scores for all three roles, created by the ACT
	 *    classifier in https://github.com/techknowledgist/act code.
	 *
	 * 3- A list with all context features of all terms, created by the code in
	 *    tgist-features (more specifically, by the extract_features.py script)
	 *
	 * @throws IOException
	 */

	public void importData()
			throws IOException
	{
		// We are doing this for cases where we re-import data, if we don't do
		// this we can get duplicate data.
		initializeData();

		String sep = File.separator;
		String termsFile = this.data + sep + INPUT_TERMS;
		String rolesFile = this.data + sep + INPUT_ROLES;
		String featuresFile = this.data + sep + INPUT_FEATURES;
		String tFile = this.location + sep + TERMS_FILE;
		String rFile = this.location + sep + ROLES_FILE;
		String fFile = this.location + sep + FEATURES_FILE;

		CheckPoint cp = new CheckPoint(true);
		TaxonomyLoader.importTerms(termsFile, this, TECHSCORE, MINCOUNT);
		TaxonomyLoader.importRoles(rolesFile, this);
		TaxonomyLoader.importFeatures(featuresFile, this);
		//cp.report("importData");
		TaxonomyWriter.writeTerms(tFile, this.technologies);
		TaxonomyWriter.writeRoles(rFile, this.roles);
		TaxonomyWriter.writeFeatures(fFile, this.features);
	}


	/**
	 * Load the features.
	 *
	 * @throws FileNotFoundException
	 */

	public void loadFeatures() throws FileNotFoundException
	{
		String vFile = this.location + File.separator + FEATURES_FILE;
		TaxonomyLoader.loadFeatures(vFile, this);
	}

	/**
	 * Apply the righthand head rule to elements in the taxonomy. As a result,
	 * instances of IsaRelation are added to technologies such that if isa(t1,t2)
	 * appears as a IsaRelation on both t1 and t1. In addition, if isa(t1,t2) then
	 * t1 is added as a hypernym to t2 and t2 is added as a hyponym to t1.
	 *
	 * @throws java.io.IOException
	 */

	public void rhhr() throws IOException
	{
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

	void addRelations() throws IOException
	{
		int relationCount = 0;
		int termRelationCount = 0;
		System.out.println();
		CooccurrenceWindow window = new CooccurrenceWindow();
		int c = 0;
		for (FeatureVector vector : this.features) {
			c++;
			//if (c > 1000) break;
			window.update(vector);
			// TODO: this only works as expected because the window only has two
			// terms in it, we will get duplication when that changes
			for (String[] p : window.cooccurrencePairs()) {
				//System.out.println("  " + p[0] + " - " + p[1]);
				Technology t1 = this.technologies.get(p[0]);
				Technology t2 = this.technologies.get(p[1]);
				relationCount++;
				// These are stored on both source and target technologies
				// (as opposed to isa relations, which go only one way)
				t1.addCooccurrenceRelation(t2);
				t2.addCooccurrenceRelation(t1);
				ArrayList<FeatureVector> vectors = window.firstTwoVectors();
				String pred = predicateFromVectorMerge(vectors);
				if (pred != null) {
					termRelationCount++;
					String doc = vectors.get(0).fileName;
					TermRelation rel = new TermRelation(doc, pred, t1, t2, vectors);
					//System.out.println(rel);
					//System.out.println("   " + rel.document);
					//System.out.print("   ");
					//rel.context.pp();
					//System.out.println();
					t1.addTermRelation(rel);
					t2.addTermRelation(rel);
				}
			}
		}
		// TODO: the first two counts make no sense (more types than tokens)
		System.out.println(String.format(
				"Occurence relations created (tokens) %,12d", relationCount));
		System.out.println(String.format(
				"Occurence relations created (types)  %,12d", countRelations()));
		System.out.println(String.format(
				"Term relations created               %,12d", termRelationCount));
		File rFile = new File(this.location + File.separator + RELATIONS_FILE);
		File trFile = new File(this.location + File.separator + TERM_RELATIONS_FILE);
		calculateMutualInformation();
		TaxonomyWriter.writeCooccurrenceRelations(rFile, this);
		TaxonomyWriter.writeTermRelations(trFile, this);
	}

	/**
	 * Collect all technologies and group them by document name. This allows us
	 * to collect relations between technologies on a document by document basis.
	 *
	 * Note. This is now depricated since we use the CooccurrenceWindow class. Using
	 * this method did not make sense for large corpora with large files.
	 *
	 * @return A Map with Strings as keys and lists of Technology instances
	 * as values.
	 */

	private	Map<String, List<Technology>> groupTechnologiesByDocument()
	{
		System.out.println("\nGrouping technologies");
		CheckPoint checkpoint = new CheckPoint();
		//checkpoint.showFootPrint("this.features", this.features);
		checkpoint.reset();
		Map<String, List<Technology>> groupedTechnologies;
		groupedTechnologies = new HashMap<>();
		for (FeatureVector vector : this.features) {
			// NOTE: a technology might occur multiple times in a document
			// TODO: should probably group the vectors and not the technologies
			groupedTechnologies
					.putIfAbsent(vector.fileName, new ArrayList<>());
			groupedTechnologies
					.get(vector.fileName)
					.add(this.technologies.get(vector.term));
		}
		return groupedTechnologies;
	}

	void filterRelations()
	{
		for (Technology technology : this.technologies.values()) {
			technology.filterRelations(); }
	}

	int countRelations()
	{
		int count = 0;
		for (Technology technology : this.technologies.values())
			count += technology.relations.size();
		return count;
	}

	void exportTables() throws IOException
	{
		TaxonomyWriter.writeTermsAsTable(this, TERMS_FILE);
		TaxonomyWriter.writeHierarchyAsTable(this, HIERARCHY_FILE);
		TaxonomyWriter.writeCoocRelationsAsTable(this, RELATIONS_FILE);
		TaxonomyWriter.writeTermRelationsAsTable(this, TERM_RELATIONS_FILE);
	}

	private void calculateMutualInformation()
	{
		int n = this.technologies.size();
		boolean debug = false;
		int c = 0;
		for (Technology t1 : this.technologies.values()) {
			c++;
			if (debug && c < 10) {
				System.out.println();
				System.out.println(t1); }
			int c2 = 0;
			for (String t2_name : t1.relations.keySet()) {
				c2++;
				Technology t2 = this.technologies.get(t2_name);
				CooccurrenceRelation rel = t1.relations.get(t2_name);
				float mi = mutualInformation(n, t1, t2, rel);
				rel.mi = mi;
				if (debug && c < 10) {
					System.out.println(
					String.format(
							"   %f %d %d %d %s",
							mi, rel.count, t1.count, t2.count, t2.name));
				}
			}
		}
	}

	private float mutualInformation(int n, Technology t1, Technology t2, CooccurrenceRelation rel)
	{
		float p_x_y = rel.count * 2 / (float) n;
		float p_x = t1.count / (float) n;
		float p_y = t2.count / (float) n;
		return (float) log(p_x_y / (p_x * p_y));
	}

	/**
	 * Return the name of the predicate if two vectors can be merged.
	 *
	 * Two vectors can merge if they occur in the same sentence and the first term
	 * vector has the same verb in its next_V field as the second has in its prev_V
	 * field. At least, that is the theory, in practice we do not have a next_V field
	 * so we fake it by looking at sentence positions.
	 *
	 * @param vectors
	 * @return the name of the predicate or null
	 */
	private String predicateFromVectorMerge(ArrayList<FeatureVector> vectors)
	{
		FeatureVector v1 = vectors.get(0);
		FeatureVector v2 = vectors.get(1);
		if (v1.potentiallyRelatedTo(v2)) {
			int pos1 = end_pos(v1.featuresIdx.get("sent_loc"));
			int pos2 = start_pos(v2.featuresIdx.get("sent_loc"));
			if ((pos2 - pos1) < 6)
				return v2.featuresIdx.get("prev_V"); }
		return null;
	}

	private int start_pos(String loc)
	{
		return Integer.parseInt(loc.split("-")[0]);
	}

	private int end_pos(String loc)
	{
		return Integer.parseInt(loc.split("-")[1]);
	}

}
