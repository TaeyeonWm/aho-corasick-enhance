# aho-corasick-enhance
enhancement of  aho-corasick automa with incrementally adding or removing patterns

# usage

```
String[] keys = {"he", "she", "his", "hers", "ers"};

Trie trie1 = new Trie().removeOverlaps().onlyWholeWords();

Trie trie2 = new Trie().removeOverlaps().onlyWholeWords();

for(String k : keys)
{
	System.out.println("add pattern:" + k + "==============");
	System.out.println("trie 1-------");
	trie1.addKeywordInc(k,k);
	trie1.printTrie();
	
	System.out.println("trie 2-------");
	trie2.addKeywordInc2(k,k);
	trie2.printTrie();
}

List<String> lines = new ArrayList<>();

TrieNodeVisitor visitor = new TrieNodeVisitor() {

	@Override
	public boolean visit(State state) {
		
		String from = state.getId() + "";
		
		for(State child: state.getSuccess().values())
		{
			String to = child.getId() + "";
			
			lines.add(from + "->" + to + "[label = \""+child.getAbsorb()+"\", fontsize=20 ]");
		}
		
		for(State child: state.getBeFailuredBys())
		{
			String to = child.getId() + "";
			
			lines.add(to + "->" + from + "[style=\"dashed\",color = red ]");
		}
		
		return true;
	}
};


trie2.visit(visitor);

for(String line: lines)
{
	System.out.println(line);
}


trie2.removeKeywordInc2("ers");

System.out.println("-----------------");

lines.clear();

trie2.visit(visitor);

for(String line: lines)
{
	System.out.println(line);
}

trie1.checkBuild();

trie1.printTrie();

trie2.printTrie();
```
