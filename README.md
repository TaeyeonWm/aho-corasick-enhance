# aho-corasick-enhance
enhancement of  aho-corasick automa with incrementally adding or removing patterns

# usage

```
String text = "The ga3 mutant of Arabidopsis is a gibberellin-responsive dwarf. We present data showing that the ga3-1 mutant is deficient in ent-kaurene oxidase activity, the first cytochrome P450-mediated step in the gibberellin biosynthetic pathway. By using a combination of conventional map-based cloning and random sequencing we identified a putative cytochrome P450 gene mapping to the same location as GA3. Relative to the progenitor line, two ga3 mutant alleles contained single base changes generating in-frame stop codons in the predicted amino acid sequence of the P450. A genomic clone spanning the P450 locus complemented the ga3-2 mutant. The deduced GA3 protein defines an additional class of cytochrome P450 enzymes. The GA3 gene was expressed in all tissues examined, RNA abundance being highest in inflorescence tissue.";
		
String[] terms = {
    "microsome",
    "cytochrome",
    "cytochrome P450 activity", 
    "gibberellic acid biosynthesis", 
    "GA3", 
    "cytochrome P450", 
    "oxygen binding", 
    "AT5G25900.1", 
    "protein", 
    "RNA", 
    "gibberellin", 
    "Arabidopsis", 
    "ent-kaurene oxidase activity", 
    "inflorescence", 
    "tissue", 
    "generally",
    "famous",
    "Europe and many"
};


Trie trie = new Trie();

for(String t : terms)
{
	trie.addPattern(t,t);
	
}
trie.checkBuild();
 
Collection<Emit> emits = trie.match(text);

emits.stream().forEach(i->System.out.println(i));

trie.match(text, new EmitVisitor() {

	@Override
	public boolean visit(State state, Emit emit) {
		System.out.println(emit);
		return true;
	}
	
});
```
