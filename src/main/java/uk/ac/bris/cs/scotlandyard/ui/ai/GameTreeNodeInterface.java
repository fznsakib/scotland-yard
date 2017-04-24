package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.List;

public interface GameTreeNodeInterface {

    GameTreeNode addChild(int child);

    void setData(int newData);

    int getData();

    GameTreeNode getChildAt(int childIndex);

    int getChildCount();

    GameTreeNode getParent();

    int getIndex(GameTreeNode nodeToFind);

    boolean getAllowsChildren();

    boolean isLeaf();

    List<GameTreeNode> children();
}
