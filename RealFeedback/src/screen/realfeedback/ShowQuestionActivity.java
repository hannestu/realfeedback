package screen.realfeedback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import wrapper.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
* Shows an question to answer.
*/
public class ShowQuestionActivity extends Activity {
	private String[] answer_ids = null; 
	private String question_id = "";	
	private RadioGroup rg;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_question);
		
		//Action bar
        ActionBar actionBar = this.getActionBar();
        //show upButton
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		initQuestion();
		
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId()) 
    	{
	    	case android.R.id.home:
	            // up
	            Intent intent = new Intent(this, QuestionViewActivity.class);
	            startActivity(intent);
	            return true;
    	}
    	return false;
    }
	
		
	/**
	* Initializes a question to answer.
	*/	
 	public void initQuestion()
 	{
 		
 		TextView text = (TextView) findViewById(R.id.questionText); 
 		Intent intent = getIntent();
		String text_string = "";	
		
	    Bundle extras = intent.getExtras();
	    if(extras == null) 
	    {
	        text_string = null;
	    } else 
	    {
	    	text_string= extras.getString("question");
	    }
		
	    
	    //get values of question
		String question_text = "";
		//boolean active = true;
		//String question_id = "";
		String[] answer_texts = null;
		//String[] answer_votes = null;
		
		if (text_string != null)
		{			
			String[] string_array = text_string.split("\n");
			question_text = string_array[Question.QUESTION_TEXT].replace("text:", "");
			this.question_id = string_array[Question.QUESTION_ID].replace("question_id:", "");
			answer_texts = string_array[Question.ANSWER_TEXTS].replace("answertexts:", "").split(",");	
			this.answer_ids = string_array[Question.ANSWER_IDS].replace("answerids:", "").split(",");			
		}
		
		
		//set values
		text.setText(question_text);
		
		
		//set radio buttons
		int length = answer_texts.length;
	
		final RadioButton[] rb = new RadioButton[length];
 	    rg = (RadioGroup)findViewById(R.id.radioGroup);  	    
 	    rg.setOrientation(LinearLayout.VERTICAL); 	    
		
		for (int i = 0; i < length; i++)
		{
			rb[i]  = new RadioButton(this);
 	        rg.addView(rb[i]); 
 	        rb[i].setText(answer_texts[i]);				
		}		
 	}

    
 	/**
	* Checks if one radiobutton is selected, sends answer to the server and shows an ErrorDialog otherwise.
	* @param view View
	*/
	public void onSubmit(View view)
	{
		int checked_id = rg.getCheckedRadioButtonId();
		View radioButton = rg.findViewById(checked_id);
		int index = rg.indexOfChild(radioButton);

		if (index == -1)
		{
			showDialog(this.getString(R.string.no_radiobutton_title),this.getString(R.string.no_radiobutton_message));
		}
		else
		{
			//send vote for one question
			String answer_id = this.answer_ids[index];

			// Send vote to server.
			SendVoteOperation svo = new SendVoteOperation();
			try 
			{
				svo.execute(answer_id,this.question_id).get(10, TimeUnit.SECONDS);				
			}
			catch (Exception e) 
			{
				svo.connectionProblem = true;
			} 
			
			//Error handling
			if(svo.connectionProblem == true)
			{
				showDialog(this.getString(R.string.noConnectionTitle),this.getString(R.string.noConnection));
            }
            else if(svo.projectProblem == true)
            {
            	showDialog(this.getString(R.string.noProjectTitle),this.getString(R.string.noProject));
            }
            else if(svo.image_url != null)
            {
            	//cat Activity
    			this.finish();		
    			Intent intent = new Intent(this, CatActivity.class);
    			intent.putExtra("question_id",this.question_id);
    			intent.putExtra("image_url", svo.image_url);
    	        startActivity(intent);  
            }				
		} 
	}
	
	
	/**
   	* Shows an dialog with an error message.
   	* @param title String
   	* @param message String
   	*/
    private void showDialog(String title, String message)
	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {

		           }
		       });
	
		builder.setMessage(message)
		       .setTitle(title);

		AlertDialog dialog = builder.create();
		dialog.show();    	
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_question, menu);
		return true;
	}
	
	
	/**
    * Defines an asynchronous task for sending a vote.
    * @param answer_id String
    * @param question_id String
    */
	private class SendVoteOperation extends AsyncTask<String, Void, String> {
        private String image_url = null;
        private Boolean connectionProblem = false;
        private Boolean projectProblem = false;
        
        @Override
        protected String doInBackground(String... params) {
            String answer_id = params[0];
            String question_id = params[1];
            RealFeedbackWrapper wrapper = RealFeedbackWrapper.getInstance(ShowQuestionActivity.this);
            
            try 
            {
                this.image_url = wrapper.sendVote(answer_id,question_id);
            } 
            catch (IOException e) 
            {
                this.connectionProblem = true;   
            } 
            catch (ProjectInvalidException e) 
            {
                this.projectProblem = true;   
            }            
            return null;
        }     

        @Override
        protected void onPostExecute(String result) 
        {                       
        }

        @Override
        protected void onPreExecute() 
        {        			
        }

        @Override
        protected void onProgressUpdate(Void... values) 
        {
        }
    }
}
