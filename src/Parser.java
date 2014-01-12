import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.sound.midi.MidiDevice.Info;

class StopWords {

	static HashMap<String, Boolean> words = new HashMap<String, Boolean>();
	boolean filled = false;
	
	public void FillStopWords()
	{
		words.put("is", true);
		words.put("are", true);
		words.put("an", true);
		words.put("am", true);
		words.put("was", true);
		words.put("here", true);
		words.put("there", true);
		words.put("no", true);
		words.put("the", true);
		words.put("of", true);
		words.put("gt", true);
		words.put("lt", true);
		
		filled = true;
	}
	
	public HashMap<String, Boolean> getWords()
	{
		if(!filled)
			FillStopWords();
		return words;
	}
	
}

public class Parser {
	
	private static HashMap<String, Boolean> stopWords;
	private static HashMap<String, Integer> words;
	
	public Parser()
	{
			stopWords = new StopWords().getWords();
			words = new HashMap<String, Integer>();
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
                System.out.println("No match found");
            }
            System.out.println("Bolcks = " + times);
            return result;
	}
	
	public void GetWordCount(String text)
	{
		// For each word
		Pattern wordPattern = Pattern.compile("[A-Za-z]+");
		Matcher wordMatcher = wordPattern.matcher(text);
		
		Stemmer stemmer = new Stemmer();

		while(wordMatcher.find())
		{
			String word = wordMatcher.group();

			// Check if it is a stop word
			if(stopWords.containsKey(word))
				// This is a stop word.
			{
				//System.out.println(word + "is in hash map");
				continue;
			}

			String stemmedWord = stemmer.StemWord(word);
			
			int count = 0;
			
			if(words.containsKey(stemmedWord))
			{
				count = words.get(stemmedWord);
			}
			
			words.put(stemmedWord, ++count);
		}
	}
	
	public void ExtractWordsAfterEqualTo(String data)
	{
		try
		{
			words.clear();
			
			String[] lines = data.split("\\n");
			System.out.println("List of words");
			
			for(String key: stopWords.keySet())
			{
				System.out.println("Key : " + key);
			}
			
			// For the right part
			Pattern rightSidePattern = Pattern.compile("[^=]*=(.*)");
	
			for(String line:lines)
			{
				Matcher matcher = rightSidePattern.matcher(line);
				if(matcher.find())
				{
					//System.out.println(matcher.group(1));		
					GetWordCount(matcher.group(1));
					
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	public void parse(String text)
	{
		try
		{		 
			ExtractWordsAfterEqualTo(ExtractInfobox(text));
	        
	        System.out.println("total words: " + words.size());
	        
	        for(String key : words.keySet())
	        {
	        	System.out.println(key + " : " + words.get(key));
	        }
	    }
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}
