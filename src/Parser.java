import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.sound.midi.MidiDevice.Info;

class StopWords {
	
	// *****************
	// Try using HashSet..... May reduce time complexity
	// *****************
	static HashSet<String> words = new HashSet<String>();
	boolean filled = false;
	
	//**************
	// Read this from file
	//**************
	public void FillStopWords() throws Exception
	{

			BufferedReader br = null;
			try 
			{
				StringBuilder sbr = new StringBuilder(StopWords.class.getProtectionDomain().getCodeSource().getLocation().getPath());
				sbr.append("/");
				sbr.append("stop_words.txt");
	
			    br = new BufferedReader(new FileReader(sbr.toString()));

			//System.out.println("Read stop words from " + sbr.toString());
		    
		        StringBuilder sb = new StringBuilder();
		        String line = br.readLine();

		        while (line != null) {
		            sb.append(line);
		            sb.append(System.lineSeparator());
		            line = br.readLine();
		        }
		        
		        for(String s : sb.toString().split(","))
		        {
		        	words.add(s);
		        	//System.out.println(s);
		        }
		        
			}
			catch(Exception e)
			{
				//System.out.println("Couldn't open file");

				if(br != null)
					br.close();
			}
		
		filled = true;
	}
	
	public HashSet<String> getWords()
	{
		if(!filled)
		{
			try
			{
				FillStopWords();
			}
			catch(Exception e)
			{
				// Ignore
			}
		}
		return words;
	}
	
}

enum eCategory
{
	Title(1),
	Body(2),
	Infobox(4),
	Category(8),
	Link(16),
	Reference(32),
	None(64);
	
	int value;
	
	private eCategory(int value)
	{
		this.value = value;
	}
}


class DocTermInfo
{
	int count;
	EnumSet<eCategory> flags;
	
	public DocTermInfo() {
		count = 0;
		flags = EnumSet.noneOf(eCategory.class);
	}
	
	public DocTermInfo(int count, eCategory flag)
	{
		this.count = count;
		this.flags = EnumSet.noneOf(eCategory.class);
		this.flags.add(flag);
	}
}

public class Parser {
	
	private static HashSet<String> stopWords;
	private static TreeMap<String, HashMap<Integer, DocTermInfo>> words;
	
	private Stemmer stemmer;
	
	public Parser()
	{
			stopWords = new StopWords().getWords();
			words = new TreeMap<String, HashMap<Integer, DocTermInfo>>();
			stemmer = new Stemmer();
	}
	
	public void ClearMap()
	{
		words.clear();
	}
	
	public TreeMap<String, HashMap<Integer, DocTermInfo>> GetWords()
	{
		return words;
	}
	
	public String ExtractInfobox(String text)
	{
			//Pattern pattern = Pattern.compile("(.*)"); Not matching new lines
			Pattern pattern = Pattern.compile("\\{\\{Infobox([^{]*)(\\{\\{[^{]*}})*", Pattern.DOTALL);
		
            Matcher matcher = pattern.matcher(text);

            int times = 0;
            String result = null;
            
            boolean found = false;
            while (matcher.find()) {
                result = matcher.group();
                found = true;
                times++;
            }
            if(!found){
                //System.out.println("No match found");
            }
            //System.out.println("Bolcks = " + times);
            return result;
	}
	
	public void GetWordCount(String text, int docId, eCategory flag)
	{
		// For each word
		Pattern wordPattern = Pattern.compile("[A-Za-z]+");
		Matcher wordMatcher = wordPattern.matcher(text);
		
		Stemmer stemmer = new Stemmer();

		while(wordMatcher.find())
		{
			String word = wordMatcher.group();

			// Check if it is a stop word
			if(stopWords.contains(word))
				// This is a stop word.
			{
				//System.out.println(word + "is in hash map");
				continue;
			}

			String stemmedWord = stemmer.StemWord(word);
			
			/*
			int count = 0;
			
			if(words.containsKey(stemmedWord))
			{
				count = words.get(stemmedWord);
			}
			words.put(stemmedWord, ++count);
			*/
			
			// Store doc ID..
			
			HashMap<Integer, DocTermInfo> docs =  words.get(stemmedWord);
			
			if(docs == null)
				docs = new HashMap<Integer, DocTermInfo>();
			
			// Is it reference type or value type
			DocTermInfo dti = docs.get(docId);
			
			if(dti != null)
			{
				dti.flags.add(flag);
				dti.count++;
			}
			else
			{
				dti = new DocTermInfo(1, flag);
				docs.put(docId, dti);
			}
			
			words.put(stemmedWord, docs);
		}
	}
	
	public void ExtractWordsAfterEqualTo(String data, int docId)
	{
		try
		{
			words.clear();
			
			String[] lines = data.split("\\n");
			///System.out.println("List of words");
			
			/*
			for(String key: stopWords.keySet())
			{
				System.out.println("Key : " + key);
			}
			*/
			
			// For the right part
			Pattern rightSidePattern = Pattern.compile("[^=]*=(.*)");
	
			for(String line:lines)
			{
				Matcher matcher = rightSidePattern.matcher(line);
				if(matcher.find())
				{
					//System.out.println(matcher.group(1));		
					GetWordCount(matcher.group(1), docId, eCategory.Body);
					
				}
			}
		}
		catch(Exception e)
		{
			//System.out.println(e.getMessage());
		}
	}
	
	public String ExtractAferInfoBox(String text)
	{
			//Pattern pattern = Pattern.compile("(.*)"); Not matching new lines
			Pattern pattern = Pattern.compile("\\{\\{Infobox([^{]*)(\\{\\{[^{]*}})*(.*)", Pattern.DOTALL);
		
	        Matcher matcher = pattern.matcher(text);
	
	        int times = 0;
	        String result = null;
	        
	        boolean found = false;
	        while (matcher.find()) {
	            result = matcher.group(3);
	            found = true;
	            times++;
	        }
	        
	        if(!found){
	            //System.out.println("No match found");
	        }
	        //System.out.println("Bolcks = " + times);
	        //System.out.println("Text after Info box is : " + result);
	        return result;
	}
	
	public void AddWord(String word, int docId, eCategory flag)
	{
		try
		{
		
			//System.out.println("Addword");
			// Check if it is a stop word
			if(stopWords.contains(word))
			{
				//System.out.println(word+" is a stop word");
				// This is a stop word.
				return;
			}
		
			String stemmedWord = stemmer.StemWord(new String(word));
			
			/*
			int count = 0;
			
			if(words.containsKey(stemmedWord))
			{
				count = words.get(stemmedWord);
			}
			
			words.put(stemmedWord, ++count);
			*/
			
			HashMap<Integer, DocTermInfo> docs =  words.get(stemmedWord);
			
			if(docs == null)
				docs = new HashMap<Integer, DocTermInfo>();
			
			// Is it reference type or value type
			DocTermInfo dti = docs.get(docId);
			
			if(dti != null)
			{
				dti.flags.add(flag);
				dti.count++;
			}
			else
			{
				dti = new DocTermInfo(1, flag);
				docs.put(docId, dti);
			}
			
			words.put(stemmedWord, docs);
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void ExtractWords(StringBuilder text, int docId, eCategory passedCategory)
		{
			// ********************
			// Clear previous words when we write to disk....
			//  words.clear();
			// *******************
		
		
		
			boolean infoBoxOn = false;
			int bracesBlocks = 0, j = 0;
			//String infoBoxEndedAfter = null;
			
			
			char[] word = new char[100];
			char c, prevChar = '\0';
			
			int i=0, textLength = text.length();
			
			for(; i < textLength; i++)
			{
				c = text.charAt(i);
				
				if(c == '{' && prevChar == '{')
				{
					bracesBlocks++;
					continue;
				}
				else if(c == '}' && prevChar == '}')
				{
					bracesBlocks--;
					
					if(bracesBlocks == 0 && infoBoxOn)
					{
						//System.out.println("\n\n\nInfobox ended.\n\n\n");
						//infoBoxEndedAfter = new String(word);
						infoBoxOn = false;
						break;
					}
					
					if(bracesBlocks < 0)
						bracesBlocks = 0;
					
					continue;
				}
				
				if((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
					word[j++]=c;
				else
				{
					if(j>2)
					{
						//word[j] = '\0';
						
						String w = new String(word, 0, j);
						String in = new String("Infobox");
						
						
						if(w.equals(in))
						{
							infoBoxOn = true;
						}
						else
						{
							if(infoBoxOn)
								AddWord(w, docId, eCategory.Infobox); // Say that this is in infobox
							else
								AddWord(w, docId, passedCategory);
						}
						
						
						j = 0;
						
						//Process word...
						//System.out.println(word + " - ");
						
						
						
					}
					j = 0;
				}
				
				prevChar = c;
			}
			
			// Now process rest of the part
			prevChar = '\0';
			String prevWrod = null;
			j = 0;
			boolean categoryStarted = false, referencesStarted = false, externalLinkStarted = false;
			boolean checkIfCategory = false, storeCategory = false;
			
			for(i++ ; i < textLength; i++)
			{
				c = text.charAt(i);
				
				if(c == '=' && prevChar == '=')
				{
					if(!categoryStarted)
						categoryStarted = true;
				}
				if(c == '[' && prevChar == '[')
				{
					if(!checkIfCategory)
						checkIfCategory = true;
				}
				else if(c == ']' && prevChar == ']')
				{
					if(checkIfCategory)
						checkIfCategory = false;
					if(storeCategory)
						storeCategory = false;
				}
				else if((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
					word[j++]=c;
				else if(c == '\n' && prevChar == '\n')
				{
					if(categoryStarted)
					{
						categoryStarted = false;
						
						if(referencesStarted)
						{
							//System.out.println("\nRef End\n");
							referencesStarted = false;
						}
						
						if(externalLinkStarted)
						{
							//System.out.println("\nExternal End\n");
							externalLinkStarted = false;
						}
					}
					
				}
				else	
				{
					if(j>2)
					{
						//word[j] = '\0';
						String w = new String(word, 0, j);
						
						if(new String(word, 0, j).equals(new String("References")) && categoryStarted)
						{
							referencesStarted = true;
							
							//System.out.println("\nRef started\n");
							
						}
						else if(new String(word, 0, j).equals(new String("External")) && categoryStarted)
						{
							externalLinkStarted = true;
							
							//System.out.println("\nExternal started\n");
							
						}
						else if(w.equalsIgnoreCase("links") && categoryStarted && prevWrod.equalsIgnoreCase("external"))
						{
							
						}
						else if(w.equalsIgnoreCase("category") && checkIfCategory)
						{
							storeCategory = true;
						}
						else
						{
							System.out.println(w + " in  " + (referencesStarted ? "reference ":"") + (externalLinkStarted? "External link":"" ) + (storeCategory ? " Category :":""));
							
							// Check which flag is true according add it...
							
							eCategory category;
							
							if(passedCategory == eCategory.Title)
								category = eCategory.Title;
							else
							{	
								category= eCategory.Body;

								if(referencesStarted)
									category = eCategory.Reference;
								else if(externalLinkStarted)
									category = eCategory.Link;
								else if(storeCategory)
									category = eCategory.Category;
							
							}
							
							AddWord(w, docId, category);
						}
						//else
						//	System.out.println(word);
						
						
						j = 0;
						
						//Process word...
						//System.out.println(word + " - ");
						prevWrod = w;
						

						
					}
					
					j = 0;
				}
				
				prevChar = c;
				
			}
			
			
			
			///System.out.println("\n\nInfobox endeda after : " + new String(infoBoxEndedAfter));
		}
	
		/* Using Regex
		public void parse(int pageId, String title, String text)
		{
			try
			{		 
				ExtractWordsAfterEqualTo(ExtractInfobox(text));
				
				// Add words from title..
				
				// Add words from text tag apart from Info box...
				ExtractAferInfoBox(text);
				
				
		        //System.out.println("total words: " + words.size());
		        
		        //System.out.println("For page id : " + pageId + " and Title: " + title + " words are-");
		        
		        for(String key : words.keySet())
		        {
		        	//System.out.println(key + " : " + words.get(key));
		        }
		    }
			catch(Exception e)
			{
				//System.out.println(e.getMessage());
			}
		}
		*/
	
	
	public void parse(int pageId, StringBuilder title, StringBuilder text)
	{
		try
		{		 
			//ExtractWordsAfterEqualTo(ExtractInfobox(text));
			
			// Add words from title..
			
			// Add words from text tag apart from Info box...
			ExtractWords(text, pageId, eCategory.None);
			ExtractWords(title, pageId, eCategory.Title);
			
			
	        //System.out.println("total words: " + words.size());
	        
	        //System.out.println("For page id : " + pageId + " and Title: " + title + " words are-");
	        
			/*
	        for(String key : words.keySet())
	        {
	        	System.out.println(key + " : " + words.get(key));
	        }
	        */
	        //System.out.println("Page Id : " + pageId);
	    }
		catch(Exception e)
		{
			//System.out.println(e.getMessage());
		}
	}
	
	
}
