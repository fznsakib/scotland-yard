package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.LinkedList;
import java.util.List;


public class GameTreeNode<GameConfig> {

    private GameConfig data;
    public GameTreeNode<GameConfig> parent;
    private List<GameTreeNode<GameConfig>> children;

    GameTreeNode(GameConfig data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public void setData(GameConfig newData){
        this.data = newData;
    }

    public GameConfig getData(){
        return data;
    }

    public GameTreeNode<GameConfig> addChild(GameConfig child) {
        GameTreeNode<GameConfig> childNode = new GameTreeNode<>(child);
        childNode.parent = this;
        children.add(childNode);
        return childNode;
    }

    public GameTreeNode<GameConfig> getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public GameTreeNode<GameConfig> getParent() {
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

    public List<GameTreeNode<GameConfig>> children() {
        return children;
    }
}
