package course.project;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import course.project.exceptions.DeadlockException;
import course.project.exceptions.IncorrectWorkflowException;
import course.project.exceptions.InvalidLoopException;
import course.project.exceptions.LackOfSynchronizationException;
import course.project.exceptions.WorkflowException;

public class WorkflowVerificationPlugin {
    @Plugin(
            name = "Workflow Verification",
            parameterLabels = {"BPMN Diagram"},
            returnLabels = {"BPMN Diagram"},
            returnTypes = {BPMNDiagram.class},
            userAccessible = true,
            help = "Verifies Workflow Diagram for structural conflicts such as deadlock and lack of syncronization."
    )
    @UITopiaVariant(
            affiliation = "HSE",
            author = "Anton Tolstov",
            email = "astolstov@edu.hse.ru"
    )
    public static BPMNDiagram method(PluginContext context, BPMNDiagram bpmn) {

        Workflow workflow = new Workflow(bpmn);     
    	    	  
        try {
            workflow.verify();
            workflow.addConditionsToLables();
        } catch (IncorrectWorkflowException e) {
            showMessage("Incorrect workflow.");
        } catch (InvalidLoopException e) {
            showMessage("One of loops is invalid.");
        } catch (LackOfSynchronizationException e) {
        	showMessage("Lack of Synchronisation in " + e.getLable());
        } catch (DeadlockException e) {
        	showMessage("Deadlock in " + e.getLable());
        } catch (WorkflowException _) { }    
  
        
        return bpmn;
    }
    
    private static void showMessage(String message) {        
        JOptionPane.showMessageDialog(new JFrame(), message);
    }
}
