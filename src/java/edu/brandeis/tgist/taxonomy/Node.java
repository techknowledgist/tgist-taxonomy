package edu.brandeis.tgist.taxonomy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Node {

	static final boolean DEBUG = false;
	static final String ANSI_RESET = "\u001B[0m";
	static final String ANSI_BLUE = "\u001B[34m";
	static final String ANSI_GREEN = "\u001B[32m";
	static final String ANSI_RED = "\u001B[31m";

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
		System.out.println(ANSI_RED + msg + ANSI_RESET);
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
		for (Node child : this.children.values()) {
			child.prettyPrint(indent + "  ");
		}
	}

}
