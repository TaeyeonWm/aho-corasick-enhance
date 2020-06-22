

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ac.Emit;
import org.ac.State;
import org.ac.Trie;
import org.ac.TrieNodeVisitor;


public class TestAC {

	
	public static void performance()
	{
		String text = "The ga3 mutant of Arabidopsis is a gibberellin-responsive dwarf. We present data showing that the ga3-1 mutant is deficient in ent-kaurene oxidase activity, the first cytochrome P450-mediated step in the gibberellin biosynthetic pathway. By using a combination of conventional map-based cloning and random sequencing we identified a putative cytochrome P450 gene mapping to the same location as GA3. Relative to the progenitor line, two ga3 mutant alleles contained single base changes generating in-frame stop codons in the predicted amino acid sequence of the P450. A genomic clone spanning the P450 locus complemented the ga3-2 mutant. The deduced GA3 protein defines an additional class of cytochrome P450 enzymes. The GA3 gene was expressed in all tissues examined, RNA abundance being highest in inflorescence tissue.";
		
		String speech =
                "Turning once again, and this time more generally, to the question of invasion, I would observe that there has never been a period in all these long centuries of which we boast when an absolute guarantee against invasion, still less against serious raids, could have been given to our people. In the days of Napoleon, of which I was speaking just now, the same wind which would have carried his transports across the Channel might have driven away the blockading fleet. There was always the chance, and it is that chance which has excited and befooled the imaginations of many Continental tyrants. Many are the tales that are told. We are assured that novel methods will be adopted, and when we see the originality of malice, the ingenuity of aggression, which our enemy displays, we may certainly prepare ourselves for every kind of novel stratagem and every kind of brutal and treacherous manœuvre. I think that no idea is so outlandish that it should not be considered and viewed with a searching, but at the same time, I hope, with a steady eye. We must never forget the solid assurances of sea power and those which belong to air power if it can be locally exercised.\n" +
                "I have, myself, full confidence that if all do their duty, if nothing is neglected, and if the best arrangements are made, as they are being made, we shall prove ourselves once more able to defend our island home, to ride out the storm of war, and to outlive the menace of tyranny, if necessary for years, if necessary alone. At any rate, that is what we are going to try to do. That is the resolve of His Majesty's Government – every man of them. That is the will of Parliament and the nation. The British Empire and the French Republic, linked together in their cause and in their need, will defend to the death their native soil, aiding each other like good comrades to the utmost of their strength.\n" +
                "Even though large tracts of Europe and many old and famous States have fallen or may fall into the grip of the Gestapo and all the odious apparatus of Nazi rule, we shall not flag or fail. We shall go on to the end. We shall fight in France, we shall fight on the seas and oceans, we shall fight with growing confidence and growing strength in the air, we shall defend our island, whatever the cost may be. We shall fight on the beaches, we shall fight on the landing grounds, we shall fight in the fields and in the streets, we shall fight in the hills; we shall never surrender, and if, which I do not for a moment believe, this island or a large part of it were subjugated and starving, then our Empire beyond the seas, armed and guarded by the British Fleet, would carry on the struggle, until, in God's good time, the New World, with all its power and might, steps forth to the rescue and the liberation of the old.";
		
		
		text = text + speech;
		
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
			trie.addKeyword(t,t);
			
		}
		trie.checkBuild();
		 
		long t1 = System.currentTimeMillis();
		
		for(int i = 0 ; i < 1; ++ i)
		{
			Collection<Emit> emits = trie.parseText(text);
			
			System.out.println(emits.size());
		}
		
		
	}
	
	
	public static void testAC()
	{
		{
			
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
		    
		    
		    
//		    System.out.println(trie1.parseText(text));
//		    
//		    System.out.println(trie2.parseText(text));
		    
//		    
//		    Collection<Token> tokens = trie.tokenize(text);
//		    StringBuffer html = new StringBuffer();
//	 	    html.append("<html><body><p>");
//	 	    for (Token token : tokens) {
//	 	        if (token.isMatch()) {
//	 	            html.append("<i>");
//	 	        }
//	 	        html.append(token.getFragment());
//	 	        if (token.isMatch()) {
//	 	            html.append("</i>");
//	 	        }
//	 	    }
//	 	    html.append("</p></body></html>");
//	 	    System.out.println(html);
	 	    
		}

	    
//	    {
//	    	 String speech = "The Answer to the great Question... Of Life, " +
//	 	            "the Universe and Everything... Is... Forty-two,' said " +
//	 	            "Deep Thought, with infinite majesty and calm.";
//	 	    Trie trie = new Trie().removeOverlaps().onlyWholeWords().caseInsensitive();
//	 	    trie.addKeyword("great question");
//	 	    trie.addKeyword("forty-two");
//	 	    trie.addKeyword("deep thought");
//	 	    Collection<Token> tokens = trie.tokenize(speech);
//	 	    StringBuffer html = new StringBuffer();
//	 	    html.append("<html><body><p>");
//	 	    for (Token token : tokens) {
//	 	        if (token.isMatch()) {
//	 	            html.append("<i>");
//	 	        }
//	 	        html.append(token.getFragment());
//	 	        if (token.isMatch()) {
//	 	            html.append("</i>");
//	 	        }
//	 	    }
//	 	    html.append("</p></body></html>");
//	 	    System.out.println(html);
//	    }
	}
	
	public static void test3()
	{
		 Trie trie = new Trie();
    	  trie.caseInsensitive().removeOverlaps().onlyWholeWords();
    	  trie.addKeyword("to8to_table", "1");
    	  trie.addKeyword("to8to_apply", "2");
    	  trie.addKeyword("to8to_yuyue", "3");
    	  trie.checkBuild();
    	  Collection<Emit> emits1 = trie.parseText("to8to_table to8to_tableto8to_yuyue to8to_apply");
    	  
    	  for(Emit e : emits1)
    	  {
    		  System.out.println(e);
    		  System.out.println(e.getData());
    	  }
	}
	
	public static void main(String[] args) {
//		performance();
//		test3();
		testAC();
	}

}
