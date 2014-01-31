import java.util.Comparator;


public class PQueueNode implements Comparable<PQueueNode> {
	String word;
	int index;
	
	public PQueueNode(StringBuilder sbr, int index)
	{
		this.word = sbr.toString();
		this.index = index;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int compareTo(PQueueNode arg) {
		// TODO Auto-generated method stub
		return this.word.compareTo(arg.getWord());
	}
	
	
}
