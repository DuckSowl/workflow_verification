package course.project;

import java.util.HashMap;
import java.util.Map;
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
		//isWorkflowCorrect();
	}
	
	public boolean isWorkflowCorrect () {
		if (findSource()) {
			System.out.println("Source found: " + true);
			System.out.println("Source: " + source.getLabel());
			
			if (checkAccessibleness()) {
				System.out.println("All Nodes Aceccible: true");
				
				if (checkOutEdges()) {
					System.out.println("All Gateway Outs Correct: true");
					return true;
				}
				return false;
			} else {
				System.out.println("All Nodes Aceccible: false");
				return false;
			}
		} else {
			System.out.println("Source found: " + false);	
			return false;
		}
	}
	
	public Activity addActivity(String label) {
		return addActivity(label, false, false, false, false, false);
	}
	
	private boolean findSource () {
		BPMNNode probableSource = null;
		for (BPMNNode node : getNodes()) {
			if (getInEdges(node).isEmpty()) {
				if (probableSource == null) {
					probableSource = node;
				} else {
					return false;
				}
			}
		}
		source = probableSource;
		return true;
	}
	
	private boolean checkAccessibleness () {
		Map<BPMNNode, Boolean> visited = new HashMap<>();
		getNodes().forEach(node -> visited.put(node, false));
		Set<BPMNNode> unvisited = getNodes();
		Stack<BPMNNode> bfsStack = new Stack<>();
		bfsStack.push(source);
		
		while (!bfsStack.isEmpty()) {
			BPMNNode node = bfsStack.pop();
			for (BPMNEdge edge : getOutEdges(node)) {
				bfsStack.push((BPMNNode)edge.getTarget());
			}
			if (unvisited.remove(node)) {
				if (unvisited.isEmpty()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean checkOutEdges() {
		for (BPMNNode node : getNodes()) {
			if (node instanceof Gateway) {
				GatewayType gatewayType = ((Gateway) node).getGatewayType();
				switch (gatewayType) {
					case DATABASED: 
						if (getOutEdges(node).size() < 2) {
							return false;
						} break;
					case PARALLEL: 
						if (getOutEdges(node).size() < 1) {
							return false;
						} break;
				}
			}
		}
		return true;
	}
	
}
