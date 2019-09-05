package course.project;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;


public class WorkflowVerification {
	@Plugin(
            name = "QWE Workflow Verification", 
            parameterLabels = { "BPMN Diagram" }, 
            returnLabels = { "BPMN Diagram" }, 
            returnTypes = { BPMNDiagram.class }, 
            userAccessible = true, 
            help = "Todo: write a description"
    )
    @UITopiaVariant(
            affiliation = "HSE", 
            author = "Anton Tolstov", 
            email = "astolstov@edu.hse.ru"
    )
	public static BPMNDiagram method(PluginContext context, BPMNDiagram bpmn) {
		Workflow newbpmn = new Workflow("new one");
		Activity a = newbpmn.addActivity("A");
		Activity b = newbpmn.addActivity("B");
		Activity c = newbpmn.addActivity("C");
		Activity d = newbpmn.addActivity("D");
		Gateway split = newbpmn.addGateway("split", GatewayType.INCLUSIVE);
		Gateway merge = newbpmn.addGateway("merge", GatewayType.PARALLEL);
		newbpmn.addFlow(a, split, "");
		newbpmn.addFlow(split, b, "");
		newbpmn.addFlow(split, c, "");
		newbpmn.addFlow(b, merge, "");
		newbpmn.addFlow(c, merge, "");
		newbpmn.addFlow(merge, d, "");
		
		return newbpmn;
}	
}