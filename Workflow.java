package course.project;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;

import course.project.condition.Condition;
import course.project.condition.Proposition;
import course.project.exceptions.DeadlockException;
import course.project.exceptions.IncorrectWorkflowException;
import course.project.exceptions.InvalidLoopException;
import course.project.exceptions.LackOfSynchronizationException;
import course.project.exceptions.WorkflowException;
import course.project.exceptions.WorkflowVerificationException;

public class Workflow {

    /**
     * Reference to BPMNDiagram representation
     */
    private BPMNDiagram diagram;
    private BPMNNode source;

    public Workflow(BPMNDiagram diagram) {
        this.diagram = diagram;
    }

    /**
     * Returns true if BPMN represents a workflow.
     */
    public boolean isWorkflowCorrect() {
        findSource();

        return (source != null && checkNodes());
    }

    /**
     * Finds and sets one and only one workflow source.
     */
    private void findSource() {
        BPMNNode possibleSource = null;

        for (BPMNNode node : diagram.getNodes()) {
            if (diagram.getInEdges(node).isEmpty()) {
                if (possibleSource == null) {
                    possibleSource = node;
                } else {
                    return;
                }
            }
        }

        source = possibleSource;
    }

    /**
     * DFS trough all nodes, checking if node is correct and that all of them is reachable.
     */
    private boolean checkNodes() {
        Set<BPMNNode> unvisitedNodes = diagram.getNodes();
        Stack<BPMNNode> stack = new Stack<>();
        stack.push(source);

        while (!stack.isEmpty()) {
            BPMNNode node = stack.pop();

            if (!checkNode(node)) {
                return false;
            }

            unvisitedNodes.remove(node);

            for (BPMNEdge outEdge : diagram.getOutEdges(node)) {
                BPMNNode outNode = (BPMNNode) outEdge.getTarget();
                if (unvisitedNodes.contains(outNode))
                    stack.push(outNode);
            }
        }

        return unvisitedNodes.isEmpty();
    }

    /**
     * Checks edge label for "true" of "false" and returns it's boolean representation.
     * If edge label is not one of those returns null.
     */
    protected Boolean checkGetEdgeBool(BPMNEdge edge) {
        String edgeLable = edge.getLabel();

        return edgeLable.contentEquals("true") ? true :
                edgeLable.contentEquals("false") ? false : null;
    }

    /**
     * Check if node has correct type according to workflow definition.
     * And if it does checks conditions for this type of node.
     */
    private boolean checkNode(BPMNNode node) {
        if (node instanceof Activity) {
            return true;
        } else if (node instanceof Gateway) {
            GatewayType gatewayType = ((Gateway) node).getGatewayType();
            Collection<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> outEdges = diagram.getOutEdges(node);

            switch (gatewayType) {
                case DATABASED: // Splitting node
                    if (outEdges.size() >= 2) {
                        Tuple<Boolean, Boolean> hasTrueFalse = new Tuple<Boolean, Boolean>(false, false);

                        for (BPMNEdge outEdge : outEdges) {
                            Boolean curentCondition = checkGetEdgeBool(outEdge);
                            if (curentCondition == null) {
                                return false;
                            } else if (curentCondition == true) {
                                hasTrueFalse.first = true;
                            } else {
                                hasTrueFalse.second = true;
                            }
                        }
                        return hasTrueFalse.first && hasTrueFalse.second;
                    }
                    return false;
                case PARALLEL: // Merging node
                    return diagram.getOutEdges(node).size() == 1;
                default: // Workflow can have only DATABASED and PARALLEL gateways
                    return false;
            }
        } else {
            return false;
        }
    }

    // -------------------------------------------------------------------------------------------//

    private HashMap<BPMNNode, Integer> splitNodeNum;

    private HashMap<BPMNNode, Condition> nodeConditions = new HashMap<>();
    private HashMap<BPMNEdge<BPMNNode, BPMNNode>, Condition> edgeConditions = new HashMap<>();

    /**
     * Returns condition corresponding to edge
     */
    private Condition getEdgeCondition(BPMNEdge<BPMNNode, BPMNNode> edge) {
        return edgeConditions.get(edge);
    }

    /**
     * Returns number of a splitting node.
     */
    private int getNodeNum(BPMNNode node) {
        return splitNodeNum.get(node);
    }

    /**
     * Adds next number for a splitting node in splitNodeNum.
     */
    private void addNodeNum(BPMNNode node, int number) {
        splitNodeNum.put(node, number);
    }

    /**
     * Verifies workflow for presence of structural conditions.
     */
    public void verify() throws WorkflowException {
        if (!isWorkflowCorrect()) {
            throw new IncorrectWorkflowException();
        }

        flatLoops();
        modifiedBFS(node -> verifyStructuralConflicts(node));
        restoreLoops();
    }

    /**
     * Returns if edge condition is 'true'.
     */
    protected boolean getEdgeBool(BPMNEdge edge) {
        return edge.getLabel().equals("true");
    }

    /**
     * Returns true if node is a splitting gateway.
     */
    private boolean isSplitingNode(BPMNNode node) {
        return (node instanceof Gateway) &&
                ((Gateway) node).getGatewayType() == GatewayType.DATABASED;
    }

    /**
     * Calculates condition for node according to incoming edges.
     * Checks if 'node' have structural conflicts (deadlock or lack of synchronization).
     * If node does't have structural conflicts calculates conditions for out edges.
     * Otherwise throws corresponding exception with label of 'node' parameter.
     */
    private void verifyStructuralConflicts(BPMNNode node) throws WorkflowVerificationException {
        if (node == source) {
            nodeConditions.put(node, new Condition());

            for (BPMNEdge to : diagram.getOutEdges(node)) {
                edgeConditions.put(to, new Condition());
            }
        } else {
            Iterator<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> it = diagram.getInEdges(node).iterator();
            Condition nextCondition = getEdgeCondition((BPMNEdge) it.next());
            Condition newCondition = new Condition(nextCondition);

            while (it.hasNext()) {
                nextCondition = getEdgeCondition((BPMNEdge) it.next());

                if (node instanceof Activity || isSplitingNode(node)) {
                    if (newCondition.isDeadlock(nextCondition)) {
                        throw new DeadlockException(node.getLabel());
                    }
                } else {
                    if (!newCondition.isSynchronised(nextCondition)) {
                        throw new LackOfSynchronizationException(node.getLabel());
                    }
                }
                newCondition = newCondition.Union(nextCondition);
            }

            nodeConditions.put(node, newCondition);

            for (BPMNEdge to : diagram.getOutEdges(node)) {
                if (isSplitingNode(node)) {
                    edgeConditions.put(to, newCondition.Union(new Proposition(getNodeNum(node), getEdgeBool(to))));
                } else {
                    edgeConditions.put(to, newCondition);
                }
            }
        }
    }

    /**
     * Interface for applicable nodes
     */
    private interface ApplicableI {
        void apply(BPMNNode node) throws WorkflowVerificationException;
    }

    /**
     * Modified BFS algorithm is going trough graph as normal BFS,
     * but if won't go into a node till all parents were visited.
     * Gets method which determines what to do to every node as a parameter.
     */
    private void modifiedBFS(ApplicableI I) throws WorkflowVerificationException {
        HashSet<BPMNNode> visitedNodes = new HashSet<>();
        HashSet<BPMNEdge<BPMNNode, BPMNNode>> visitedEdges = new HashSet<>();

        Queue<BPMNNode> queue = new ArrayDeque<>();
        queue.add(source);

        splitNodeNum = new HashMap<>();
        int splittingNodeNum = 0;

        nextNode:
        while (!queue.isEmpty()) {
            BPMNNode node = queue.poll();

            // Go to node only if all in edges were already visited.
            for (BPMNEdge from : diagram.getInEdges(node)) {
                if (!visitedEdges.contains(from)) {
                    queue.add(node);
                    continue nextNode;
                }
            }

            visitedNodes.add(node);

            for (BPMNEdge toEdge : diagram.getOutEdges(node)) {
                visitedEdges.add(toEdge);
                BPMNNode possibleUnvisitedNode = (BPMNNode) toEdge.getTarget();
                if (!visitedNodes.contains(possibleUnvisitedNode)) {
                    queue.add(possibleUnvisitedNode);
                }
            }

            if (isSplitingNode(node)) {
                addNodeNum(node, splittingNodeNum++);
            }

            I.apply(node);
        }
    }

    //--------------------------------------------------------------------------------------------//

    /**
     * Set of all loops found in this BPMN graph.
     */
    private Set<Loop> loops = new HashSet<>();

    /**
     * Finds all loops and removes looping node from them,
     * thereby makes workflow graph acyclic.
     */
    private void flatLoops() throws InvalidLoopException {
        findLoops();

        for (Loop loop : loops) {
            loop.removeLoopingEdge();
        }
    }

    /**
     * Restores earlier removed nodes.
     */
    private void restoreLoops() {
        for (Loop loop : loops) {
            loop.restoreLoopingEdge();
        }
    }

    /**
     * DFS trough all workflow graph, finds all loops and adds it to 'loops'.
     * If one of loops is incorrect throws InvalidLoopException.
     */
    private void findLoops() throws InvalidLoopException {
        ArrayList<BPMNNode> route = new ArrayList<>();

        Stack<BPMNNode> next = new Stack<>();
        next.add(source);

        Set<BPMNNode> left = new HashSet<>();
        Set<BPMNNode> visited = new HashSet<>();

        while (!next.isEmpty()) {
            route.add(next.peek());
            System.out.println(route.get(route.size() - 1));
            for (BPMNEdge edge : diagram.getOutEdges(next.pop())) {
                BPMNNode to = (BPMNNode) edge.getTarget();
                if (visited.add(to)) {
                    next.add(to);
                } else {
                    if (!left.contains(to)) {
                        int startIndex = route.indexOf(to);
                        if (startIndex != -1) {
                            loops.add(new Loop(
                                    new ArrayList<BPMNNode>(route.subList(route.indexOf(to), route.size()
                                    ))));
                        }
                    }
                }
            }

            stepBack:
            while (!next.isEmpty()) {
                BPMNNode last = route.get(route.size() - 1);
                for (BPMNEdge to : diagram.getOutEdges(last)) {
                    if (to.getTarget() == next.peek()) {
                        break stepBack;
                    }
                }
                left.add(route.remove(route.size() - 1));
            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    
    /**
     * Represent a loop in Workflow.
     */
    private class Loop {

        private ArrayList<BPMNNode> nodes = new ArrayList<>();

        private BPMNNode startNode;
        private BPMNNode endNode;

        /**
         * Edge from it to startNode repeats loop
         */
        private BPMNNode loopingNode;
        private String loopingEdgeLabel;

        public Loop(ArrayList<BPMNNode> loop) throws InvalidLoopException {
            this.nodes = loop;

            if (!verifyLoop()) {
                throw new InvalidLoopException();
            }
        }

        /**
         * Removes looping edge thereby loop is no longer loop.
         */
        public void removeLoopingEdge() {
            for (BPMNEdge edge : diagram.getOutEdges(loopingNode)) {
                if (edge.getTarget() == startNode) {
                    loopingEdgeLabel = edge.getLabel();
                    diagram.removeEdge(edge);
                    return;
                }
            }
        }

        /**
         * Restores looping edge making loop the same as it was on creation.
         */
        public void restoreLoopingEdge() {
            diagram.addFlow(loopingNode, startNode, loopingEdgeLabel);
        }

        /**
         * Checks if 'node' has in edge going from outside of the loop.
         */
        private boolean hasOutsideInEdges(BPMNNode node) {
            for (BPMNEdge inEdge : diagram.getInEdges(node)) {
                if (!nodes.contains(inEdge.getSource())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks if 'node' has out edge going outside of the loop.
         */
        private boolean hasOutsideOutEdges(BPMNNode node) {
            for (BPMNEdge outEdge : diagram.getOutEdges(node)) {
                if (!nodes.contains(outEdge.getTarget())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks 'node' for having correct out edges.
         * If it's a start Node of the loop sets according fields.
         */
        private boolean checkMergingNode(BPMNNode node) {
            if (hasOutsideInEdges(node)) {
                if (this.startNode != null) {
                    return false;
                }

                BPMNNode loopingNode = null;
                for (BPMNEdge inEdge : diagram.getInEdges(node)) {
                    BPMNNode inNode = (BPMNNode) inEdge.getSource();
                    if (nodes.contains(inNode)) {
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

        /**
         * Checks 'node' for having correct in and out edges.
         * If it's an end Node of the loop sets according fields.
         */
        private boolean checkSplittingNode(BPMNNode node) {
            if (hasOutsideInEdges(node)) {
                return false;
            }

            if (hasOutsideOutEdges(node)) {
                if (this.endNode != null) {
                    return false;
                }

                Boolean outNodesCondition = null;

                for (BPMNEdge outEdge : diagram.getOutEdges(node)) {
                    boolean isNodeOutside = !nodes.contains(outEdge.getTarget());
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

        /**
         * Verifies loop, and if it is infinite or not reachable,
         * or has incorrect in/out edges returns false.
         * Otherwise true.
         */
        public boolean verifyLoop() {
            for (BPMNNode node : nodes) {
                if (node instanceof Gateway) {
                    Gateway gateway = (Gateway) node;

                    switch (gateway.getGatewayType()) {
                        case PARALLEL: // Merging Node
                            if (!checkMergingNode(node)) {
                                System.out.println("mergin" + node.getLabel());
                                return false;
                            }
                            break;
                        case DATABASED: // Splitting Node
                            if (!checkSplittingNode(node)) {
                                System.out.println("spliting" + node.getLabel());
                                return false;
                            }
                            break;
                        default:
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
    }

    //--------------------------------------------------------------------------------------------//

    /**
     * Changes label of the 'node' to given 'label'.
     */
    private void setLabel(AbstractGraphElement element, String label) {
        element.getAttributeMap().put(AttributeMap.LABEL, label);
    }

    /**
     * Appends 'ending' to the 'node' label.
     */
    private void appendToLable(AbstractGraphElement element, String ending) {
        setLabel(element, element.getLabel() + ending);
    }

    /**
     * Adds found condition to every label.
     * Format = 'label, Cn=condition'.
     */
    public void addConditionsToLables() {
        for (Entry<BPMNNode, Condition> entry : nodeConditions.entrySet()) {
            appendToLable(entry.getKey(), ", Cn=" + entry.getValue().toString());
        }
    }
}
