from abc import ABC, abstractmethod
from typing import Dict, Any, List, Optional
from pydantic import BaseModel, Field
import asyncio
import logging
import time
import uuid
from datetime import datetime

logger = logging.getLogger(__name__)

class AgentState(BaseModel):
    """Agent state management with Pydantic validation"""
    session_id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    context: Dict[str, Any] = Field(default_factory=dict)
    history: List[Dict[str, Any]] = Field(default_factory=list)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    last_updated: datetime = Field(default_factory=datetime.utcnow)
    
    def update_context(self, key: str, value: Any):
        """Update context and timestamp"""
        self.context[key] = value
        self.last_updated = datetime.utcnow()
    
    def add_to_history(self, action: str, data: Dict[str, Any]):
        """Add action to history"""
        self.history.append({
            "timestamp": datetime.utcnow().isoformat(),
            "action": action,
            "data": data
        })
        self.last_updated = datetime.utcnow()

class BaseAgent(ABC):
    """Base agent class with Pydantic state management"""
    
    def __init__(self, name: str, description: str = ""):
        self.name = name
        self.description = description
        self.logger = logging.getLogger(f"agent.{name}")
        self.states: Dict[str, AgentState] = {}
    
    def get_or_create_state(self, session_id: Optional[str] = None) -> AgentState:
        """Get existing state or create new one"""
        if session_id is None:
            session_id = str(uuid.uuid4())
        
        if session_id not in self.states:
            self.states[session_id] = AgentState(session_id=session_id)
        
        return self.states[session_id]
    
    async def execute(self, request: BaseModel, session_id: Optional[str] = None) -> Dict[str, Any]:
        """Execute agent with request validation and state management"""
        start_time = time.time()
        state = self.get_or_create_state(session_id)
        
        try:
            self.logger.info(f"Executing {self.name} agent")
            state.add_to_history("execute_started", {"request_type": type(request).__name__})
            
            # Validate request
            if not isinstance(request, BaseModel):
                raise ValueError("Request must be a Pydantic model")
            
            # Execute the agent logic
            result = await self._execute_impl(request, state)
            
            # Update state with result
            state.update_context("last_result", result)
            state.add_to_history("execute_completed", {"success": True})
            
            execution_time = time.time() - start_time
            
            return {
                "success": True,
                "message": f"{self.name} executed successfully",
                "session_id": state.session_id,
                "execution_time": execution_time,
                "data": result,
                "errors": []
            }
            
        except Exception as e:
            self.logger.error(f"Error in {self.name} agent: {str(e)}")
            state.add_to_history("execute_failed", {"error": str(e)})
            
            execution_time = time.time() - start_time
            
            return {
                "success": False,
                "message": f"Error in {self.name} agent: {str(e)}",
                "session_id": state.session_id,
                "execution_time": execution_time,
                "data": None,
                "errors": [str(e)]
            }
    
    @abstractmethod
    async def _execute_impl(self, request: BaseModel, state: AgentState) -> Dict[str, Any]:
        """Implement agent-specific logic"""
        pass
    
    def get_state(self, session_id: str) -> Optional[AgentState]:
        """Get agent state by session ID"""
        return self.states.get(session_id)
    
    def clear_state(self, session_id: str):
        """Clear agent state"""
        if session_id in self.states:
            del self.states[session_id]