package org.ac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 *
 * Based on the Aho-Corasick white paper, Bell technologies: ftp://163.13.200.222/assistant/bearhero/prog/%A8%E4%A5%A6/ac_bm.pdf
 * @author Robert Bor
 */
public class Trie {

    private TrieConfig trieConfig;

    private State rootState;
    
    private int maxId = 0;

    private boolean failureStatesConstructed = false;

    public Trie(TrieConfig trieConfig) {
        this.trieConfig = trieConfig;
        this.rootState = new State(maxId++, 0);
    }

    public Trie() {
        this(new TrieConfig());
    }

    public Trie caseInsensitive() {
        this.trieConfig.setCaseInsensitive(true);
        return this;
    }

    public Trie removeOverlaps() {
        this.trieConfig.setAllowOverlaps(false);
        return this;
    }

    public Trie onlyWholeWords() {
        this.trieConfig.setOnlyWholeWords(true);
        return this;
    }

    /**
     * 
     * add keyword and build goto transition function at the same time
     * 
     * @param keyword
     */
    public void addKeyword(String keyword, Object data) {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        
        if (trieConfig.isCaseInsensitive()) {
        	keyword = keyword.toLowerCase();
        }
        
        State currentState = this.rootState;
        for (Character character : keyword.toCharArray()) {
            currentState = addState(currentState, character);
        }
        currentState.setEmit(keyword, data);
    }
    
    public void addKeyword(String keyword) {
    	addKeyword(keyword, null);
    }
    
    public State addState(State state, Character character)
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
    
    public void addKeywordInc(String keyword, Object data)
    {
//    	if (!this.failureStatesConstructed)
//    	{
//    		addKeyword(keyword, data);
//    		return;
//    	}
    	

        if (keyword == null || keyword.length() == 0) {
            return;
        }
        if (trieConfig.isCaseInsensitive()) {
        	keyword = keyword.toLowerCase();
        }
    	
		State currentState = this.rootState;
		State nextState = null;
		char[] chars = keyword.toCharArray();
		int pos = 0;
		for(; pos < chars.length; ++ pos)
		{
			Character character = chars[pos];
			nextState = currentState.nextState(character, true);
			if(nextState == null)
				break;
			currentState = nextState;
		}
	
		if(pos >= chars.length) // existing path, not need to add new state, just add emit
		{
			if(!keyword.equals(currentState.getEmit()))
				currentState.setEmit(keyword, data);
		}
		else 
		{
			State state = currentState;
			for(; pos < chars.length; ++ pos)
			{
				Character character = chars[pos];
				state = addState(state, character);
	        }
			state.setEmit(keyword, data);
			
			if(currentState == this.rootState)
			{
				this.constructFailureStates();
			}
			else 
			{
				Queue<State> queue = new LinkedList<State>();
				queue.add(currentState);
				queue.addAll(currentState.getBeFailuredBys());
				
				buildFailure(queue);
			}
		}
    }
    
    public void addKeywordInc2(String keyword, Object data)
    {
//    	if (!this.failureStatesConstructed)
//    	{
//    		addKeyword(keyword, data);
//    		return;
//    	}
    	

        if (keyword == null || keyword.length() == 0) {
            return;
        }
        if (trieConfig.isCaseInsensitive()) {
        	keyword = keyword.toLowerCase();
        }
    	
		State currentState = this.rootState;
		State nextState = null;
		char[] chars = keyword.toCharArray();
		int pos = 0;
		for(; pos < chars.length; ++ pos)
		{
			Character character = chars[pos];
			nextState = currentState.nextState(character, true);
			if(nextState == null)
				break;
			currentState = nextState;
		}
		
		State state = currentState;
		for(; pos < chars.length; ++ pos)
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
		
		state.setEmit(keyword, data);
    }
    
    public void addKeywordInc(String keyword)
    {
    	addKeywordInc(keyword, null);
    }
    
    
    public void removeKeywordInc(String keyword)
    {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        if (trieConfig.isCaseInsensitive()) {
        	keyword = keyword.toLowerCase();
        }
        
		State currentState = this.rootState;
		State nextState = null;
		char[] chars = keyword.toCharArray();
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
			
			pruneFailure(list);
		}
		else {} //keyword path not found, just ignore
		
    }
    
    public void removeKeywordInc2(String keyword)
    {
        if (keyword == null || keyword.length() == 0) {
            return;
        }
        if (trieConfig.isCaseInsensitive()) {
        	keyword = keyword.toLowerCase();
        }
        
		State currentState = this.rootState;
		State nextState = null;
		char[] chars = keyword.toCharArray();
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
		else {} //keyword path not found, just ignore
    }
    
    private void pruneFailure(List<State> list)
    {
    	for(int i = list.size() - 1 ; i > 0; -- i)
    	{
    		State state = list.get(i);
    		
    		State prevState = list.get(i - 1);
    		
    		if(state.getEmit() == null && state.getSuccess().isEmpty())
    		{
    			for(State by : state.getBeFailuredBys()) //transfer failure function
    				by.setFailure(state.failure());
    			
    			State stateFailure = state.failure();
    			
    			if(stateFailure != null)
    				stateFailure.getBeFailuredBys().remove(state);
    			
    			prevState.getSuccess().remove(state.getAbsorb()); // remove transition from pre to state
    			
    			state = null;
    		}
    		else 
    		{
    			break;
    		}
    	}
    }

    public Collection<Token> tokenize(String text) {

        Collection<Token> tokens = new ArrayList<Token>();

        Collection<Emit> collectedEmits = parseText(text);
        int lastCollectedPosition = -1;
        for (Emit emit : collectedEmits) {
            if (emit.getStart() - lastCollectedPosition > 1) {
                tokens.add(createFragment(emit, text, lastCollectedPosition));
            }
            tokens.add(createMatch(emit, text));
            lastCollectedPosition = emit.getEnd();
        }
        if (text.length() - lastCollectedPosition > 1) {
            tokens.add(createFragment(null, text, lastCollectedPosition));
        }

        return tokens;
    }
    
    
    public Collection<Token> tokenize(Collection<Emit> collectedEmits, String text) {
        Collection<Token> tokens = new ArrayList<Token>();
        int lastCollectedPosition = -1;
        for (Emit emit : collectedEmits) {
            if (emit.getStart() - lastCollectedPosition > 1) {
                tokens.add(createFragment(emit, text, lastCollectedPosition));
            }
            tokens.add(createMatch(emit, text));
            lastCollectedPosition = emit.getEnd();
        }
        if (text.length() - lastCollectedPosition > 1) {
            tokens.add(createFragment(null, text, lastCollectedPosition));
        }
        return tokens;
    }

    private Token createFragment(Emit emit, String text, int lastCollectedPosition) {
        return new FragmentToken(text.substring(lastCollectedPosition+1, emit == null ? text.length() : emit.getStart()));
    }

    private Token createMatch(Emit emit, String text) {
        return new MatchToken(text.substring(emit.getStart(), emit.getEnd()+1), emit);
    }

    public List<Emit> parseText(String text) {
        checkBuild();

        if (trieConfig.isCaseInsensitive()) {
            text = text.toLowerCase();
        }

        int position = 0;
        State currentState = this.rootState;
        List<Emit> collectedEmits = new ArrayList<Emit>();
        for (Character character : text.toCharArray()) {
            currentState = getState(currentState, character);
            storeEmits(position, currentState, collectedEmits, trieConfig.isAllowOverlaps());
            position++;
        }

        if (trieConfig.isOnlyWholeWords()) {
            removePartialMatches(text, collectedEmits);
        }

        if (!trieConfig.isAllowOverlaps()) {
            IntervalTree intervalTree = new IntervalTree((List<Intervalable>)(List<?>)collectedEmits);
            intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
        }

        for(Emit e : collectedEmits)
        	e.end ++;
        
        return collectedEmits;
    }
    
    private boolean isEnglish(char ch)
    {
    	return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    private void removePartialMatches(String searchText, List<Emit> collectedEmits) {
        long size = searchText.length();
        List<Emit> removeEmits = new ArrayList<Emit>();
        for (Emit emit : collectedEmits) {
            if (
            		(emit.getStart() == 0 || !isEnglish(searchText.charAt(emit.getStart() - 1))) 
            		&&
            		(emit.getEnd() + 1 == size || !isEnglish(searchText.charAt(emit.getEnd() + 1)))
            	) {
                continue;
            }
            removeEmits.add(emit);
        }
        collectedEmits.removeAll(removeEmits);
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
    
    
    private void buildFailure(Queue<State> queue)
    {
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
                targetState.removeFailure();
                targetState.setFailure(traceFailureNextState);
            }
        }
    }

    private void storeEmits(int position, State currentState, List<Emit> collectedEmits, boolean includeOverlap) {
        String emit = currentState.getEmit();
        if (emit != null) 
        	collectedEmits.add(new Emit(position-emit.length() + 1, position, emit, currentState.getData()));
        
        if(includeOverlap)
        {
        	State s = currentState.failure();
        	while(s != null)
        	{
        		emit = s.getEmit();
        		if (emit != null) 
        			collectedEmits.add(new Emit(position-emit.length() + 1, position, emit, s.getData()));
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
