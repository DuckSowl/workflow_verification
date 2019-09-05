package course.project;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;

public class Workflow extends BPMNDiagramImpl {

	public Workflow(String label) {
		super(label);
	}
	
	public boolean isWorkflowCorrect () {
		//BPMNNode node = new BPMNNode
		
		
		return false;
	}
	
	public Activity addActivity(String label) {
		return addActivity(label, false, false, false, false, false);
	}

}
