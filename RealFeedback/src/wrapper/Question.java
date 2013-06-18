package wrapper;

import java.util.ArrayList;


public class Question {

	private String question_id;
	private String text;
	private Boolean active;
	private Integer startTime;
	private Boolean answered;
	public ArrayList<Answer> answers; 

	
	public static   final   int QUESTION_TEXT  		= 0;
	public static   final   int QUESTION_STATUS     = 1;
	public static   final   int QUESTION_STARTTIME  = 2;
	public static   final   int QUESTION_ID			= 3;
	public static   final   int ANSWER_TEXTS        = 4;
	public static   final   int ANSWER_VOTES        = 5;
	public static   final   int ANSWER_IDS        = 6;
	
	
	public Boolean getAnswered() {
		return answered;
	}
	public void setAnswered(Boolean answered) {
		this.answered = answered;
	}
	public Integer getStartTime() {
		return startTime;
	}
	public void setStartTime(Integer startTime) {
		this.startTime = startTime;
	}

	public Question(){
		answers = new ArrayList<Answer>();
	}
	public String getQuestionId() {
		return question_id;
	}
	public void setQuestionId(String question_id) {
		this.question_id = question_id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public String toString(){
		String res = "";
		res = res + "text:" + this.text + "\n";
		res = res + "active:" + this.active.toString() + "\n";
		res = res + "start_time:" + this.startTime.toString() + "\n";

		res = res + "question_id:" + this.question_id + "\n";
		String ids = "answerids:";
		String texts = "answertexts:";
		String votes = "answervotes:";
		
		for(int i = 0; i < answers.size();i++)
		{
			ids = ids + this.answers.get(i).getId()+",";
			texts = texts + this.answers.get(i).getText()+",";
			votes = votes + Integer.toString(this.answers.get(i).getVotes())+",";
		}
		res = res + texts + "\n";
		res = res + votes + "\n";
		res = res + ids + "\n";
		
		return res;
	}
	
	
}
