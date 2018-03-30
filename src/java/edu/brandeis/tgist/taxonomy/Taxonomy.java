package edu.brandeis.tgist.taxonomy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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

	public String name;
	public String location;
	public HashMap<String, Technology> technologies;
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
	 * Open an existing taxonomy and load it into memory. This includes technologies,
	 * features, the term hierarchy and the relations.
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
		this.features = new ArrayList<>();

		String tFile = this.location + File.separator + TECHNOLOGIES_FILE;
		String vFile = this.location + File.separator + FEATURES_FILE;
		String hFile = this.location + File.separator + HIERARCHY_FILE;
		String rFile = this.location + File.separator + RELATIONS_FILE;

		TaxonomyLoader.loadTechnologies(tFile, this);
		TaxonomyLoader.loadHierarchy(hFile, this);
		TaxonomyLoader.loadRelations(rFile, this);

	}

	@Override
	public String toString() {
		return String.format("<taxonomy.Taxonomy %s terms=%d features=%d>",
				this.name, this.technologies.size(), this.features.size());
	}

	public void prettyPrint() {
		System.out.println(this);
		for (int i = 0; i < 5 && i < this.features.size(); i++)
			System.out.println("   " + this.features.get(i));
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
	 * @param featuresFile
	 * @throws IOException
	 */
	public void importData(String termsFile, String featuresFile)
			throws IOException {

		File tFile = new File(this.location + File.separator + TECHNOLOGIES_FILE);
		File vFile = new File(this.location + File.separator + FEATURES_FILE);

		TaxonomyLoader.importTechnologies(termsFile, this, TECHSCORE, MINCOUNT);
		TaxonomyLoader.importFeatures(featuresFile, this);
		TaxonomyWriter.writeTechnologies(tFile, this.technologies);
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
	 * Add relations to technologies in the ontology. For now, we have the
	 * extremely simplistic approach that technologies are related if they occur
	 * in the same document (that is, the same WoS abstract).
	 *
	 * This code will be put in its own class.
	 */
	void addRelations() throws IOException {
		int relationCount = 0;
		Map<String, List<Technology>> allTechs = groupTechnologiesByDocument();
		for (String fname : allTechs.keySet()) {
			//System.out.println(fname);
			Object[] docTechs = allTechs.get(fname).toArray();
			for (int i = 0; i < docTechs.length; i++) {
				for (int j = i + 1; j < docTechs.length; j++) {
					Technology t1 = (Technology) docTechs[i];
					Technology t2 = (Technology) docTechs[j];
					if (t1.name.equals(t2.name))
						continue;
					// These are stored on both source and target technologies
					// (as opposed to isa relations, which go only one way)
					String relType = Relation.COOCCURENCE_RELATION;
					relationCount++;
					t1.addRelation(relType, t2);
					t2.addRelation(relType, t1);
				}
			}
		}
		System.out.println(String.format("Created %d occurence relations", relationCount));
		File rFile = new File(this.location + File.separator + RELATIONS_FILE);
		TaxonomyWriter.writeRelations(rFile, this);
	}

	private	Map<String, List<Technology>> groupTechnologiesByDocument() {
		//System.out.println(ObjectGraphMeasurer.measure(this.features));
		//System.exit(0);
		Map<String, List<Technology>> groupedTechnologies;
		groupedTechnologies = new HashMap<>();
		for (FeatureVector vector : this.features) {
			groupedTechnologies
					.putIfAbsent(vector.fileName, new ArrayList<>());
			groupedTechnologies
					.get(vector.fileName)
					.add(this.technologies.get(vector.term));
		}
		return groupedTechnologies;
	}

	void userLoop() {
		try (Scanner reader = new Scanner(System.in)) {
			while (true) {
				System.out.print("\nEnter a term: ");
				String term = reader.nextLine();
				if (term.equals("q"))
					break;
				// some abbreviations for debugging
				if (term.equals("ga")) term = "genetic algorithm";
				if (term.equals("aga")) term = "adaptive genetic algorithm";
				//System.out.println(String.format("[%s]", term));
				Technology tech = this.technologies.get(term);
				if (tech == null)
					System.out.println("Not in taxonomy");
				else
					printFragment(tech);
			}
		}
	}

	void printFragment(Technology tech) {
		String hyphens = "-------------------------------------------------";
		System.out.println("\n" + hyphens + "\n");
		if (tech.hypernyms.isEmpty())
			System.out.println("Top");
		else {
			for (Technology hyper : tech.hypernyms)
				System.out.println(hyper.name); }
		System.out.println("  " + Node.BLUE + Node.BOLD + tech.name + Node.END);
		int hypoCount = 0;
		for (Technology hypo : tech.hyponyms) {
			hypoCount++;
			if (hypoCount > 10) break;
			System.out.println("    " + hypo.name);
		}
		System.out.println("\n" + Node.UNDER + "Related terms:" + Node.END + "\n");
		int relCount = 0;
		for (String techName : tech.relations.keySet()) {
			relCount++;
			if (relCount > 10) break;
			Relation rel = tech.relations.get(techName);
			String relTarget = rel.target.name;
			System.out.println("    " + relTarget);
		}
		System.out.println("\n" + hyphens);
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

}
