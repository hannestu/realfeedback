package wrapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;


public class RealFeedbackWrapper {
	private static RealFeedbackWrapper instance = null;
	private String challenge_id, answer_id, image_url, current_user_id, question_id;
	private Double current_timestamp;
	private Project current_project;

	 
    // Database Name
    private Database db;
	
	public static RealFeedbackWrapper getInstance(Context context) {
		if (instance == null) {
			instance = new RealFeedbackWrapper(context);
		}
		return instance;
	}

	public RealFeedbackWrapper(Context context) {
		this.challenge_id = null;
		this.answer_id = null;
		this.answer_id = null;
		this.image_url = null;
		this.current_user_id = null;
		this.current_project = null;
		this.current_timestamp = 0.0;
		this.db = new Database(context);
		
		//database init, create database if not exists
	}

	public void setAnswered(String question_id) 
	{
		ArrayList<Question> questions = current_project.getQuestions();
		
		for (int i = 0; i < questions.size();i++)			
		{			
			if(questions.get(i).getQuestionId().equalsIgnoreCase(question_id))
				questions.get(i).setAnswered(true);				
				this.current_project.questions = questions;
		}		
	}

	
	public Boolean isValid(String user_id) throws IOException {
		try {

			this.current_project = this.getProject(user_id);
			this.current_user_id = user_id;
			return true;
		} catch (JSONException e) 
		{
			e.printStackTrace();
			return false;
		} catch (ProjectInvalidException e) 
		{
			e.printStackTrace();
			return false;
		}

	}

	public ArrayList<Question> getQuestions() throws ProjectInvalidException,
			IOException {
		if (this.current_project != null) {
			try {
				if (this.getProjectChanged() == true) {
					System.out.println("PROJECT CHANGED");
					this.current_project = this.getProject(this.current_user_id);

				} 
			} catch (JSONException e) {
				throw new ProjectInvalidException();
			}
			return this.current_project.getQuestions();
		} else {
			throw new ProjectInvalidException();
		}
	}

	public String sendVote(String answer_id, String question_id) throws IOException,
			ProjectInvalidException {
		String url = "https://realfeedback.tugraz.at/v1/answer/" + answer_id
				+ "/vote";
		String result = "";
		try {
			result = this.httpPutRequest(url, "");

		} catch (Exception e) {
			throw new IOException();
		}
		if (result.equals("null")) {
			throw new ProjectInvalidException();
		}
		try {
			JSONObject result_json = new JSONObject(result);
			this.challenge_id = (String) result_json.get("challenge_id");
			this.answer_id = answer_id;
			this.question_id = question_id;
			this.image_url = (String) result_json.get("image_url");
			return (String) result_json.get("image_url");
		} catch (JSONException e) {
			throw new ProjectInvalidException();
		}

	}

	public String validateVote(String x, String y) throws IOException,
	ProjectInvalidException {
		String url = "https://realfeedback.tugraz.at/v1/answer/"
				+ this.answer_id + "/vote/captcha ";

		String result = "";

		try {
			result = this.httpPutRequest(url, "x=" + x + "&y=" + y
					+ "&challenge_id=" + this.challenge_id + "&image_url="
					+ URLEncoder.encode(this.image_url,"UTF-8"));

		} catch (Exception e) {
			//System.out.println("ValidateVote Exception");
			e.printStackTrace();
			throw new IOException();
		}
		if (result.equals("null")) {

			this.db.saveQuestion(this.question_id);
			this.setAnswered(this.question_id);
			return null;
		}
		try {
			JSONObject result_json = new JSONObject(result);
			this.challenge_id = (String) result_json.get("challenge_id");
			this.image_url = (String) result_json.get("image_url");
			//save question
			//s
			return (String) result_json.get("image_url");
		} catch (JSONException e) {
			throw new ProjectInvalidException();
		}
	}

	private String getProjectID(String user_id) throws IOException,
			ProjectInvalidException, JSONException {
		
		String url = "https://realfeedback.tugraz.at/v1/project/userid/"
				+ user_id;

		String result = "";
		try {
			result = this.httpGetRequest(url);

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException();
		}
		if (result.equals("null")) {
			
			throw new ProjectInvalidException();
		}

		JSONObject result_json = new JSONObject(result);
		String id = (String) result_json.get("_id");
		return id;
	}

	private ArrayList<Question> getProjectQuestions(String project_id)
			throws IOException, JSONException {
		String url = "https://realfeedback.tugraz.at/v1/project/" + project_id
				+ "/questions";
		String result = "";
		try {
			result = this.httpGetRequest(url);

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException();
			
		}

		JSONArray questions_array = new JSONArray(result);
		String question_id, text;
		Integer start_time;
		Boolean active;
		Question q;
		ArrayList<Question> questions = new ArrayList<Question>();
		for (int i = 0; i < questions_array.length(); i++) {
			JSONObject row = questions_array.getJSONObject(i);
			//System.out.println("getProjectQuestions:  row: "+row.toString());
			q = new Question();
			question_id = (String) row.get("_id");
			text = (String) row.get("text");
			active = (Boolean) row.get("active");
			start_time = (Integer) row.get("start_time");

			q.setQuestionId(question_id);
			q.setText(text);
			q.setActive(active);
			q.setStartTime(start_time);

			q = getQuestionAnswers(question_id, q);
			questions.add(q);

			// if question was already answered
			if (this.db.doesQuestionExist(question_id)) {
				//Log.i("RealFeedBackWrapper","answered");
				q.setAnswered(true);
			} else {
				q.setAnswered(false);
			}
		}
		return questions;
	}

	private Question getQuestionAnswers(String question_id, Question q)
			throws IOException, JSONException {
		String url = "https://realfeedback.tugraz.at/v1/question/"
				+ question_id + "/answers";
		String result = "";
		try {
			result = this.httpGetRequest(url);

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException();
		}

		JSONArray answers = new JSONArray(result);
		String text, id;
		Integer votes;

		//System.out.println("  answers length: "+Integer.toString(answers.length()));
		for (int i = 0; i < answers.length(); i++) {
			JSONObject row = answers.getJSONObject(i);
			//System.out.println("getQuestionAnswers:  row: "+row.toString());

			text = (String) row.get("text");
			votes = (Integer) row.get("votes");
			id = (String) row.get("_id");
			
			//System.out.println("text: "+text+" votes: "+votes+" id: "+id);

			//for (int j = 0; j < answers.length(); j++) {
				q.answers.add(new Answer(id,text,votes));
		
			//}
				//System.out.println("anzahl answers: "+Integer.toString(answers.length()));
		}
		//System.out.println("answer size: "+Integer.toString(q.answer_texts.size()));
		return q;

	}

	// check timestamp
	private Project getProject(String user_id) throws IOException,
			ProjectInvalidException, JSONException {

		String project_id = this.getProjectID(user_id);
		ArrayList<Question> questions = this.getProjectQuestions(project_id);
		Project proj= new Project();
		proj.setProject_id(project_id);
		proj.setQuestions(questions);
		proj.setUser_id(user_id);
		return proj;
	}

	private String httpGetRequest(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

		try
		{
			if (conn.getResponseCode() != 200) 
			{	
				throw new IOException(conn.getResponseMessage());
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}

		rd.close();
		conn.disconnect();
		return sb.toString();
	}

	private String httpPutRequest(String urlStr, String params)
			throws IOException {

		URL url = new URL(urlStr);
		//System.out.println("url: "+urlStr);
		//System.out.println("params: "+params);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

		conn.setRequestMethod("PUT");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length",
				"" + Integer.toString(params.getBytes().length));
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		// conn.connect();
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(params);
		wr.flush();
		wr.close();
		
		try
		{
			if (conn.getResponseCode() != 200) 
			{	
				throw new IOException(conn.getResponseMessage());
			}
		}catch(Exception e)
		{
			//System.out.println("conn.getResponseCode()");
			//System.out.println("respnseMessage"+conn.getResponseMessage());
			e.printStackTrace();
			//throw new IOException(conn.getResponseMessage());
		}
		
			

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();

		conn.disconnect();

		return sb.toString();
	}

	private Boolean getProjectChanged() throws IOException, JSONException 
	{
		if (this.current_project == null) 
		{
			return false;
		}

		String url = "https://realfeedback.tugraz.at/v1/project/"
				+ this.current_project.project_id + "/changed";

		String result = "";
		try {
			result = this.httpGetRequest(url);

		} catch (Exception e) {
			throw new IOException();
		}
		if (result.equals("null")) {
			return false;
		}

		JSONObject result_json = new JSONObject(result);

		Double timestamp = (Double) result_json.get("timestamp");		

		if (timestamp > this.current_timestamp) {
			this.current_timestamp = timestamp;
			return true;
		}
		return false;
	}

}

	