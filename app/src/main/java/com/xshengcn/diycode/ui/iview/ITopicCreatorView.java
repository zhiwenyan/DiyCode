package com.xshengcn.diycode.ui.iview;

import com.xshengcn.diycode.entity.topic.TopicNode;
import com.xshengcn.diycode.entity.topic.TopicNodeCategory;
import java.util.List;
import java.util.Map;

public interface ITopicCreatorView {
  void showNodes(Map<TopicNodeCategory, List<TopicNode>> topicNodeCategoryListMap);

  int getNodeId();

  String getTopicTitle();

  String getTopicBody();

  void showProgressDialog();

  void hideProgressDialog();
}
