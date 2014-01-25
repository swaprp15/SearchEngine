
public class Indexer {

	private static String inputFile = null;
	private static String outputFolder = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 2)
		{
			return;
		}
		
		inputFile = args[0];
		outputFolder = args[1];
		
		try
		{
			long millis = System.currentTimeMillis();
			FileReaderWrapper reader = new FileReaderWrapper(outputFolder);
			reader.readFile(inputFile, outputFolder);
			//System.out.println((System.currentTimeMillis() - millis) / 1000f + " seconds -- Remove it..");
		}
		catch(Exception e)
		{
			
		}
	}

}
