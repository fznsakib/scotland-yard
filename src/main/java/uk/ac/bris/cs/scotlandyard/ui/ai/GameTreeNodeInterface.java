package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.List;

public interface GameTreeNodeInterface<T> {

    //void addChild(T child);

    //void setData(T newData);

    //T getData();

    GameTreeNode<T> getChildAt(int childIndex);

    int getChildCount();

    GameTreeNode<T> getParent();

    int getIndex(GameTreeNode<T> nodeToFind);

    boolean getAllowsChildren();

    boolean isLeaf();

    List<GameTreeNode<T>> children();
}
