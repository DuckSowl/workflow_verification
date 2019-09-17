package course.project;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;


//public class WorkflowVerification {
//	@Plugin(
//            name = "QWE Workflow Verification", 
//            parameterLabels = { "BPMN Diagram" }, 
//            returnLabels = { "BPMN Diagram" }, 
//            returnTypes = { BPMNDiagram.class }, 
//            userAccessible = true, 
//            help = "Todo: write a description"
//    )
//    @UITopiaVariant(
//            affiliation = "HSE", 
//            author = "Anton Tolstov", 
//            email = "astolstov@edu.hse.ru"
//    )
//	public static BPMNDiagram method(PluginContext context , BPMNDiagram bpmn ) {
//		Workflow newbpmn = new Workflow("new one");
//		Activity a = newbpmn.addActivity("A");
//		Activity b = newbpmn.addActivity("B", false,false, false,true, false);
//		Activity c = newbpmn.addActivity("C");
//		Activity d = newbpmn.addActivity("D");
//		Gateway split = newbpmn.addGateway("split", GatewayType.DATABASED);
//		Gateway merge = newbpmn.addGateway("merge", GatewayType.PARALLEL);
//		newbpmn.addFlow(a, split, "&time==3");
//		newbpmn.addFlow(split, b, "time!=3");
//		newbpmn.addFlow(split, c, "");
//		newbpmn.addFlow(b, merge, "");
//		newbpmn.addFlow(c, merge, "");
//		newbpmn.addFlow(merge, d, "");
//		
//		// Wrong bpmn for tests
////		Activity n = newbpmn.addActivity("N");
////		newbpmn.addFlow(n, n, "");
//		// --------------------
//		
//		newbpmn.isWorkflowCorrect();
//		AbstractDirectedGraph graph = newbpmn.getGraph();
//		
//		System.out.print("<----");
//
//		
//		//for (graph.getNodes()
//		
//		
//		return bpmn;
//	}	
//}

public class WorkflowVerification {
	@Plugin(
            name = "QWE Workflow Verification", 
            parameterLabels = {/* "BPMN Diagram"*/ }, 
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
	public static BPMNDiagram method(PluginContext context /*, BPMNDiagram bpmn */) {
		
		System.out.print("\n-----------\n");
		
		Workflow newbpmn = testbpmn4();
		//newbpmn.isWorkflowCorrect();
		
		System.out.print("\n-----------\n");
		
//		for (BPMNNode node : newbpmn.findLoopNodes()) {
//			System.out.println(node.getLabel());
//		}
		
		newbpmn.findLoops();
		newbpmn.debugPrintLoops();
		//newbpmn.bfsModified();
		
		return newbpmn;
}	
	
	public static Workflow testbpmn2() {
		Workflow bpmn = new Workflow("new one");
		Activity ts = bpmn.addActivity("ts");
		Activity a = bpmn.addActivity("A");
		Activity c = bpmn.addActivity("C");
		Activity e = bpmn.addActivity("E");
		Activity f = bpmn.addActivity("F");
		Activity i = bpmn.addActivity("I");
		
		Gateway b = bpmn.addGateway("B", GatewayType.DATABASED);
		Gateway d = bpmn.addGateway("D", GatewayType.DATABASED);
		Gateway g = bpmn.addGateway("G", GatewayType.PARALLEL);
		Gateway h = bpmn.addGateway("H", GatewayType.PARALLEL);
		
		bpmn.addFlow(ts, a, "");
		bpmn.addFlow(ts, b, "");
		bpmn.addFlow(ts, c, "");
		bpmn.addFlow(a, h, "");
		bpmn.addFlow(b, d, "false");
		bpmn.addFlow(b, g, "true");
		bpmn.addFlow(b, e, "true");

		bpmn.addFlow(d, f, "false");
		bpmn.addFlow(d, g, "true");

	    bpmn.addFlow(c, e, "");
		
		bpmn.addFlow(g, h, "");
		bpmn.addFlow(h, i, "");
		
		return bpmn;
	}
	
	public static Workflow testbpmn4() {
		Workflow bpmn = new Workflow("new one");
		Activity ts = bpmn.addActivity("ts");
		Activity b = bpmn.addActivity("B");
		Activity d = bpmn.addActivity("D");
		Activity e = bpmn.addActivity("E");

		Gateway a = bpmn.addGateway("A", GatewayType.PARALLEL);
		Gateway c = bpmn.addGateway("C", GatewayType.DATABASED);
		
		bpmn.addFlow(ts, a, "");
		bpmn.addFlow(a, b, "");
		bpmn.addFlow(b, c, "");
		bpmn.addFlow(c, d, "true");
		bpmn.addFlow(c, e, "false");
		bpmn.addFlow(e, a, "");
				
		return bpmn;
	}
	
	public static Workflow testbpmn3() {
		Workflow bpmn = new Workflow("new one");
		Activity ts = bpmn.addActivity("ts");
		
		Activity b = bpmn.addActivity("B");
		Activity c = bpmn.addActivity("C");
		Activity e = bpmn.addActivity("E");
		
		Gateway a = bpmn.addGateway("A", GatewayType.PARALLEL);
		Gateway d = bpmn.addGateway("D", GatewayType.DATABASED);
		
		bpmn.addFlow(ts, a, "");
		bpmn.addFlow(a, b, "");
		bpmn.addFlow(b, d, "");
		bpmn.addFlow(d, c, "false");
		bpmn.addFlow(c, a, "");
		bpmn.addFlow(d, e, "true");
		bpmn.addFlow(c, b, ""); ////

		return bpmn;
	}
	
	public static Workflow testbpmn1() {
		Workflow newbpmn = new Workflow("new one");
		Activity a = newbpmn.addActivity("A");
		Activity b = newbpmn.addActivity("B");
		Activity c = newbpmn.addActivity("C");
		Activity d = newbpmn.addActivity("D");
		Gateway split = newbpmn.addGateway("split", GatewayType.DATABASED);
		Gateway merge = newbpmn.addGateway("merge", GatewayType.PARALLEL);
		newbpmn.addFlow(a, split, "");
		newbpmn.addFlow(split, b, "true");
		newbpmn.addFlow(split, c, "false");
		newbpmn.addFlow(b, merge, "");
		newbpmn.addFlow(c, merge, "");
		newbpmn.addFlow(merge, d, "");
		return newbpmn;
		// Wrong bpmn for tests
//		Activity n = newbpmn.addActivity("N");
//		newbpmn.addFlow(n, n, "");
		// --------------------
		
	}
}

