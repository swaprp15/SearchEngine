import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;


class DocScore implements Comparable<DocScore>
{
	int docId;
	float score;
	boolean isTermInTitle;
	
	public DocScore(int docId, float score, boolean isTermInTitle)
	{
		this.docId = docId;
		this.score = score;
		this.isTermInTitle = isTermInTitle;
	}

	@Override
	public int compareTo(DocScore o) {
		// TODO Auto-generated method stub
		
		// If this returns -1 then it will be moved to the front i.e. higher ranking.
		// If this score is greater that o.. keep it front
		// else if this is in title but o is not in title then keep this front
		// else keep o at front.
		return this.score < o.score ? ( this.isTermInTitle && !o.isTermInTitle ? -1 : 1) : -1;
	}
}

class TermData
{
	String term;
	int documentFrequnecy;
	TreeSet<Tuple<Integer, Integer, Byte>> docs; 
	
	public TermData()
	{
		term = null;
		documentFrequnecy  = 0;
		docs = new TreeSet<>();
	}
}

public class QueryProcessor {

	public static IndexHelper helper;
	
	public static StringBuilder ExtractWord(String word)
	{
		StringBuilder sbr = new StringBuilder();
		char prevChar = 0;
		
		for(char c : word.toCharArray())
		{
			if((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || ( c >= 48 && c <= 57) || ( c == ' ') || 
					((c == ':') && (prevChar == 't' || prevChar == 'b' || prevChar == 'c' || prevChar == 'i' || prevChar == 'l' || prevChar == 'r') ))
			{
				sbr.append(c);
			}
			
			prevChar = c;
		}
		
		return sbr;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if(args.length != 1)
		{
			return;
		}
		
		String indexFolder = args[0];
		
		try
		{
			helper = new IndexHelper(indexFolder);
		   	helper.CreateSecondaryIndex();

			
			Scanner scanner = new Scanner(System.in);
			
			String line = scanner.nextLine();
			
			if(line == null)
			{
				scanner.close();
				return;
			}
			
			int noOfQueries = Integer.parseInt(line);
			
			String query = null;
			


			//IndexHelper helper = new IndexHelper(indexFolder);
			//helper.LoadSecondaryIndex();
			
			for(int i=0; i < noOfQueries; i++)
			{
				try
				{
					
					query = scanner.nextLine();
					
					long milliSec = System.currentTimeMillis();
					
					if(query == null)
						continue;

					StringBuilder sbr = ExtractWord(query);
					
					// Stem word and search it.
					//helper.SearchWord(stemmer.StemWord(sbr.toString()));
					
					PreProcessQuery(sbr.toString());
					
					System.out.println(((System.currentTimeMillis() - milliSec)/1000f) + " second");
				}
				catch(Exception e)
				{
					
				}
			}
			
			scanner.close();

		}		
		catch(Exception e)
		{
			
		}
	}
	
	public static void PreProcessQuery(String query)
	{
		try
		{
		
			Stemmer stemmer = new Stemmer();
			
			String[] parts = query.split("\\s");
			
			int noOfTerms = parts.length;
			
			TermData[] docSet = new TermData[noOfTerms];
			
			/*
			TreeSet<Tuple<Integer, Integer, Byte>>[] titleSet = new TreeSet[noOfTerms];
			TreeSet<Tuple<Integer, Integer, Byte>>[] bodySet = new TreeSet[noOfTerms];
			TreeSet<Tuple<Integer, Integer, Byte>>[] categorySet = new TreeSet[noOfTerms];
			TreeSet<Tuple<Integer, Integer, Byte>>[] infoboxSet = new TreeSet[noOfTerms];
			TreeSet<Tuple<Integer, Integer, Byte>>[] linkSet = new TreeSet[noOfTerms];
			TreeSet<Tuple<Integer, Integer, Byte>>[] referenceSet = new TreeSet[noOfTerms];
			TreeSet<Tuple<Integer, Integer, Byte>>[] generalSet = new TreeSet[noOfTerms];
			*/
			
			
			int index = 0;
			
			for(index = 0; index < noOfTerms; index++)
			{
				docSet[index] = new TermData();
				/*
				titleSet[index] = new TreeSet<>();
				bodySet[index] = new TreeSet<>();
				categorySet[index] = new TreeSet<>();
				infoboxSet[index] = new TreeSet<>();
				linkSet[index] = new TreeSet<>();
				referenceSet[index] = new TreeSet<>();
				generalSet[index] = new TreeSet<>();
				*/
			}
			
			index = 0;
			
			for(String s : parts)
			{
				System.out.println(s + " ");
				
				if(s.charAt(1) == ':')
				{
					switch(s.charAt(0))
					{
					case 't':
						docSet[index] = helper.SearchWord(stemmer.StemWord(s.substring(2)), eCategory.Title);
						//docSet[index].addAll(titleSet[index]);						
						
						break;
						
					case 'b':
						docSet[index] = helper.SearchWord(stemmer.StemWord(s.substring(2)), eCategory.Body);
						//docSet[index].addAll(bodySet[index]);
						break;
						
					case 'c':
						docSet[index] = helper.SearchWord(stemmer.StemWord(s.substring(2)), eCategory.Category);
//						docSet[index].addAll(categorySet[index]);
						break;
						
					case 'i':
						docSet[index] = helper.SearchWord(stemmer.StemWord(s.substring(2)), eCategory.Infobox);
//						docSet[index].addAll(infoboxSet[index]);
						break;
						
					case 'l':
						docSet[index] = helper.SearchWord(stemmer.StemWord(s.substring(2)), eCategory.Link);
//						docSet[index].addAll(linkSet[index]);
						break;
						
					case 'r':
						docSet[index] = helper.SearchWord(stemmer.StemWord(s.substring(2)), eCategory.Reference);
//						docSet[index].addAll(referenceSet[index]);
						break;
						
					default:
						break;
					}
				}
				else
				{
					docSet[index] = helper.SearchWord(stemmer.StemWord(s), eCategory.None);
//					docSet[index].addAll(generalSet[index]);
				}
				
				
				index++;
					
			}
		
			// Now order documents according o tf-idf
			
			// Step 1.
			// 		Find out distinct document IDs
			
			HashSet<Integer> distintDocIds = new HashSet<>();
			
			for(TermData termData : docSet)
			{
				for(Tuple<Integer, Integer, Byte> tuple : termData.docs)
				{
					distintDocIds.add(tuple.x);
				}
			}
			
			// Step 2.
			//		For each document calculate Score(q,d)
			
			float score = 0;
			
			// ***************** Count total number of documents
			
			int N = 10000;
			
			TreeSet<DocScore> docsByRelevance = new TreeSet<>();
			
			for(Integer docId : distintDocIds)
			{
				score = 0;
				
				boolean isTermInTitle = false;
				
				for(TermData termData : docSet)
				{
					for(Iterator<Tuple<Integer, Integer, Byte>> it = termData.docs.iterator(); it.hasNext(); )
					{
						Tuple<Integer, Integer, Byte> tuple = it.next();
						
						if(tuple.x == docId)
						{
							score += (tuple.y * (Math.log(N/termData.documentFrequnecy)));
							if((tuple.z & 1) == 1)
								isTermInTitle = true;
						}
					}
				}
				
				docsByRelevance.add(new DocScore(docId, score, isTermInTitle));
				
			}
			
			for(Iterator<DocScore> it = docsByRelevance.iterator(); it.hasNext();)
			{
				DocScore ds =  it.next();
				
				System.out.println(ds.docId + " " + ds.score + (ds.isTermInTitle?" in title":""));
			}
			
			/*
			int totalDocs = 0;
			
			for(int i=0; i < noOfTerms; i++)
			{
				totalDocs += docSet[i].docs.size();
			}
			
			TreeSet<Tuple<Integer, Integer, Byte>> finalSet = new TreeSet<Tuple<Integer,Integer,Byte>>();
			
			if(totalDocs < 10)
			{
				// Perform OR
				
				for(TreeSet<Tuple<Integer, Integer, Byte>> set : docSet)
				{
					finalSet.addAll(set);
				}
				
			}
			else
			{
				// Perform AND
				System.out.println("ANDing");
			}
			
			

			for(Iterator<Tuple<Integer, Integer, Byte>> it = finalSet.iterator(); it.hasNext();)
			{
				Tuple<Integer, Integer, Byte> tuple = it.next();
				
				System.out.println(tuple.x + " ");
			}

			*/
			
			//helper.SearchWord(stemmer.StemWord(""));
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		
	}

}
