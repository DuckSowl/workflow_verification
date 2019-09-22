package course.project;

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
            System.out.println("Incorrect workflow.");
        } catch (InvalidLoopException e) {
            System.out.println("One of loops is invalid.");
        } catch (LackOfSynchronizationException e) {
            System.out.println("Lack of Synchronisation in " + e.getLable());
        } catch (DeadlockException e) {
            System.out.println("Deadlock in " + e.getLable());
        } catch (WorkflowException _) { }    

        return bpmn;
    }
}
