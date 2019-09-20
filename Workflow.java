package course.project;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;

import course.project.exceptions.InvalidLoopException;
import course.project.exceptions.LackOfSynchronizationException;
import course.project.exceptions.WorkflowVerificationException;

public class Workflow extends BPMNDiagramImpl {

	public Workflow(String label) {
		super(label);
	}

	private BPMNNode source;
	
	public Activity addActivity(String label) {
		return addActivity(label, false, false, false, false, false);
	}

	public boolean isWorkflowCorrect () {
		return (isSourceUnique() &&
				// isAllNodesReachable() &&
				checkingGateways());
	}
	
	/** */
	private boolean isSourceUnique() {
		findSource();
		return source != null;
	}
	
	/** Finds and sets one and only one workflow source */
	private void findSource() {
		BPMNNode possibleSource = null;
		
		for (BPMNNode node : getNodes()) {
			if (getInEdges(node).isEmpty()) {
				if (possibleSource == null) {
					possibleSource = node;
				} else {
					return;
				}
			}
		}
		
		source = possibleSource;
	}
	
	
	private boolean isAllNodesReachable() {
		Set<BPMNNode> unvisitedNodes = getNodes();
		Stack<BPMNNode> stack = new Stack<>();
		stack.push(source);
		
		while (!stack.isEmpty()) {
			BPMNNode node = stack.pop();
			for (BPMNEdge edge : getOutEdges(node)) {
				stack.push((BPMNNode)edge.getTarget());
			}
			if (unvisitedNodes.remove(node)) {
				if (unvisitedNodes.isEmpty()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean checkingGateways() {
		for (BPMNNode node : getNodes()) {
			if (node instanceof Gateway) {
				switch (((Gateway) node).getGatewayType()) {
					case DATABASED: // Splitting node
						if (getOutEdges(node).size() < 2) {
							return false;
						} break;
					case PARALLEL: // Merging node
						if (getOutEdges(node).size() < 1) {
							return false;
						} break;
					default: // Workflow can have only DATABASED and PARALLEL gateways
						return false;
				}
			}
		}
		return true;
	}
	
	void debugPrintLoops() {
		System.out.println(loops.size());
	}
	
	// ABC
	
	HashMap<BPMNNode, Integer> splitNodeNum = new HashMap<>();
	
	HashMap<BPMNNode, Disjunction> Cn = new HashMap<>();
	HashMap<BPMNEdge<BPMNNode, BPMNNode>, Disjunction> Cf = new HashMap<>();
	
	private Disjunction getCondition(BPMNNode node) {
		return Cn.get(node);
	}
	
	private Disjunction getCondition(BPMNEdge<BPMNNode, BPMNNode> edge) {
		return Cf.get(edge);
	}
	
	private void setLabel(AbstractGraphElement element, String label) {
		element.getAttributeMap().put(AttributeMap.LABEL, label);
	}
	
	private void appendToLable(AbstractGraphElement element, String ending) {
		setLabel(element, element.getLabel() + ending);
	}
	
	public void addConditionsToLables() {
		for (Entry<BPMNNode, Disjunction> entry : Cn.entrySet()) {
			appendToLable(entry.getKey(), ", Cn=" + entry.getValue().toString());
		}
		
		// for (Entry<BPMNEdge<BPMNNode, BPMNNode>, Disjunction> entry : Cf.entrySet()) {
		//     appendToLable(entry.getKey(), ", Cf=" + entry.getValue().toString());
		// }
	}
	
	public void verify() throws WorkflowVerificationException {
		flatLoops();
		debugPrintLoops();
		bfsModified();
		restoreLoops();
	}
	
	/** Calculates condition for node according to incoming edges.
	 *  Checks if 'node' have structural conflicts (deadlock or lack of synchronization
	 *  If node does't have structural conflicts calculates conditions for out edges. */
	private void verifyStructuralConflicts(BPMNNode node) {
		if (node == source) { 
			Cn.put(node, new Disjunction());
			
			for (BPMNEdge to : getOutEdges(node)) {
				Cf.put(to, new Disjunction());
			}	
		} else {			
			Iterator<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> it = getInEdges(node).iterator();
			Disjunction nextCondition = getCondition((BPMNEdge)it.next());
			Disjunction newCondition = new Disjunction(nextCondition);
			
			while (it.hasNext()) {
				nextCondition = getCondition((BPMNEdge)it.next());
				if (node instanceof Activity || isSplitingNode(node)) {
					if (newCondition.isDeadlock(nextCondition)) {
						System.out.println("Deadlock in " + node.getLabel());
					}
				} else {
					if (!newCondition.isSynchronised(nextCondition)) {
						System.out.println("Not Sync in " + node.getLabel());
					}
				}
				newCondition = newCondition.Union(nextCondition);
			}
			
			Cn.put(node, newCondition);			
			
			for (BPMNEdge to : getOutEdges(node)) {
				if (isSplitingNode(node)) {
					Cf.put(to, newCondition.Union(new Proposition(getNodeNum(node), getEdgeBool(to))));
				} else {
					Cf.put(to, newCondition);
				}
			}
		}
	}
	
	/** Returns number of a splitting node */
	private int getNodeNum(BPMNNode node) {
		return splitNodeNum.get(node);
	}
	
	/** Adds next number for a splitting node in splitNodeNum */
	private void addNodeNum(BPMNNode node, int number) {
		splitNodeNum.put(node, number);
	}
	
	/** Returns splitting edge condition in boolean */
	protected boolean getEdgeBool(BPMNEdge edge) {
		return edge.getLabel() == "true";
	}
	
	/** Returns true if node is a splitting gateway */
	private boolean isSplitingNode(BPMNNode node) {
		return (node instanceof Gateway) && 
				((Gateway) node).getGatewayType() == GatewayType.DATABASED;
	}
	
	private void bfsModified() throws LackOfSynchronizationException {
		HashSet<BPMNNode> visitedNodes = new HashSet<>();
		HashSet<BPMNEdge<BPMNNode, BPMNNode>> visitedEdges = new HashSet<>();

		Queue<BPMNNode> queue = new ArrayDeque<>();
		queue.add(source);
		
		int index = 0;
		
		nextNode: while (!queue.isEmpty()) {
			BPMNNode node;
			
			do {
				node = queue.poll();
			} while (visitedNodes.contains(node));
			
			for (BPMNEdge from : getInEdges(node)) {
				if (!visitedEdges.contains(from)) {
					queue.add(node);
					continue nextNode;
				}
			}
			
			
			
			visitedNodes.add(node);
			for (BPMNEdge to : getOutEdges(node)) {
				visitedEdges.add(to);
				queue.add((BPMNNode)to.getTarget());
			}
			
			if (isSplitingNode(node)) {
				addNodeNum(node, index++);
			}
			
			verifyStructuralConflicts(node);
		}
		
		System.out.println(" done\n");
	}
	
	private Set<Loop> loops = new HashSet<>();
		
	private void flatLoops() throws InvalidLoopException {
		findLoops();
		
		for (Loop loop : loops) {
			loop.removeLoopingEdge();
		}
	}
	
	private void restoreLoops() {		
		for (Loop loop : loops) {
			loop.restoreLoopingEdge();
		}
	}	
		
	private void findLoops() throws InvalidLoopException {				
		ArrayList<BPMNNode> route = new ArrayList<>();
		
		Stack<BPMNNode> next = new Stack<>();
		next.add(source);
		
		Set<BPMNNode> left = new HashSet<>();
		Set<BPMNNode> visited = new HashSet<>();
		
		while (!next.isEmpty()) {
			route.add(next.peek());
			
			boolean stepBack = true;
			// System.out.println("Entered: " + route.get(route.size() - 1).getLabel());
			for (BPMNEdge edge :  getOutEdges(next.pop())) {
				BPMNNode to = (BPMNNode)edge.getTarget();
				if (visited.add(to)) {
					stepBack = false;
					next.add(to);
				} else {
					if (!left.contains(to)) {
						loops.add(new Loop(
								  new ArrayList<BPMNNode>(route.subList(route.indexOf(to), route.size()
										  ))));
					}
				}
			}
			
			stepBack: while (!next.isEmpty()) {
				BPMNNode last = route.get(route.size() - 1);
				for (BPMNEdge to : getOutEdges(last)) {
					if (to.getTarget() == next.peek()) {
						break stepBack;
					}
				}
				left.add(route.remove(route.size() - 1));	
			}
		}
	}	
	
	private class Loop {
		ArrayList<BPMNNode> loop = new ArrayList<>();
		private BPMNNode startNode;
		private BPMNNode endNode;
		private BPMNNode loopingNode; // Edge from it to startNode repeats loop
		private String loopingEdgeLabel;
		
		public Loop(ArrayList<BPMNNode> loop) throws InvalidLoopException {
			this.loop = loop;
			
			if (!verifyLoop()) {
				throw new InvalidLoopException();
			}
		}
		
		public void removeLoopingEdge () {
			for (BPMNEdge edge : getOutEdges(loopingNode)) {
				if (edge.getTarget() == startNode) {
					loopingEdgeLabel = edge.getLabel();
					removeEdge(edge);
					return;
				}
			}
		}
		
		public void restoreLoopingEdge () {
			addFlow(loopingNode, startNode, loopingEdgeLabel);
		}
		
		public boolean verifyLoop() {
			for (BPMNNode node : loop) {
				if (node instanceof Gateway) {
					Gateway gateway = (Gateway)node;
					
					switch (gateway.getGatewayType()) {
						case PARALLEL: // Merging Node
							if (!checkMergingNode(node)) {
								return false;
							}
							break;
						case DATABASED: // Splitting Node
							if (!checkSplittingNode(node)) {
								return false;
							}
							break;
					}
				} else {
					if (hasOutsideInEdges(node) || hasOutsideOutEdges(node)) {
						return false;
					}
				}
			}
			
			if (startNode == null || endNode == null) {
				return false;
			}
			
			return true;
		}
		
		private boolean hasOutsideInEdges(BPMNNode node) {
			for (BPMNEdge inEdge : getInEdges(node)) {
				if (!loop.contains(inEdge.getSource())) {
					return true;
				}
			}
			return false;
		}
		
		private boolean hasOutsideOutEdges(BPMNNode node) {
			for (BPMNEdge outEdge : getOutEdges(node)) {
				if (!loop.contains(outEdge.getTarget())) {
					return true;
				}
			}
			return false;
		}
		
		public boolean checkMergingNode(BPMNNode node) {
			if (hasOutsideInEdges(node)) {
				if (this.startNode != null) {
					return false;
				}
				
				BPMNNode loopingNode = null;
				for (BPMNEdge inEdge : getInEdges(node)) {
					BPMNNode inNode = (BPMNNode)inEdge.getSource();
					if (loop.contains(inNode)) {
						if (loopingNode == null) {
							loopingNode = inNode;
						} else {
							return false;
						}
					}
				}
				
				if (loopingNode == null) {
					return false;
				}
				
				this.startNode = node;
				this.loopingNode = loopingNode;
			}
			
			return true;
		}
		
		public boolean checkSplittingNode(BPMNNode node) {
			if (hasOutsideInEdges(node)) {
				return false;
			}
			
			if (hasOutsideOutEdges(node)) {
				if (this.endNode != null) {
					return false;
				}
				
				Boolean outNodesCondition = null;
				
				for (BPMNEdge outEdge : getOutEdges(node)) {
					boolean isNodeOutside = !loop.contains(outEdge.getTarget());
					boolean currentOutNodeContision = isNodeOutside ? getEdgeBool(outEdge) : !getEdgeBool(outEdge);
					if (outNodesCondition == null) {
						outNodesCondition = currentOutNodeContision;
					} else {
						if (outNodesCondition != currentOutNodeContision) {
							return false;
						}
					}
				}
				
				this.endNode = node;
			}

			return true;
		}
		
//		public String toString() {
//			String string = "Loop: (";
//			
//			for (BPMNNode node : loop) {
//				string += node.getLabel() + ", ";
//			}
//			
//			// string += ". startNode = " + getStartNode().getLabel();
//			
//			return string + ")\n";
//		}
	}
	
	
//	public String toString() {
//		String string = "Loops: {";
//		
//		for (Loop loop : loops) {
//			string += loop;
//		}
//					
//		return string + "}\n";
//	}
}
