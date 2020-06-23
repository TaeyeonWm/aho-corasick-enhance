package io.github.jayzhang.ac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 *
 * Based on the Aho-Corasick white paper, Bell technologies: ftp://163.13.200.222/assistant/bearhero/prog/%A8%E4%A5%A6/ac_bm.pdf
 */
public class Trie {	
    private TrieConfig trieConfig = new TrieConfig();
    private State rootState;
    private int maxId = 0;
    private boolean failureStatesConstructed = false;

    public Trie(TrieConfig trieConfig) 
    {
        this.trieConfig = trieConfig;
        this.rootState = new State(maxId++, 0);
    }
    
    public Trie() {
    	this.rootState = new State(maxId++, 0);
    }
    
    public Trie caseInsensitive() 
    {
        this.trieConfig.setCaseInsensitive(true);
        return this;
    }

    public Trie removeOverlaps() {
        this.trieConfig.setAllowOverlaps(false);
        return this;
    }

    public Trie onlyWholeWords() 
    {
        this.trieConfig.setOnlyWholeWords(true);
        return this;
    }

    /**
     * 离线添加模式
     */
    public void addPattern(String pattern, Object data) 
    {
        if (pattern == null || pattern.length() == 0) {
            return;
        }
        
        if (trieConfig.isCaseInsensitive()) {
        	pattern = pattern.toLowerCase();
        }
        
        State currentState = this.rootState;
        for (Character character : pattern.toCharArray()) 
        {
            currentState = addState(currentState, character);
        }
        currentState.setEmit(pattern, data);
    }
    
    /**
     * 离线添加模式
     */
    public void addPattern(String keyword) {
    	addPattern(keyword, null);
    }
    
    /**
     * 在线添加模式
     */
    public void addPatternOnline(String pattern, Object data)
    {
        if (pattern == null || pattern.length() == 0) {
            return;
        }
        if (trieConfig.isCaseInsensitive()) {
        	pattern = pattern.toLowerCase();
        }
    	
		State currentState = this.rootState;
		State nextState = null;
		char[] chars = pattern.toCharArray();
		int pos = 0;
		for(; pos < chars.length; ++ pos)  ///沿着根节点查找，不创建节点
		{
			Character character = chars[pos];
			nextState = currentState.nextState(character, true);
			if(nextState == null)
				break;
			currentState = nextState;
		}
		
		State state = currentState;
		for(; pos < chars.length; ++ pos) //创建新节点
		{
			//1. 创建next状态，设置goto
			Character character = chars[pos];
			State failure = state.findFailure(character);
			State next = addState(state, character);
			
			//2. 设置当前新节点next的failure
			next.setFailure(failure); 
			
			//3. 更新别的节点的failure指向next的节点的failure值
			Set<State> reverseFailures = new HashSet<>();
			reverseFailures.addAll(state.getBeFailuredBys());
			for(State reverseFailure: reverseFailures)
			{
				State reverseFailureNext = reverseFailure.getSuccess().get(character);
				if(reverseFailureNext != null)
				{
					reverseFailureNext.setFailure(next);
				}
			}
			state = next; // state指向next，继续往下走
        }
		
		state.setEmit(pattern, data);
    }
    
    /**
     * 在线添加模式
     */
    public void addPatternOnline(String pattern)
    {
    	addPatternOnline(pattern, null);
    }
   
    
    private State addState(State state, Character character)
    {
        State nextState = state.getSuccess().get(character);
        if (nextState == null) 
        {
            nextState = new State(maxId ++, state.getDepth() + 1);
            state.getSuccess().put(character, nextState);
            nextState.setAbsorb(character);
            if(state == this.rootState)  //如果nextState是深度为1的节点（也就是根节点的直接子节点，则其failure指向根节点）
            {
            	nextState.setFailure(this.rootState);
            }
        }
        return nextState;
    }
    
   
    /**
     * 在线删除模式
     */
    public void removePatternOnline(String pattern)
    {
        if (pattern == null || pattern.length() == 0) {
            return;
        }
        if (trieConfig.isCaseInsensitive()) {
        	pattern = pattern.toLowerCase();
        }
        
		State currentState = this.rootState;
		State nextState = null;
		char[] chars = pattern.toCharArray();
		int pos = 0;
		List<State> list = new ArrayList<State>(); 
		for(; pos < chars.length; ++ pos)
		{
			list.add(currentState);
			Character character = chars[pos];
			nextState = currentState.nextState(character, true);
			if(nextState == null)
				break;
			currentState = nextState;
		}
	
		if(pos >= chars.length)//keyword path match in the goto tree
		{
			currentState.setEmit(null);
			
			if(currentState.getSuccess().size() > 0) // no need to prune the tree, just return
				return;
			
			list.add(currentState);
			
			for(int i = list.size() - 1; i > 0; -- i)
			{
				State state = list.get(i);
				State prev = list.get(i - 1);
				
				//1. 调整failure指向state的节点的failure值
				State statefailure = state.failure();
				Set<State> reverseFailures = new HashSet<>();
				reverseFailures.addAll(state.getBeFailuredBys());
				for(State failedby: reverseFailures)
				{
					failedby.setFailure(statefailure);
				}
				
				//2. 调整state本身的failure值
				state.setFailure(null);
				
				//3. 从goto链上删除当前节点
				prev.getSuccess().remove(state.getAbsorb());
				
				if(prev.getSuccess().size() > 0) //遇到prev是分叉节点，则剪枝提前结束
				{
					break;
				}
			}
		}
		else {} //patern path not found, just ignore
    }
     
   
    public List<Emit> match(String text)
    {
        checkBuild();
        if (trieConfig.isCaseInsensitive()) {
            text = text.toLowerCase();
        }
        int position = 0;
        State currentState = this.rootState;
        List<Emit> collectedEmits = new ArrayList<Emit>();
        for (Character character : text.toCharArray()) 
        {
            currentState = getState(currentState, character);
            collectEmits(position, currentState, collectedEmits, trieConfig.isAllowOverlaps());
            position++;
        }
        return collectedEmits;
    }
    
    public void match(String text, EmitVisitor visitor)
    {
    	checkBuild();
        if (trieConfig.isCaseInsensitive()) {
            text = text.toLowerCase();
        }
        int position = 0;
        State currentState = this.rootState;
        
        for (Character character : text.toCharArray()) 
        {
            currentState = getState(currentState, character);
            String emit = currentState.getEmit();
            if (emit != null) 
            {
            	Emit e = new Emit(position-emit.length() + 1, position + 1, emit, currentState.getData());
            	boolean cont = visitor.visit(currentState, e);
            	if(!cont)
            	{
            		break;
            	}
            }
            position++;
        }
    }
    
    private State getState(State currentState, Character character) {
        State newCurrentState = currentState.nextState(character);
        while (newCurrentState == null) {
            currentState = currentState.failure();
            newCurrentState = currentState.nextState(character);
        }
        return newCurrentState;
    }

    public void checkBuild() {
        if (!this.failureStatesConstructed) {
            constructFailureStates();
            this.failureStatesConstructed = true;
        }
    }

    private void constructFailureStates() {
        Queue<State> queue = new LinkedList<State>();
        
        // First, set the fail state of all depth 1 states to the root state
        for (State depthOneState : this.rootState.getStates()) {
            depthOneState.setFailure(this.rootState);
            queue.offer(depthOneState);
        }
        
        // Second, determine the fail state for all depth > 1 state
        while (!queue.isEmpty()) {
        	
            State currentState = queue.poll();

            for (Character transition : currentState.getTransitions()) 
            {	
                State targetState = currentState.nextState(transition);
                
                queue.offer(targetState);

                State traceFailureState = currentState.failure();
                
                State traceFailureNextState = traceFailureState.nextState(transition);
                
                while (traceFailureNextState == null) 
                {
                    traceFailureState = traceFailureState.failure();
                    traceFailureNextState = traceFailureState.nextState(transition);
                }
                targetState.setFailure(traceFailureNextState);
                
//                targetState.addEmit(traceFailureNextState.emit()); //is this necessary? I don't think so, because we don't need patterns contained by larger patterns
            }
        }
    }
     
    private void collectEmits(int position, State currentState, List<Emit> collectedEmits, boolean includeOverlap) 
    {
        String emit = currentState.getEmit();
        if (emit != null) 
        {
        	collectedEmits.add(new Emit(position-emit.length() + 1, position + 1, emit, currentState.getData()));
        }
        
        if(includeOverlap)
        {
        	State s = currentState.failure();
        	while(s != null)
        	{
        		emit = s.getEmit();
        		if (emit != null) 
        		{
        			collectedEmits.add(new Emit(position-emit.length() + 1, position + 1, emit, s.getData()));
        		}
        		s = s.failure();
        	}
        }
    }

    
    public void printTrie()
    {
    	this.rootState.printTree();
    }
    
    public void visit(TrieNodeVisitor visitor)
    {
    	this.rootState.visit(visitor);
    }
}
