package screen.realfeedback;

import wrapper.Question;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShowResultsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_results);
		
		//Action bar
        ActionBar actionBar = this.getActionBar();
        //show upButton
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		initQuestion();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_results, menu);
		return true;
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
	* Initializes an question with results.
	*/	
 	public void initQuestion()
 	{ 		
 		TextView text = (TextView) findViewById(R.id.questionText);
 		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutResults);
	
 		Intent intent = getIntent();
		String text_string = "";	

	    Bundle extras = intent.getExtras();
	    if(extras == null) 
	    {
	        text_string = null;
	    } 
	    else 
	    {
	    	text_string= extras.getString("question");
	    }
		
	    //get values of question
		String question_text = "";
		boolean active = true;
		String[] answers = null;
		String[] votes_string = null;		
		int length = 0;
		int totalVotes = 0;
		int[] votes = null;
		
		if (text_string != null)
		{
			String[] string_array = text_string.split("\n");
			question_text = string_array[Question.QUESTION_TEXT].replace("text:", "");
			active = Boolean.parseBoolean(string_array[Question.QUESTION_STATUS].replace("active:", ""));
			answers = string_array[Question.ANSWER_TEXTS].replace("answertexts:", "").split(",");
			length = answers.length;
			votes_string = string_array[Question.ANSWER_VOTES].replace("answervotes:", "").split(",");
			votes = new int[length];
		}
		
     	//init votes
		int max = 0;
		for (int i = 0; i < length; i++)
		{
			votes[i] = Integer.parseInt(votes_string[i]);
			totalVotes = totalVotes + votes[i];
			if (votes[i]>max)
			{
				max = votes[i];
			}			
		}
		
		//set values
		text.setText(question_text);
		text.setTextSize(16);
		if (active)
		{
			this.setTitle("Actual Results");
			text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_answered,0, 0, 0);
		}
		else
		{
			text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_closed,0, 0, 0);
		}
		
		
		for (int i = 0; i < length; i++ )
		{
			TextView answertext = new TextView(this);
			answertext.setText(answers[i]);
			answertext.setTextSize(16);
			TextView percentage = new TextView(this);
			int percent = 0;
			if (totalVotes != 0)
			{
				percent = Math.round((Integer) (votes[i]*100)/totalVotes);
				
			}			 
			percentage.setText(Integer.toString(votes[i])+"/"+Integer.toString(totalVotes)+" ("+Integer.toString(percent)+"%)");
			percentage.setTextColor(Color.parseColor("#33B5E5"));
			percentage.setTextSize(16);
			percentage.setPadding(0, 0, 0, 20);
			ProgressBar bar = new ProgressBar(this,
                    null, 
                    android.R.attr.progressBarStyleHorizontal);
			bar.setMax(totalVotes);
			bar.setProgress(votes[i]);	
		
			layout.addView(answertext);
			layout.addView(bar);
			layout.addView(percentage);			
		}
 	}
}
