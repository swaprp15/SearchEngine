
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long millis = System.currentTimeMillis();
		FileReaderWrapper reader = new FileReaderWrapper();
		//reader.readFile("sample.xml");
		reader.readFile("sample.xml");
		System.out.println((System.currentTimeMillis() - millis) / 1000f + " seconds");
	}

}
