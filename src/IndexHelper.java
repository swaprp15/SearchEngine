import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;


class IndexNode
{
	private String word;
	private long offset;
	
	public IndexNode(String word, long offset)
	{
		this.word = word;
		this.offset = offset;
	}
	
	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int CompareTo(IndexNode arg)
	{
		return this.word.compareTo(arg.getWord());
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
}

class IndexHelper
{
	public static TreeMap<String, Long> secondaryIndex = new TreeMap<>();
	public static IndexNode[] indexStructure;
	
	private String indexFile;
	private String indexFolder;
	
	public IndexHelper(String indexFolder)
	{
		this.indexFolder = indexFolder;
		StringBuilder file = new StringBuilder(indexFolder);
		file.append('/');
		file.append(Constants.indexFileName);
		indexFile = file.toString();
	}
	
	public void LoadSecondaryIndex() throws IOException
	{
		StringBuilder sbr = new StringBuilder(this.indexFolder);
		sbr.append('/');
		sbr.append(Constants.secondaryIndexFileName);
		
		BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new FileReader(sbr.toString()));	
			String str = reader.readLine();
			
			while(str != null)
			{
				String[] parts = str.split(",");
				try
				{
					secondaryIndex.put(parts[0], Long.parseLong(parts[1]));
				}
				catch(Exception e)
				{
					
				}
				str = reader.readLine();
			}
		}
		catch(Exception e)
		{
			
		}
		finally
		{
			reader.close();
		}
		
		indexStructure = new IndexNode[secondaryIndex.size()];
		
		int i=0;
		for(String s : secondaryIndex.keySet())
		{
			IndexNode node = new IndexNode(s, secondaryIndex.get(s));
			indexStructure[i++] = node;
			
		}

	}
	
	public void CreateSecondaryIndex() throws IOException
	{
		long offset = 0;
		final int INR = 2000;
		
		RandomAccessFile rFile = null;
		
		try
		{
			
			
			rFile = new RandomAccessFile(this.indexFile, "r");
			
			long length = rFile.length();
			
			// Add first word
			StringBuilder sbr = new StringBuilder();
			byte b;
			char c;
			
			sbr.setLength(0);
			
			b = rFile.readByte();
			c = (char)b;
			while(c != ':')
			{
				sbr.append(c);
				c = (char)rFile.readByte();
			}
			
			secondaryIndex.put(sbr.toString(), (long)0);
			
			offset += INR;
			
			
			while(offset < length)
			{
				rFile.seek(offset);
			
				// Go to next \n
				b = rFile.readByte();
				c = (char)b;
				
				char prevCahr = c;
				
				c = (char)rFile.readByte();
				
				while(true)
				{
					//sbr.append(c);
					
					if(prevCahr == '!' && (c >= 97 && c <= 122))
						break;
					prevCahr = c;
					offset = rFile.getFilePointer();
					c = (char)rFile.readByte();
				}
				
				//offset = rFile.getFilePointer();
				
				sbr.setLength(0);
				sbr.append(c);
				
				b = rFile.readByte();
				c = (char)b;
				while(c != ':')
				{
					sbr.append(c);
					c = (char)rFile.readByte();
				}
				
				System.out.println(sbr.toString() + sbr.toString().length());
				
				secondaryIndex.put(sbr.toString(), offset);
				
				offset += INR;
			}
			
			
		}
		catch(Exception e)
		{
			System.out.println(e.getCause());
		}
		finally
		{
			rFile.close();
		}

		indexStructure = new IndexNode[secondaryIndex.size()];
		
		int i=0;
		for(String s : secondaryIndex.keySet())
		{
			IndexNode node = new IndexNode(s, secondaryIndex.get(s));
			indexStructure[i++] = node;
			
		}
		
	}
	
//	public void CreateSecondaryIndex() throws IOException
//	{
//		long offset = 0;
//		final int INR = 2000;
//		
//		RandomAccessFile rFile = null;
//		
//		try
//		{
//			
//			
//			rFile = new RandomAccessFile(this.indexFile, "r");
//			
//			long length = rFile.length();
//			
//			// Add first word
//			String line = rFile.readLine();
//			int index = line.indexOf(',');
//			String word = line.substring(0, index);
//			
//			secondaryIndex.put(word, offset);
//			//System.out.println(word + " : " + secondaryIndex.get(word));
//			//str.setLength(0);
//			
//			offset += INR;
//			
//			while(offset < length)
//			{
//				rFile.seek(offset);
//			
//				line = rFile.readLine();
//				if(line == null)
//					break;
//				
//				offset = rFile.getFilePointer();
//				line = rFile.readLine();
//				if(line == null)
//					break;
//				index = line.indexOf(',');
//				
//				
//				if(index == -1)
//				{
//					offset = rFile.getFilePointer();
//					continue;
//				}
//				
//				/*
//				
//				while(c != ',')
//				{
//					str.append(c);
//					c = (char)rFile.readChar();
//					System.out.println(new String(c));
//					System.err.println(rFile.getFilePointer());
//				}
//				*/
//				
//				word = line.substring(0, index);
//				
//				secondaryIndex.put(word, offset);
//				//System.out.println(word + " : " + secondaryIndex.get(word));
//				//str.setLength(0);
//				
//				offset += INR;
//			}
//			
//			
//		}
//		catch(Exception e)
//		{
//			System.out.println(e.getCause());
//		}
//		finally
//		{
//			rFile.close();
//		}
//		
//		/*
//		for(String s : secondaryIndex.keySet())
//		{
//			System.out.println(s + " : " + secondaryIndex.get(s));
//		}
//		
//		System.out.println("Now see if offsets are correct..");
//		
//		try
//		{
//			rFile = new RandomAccessFile("index", "r");
//			
//			for(String s: secondaryIndex.keySet())
//			{
//				rFile.seek(secondaryIndex.get(s));
//				System.out.println(rFile.readLine());
//			}
//			
//		}
//		catch(Exception e)
//		{
//			System.out.println(e.getStackTrace());
//		}
//		finally
//		{
//			rFile.close();
//		}
//		*/
//		
//		/*
//		StringBuilder sbr = new StringBuilder(this.indexFolder);
//		sbr.append('/');
//		sbr.append(Constants.secondaryIndexFileName);
//		
//		BufferedWriter writer =null;
//		
//		try
//		{
//			writer = new BufferedWriter(new FileWriter(sbr.toString()));
//			
//			sbr.setLength(0);
//			
//			for(String s : secondaryIndex.keySet())
//			{
//				sbr.append(s);
//				sbr.append(',');
//				sbr.append(secondaryIndex.get(s));
//				sbr.append('\n');
//				
//				writer.write(sbr.toString());
//				sbr.setLength(0);
//			}
//		}
//		catch(Exception e)
//		{
//			
//		}
//		finally
//		{
//			writer.close();
//		}
//		*/
//
//		indexStructure = new IndexNode[secondaryIndex.size()];
//		
//		int i=0;
//		for(String s : secondaryIndex.keySet())
//		{
//			IndexNode node = new IndexNode(s, secondaryIndex.get(s));
//			indexStructure[i++] = node;
//			
//		}
//		
//	}
	
	public TermData SearchWord(String word, eCategory category) throws IOException
	{
		int start = 0;
		int end = secondaryIndex.size() - 1;
		
		int middle = 0;
		boolean found = false;
		
		TreeSet<Tuple<Integer, Integer, Byte>> docSet = new TreeSet<>();
		
		TermData termdata = new TermData();
		
		while(end >= start)
		{
			middle = start + (end-start)/2;
			
			int diff = indexStructure[middle].getWord().compareTo(word);
			
			if(diff == 0)
			{
				found = true;
				break;
			}
			else if(diff < 0)
			{
				start = middle + 1;
			}
			else
				end = middle - 1;
			
		}
		
		if(found)
		{
			//System.out.println("Searched word is : " + indexStructure[middle].getWord());
			termdata = SearchWordInIndex(indexStructure[middle].getOffset(), indexStructure[middle].getOffset(), word, false);
		}
		else if (start == end)
		{
			if(indexStructure[start].getWord().compareTo(word) == 0)
			{
				//System.out.println("Searched word is : " + indexStructure[start].getWord());
				termdata = SearchWordInIndex(indexStructure[start].getOffset(), indexStructure[start].getOffset(), word, false);
			}
			else
			{
				/*
				if(indexStructure[start].getWord().compareTo(word) < 0)
				{
					// start to EOF
					
					System.out.println("Search between " + indexStructure[start].getWord() + " and " + indexStructure[start + 1].getWord());
					SearchWordInIndex(indexStructure[start].getOffset(), indexStructure[end].getOffset(), word, true);
				}
				else
				{
					System.out.println("Search between " + indexStructure[start - 1].getWord() + " and " + indexStructure[start].getWord());
					SearchWordInIndex(indexStructure[start].getOffset(), indexStructure[middle].getOffset(), word, false);
				}
				*/
				
				//if(indexStructure[start].getWord().compareTo(word) < 0)
				if(middle < start)
				{
					// start to EOF
					
					if(indexStructure[start].getWord().compareTo(word) > 0)
					{
						//System.out.println("Search between " + indexStructure[start].getWord() + " and " + indexStructure[start + 1].getWord());
						termdata = SearchWordInIndex(indexStructure[middle].getOffset(), indexStructure[start].getOffset(), word, false);
					}
					else
					{
						termdata = SearchWordInIndex(indexStructure[start].getOffset(), indexStructure[start].getOffset(), word, true);
					}
				}
				else if(middle > start)
				{
					if(indexStructure[start].getWord().compareTo(word) > 0)
					{
						//System.out.println("Search between " + indexStructure[start - 1].getWord() + " and " + indexStructure[start].getWord());
						termdata = SearchWordInIndex(indexStructure[start].getOffset(), indexStructure[start - 1].getOffset(), word, false);
					}
					else
					{
						termdata = SearchWordInIndex(indexStructure[start].getOffset(), indexStructure[middle].getOffset(), word, false);
					}
				}
			}
		}
		else
		{
			if(indexStructure[middle].getWord().compareTo(word) > 0)
			{
				//System.out.println("Search between " + indexStructure[start].getWord() + " and " + indexStructure[middle].getWord());
				if(middle > 0)
					termdata = SearchWordInIndex(indexStructure[middle - 1].getOffset(), indexStructure[middle].getOffset(), word, false);
				else
					termdata = SearchWordInIndex(indexStructure[middle].getOffset(), indexStructure[middle].getOffset(), word, false);
			}
			else
			{
				//System.out.println("Search between " + indexStructure[middle].getWord() + " and " + indexStructure[end].getWord());
				termdata = SearchWordInIndex(indexStructure[middle].getOffset(), indexStructure[middle].getOffset(), word, true);
			}
		}
		
		
		if(termdata.docs.size() == 0)
		{
			System.out.println("No docs found");
			return termdata;
		}
		
		TreeSet<Tuple<Integer, Integer, Byte>> filteredDocSet = new TreeSet<>();
		
		for(Iterator<Tuple<Integer, Integer, Byte>> it = termdata.docs.iterator(); it.hasNext();)
		{
			Tuple<Integer, Integer, Byte> tuple = it.next();
			
			int docId = tuple.x;
			byte cat = tuple.z;
			
			switch (category) 
			{
				case Title:
					
					if((cat & 1) == 1)	
						filteredDocSet.add(tuple);
						//System.out.println(docId + " ");
					break;
	
				case Body:
					if((cat & 2) == 2)	
						filteredDocSet.add(tuple);
					//System.out.println(docId + " ");
					break;
					
				case Infobox:
					if((cat & 4) == 4)	
						filteredDocSet.add(tuple);
					//System.out.println(docId + " ");
					break;
					
				case Category:
					if((cat & 8) == 8)	
						filteredDocSet.add(tuple);
					//System.out.println(docId + " ");
					break;
					
				case Link:
					if((cat & 16) == 16)	
						filteredDocSet.add(tuple);
					//System.out.println(docId + " ");
					break;
					
				case Reference:
					if((cat & 32) == 32)	
						filteredDocSet.add(tuple);
					//System.out.println(docId + " ");
					break;
					
				case None:
					filteredDocSet.add(tuple);
					//System.out.println(docId + " ");
					break;
					
				default:
					break;
			}
		}
		
		termdata.docs = filteredDocSet;
		
		return termdata;
	}
	
	public TermData SearchWordInIndex(long start, long end, String wordToSearch, boolean searchTillEnd) throws IOException
	{
		RandomAccessFile rFile = null;
		TreeSet<Tuple<Integer, Integer, Byte>> ids = new TreeSet<>();
		TermData termData = new TermData();
		long offset = 0;
		try
		{
		 	
		 
		 	
		 rFile = new RandomAccessFile(this.indexFile, "r");
		 
		 rFile.seek(start);
		 boolean found = false;
		 StringBuilder sbr = new StringBuilder();
		 byte b;
		 char c;
		 
		 
		 
		 while(rFile.getFilePointer() <= end || searchTillEnd)
		 {
			sbr.setLength(0);
				
			b = rFile.readByte();
			c = (char)b;
			while(c != ':')
			{
				sbr.append(c);
				c = (char)rFile.readByte();
			}
			
			int result = wordToSearch.compareTo(sbr.toString());
			
			// Read the entire record.
			int numOfDocs = rFile.readInt();
			
			
			termData.term = wordToSearch;
			termData.documentFrequnecy = numOfDocs;
			
			while(numOfDocs > 0)
			{
				Tuple<Integer, Integer, Byte> tuple = new Tuple<Integer, Integer, Byte>();
				
				tuple.x = rFile.readInt();
				tuple.y = rFile.readInt();
				tuple.z = rFile.readByte();
				
				ids.add(tuple);
				
				numOfDocs--;
			}
			
			// read new line
			rFile.readByte();			
			
			if(result < 0) {
				//System.out.println("Not found");
				//rFile.close();
				ids.clear();
				break;
				
			} else if(result == 0)
			{
				//System.out.println("Found");
				
				found = true;
				
				for(Iterator<Tuple<Integer, Integer, Byte>> it = ids.iterator(); it.hasNext();)
				{
					Tuple<Integer, Integer, Byte> tuple = it.next();
					
					//System.out.print(tuple.x + " ");
					//System.out.print(tuple.y + " ");
					//System.out.print(tuple.z + " ");
				}
				
				/*; 
				numOfDocs = rFile.readInt();
				
				while(numOfDocs > 0)
				{
					
					numOfDocs--;
				}
				
				// read new line
				rFile.readByte();
				System.out.println();
				*/
				break;
			}
			
			ids.clear();
		 }
		 
		 if(!found){
			 System.out.println(); //Not found
		 }
		
		}
		catch(Exception e)
		{
			System.out.println(e.getCause());
		}
		finally
		{
			rFile.close();
		}
		
		
		termData.docs = ids;
		return termData;
		/*
		
		System.out.println("Offset is : " + offset + ". Line is  ");
		
		BufferedReader br = new BufferedReader(new FileReader("index"));
		
		System.out.println(br.readLine());
		
		br.close();*/
	}
	
	public void PrintDocumentIds(StringBuilder line)
	{
		String docIdsString = line.substring(line.indexOf(",") + 1);
		
		String[] docIds = docIdsString.split("\\.");
		
		TreeSet<Integer> docIdsSet = new TreeSet<>();
		
		for(String docId : docIds)
		{
			try
			{
				docIdsSet.add(Integer.parseInt(docId));
			}
			catch(Exception e)
			{
				
			}
		}
		
		StringBuilder finalIds = new StringBuilder();
		
		Iterator<Integer> it = docIdsSet.iterator();
		
		if(it == null)
			return;
		
		while(true)
		{
			finalIds.append(it.next());
			
			if(it.hasNext())
				finalIds.append(',');
			else
				break;
		}
		//System.out.println(line.substring(0, line.indexOf(",")) + " remove this........");
		System.out.println(finalIds.toString());
	}
}
