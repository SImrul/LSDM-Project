package imrul.dgb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeElement implements IndexChangeListener{
	private NodeType nodeType;
	private ArrayList<NodeElement> childern;
	private NodeElement parent;
	private String deweyIndex; // e.g 1.1, 1.2.1
	/* Predicate should be associated with Object Node (NOT Subject) */
	private String predicate; // e.g predicate = NULL means this node is a child of ROOT node.
	
	public NodeElement(NodeElement parent, NodeType type, String dIndex) {
		this.parent = parent;
		this.nodeType = type;
		this.deweyIndex = dIndex;
		this.childern = new ArrayList<NodeElement>(1);
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public NodeElement getParent() {
		return parent;
	}

	public void setParent(NodeElement parent) {
		this.parent = parent;
	}

	public String getDeweyIndex() {
		return deweyIndex;
	}

	public void setDeweyIndex(String deweyIndex) {
		this.deweyIndex = deweyIndex;
	}
	
	public void addChild(NodeElement child) {
		childern.add(child);
	}

	//TODO: Test this function
	public void removeChild(NodeElement child) {
		// e.g child deweyIndex = 1.2.1.3, substring will return 3. index = 3 - 1
		int index = Integer.parseInt(child.deweyIndex.substring(child.deweyIndex.lastIndexOf(".")+1)) - 1;
		// If there are following children, update their index. 
		if(index < this.childern.size() - 1) {
			for(int i = index+1; i < this.childern.size(); i++) {
				NodeElement cn = this.childern.get(i);
				cn.deweyIndex = this.deweyIndex + "." + i; // dIndex value  = actual array pos + 1
				cn.update();
			}
		}
		childern.remove(index);
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setChildern(ArrayList<NodeElement> childern) {
		this.childern = childern;
	}

	public ArrayList<NodeElement> getChildern() {
		return childern;
	}
	
	@Override
	public void update() {
		String oldParentPrefix = this.deweyIndex.substring(0, this.deweyIndex.lastIndexOf("."));
		if(this.parent != null && !oldParentPrefix.equals(this.parent.deweyIndex)) {
			this.deweyIndex = this.parent.deweyIndex + "." + this.deweyIndex.substring(this.deweyIndex.lastIndexOf(".")+1);
		}
//		// Root node doesn't have parent, so only update it's children.
//		if(this.parent != null) { 
//			// Context should be the parent node for this node.
//			int position = Integer.parseInt(this.deweyIndex.substring(this.deweyIndex.lastIndexOf(".") + 1));
//			// Check the actual position in parent list
//			if(context.getChildern().get(position) != this) {
//				position = context.getChildern().indexOf(this);
//			}
//			this.deweyIndex = context.deweyIndex + "." + position;
//		}
		// Notify the update event to it's children.
		for(NodeElement child: this.childern) {
			child.update();
		}
	}
}
