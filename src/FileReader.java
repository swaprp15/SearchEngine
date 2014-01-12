import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class FileReader {
	
	DefaultHandler handler;
	Parser parser;
	
	public FileReader()
	{
		parser = new Parser();
		
		handler = new DefaultHandler() {
			 
			boolean textTag = false;
			String textData;
		 
			public void startElement(String uri, String localName,String qName, 
		                Attributes attributes) throws SAXException {
		 
				if (qName.equalsIgnoreCase("text")) {
					textTag = true;
				}
		 
			}
		 
			public void endElement(String uri, String localName,
				String qName) throws SAXException {
				
				if(qName.equalsIgnoreCase("text")){
					if(textTag)
					{
						parser.parse(textData);
						textTag = false;
						textData = null;
					}
				}
			}
		 
			public void characters(char ch[], int start, int length) throws SAXException {
					
				if (textTag) {
					textData += new String(ch, start, length);
				}
			}
		 
		     };
	}
 
   public void readFile(String path)
   {
	   try
	   {
		   SAXParserFactory factory = SAXParserFactory.newInstance();
		   SAXParser saxParser = factory.newSAXParser();

		   saxParser.parse(path, handler);
	   }
	   catch (Exception e) 
	   {
		   e.printStackTrace();
	   }	
   }
 
}

