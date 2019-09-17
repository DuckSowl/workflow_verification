package course.project;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;

public class Workflow extends BPMNDiagramImpl {

	private BPMNNode source;
	
	public Workflow(String label) {
		super(label);
	}
	
	public Activity addActivity(String label) {
		return addActivity(label, false, false, false, false, false);
	}

	public boolean isWorkflowCorrect () {
		System.out.println("0");
		hasUniqueSource();
		System.out.println("1");
		//isAllNodesReachable();
		System.out.println("2");
		checkingGateways();
		System.out.println("3");
		return (hasUniqueSource() &&
				//isAllNodesReachable() &&
				checkingGateways());
	}
	
	private boolean hasUniqueSource() {
		findSource();
		return source != null;
	}
	
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
		Set<BPMNNode> unvisited = getNodes();
		Stack<BPMNNode> stack = new Stack<>();
		stack.push(source);
		
		while (!stack.isEmpty()) {
			BPMNNode node = stack.pop();
			for (BPMNEdge edge : getOutEdges(node)) {
				stack.push((BPMNNode)edge.getTarget());
			}
			if (unvisited.remove(node)) {
				if (unvisited.isEmpty()) {
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
					case DATABASED: 
						if (getOutEdges(node).size() < 2) {
							return false;
						} break;
					case PARALLEL: 
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
	
	private Loops loops = new Loops();
	
	public void findLoops() {
		try {
			loops.findLoops();
		} catch (Exception e) {
			System.out.println("Loops search failed!");
			e.printStackTrace();
		}
	}
	
	void debugPrintLoops() {
		System.out.println(loops.toString());
	}
	
	// ABC
	
	HashMap<BPMNNode, Integer> num = new HashMap<>();
	
	HashMap<BPMNNode, Disjunction> Cn = new HashMap<>();
	HashMap<BPMNEdge<BPMNNode, BPMNNode>, Disjunction> Cf = new HashMap<>();
	
	private Disjunction getCondition(BPMNNode node) {
		return Cn.get(node);
	}
	
	private Disjunction getCondition(BPMNEdge<BPMNNode, BPMNNode> edge) {
		return Cf.get(edge);
	}
	
	public void alg(BPMNNode node) {
		if (node == source) { 
			Cn.put(node, new Disjunction());
			
			for (BPMNEdge to : getOutEdges(node)) {
				Cf.put(to, new Disjunction());
			}	
		}
		else {
			if (node instanceof Activity || isSplitingNode(node)) {
				if (getInEdges(node).size() > 1) {
					Iterator<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> it = getInEdges(node).iterator();
					Disjunction newCn = new Disjunction(getCondition((BPMNEdge)it.next()));
					while (it.hasNext()) {
	//					BPMNNode tempBpmnNode = (BPMNNode)(((BPMNEdge)it).getSource());
	//					System.out.println("node: " + node.getLabel() + ", from: " + tempBpmnNode.getLabel());
						if (newCn.isDeadlock(getCondition((BPMNEdge)it.next()))) {
							System.out.println("Deadlock in " + node.getLabel());
							// throw new Exception("Deadlock!");
						}
					}
				}
			}
		}	
	}
	
	public boolean isSplitingNode(BPMNNode node) {
		if (node instanceof Gateway) {
			return ((Gateway) node).getGatewayType() == GatewayType.DATABASED;
		}
		return false;
	}
	
	public void debug(String s) {
		System.out.print(s);
	}
	
	public void bfsModified() {
		debug("bfsModified");
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
			
			// alg(node);
//			try {
//			
//			} catch (Exception e) {
//				System.out.println(e);
//				System.out.println("Deadlock!");
//			}
			debug(" -> " + node.getLabel());
			
			visitedNodes.add(node);
			for (BPMNEdge to : getOutEdges(node)) {
				visitedEdges.add(to);
				queue.add((BPMNNode)to.getTarget());
			}
			
			if (isSplitingNode(node)) {
				num.put(node, index++);
				
				debug("(" + (index - 1) + ")");
			}
		}
		
		debug(" done\n");
	}
		
	class Loops {
		Set<Loop> loops = new HashSet<>();
		
		public void findLoops() throws Exception {				
			if (!isWorkflowCorrect()) {
				throw new Exception("Not correct workflow!");
			}
			
			ArrayList<BPMNNode> route = new ArrayList<>();
			
			Stack<BPMNNode> next = new Stack<>();
			next.add(source);
			
			Set<BPMNNode> left = new HashSet<>();
			Set<BPMNNode> visited = new HashSet<>();
			
			while (!next.isEmpty()) {
				route.add(next.peek());
				
				boolean stepBack = true;
				System.out.println("Entered: " + route.get(route.size() - 1).getLabel());
				for (BPMNEdge edge :  getOutEdges(next.pop())) {
					BPMNNode to = (BPMNNode)edge.getTarget();
					if (visited.add(to)) {
						stepBack = false;
						next.add(to);
					} else {
						if (!left.contains(to)) {
							addLoop(route, to);
						}
					}
				}
				
				stepBack: while (true) {
					if (next.isEmpty()) {
						break;
					}
					BPMNNode last = route.get(route.size() - 1);
					for (BPMNEdge to : getOutEdges(last)) {
						if (to.getTarget() == next.peek()) {
							break stepBack;
						}
					}
					left.add(route.remove(route.size() - 1));	
				}
				
//				if (stepBack) {
//					System.out.println("Left: " + route.get(route.size() - 1).getLabel());
//					left.add(route.remove(route.size() - 1));	
//				}
			}
			
			// System.out.println("Loops found: " + loops.size());	
		}
		
//		private void removeLoops() {
//			for (Loop loop : loops) {
//				
//			}
//		}
		
		private void addLoop(ArrayList<BPMNNode> stack, BPMNNode start) {
			loops.add(new Loop(new ArrayList<BPMNNode>(stack.subList(stack.indexOf(start), stack.size()))));
		}
		
		class Loop {
			ArrayList<BPMNNode> loop = new ArrayList<>();
			
			public Loop(ArrayList<BPMNNode> loop) {
				this.loop = loop;
				
				unloop();
			}
			
			public void unloop () {
				for (BPMNEdge edge : getInEdges(getStartNode())) {
					if (loop.contains(edge.getSource())) {
						System.out.println("Remove: " +  getStartNode().getLabel() + " - " + edge.getSource());
						removeEdge(edge);
					}
				}
			}
			
			public BPMNNode getStartNode() {
				for (BPMNNode node : loop) {
					if (node instanceof Gateway && !isSplitingNode(node)) {
						for (BPMNEdge inEdge : getInEdges(node)) {
							if (!loop.contains(inEdge.getSource())) {
								return node;
							}
						}
					}
				}
				System.out.println("Loop is wrong");
				return null;
			}
			
			public String toString() {
				String string = "Loop: (";
				
				for (BPMNNode node : loop) {
					string += node.getLabel() + ", ";
				}
				
				// string += ". startNode = " + getStartNode().getLabel();
				
				return string + ")\n";
			}
		}
		
		
		public String toString() {
			String string = "Loops: {";
			
			for (Loop loop : loops) {
				string += loop;
			}
						
			return string + "}\n";
		}
			
	}

	
}
