package io.github.jayzhang.ac;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class State {

	private int id = 0;
	
    /** effective the size of the keyword */
    private final int depth;

    /** only used for the root state to refer to itself in case no matches have been found */
    private final State rootState;

    /**
     * referred to in the white paper as the 'goto' structure. From a state it is possible to go
     * to other states, depending on the character passed.
     */
    private Map<Character,State> success = new HashMap<Character, State>();

    /** if no matching states are found, the failure state will be returned */
    private State failure = null;

    /** whenever this state is reached, it will emit the matches keywords for future reference */
    private String emit = null;
    
    private Object data = null;
    
    private Set<State> beFailuredBys = new HashSet<State>();
    
    private Character absorb = null;
    
    public State() {
        this(0, 0);
    }

    public State(int id, int depth) {
    	this.id = id;
        this.depth = depth;
        this.rootState = depth == 0 ? this : null;
    }

    /**
     * 
     * 返回当前字符指向的next状态，如果ignoreRootState=false，当next为空时返回根节点，否则返回next节点
     */
    public State nextState(Character character, boolean ignoreRootState) {
        State nextState = this.success.get(character);
        if (!ignoreRootState && nextState == null && this.rootState != null) {
            nextState = this.rootState;
        }
        return nextState;
    }
    
    public State next(Character character)
    {
    	State next = this.success.get(character);
    	if(next != null)
    	{
    		return next;
    	}
    	
    	if(this == rootState)
    	{
    		return this;
    	}
    	return null;
    }
    

    public State nextState(Character character) {
        return nextState(character, false);
    }

    public Character getAbsorb() {
		return absorb;
	}

	public void setAbsorb(Character absorb) {
		this.absorb = absorb;
	}

	public int getId() {
		return id;
	}


    public int getDepth() {
        return this.depth;
    }

    public void setEmit(String emit)
    {
    	this.emit = emit;
    }
    
    public void setEmit(String emit, Object data)
    {
    	this.emit = emit;
    	this.data = data;
    }
    
    public String getEmit()
    {
    	return this.emit;
    }
    
    
    public Object getData() {
		return data;
	}

	public Map<Character, State> getSuccess() {
		return success;
	}


    public State failure() {
        return this.failure;
    }

    public void setFailure(State failState) 
    {
    	if(this.failure != null)
    	{
    		this.failure.beFailuredBys.remove(this);
    	}
    	
    	if(failState != null)
    	{
    		failState.beFailuredBys.add(this);
    	}
    	
    	this.failure = failState;
    }
    
    public void removeFailure()
    {
    	if(failure != null)
    	{
    		failure.beFailuredBys.remove(this);
    		failure = null;
    	}
    }
    

    public Collection<State> getStates() {
        return this.success.values();
    }

    public Collection<Character> getTransitions() {
        return this.success.keySet();
    }
    
    public void printTree()
    {
    	System.out.println(toString());
    	for(State s : getStates())
    		s.printTree();
    }
    
    public State findFailure(Character ch)
    {
    	State state = this.failure;
    	
    	if(state == null && this == rootState)
    	{
    		state = this;
    	}
    	
    	State next = state.next(ch);
    	
    	while(next == null)  //最坏情况最终会到root停止
    	{
    		state = state.failure;
    		
    		next = state.next(ch);
    	}
    	return next;
    }
    
    public Set<State> getBeFailuredBys() {
		return beFailuredBys;
	}
    

	public String toString()
    {
    	String go = "{";
    	for(Entry<Character,State> e : success.entrySet())
    		go += e.getKey() + "->" + e.getValue().getId() + ", ";
    	go += "}";
    	
    	String befailuredby = "";
    	
    	for(State s : this.beFailuredBys)
    		befailuredby += s.getId() + ",";
    	
    	String tab = "";
    	
    	for(int i = 0 ; i < depth; ++ i)
    		tab += "\t";
    	
    	String txt = tab  + "{id=" + id + ", absorb=" + absorb + ",go=" + go + ", failure=" + (failure != null? failure.getId(): "null") + ", befailuredby=[" + befailuredby + "], emit=" + emit + ", data=" + data + "}";
    	return txt;
    }

	public void visit(TrieNodeVisitor visitor) 
	{
		boolean cont = visitor.visit(this);
		
		if(!cont)
		{
			return ;
		}
		
		for(State s: success.values())
		{
			s.visit(visitor);
		}
	}

}
