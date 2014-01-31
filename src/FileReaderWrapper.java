import java.awt.image.TileObserver;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class Tuple<X,Y,Z> implements Comparable<X>
{
	public X x;
	public Y y;
	public Z z;
	
	public Tuple(X x, Y y, Z z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Tuple()
	{
	}

	@Override
	public int compareTo(X o) {
		// TODO Auto-generated method stub
		return o == x ? 0:1;
	}
}

public class FileReaderWrapper 
{
	
	DefaultHandler handler;
	Parser parser;
	int numberOfDocs;
	
	int fileNumber;
	PriorityQueue<PQueueNode> queue;
	TreeMap<Integer, Boolean> fileAvailable;

	char[][] lines;
   	char[][] terms;
   	boolean[] readLine;
   	
   	StringBuilder buffer;
   	//BufferedReader[] readers;
   	//DataInputStream[] readers;
   	RandomAccessFile[] readers;
   	StringBuilder[] words;
   	
   	DataOutputStream writer = null;
   	
   	private String outputFilePath;
	
	public FileReaderWrapper(String folder)
	{
		StringBuilder sb = new StringBuilder(folder);
		sb.append('/');
		sb.append(Constants.outputFileName);
		outputFilePath = sb.toString();
		
		parser = new Parser();
		queue = new PriorityQueue<PQueueNode>();
		buffer = new StringBuilder();
		fileNumber = 0;
		numberOfDocs = 0;
		
		handler = new DefaultHandler() {
			 
			boolean textTag = false;
			boolean idTag = false;
			boolean titleTag = false;
			boolean hasPageId = false;
			boolean pageTagStarted = false;
			
			int pageId = 0;
			
			StringBuilder textData = new StringBuilder();
			StringBuilder title = new StringBuilder();
			String pageIdString = null;
		 
			public void startElement(String uri, String localName,String qName, 
		                Attributes attributes) throws SAXException {
		 
				if (qName.equalsIgnoreCase("text")) {
					textTag = true;
				}
				else
				if(qName.equalsIgnoreCase("id"))
				{
					if(!hasPageId)
						idTag = true;
					else
						idTag = false;
				}
				else
				if(qName.equalsIgnoreCase("title"))
				{
					titleTag = true;
				}
				else if(qName.equalsIgnoreCase("page"))
				{
					hasPageId = false;
					pageTagStarted = true;
				}
		 
			}
		 
			public void endElement(String uri, String localName,
				String qName) throws SAXException {
				
				if(qName.equalsIgnoreCase("text")){
					if(textTag)
					{
						parser.parse(pageId, title, textData);
						numberOfDocs++;
						
						if(numberOfDocs == 1500)
						{
							WriteMapToFile();
							numberOfDocs = 0;
						}
						textTag = false;
						textData.setLength(0);
						title.setLength(0);
						pageIdString = null;
						pageTagStarted = false;
					}
				}
				else if(qName.equalsIgnoreCase("id") && idTag && !hasPageId)
				{
					try
					{
						pageId = Integer.parseInt(pageIdString);
						
					}
					catch(Exception e)
					{
						// Invalid number
					}
					hasPageId = true;
					idTag = false;
					
				}
				else if(qName.equalsIgnoreCase("title"))
				{
					titleTag = false;
					
				}
 					
			}
		 
			public void characters(char ch[], int start, int length) throws SAXException {
					
				if (textTag) {
					
						textData.append(ch, start, length);
				}
				else if(titleTag)
				{
					title.append(ch, start, length);
				}
				else if(idTag && !hasPageId && pageTagStarted)
				{
					
					if(pageIdString == null)
						pageIdString = new String(ch, start, length);
					else
						pageIdString += new String(ch, start, length);
					
				
				}
				
					
			}
		 
		     };
	}
 
   public void readFile(String path, String indexFolder)
   {
	   try
	   {
		   SAXParserFactory factory = SAXParserFactory.newInstance();
		   SAXParser saxParser = factory.newSAXParser();

		   saxParser.parse(path, handler);
		   
		   MergeFiles();
		   
		   //System.out.print("Done..");

	   }
	   catch (Exception e) 
	   {
		   e.printStackTrace();
	   }	
   }
   
//   public void WriteMapToFile()
//   {
//	   
//	   
//	   try
//	   {
//	   
//		   File file = new File(outputFilePath + fileNumber);
//		   file.createNewFile();
//		   
//		   BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//		   
//		   TreeMap<String, HashMap<Integer, DocTermInfo>> words = parser.GetWords();; 
//		   HashMap<Integer, DocTermInfo> docMap;
//		   
//		   
//		   StringBuilder br = new StringBuilder();
//		   
//		   for(String word: words.keySet())
//		   {
//			   br.append(word);
//			   br.append(',');
//			   
//			   docMap = words.get(word);
//			   
//			   StringBuilder ids = new StringBuilder();
//			   
//			   for(int id : docMap.keySet())
//			   {
//				   
//				   /*
//				   		   
//				   char no1 = (char)(id >> 16);
//				   char no2 = (char)(id & Integer.parseInt("000FFFF", 16));
//				   
//				   writer.write(no1);
//				   writer.write(no2);
//				   
//				   */
//				   
//				   br.append(id);
//				   DocTermInfo dti = docMap.get(id);
//				   br.append("Count:" + dti.count+",");
//				   
//				   EnumSet<eCategory> flags = dti.flags;
//				   
//				   if(flags.contains(eCategory.Title))
//					   br.append("Title");
//				   
//				   if(flags.contains(eCategory.Body))
//					   br.append("body");
//				   
//				   if(flags.contains(eCategory.Category))
//					   br.append("category");
//				   
//				   if(flags.contains(eCategory.Infobox))
//					   br.append("infobox");
//				   
//				   if(flags.contains(eCategory.Link))
//					   br.append("link");
//				   
//				   if(flags.contains(eCategory.Reference))
//					   br.append("refernce");
//				   
//				   
//				   br.append('.');
//				   
//				   
//			   }
//			   br.append('\n');
//			   writer.write(br.toString());
//			   br.setLength(0);
//			   
//		   }
//		   
//		   writer.flush();
//	   }
//	   catch(Exception e)
//	   {
//		   
//	   }
//	   
//	   fileNumber++;
//	   parser.ClearMap();
//   }
   
   public void WriteMapToFile()
   {
	   
	   
	   try
	   {
	   
		   File file = new File(outputFilePath + fileNumber);
		   file.createNewFile();
		   
		   DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		   //BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		   
		   TreeMap<String, HashMap<Integer, DocTermInfo>> words = parser.GetWords();; 
		   HashMap<Integer, DocTermInfo> docMap;
		   
		   
		   StringBuilder br = new StringBuilder();
		   
		   for(String word: words.keySet())
		   {
			   //br.append(word);
			   
			   dos.write(word.getBytes());
			   dos.writeByte(Constants.WordSeparator);
			   
			   //br.append(',');
			   
			   docMap = words.get(word);
			   
			   // Number of docs
			   dos.writeInt(docMap.size());
			   
			   StringBuilder ids = new StringBuilder();
			   
			   for(int id : docMap.keySet())
			   {
				   
				   /*
				   		   
				   char no1 = (char)(id >> 16);
				   char no2 = (char)(id & Integer.parseInt("000FFFF", 16));
				   
				   writer.write(no1);
				   writer.write(no2);
				   
				   */
				   
				   //br.append(id);
				   
				   dos.writeInt(id);
				   
				   DocTermInfo dti = docMap.get(id);
				   
				   //br.append("Count:" + dti.count+",");
				   
				   dos.writeInt(dti.count);
				   
				   EnumSet<eCategory> flags = dti.flags;
				   
				   byte flag = 0;
				   
				   if(flags.contains(eCategory.Title))
					   //br.append("Title");
					   flag |= 1;
				   
				   if(flags.contains(eCategory.Body))
					   //br.append("body");
					   flag |= 2;
				   
				   if(flags.contains(eCategory.Category))
					   //br.append("category");
					   flag |= 4;
				   
				   if(flags.contains(eCategory.Infobox))
					   //br.append("infobox");
					   flag |= 8;
				   
				   if(flags.contains(eCategory.Link))
					   //br.append("link");
					   flag |= 16;
				   
				   if(flags.contains(eCategory.Reference))
					   //br.append("refernce");
					   flag |= 32;
				   
				   
				   //br.append('.');
				   dos.writeByte(flag);
				   
				   
			   }
			   
			   //br.append('\n');
			   dos.writeByte(Constants.RecordSeparator);
			   
			   //writer.write(br.toString());
			   //br.setLength(0);
			   
		   }
		   
		   //writer.flush();
		   dos.flush();
	   }
	   catch(Exception e)
	   {
		   
	   }
	   
	   fileNumber++;
	   parser.ClearMap();
   }
   
   public int GetAvailableFileIndex()
   
   {
	   for(int index:fileAvailable.keySet())
	   {
		   if(fileAvailable.get(index) == true)
			   return index;
	   }
	   return -1;
   }
   
   public void MergeFiles()  throws IOException
   {
	   // Dump any remaining data
	   if(numberOfDocs > 0)
		   WriteMapToFile();
	   

	   
	   

	   try
	   {
		   File file = new File(outputFilePath);
		   
		   writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		   
		   //readers = new BufferedReader[fileNumber+1];
		   //readers = new DataInputStream[fileNumber+1];
		   readers = new RandomAccessFile[fileNumber+1];
		   
		   lines = new char[fileNumber+1][];
		   terms = new char[fileNumber+1][];
		   words = new StringBuilder[fileNumber+1];
		   readLine = new boolean[fileNumber+1];
		   
		   for(int i =0; i < fileNumber; i++)
		   {
			   //readers[i] = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFilePath+i)));
			   readers[i] = new RandomAccessFile(outputFilePath+i, "r");
			   
			   try
			   {
				   //readers[i].readInt();
			   }
			   catch(Exception e)
			   {
				   System.out.println(e.getMessage());
			   }
			   //lines[i] = readers[i].readLine().toCharArray();
			   words[i] = new StringBuilder();
			   
			   char c;
			   byte b;
			   
			   while(true)
			   {
				   b = readers[i].readByte();
				   if(b == (byte)Constants.WordSeparator)
					   break;
				   words[i].append((char)b);
			   }
			   
			   //System.out.println(readers[i].readInt());
			   
			   PQueueNode node = new PQueueNode(words[i], i);
			   words[i].setLength(0);
			   
			   queue.add(node);
			   
			   
		   }
		   
		   fileAvailable = new TreeMap<Integer, Boolean>();
		   for(int k = 0; k < fileNumber; k++)
		   {
			   fileAvailable.put(k, true);
		   }
		   
		   Boolean noFileAvailable = false;
		   
		   while(true)
		   {
			   
			   if(!RemoveLines())
				   break;
			   
			   AddLines();
			   
		   }
		   
//		   while(!queue.isEmpty())
//		   {
//			   PQueueNode head = queue.remove();
//			   int index = head.getIndex();
//			   writer.write(lines[index]);
//			   writer.write('\n');
//		   }
		   
		   
		   writer.flush();
		   
	   }
	   catch(Exception e)
	   {
		   System.out.print(e.getMessage());
	   }
	   finally
	   {
		   writer.close();                                  // *****************
		   
		   for(int i =0; i < fileNumber; i++)
		   {
			   readers[i].close();
			   //File fileToDelete = new File(outputFilePath+i);
			   //fileToDelete.delete();
		   }
	   }
	   
	   
	   
   }
   
   public boolean RemoveLines() throws IOException
   {
	   if(queue.isEmpty())
		   return false;
	   
	   PQueueNode head = queue.remove();
	   int index = head.getIndex();
	   
	   readLine[index] = true;
	   
	   String word = head.word;
	   
	   // Write word and separator
	   writer.write(word.getBytes());
	   writer.writeByte(Constants.WordSeparator);
	   
	   TreeSet<Tuple<Integer, Integer, Byte>> ids = new TreeSet<>();
	   
	   
	   // Now accumulate doc ids and info
	   //if(queue.isEmpty())
	   //{
		   try
		   {
			   //int noOfIds = readers[index].readInt();
			   
			   int numOfDocs = readers[index].readInt();
			   
			   while(numOfDocs > 0)
			   {
				   Tuple<Integer, Integer, Byte> tuple = new Tuple<Integer, Integer, Byte>();
				   
				   tuple.x = readers[index].readInt();
				   tuple.y = readers[index].readInt();
				   tuple.z = readers[index].readByte();
				   
				   ids.add(tuple);
				   numOfDocs--;
			   }
			   
			   // read new line byte..
			   readers[index].readByte();
		   }
		   catch(Exception e)
		   {
			   System.out.println(e.getMessage());
		   }
		   //return true;
	  // }
	   
	   while(!queue.isEmpty() && IsEqual(queue.peek().getWord(), word))
	   {
		   head = queue.remove();
		   int newIndex = head.getIndex();
		   readLine[newIndex] = true;
		   
		   words[newIndex].setLength(0);
		   
		   try
		   {
			   int numOfDocs = readers[newIndex].readInt();
			   
			   while(numOfDocs > 0)
			   {
				   Tuple<Integer, Integer, Byte> tuple = new Tuple<Integer, Integer, Byte>();
				   
				   tuple.x = readers[newIndex].readInt();
				   tuple.y = readers[newIndex].readInt();
				   tuple.z = readers[newIndex].readByte();
				   
				   ids.add(tuple);
				   numOfDocs--;
			   }
			   
			   // read new line byte..
			   readers[newIndex].readByte();
		   }
		   catch(Exception e)
		   {
			   System.out.println(e.getMessage());
		   }
		   
		   
	   }
	   
	   int noOfIds = ids.size();
	   
	   writer.writeInt(noOfIds);
	   
	   for(Iterator<Tuple<Integer, Integer, Byte>> it = ids.iterator(); it.hasNext();)
	   {
		   Tuple<Integer, Integer, Byte> tuple = it.next();
		   writer.writeInt(tuple.x);
		   writer.writeInt(tuple.y);
		   writer.writeByte(tuple.z);
	   }
	   
	   //writer.writeByte(Constants.NewLineChar);
	   writer.writeByte(Constants.RecordSeparator);
	   
	   return true;
   }
   
   public void AddLines() throws IOException
   {
	   for(int index = 0; index < fileNumber; index++)
	   {
		   if(readLine[index] == false || fileAvailable.get(index) == false)
			   continue;
		   
		   try
		   {
			   char c;
			   words[index].setLength(0);
			   while(true)
			   {
				   c = (char)readers[index].readByte();
				   if(c == Constants.WordSeparator)
					   break;
				   words[index].append(c);
			   }
			   
			   readLine[index] = false;			 
			   
			   PQueueNode node = new PQueueNode(words[index], index);
			   
			   queue.add(node);
		   }
		   catch(EOFException e)
		   {
			   readLine[index] = false;
			   fileAvailable.put(index, false);
			   
			   System.out.println(e.getMessage());
		   }
	   }
   }
   
   public boolean IsEqual(String first, String second)
   {
	   return first.equalsIgnoreCase(second);
	  
   }
   
//   public void MergeFiles()  throws IOException
//   {
//	   // Dump any remaining data
//	   if(numberOfDocs > 0)
//		   WriteMapToFile();
//	   
//
//	   
//	   BufferedWriter writer = null;
//
//	   try
//	   {
//		   File file = new File(outputFilePath);
//		   
//		   writer = new BufferedWriter(new FileWriter(file));
//		   
//		   readers = new BufferedReader[fileNumber+1];
//		   
//		   lines = new char[fileNumber+1][];
//		   terms = new char[fileNumber+1][];
//		   readLine = new boolean[fileNumber+1];
//		   
//		   for(int i =0; i < fileNumber; i++)
//		   {
//			   readers[i] = new BufferedReader(new FileReader(outputFilePath+i));
//			   lines[i] = readers[i].readLine().toCharArray();
//			   
//			   int j = 0, length = lines[i].length;
//			   
//			   terms[i] = new char[length];
//			   
//			   for(j=0; j < length; j++)
//			   {
//				   if(lines[i][j] == ',')
//					   break;
//				   terms[i][j] = lines[i][j];
//			   }
//			   
//			   PQueueNode node = new PQueueNode(terms[i], j, i);
//			   
//			   queue.add(node);
//			   
//			   
//		   }
//		   
//		   fileAvailable = new TreeMap<Integer, Boolean>();
//		   for(int k = 0; k < fileNumber; k++)
//		   {
//			   fileAvailable.put(k, true);
//		   }
//		   
//		   Boolean noFileAvailable = false;
//		   
//		   while(true)
//		   {
//			   
//			   StringBuilder line = RemoveLines();
//			   
//			   if(line == null)
//				   break;
//			   
//			   writer.write(line.toString().toCharArray());
//			   writer.write('\n');
//			   
//			   // Add lines...
//			   
//			   AddLines();
//			   
//		   }
//		   
//		   while(!queue.isEmpty())
//		   {
//			   PQueueNode head = queue.remove();
//			   int index = head.getIndex();
//			   writer.write(lines[index]);
//			   writer.write('\n');
//		   }
//		   
//		   
//		   writer.flush();
//		   
//	   }
//	   catch(Exception e)
//	   {
//		   System.out.print(e.getMessage());
//	   }
//	   finally
//	   {
//		   writer.close();                                  // *****************
//		   
//		   for(int i =0; i < fileNumber; i++)
//		   {
//			   readers[i].close();
//			   File fileToDelete = new File(outputFilePath+i);
//			   fileToDelete.delete();
//		   }
//	   }
//	   
//	   
//	   
//   }
   
//   public StringBuilder RemoveLines()
//   {
//	   if(queue.isEmpty())
//		   return null;
//	   
//	   PQueueNode head = queue.remove();
//	   int index = head.getIndex();
//	   
//	   readLine[index] = true;
//	   
//	   if(queue.isEmpty())
//		   return new StringBuilder(new String(lines[index]));
//	   
//	   buffer.setLength(0);
//	   
//	   int length=0;
//	   for(;length < lines[index].length; length++)
//	   {
//		   if(lines[index][length] == ',')
//			   break;
//	   }
//	   
//	   buffer.append(lines[index], 0, length);
//	   buffer.append(',');
//	   
//	   char[] currentTerm = new char[length];
//	   for(int i =0 ; i < length; i++)
//	   {
//		   currentTerm[i] = lines[index][i];
//	   }
//	   
//	   
//	   // ****		For now don't add doc ids.. *****************
//	   length++;
//	   while(length < lines[index].length)
//	   {
//		   buffer.append(lines[index][length++]);
//	   }
//	   
//	   
//	   while(!queue.isEmpty() && IsEqual(queue.peek().getWord().toCharArray(), currentTerm))
//	   {
//		   head = queue.remove();
//		   int newIndex = head.getIndex();
//		   readLine[newIndex] = true;
//		   
//		   int i=0;
//		   
//		   while(i < lines[newIndex].length && lines[newIndex][i++]!=',');
//		   
//		   //i++;
//		   
//		   while(i < lines[newIndex].length)
//		   {
//			   buffer.append(lines[newIndex][i++]);
//		   }
//		   
//		   
//	   }
//	   
//	   return buffer;
//   }
//   
//   public void AddLines() throws IOException
//   {
//	   for(int index = 0; index < fileNumber; index++)
//	   {
//		   if(readLine[index] == false || fileAvailable.get(index) == false)
//			   continue;
//		   
//		   String line = readers[index].readLine();
//		   
//		   if(line == null)
//		   {
//			   fileAvailable.put(index, false);
//		   }
//		   else
//		   {
//			   lines[index] = line.toCharArray();
//			   readLine[index] = false;
//			   
//			   int i = 0;
//			   while(lines[index][i++] != ',');
//			   
//			   PQueueNode node = new PQueueNode(lines[index], i-1, index);
//			   
//			   queue.add(node);
//		   }
//		   
//	   }
//   }
//   
//   public boolean IsEqual(char[] first, char[] second)
//   {
//	   int firstLength = first.length;
//	   int secondLength = second.length;
//	   
//	   int i = 0;
//	   while( i < firstLength && i < secondLength)
//	   {
//		   int diff = first[i] - second[i];
//		   i++;
//		   if(diff == 0)
//			   continue;
//		   else
//			   return false;
//	   }
//	   
//	   return firstLength == secondLength;
//	  
//   }
   
   /*
   public void MergeFiles()
   {
	   try
	   {
		   File file = new File(Constants.fileName);
		   
		   BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		   
		   BufferedReader[] readers = new BufferedReader[fileNumber+1];
		   
		   char[][] lines = new char[fileNumber+1][];
		   char[][] terms = new char[fileNumber+1][];
		   
		   for(int i =0; i < fileNumber; i++)
		   {
			   readers[i] = new BufferedReader(new FileReader(new String(Constants.fileName)+i));
			   lines[i] = readers[i].readLine().toCharArray();
			   
			   int j = 0, length = lines[i].length;
			   
			   terms[i] = new char[length];
			   
			   for(j=0; j < length; j++)
			   {
				   if(lines[i][j] == ',')
					   break;
				   terms[i][j] = lines[i][j];
			   }
			   
			   PQueueNode node = new PQueueNode(terms[i], j, i);
			   
			   queue.add(node);
			   
			   
		   }
		   
		   fileAvailable = new TreeMap<Integer, Boolean>();
		   for(int k = 0; k < fileNumber; k++)
		   {
			   fileAvailable.put(k, true);
		   }
		   
		   Boolean noFileAvailable = false;
		   
		   while(true)
		   {
			   PQueueNode head = queue.remove();
			   
			   int index = head.getIndex();
			   writer.write(lines[index]);
			   writer.write('\n');
			   
			   index = GetAvailableFileIndex();
			   
			   if(index == -1)
				   break;
			   
			   String s;
			   
			   if((s = readers[index].readLine()) == null) // File ends..
			   {
				   fileAvailable.put(index, false);
				   
				   int tmp = 0;
				   
				   while(true)
				   {
					   index = GetAvailableFileIndex();
					   if(index == -1 || ((s = readers[index].readLine() ) == null))
					   {
						   tmp++;
						   if(tmp > fileNumber)
						   {
							   noFileAvailable = true;
							   break;
						   }
					   }
				   }
			   }
			   
			   if(noFileAvailable)
				   break;
			   
			   lines[index] = s.toCharArray();
			   int j = 0, length = lines[index].length;
			   
			   terms[index] = new char[length];
			   
			   for(j=0; j < lines[index].length; j++)
			   {
				   if(lines[index][j] == ',')
					   break;
				   terms[index][j] = lines[index][j];
			   }
			   
			   PQueueNode node = new PQueueNode(terms[index], j, index);
			   queue.add(node);
			   
		   }
		   
		   
		   
		   
		   
		   
		   writer.flush();
		   
	   }
	   catch(Exception e)
	   {
		   System.out.print(e.getMessage());
	   }
   }*/
 
}


