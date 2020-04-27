package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	private String removeCarrots(String word) {
		String result = "";
		for(int i = 0; i < word.length(); i++) {
			if((word.charAt(i) == '<') || (word.charAt(i) == '>')) {
				continue;
			}
			else {
				result = result + word.charAt(i);
			}
		}
		
		return result;
	}
	/**
	 * Builds the DOM tree from input HTML file, through scanner passed
	 * in to the constructor and stored in the sc field of this object. 
	 * 
	 * The root of the tree that is built is referenced by the root field of this object.
	 */
	public void build() {
		/** COMPLETE THIS METHOD **/
		if(!sc.hasNextLine()) { //checks for empty input
			return;
		}
		else {
			Stack<TagNode> tag = new Stack<TagNode>(); //used to store the tags so that if the next tag or word(s) are added in the right place
			String word = removeCarrots(sc.nextLine()); //gets the first word of html
			
			//initialize root and put into stack
			root = new TagNode(word, null, null);
			tag.push(root);
			
			while(sc.hasNextLine()) { //runs while there is more lines
				String name1 = sc.nextLine(); 
				String name2 = removeCarrots(name1);
				
				if((name1.charAt(0) == '<') && (name2.charAt(0) == '/')) { //if it is end tag, remove tag from stack and add to list of used tags
					tag.pop();
				}
				else {
					TagNode tagName = new TagNode(name2, null, null);
					TagNode current = tag.peek();
						
					if(current.firstChild == null) { //if this is the first thing inside the tag, place as child
						current.firstChild = tagName;
					}
					else { //if it is not the first thing, place as sibling of child
						TagNode temp = current.firstChild;
						
						while(current.firstChild.sibling != null) { //checks for the next empty sibling place
							current.firstChild = current.firstChild.sibling;
						}
						current.firstChild.sibling = tagName;
						
						current.firstChild = temp;
					}
				
					if(name1.charAt(0) == '<') { //if it is an open tag, put into stack
						tag.push(tagName);
					}
				}
			}
		}
	}
	
	private Stack<TagNode> runThrough(TagNode temp, Stack<TagNode> tempChildren){ //places the tree into a stack
		if(temp == null) {
			return tempChildren;
		}
		else {
			tempChildren.push(temp);
			runThrough(temp.firstChild, tempChildren);
			runThrough(temp.sibling, tempChildren);
			
			return tempChildren;
		}
	}
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		/** COMPLETE THIS METHOD **/
		Stack<TagNode> tempChildren = new Stack<TagNode>();
		Stack<TagNode> list = runThrough(root, tempChildren);
		
		while(true) {
			if(list.isEmpty() == true) { //if stack is empty, all tags have been replaced
				break;
			}
			else {
				TagNode current = list.pop();
				if(current.tag.equals(oldTag)) { //replaces old tag
					current.tag = newTag;
				}
			}
		}
	}
	
	private void boldRowRec(TagNode temp, int row) {
		if(temp == null) {
			return;
		}
		
		if(temp.tag.equals("table")) {
			TagNode currentRow = temp.firstChild;
			int tr = 1;
			
			while(tr != row) { //checks which row is being looked at else move on to next row
				if(tr > row) {
					return;
				}
				currentRow = currentRow.sibling;
				tr++;
			}
			
			currentRow = currentRow.firstChild; //moves along the tree one so that we can bold the columns not the entire row
			
			while(currentRow != null) { //bolds columns
				TagNode bold = new TagNode("b", currentRow.firstChild, null);
				currentRow.firstChild = bold;
				currentRow = currentRow.sibling;
			}
		}
		//recursion through the tree
		boldRowRec(temp.firstChild, row);
		boldRowRec(temp.sibling, row);
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		/** COMPLETE THIS METHOD **/
		boldRowRec(root, row);
	}
	
	private void removeTagRec(TagNode current, TagNode child, String tag) {
		if(current == null || child == null) {
			return;
		}
		
		if(child.tag.equals(tag)) { //makes sure the tag is equal to the tag we want to remove
			if(child.tag.equals("ol") || child.tag.equals("ul")) { //checks if the tag we want to remove is ol or ul
				
				if(child.firstChild.tag.equals("li")) { //for first instance of li replaces with p
					child.firstChild.tag = "p";
				}
				
				TagNode temp = child.firstChild.sibling;
				while(temp != null) { //for next instances of li, replace with p 
					if(temp.tag.equals("li")) {
						temp.tag = "p";
					}
					temp = temp.sibling;
				}
			}
			
			TagNode temp1 = child.firstChild; //need to set as separate TagNode so firstChild is not set incorrectly
			if(current.firstChild == child){ //checks if we are looking at a situation where they are parent and child
				current.firstChild = child.firstChild;
				
				while(temp1.sibling != null) { 
					temp1 = temp1.sibling;
				}
				
				temp1.sibling = child.sibling;
			}
			else if(current.sibling == child) { //checks if we are looking at a situation where two nodes are siblings
				while(temp1.sibling != null) {
					temp1 = temp1.sibling;
				}
				
				temp1.sibling = child.sibling;
				current.sibling = child.firstChild;
			}
			return;
		}
		//recursion to run through tree
		removeTagRec(child, child.firstChild, tag);
		removeTagRec(child, child.sibling, tag);
	}
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		/** COMPLETE THIS METHOD **/
		if(root == null) {
			return;
		}
		else {
			Stack<TagNode> tempChildren = new Stack<TagNode>();
			Stack<TagNode> list = runThrough(root, tempChildren);
			
			while(true) {
				if(list.isEmpty()) { //if stack is empty end
					break;
				}
				
				if(list.pop().tag.equals(tag)) { //run removeTagRec as many times as there are occurrences of the tag
					removeTagRec(root, root.firstChild, tag);
				}
			}
		}
		
	}
	private static boolean isValid(char puncuation) {
		if(puncuation == '!') {
			return true;
		}
		if(puncuation == ':') {
			return true;
		}
		if(puncuation == ';') {
			return true;
		}
		if(puncuation == '?') {
			return true;
		}
		if(puncuation == ',') {
			return true;
		}
		if(puncuation == '.') {
			return true;
		}
		return false;
	}
	
	private void addTagRec(TagNode temp, String word, String tag) {
		if(temp == null) {
			return;
		}
		
		if(temp.tag.toLowerCase().contains(word.toLowerCase())) { //checks if word is in node
			if(temp.tag.toLowerCase().equals(word.toLowerCase())) { //if node is just word then add tag this way
				String tempTag = temp.tag;
				temp.tag = tag;
				temp.firstChild = new TagNode(tempTag, temp.firstChild, null);
			}
			else {
				int index = temp.tag.toLowerCase().indexOf(word.toLowerCase());
				
				String before = temp.tag.substring(0, index);
				String fullWord = temp.tag.substring(index, index + word.length());
				String after = temp.tag.substring(index + word.length(), temp.tag.length());
				String puncuation = "";

				if(after.length() == 1 && isValid(after.charAt(0))) { //case where only one punctuation character after word
					fullWord = fullWord + after;
					after = "";
				}
				
				if((after.length() > 1) && (isValid(after.charAt(0))) && (!isValid(after.charAt(1)))) { //case of punctuation after word and more after
					puncuation = puncuation + after.charAt(0);
					after = after.substring(index + word.length() + 1, temp.tag.length());
				}
				
				temp.tag = before;
				TagNode firstChild = new TagNode(fullWord + puncuation, null, null);
				TagNode temp1 = temp.sibling;
				temp.sibling = new TagNode(tag, firstChild, null);
				
				if(after.length() > 0) {
					if(temp1 != null) {
						temp.sibling.sibling = new TagNode(after, null, temp1);
					}
					else {
						temp.sibling.sibling = new TagNode(after, null, null);
					}
				}
				else if(temp1 != null) {
					temp.sibling.sibling = temp1;
				}
			}
			if(temp.sibling != null) {
				addTagRec(temp.sibling.sibling, word, tag);
			}
		}
		else {
			addTagRec(temp.firstChild, word, tag);
			addTagRec(temp.sibling, word, tag);
		}
	}
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		/** COMPLETE THIS METHOD **/
		addTagRec(root, word, tag);
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
	/**
	 * Prints the DOM tree. 
	 *
	 */
	public void print() {
		print(root, 1);
	}
	
	private void print(TagNode root, int level) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			for (int i=0; i < level-1; i++) {
				System.out.print("      ");
			};
			if (root != this.root) {
				System.out.print("|----");
			} else {
				System.out.print("     ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level+1);
			}
		}
	}
}
