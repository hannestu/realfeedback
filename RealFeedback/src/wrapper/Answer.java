package wrapper;


/**
 * Defines an answer.
 */
public class Answer {
	private String id;
	private String text;
	private int votes;
	
	/**
	* Creates an answer.
	* @param id String
	* @param text String
	* @param votes int
	*/
	public Answer(String id,String text, int votes){
		this.id = id;
		this.text = text;
		this.votes = votes;
	}
	
	/**
	* Returns the question_id.
	* @return id String
	*/
	public String getId()
	{
		return this.id;
	}
	
	/**
	* Returns the question text.
	* @return text String
	*/
	public String getText()
	{
		return this.text;
	}
	
	/**
	* Returns the number of votes.
	* @return votes int
	*/
	public int getVotes()
	{
		return this.votes;
	}

}
