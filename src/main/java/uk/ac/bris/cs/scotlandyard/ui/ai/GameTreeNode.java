package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.LinkedList;
import java.util.List;


public class GameTreeNode implements GameTreeNodeInterface {

    private int data;
    public GameTreeNode parent;
    private List<GameTreeNode> children;

    GameTreeNode(int data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public void setData(int newData){
        this.data = newData;
    }

    public int getData(){
        return data;
    }

    public GameTreeNode addChild(int child) {
        GameTreeNode childNode = new GameTreeNode(child);
        childNode.parent = this;
        children.add(childNode);
        return childNode;
    }

    public GameTreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public GameTreeNode getParent() {
        return parent;
    }

    public int getIndex(GameTreeNode nodeToFind) {
        for (int i = 0; i < children.size(); i ++)
        {
            if (children.get(i).equals(nodeToFind))
                return i;
        }
        return 0;
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return (children.size() == 0);
    }

    public List<GameTreeNode> children() {
        return children;
    }
}
