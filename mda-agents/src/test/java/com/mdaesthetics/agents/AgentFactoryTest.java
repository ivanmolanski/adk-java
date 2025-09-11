package com.mdaesthetics.agents;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentFactoryTest {

  @Test
  void createsTrendAnalyzer() {
    assertNotNull(AgentFactory.trendAnalyzer());
  }

  @Test
  void createsContentCreator() {
    assertNotNull(AgentFactory.contentCreator());
  }

  @Test
  void createsQaAgent() {
    assertNotNull(AgentFactory.qaAgent());
  }
}
