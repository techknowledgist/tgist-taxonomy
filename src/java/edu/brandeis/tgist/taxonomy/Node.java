package edu.brandeis.tgist.taxonomy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Node {

	static final boolean DEBUG = false;
	static final String BLUE = "\u001B[34m";
	static final String GREEN = "\u001B[32m";
	static final String RED = "\u001B[31m";
	static final String BOLD = "\u001B[1m";
	static final String UNDER = "\u001B[4m";
	static final String END = "\u001B[0m";

	public String name;
	public Node parent;
	public Map<String, Node> children;
	public Technology technology;

	Node(String name) {
		this.name = name;
		this.parent = null;
		this.children = new HashMap<>();
		this.technology = null;
	}

	static void warning(String msg) {
		// TODO: this does not work for the windows command prompt
		System.out.println(RED + msg + END);
	}

	@Override
	public String toString() {
		String techName = "";
		if (this.technology != null)
			techName = " " + this.technology.name.toUpperCase();
		Object tech = this.technology == null ? "" : techName;
		return String.format("<Node name='%s'%s>", this.name, tech);
	}

	public void insert(Technology technology, String[] tokens, int idx) {

		if (DEBUG)
			System.out.println(String.format(
					"\n    <Node %s>\n    <Tech %s] %d>",
					this.name, technology.name, idx));

		// check whether the node matches the technology
		if (idx == 0) {
			if (this.name.equals(technology.name)) {
				if (DEBUG)
					System.out.println("    ==> inserting technology on the node");
				this.technology = technology;
			} else {
				warning("WARNING: term '" + technology.name + "' could not be inserted");
			}
			return;
		}

		// the term that you will match to the children of the node
		String searchTerm = String.join(
				" ", Arrays.copyOfRange(tokens, idx -1 , tokens.length));

		if (this.children.containsKey(searchTerm)) {
			if (DEBUG)
				System.out.println("    ==> recurse over existing child");
			this.children.get(searchTerm).insert(technology, tokens, idx - 1);

		} else {
			if (DEBUG)
				System.out.println("    ==> insert new child and recurse");
			Node newNode = new Node(searchTerm);
			newNode.parent = this;
			this.children.put(searchTerm, newNode);
			newNode.insert(technology, tokens, idx - 1);
		}
	}

	public void prettyPrint() {
		prettyPrint("");
	}

	private void prettyPrint(String indent) {
		System.out.println(indent + this);
		for (Node child : this.children.values())
			child.prettyPrint(indent + "  ");
	}

	void addIsaRelations(Technology hypernym) {

		//System.out.println(BOLD + this.name + END + " " + this.technology + " " + BLUE + hypernym + END);
		Technology nhypernym = hypernym;
		if (this.technology != null) {
			if (hypernym != null) {
				//System.out.println("--> ADDING " + hypernym.name + " ==> " + this.technology.name);
				IsaRelation isa = new IsaRelation(IsaRelation.RHHR, this.technology, hypernym);
				this.technology.isaRelations.add(isa);
				this.technology.hypernyms.add(hypernym);
				hypernym.hyponyms.add(this.technology);
			}
			nhypernym = this.technology;
			//System.out.println("--> Updating hypernym to " + GREEN + hypernym.name + END);
		}

		//System.out.println("--> Hypernym is now " + GREEN + hypernym + END);

		//for (int i = 0; i < 2; i++)
		//	System.out.println("--> Hypernym in for " + GREEN + hypernym + END);

		//Set<String> childNames = this.children.keySet();
		//for (String s : childNames) {
		//	System.out.println("--> Hypernym in set " + GREEN + hypernym + END + " " + s);
		//}

		for (Node child : this.children.values()) {
			//System.out.println("--> Hypernym in loop " + GREEN + hypernym + END);
			child.addIsaRelations(nhypernym);
		}
	}

}
