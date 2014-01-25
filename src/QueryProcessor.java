import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;


public class QueryProcessor {

	public static StringBuilder ExtractWord(String word)
	{
		StringBuilder sbr = new StringBuilder();
		
		for(char c : word.toCharArray())
		{
			if((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || ( c >= 48 && c <= 57))
			{
				sbr.append(c);
			}
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
			IndexHelper helper = new IndexHelper(indexFolder);
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
			
			Stemmer stemmer = new Stemmer();

			//IndexHelper helper = new IndexHelper(indexFolder);
			//helper.LoadSecondaryIndex();
			
			for(int i=0; i < noOfQueries; i++)
			{
				try
				{
					
					query = scanner.nextLine();
					
					//long milliSec = System.currentTimeMillis();
					
					if(query == null)
						continue;

					StringBuilder sbr = ExtractWord(query);
					
					// Stem word and search it.
					helper.SearchWord(stemmer.StemWord(sbr.toString()));
					
					//System.out.println(((System.currentTimeMillis() - milliSec)/1000f) + " second");
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

}
