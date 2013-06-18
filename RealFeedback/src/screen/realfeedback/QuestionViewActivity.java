package screen.realfeedback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import wrapper.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


/**
* Shows all questions or otherwise "No questions available".
*/
public class QuestionViewActivity extends ListActivity {

    public static   final   int ALL         = 0;
	public static   final   int ACTIVE      = 1;
	public static   final   int UNANSWERED  = 2;
	public static   final   int ANSWERED    = 3;
	public static   final   int CLOSED      = 4;
	
	private ArrayList<Question> questions = null;
    private ArrayList<Question> currentQuestions = null;
    private ListView listview;
    private QuestionAdapter adapter;
	private int show = ALL;
	private Menu menu;
    

	
	/**
	* Creates QuestionViewActivity. 
	* @param savedInstanceState Bundle
	*/
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);  
        this.setTitle("Questions");
  

        setContentView(R.layout.activity_question);        
        listview = this.getListView();
         
        //Action bar
        ActionBar actionBar = this.getActionBar();
        //show upButton
        actionBar.setDisplayHomeAsUpEnabled(true);
        //spinner
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
        		R.array.filter, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        actionBar.setListNavigationCallbacks( spinnerAdapter,
        	    new OnNavigationListener()
        	    {
        	        @Override
        	        public boolean onNavigationItemSelected(int itemPosition,long itemId )
        	        {
        	        	switch(itemPosition)
        	        	{
        	        		case (ALL):
        	        		{
        	        			show = ALL;
        	        			break;
        	        		}
        	        		case (ACTIVE):
        	        		{
        	        			show = ACTIVE;
        	        			break;
        	        		}
        	        		case (UNANSWERED):
        	        		{
        	        			show = UNANSWERED;
        	        			break;
        	        		}
        	        		case (ANSWERED):
        	        		{
        	        			show = ANSWERED;
        	        			break;
        	        		}
        	        		case (CLOSED):
        	        		{
        	        			show = CLOSED;
        	        			break;
        	        		}
        	        	}

        	        	filterQuestions();

        	            return true;
        	        }
        	    }
        	);

        
        

        
        //update View
        updateQuestions();

        
        //adds an itemClickListener for every item in the list
        
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
            	if (currentQuestions.get(position).getActive())
            	{
            		if (currentQuestions.get(position).getAnswered())
            		{
            			showResults(view,position);
            		}
            		else
            		{
            			showOpenQuestion(view,position);
            		}
            	}
            	else
            	{
            		showResults(view,position);
            		
            	}     
            }
        });
    }
    
  
    
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	this.menu = menu;
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.action_bar, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId()) {
    	case R.id.menu_refresh:
    		//refresh
    		updateQuestions();
    		return true;
    	case android.R.id.home:
            // up
            Intent intent = new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    
    /**
   	* Shows an progress bar on refresh button in action bar.
   	* @param isRefreshing boolean
   	*/
    public void setRefreshButton(boolean isRefreshing) 
    {
        if (this.menu != null) 
        {
        	MenuItem refresh = menu.findItem(R.id.menu_refresh);
            if (refresh != null) 
            {
                if (isRefreshing) 
                {
                	//show ProgressBar
                	refresh.setActionView(R.layout.actionbar_intermediate_progress);
                } 
                else 
                {
                	refresh.setActionView(null);
                }
            }
        }
    }
    

    /**
   	* Filters the questions.
   	*/
    private void filterQuestions()
    {
    	 currentQuestions = new ArrayList<Question>();
         if (this.show != ALL)
	        {
	            for (Question q : questions)
	            {
		            switch(this.show)
		            {
		            	case(ACTIVE):
		            		if (q.getActive())
		            			currentQuestions.add(q);break;
		            	case(UNANSWERED):
		            		if (q.getAnswered() == false && q.getActive() == true)
		            			currentQuestions.add(q);break;
		            	case(ANSWERED):
		            		if (q.getAnswered() && q.getActive() == true)
		            			currentQuestions.add(q);break;	            		
		            	case(CLOSED):
		            		if (q.getActive() == false)
		            			currentQuestions.add(q);break;		            			
		            }	
	            }
         }
         else
         {
        	 //no filter
         	this.currentQuestions = questions;
         }
        
         adapter = new QuestionAdapter(this, R.layout.activity_question_row, currentQuestions);
         listview.setAdapter(adapter);     	
    }

    
    /**
	* Gets questions from wrapper. 
	*/
    private void updateQuestions()
    {
    	GetQuestionOperation gqo = new GetQuestionOperation();
    	
    	try 
    	{    		
			gqo.execute().get(10,TimeUnit.SECONDS);
    	}
    	catch (Exception e) 
    	{
    		gqo.connectionProblem = true;
		} 
    	
			
    	//Error handling
		if(gqo.connectionProblem == true)
		{
			showDialog(this.getString(R.string.noConnectionTitle),this.getString(R.string.noConnection));    					
        }
        else if(gqo.projectProblem == true)
        {
        	showDialog(this.getString(R.string.noProjectTitle),this.getString(R.string.noProject));    		
        }
        else if(gqo.task_questions != null)
        {
            questions = gqo.task_questions;  
            currentQuestions = new ArrayList<Question>();
            if (this.show != ALL)
	        {
	            for (Question q : questions)
	            {
		            switch(this.show)
		            {
		            	case(ACTIVE):
		            		if (q.getActive())
		            			currentQuestions.add(q);break;
		            	case(UNANSWERED):
		            		if (q.getAnswered() == false && q.getActive() == true)
		            			currentQuestions.add(q);break;
		            	case(ANSWERED):
		            		if (q.getAnswered() && q.getActive() == true)
		            			currentQuestions.add(q);break;	            		
		            	case(CLOSED):
		            		if (q.getActive() == false)
		            			currentQuestions.add(q);break;		            			
		            }	
	            }
            }
            else
            	this.currentQuestions = questions;
           
            adapter = new QuestionAdapter(this, R.layout.activity_question_row, currentQuestions);
            listview.setAdapter(adapter); 
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
    
    
    /**
	* Shows an open question in ShowQuestionActivity. 
	* @param view View
	* @param position integer
	*/
    public void showOpenQuestion(View view, int position) {
		Intent intent = new Intent(this, ShowQuestionActivity.class);
		String question = currentQuestions.get(position).toString();	
		intent.putExtra("question", question);
		intent.putExtra("show", Integer.toString(show));
        startActivity(intent);         
    }
    
    
    
    /**
	* Shows results in ShowResultsActivity. 
	* @param view View
	* @param position integer
	*/
    public void showResults(View view, int position) {
        Intent intent = new Intent(this, ShowResultsActivity.class);
		String question = currentQuestions.get(position).toString();	
		intent.putExtra("question", question);
		intent.putExtra("show", Integer.toString(show));
        startActivity(intent);
    }
    
    
    /**
    * Defines an Adapter for the listview for all questions with the question text and a suitable icon.
    */
    private class QuestionAdapter extends ArrayAdapter<Question> {

        private ArrayList<Question> items;

        
        public QuestionAdapter(Context context, int textViewResourceId, ArrayList<Question> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.activity_question_row, null);
                }
                Question question = items.get(position);
                if (question != null) 
                {
                	    ImageView icon = (ImageView) view.findViewById(R.id.icon);
                	    if (question.getActive())
                	    	if (question.getAnswered())
                	    		icon.setImageResource(R.drawable.ic_answered);
                	    	else
                	    		icon.setImageResource(R.drawable.ic_open);
                	    else
                	    	icon.setImageResource(R.drawable.ic_closed);
                	    
                        TextView text = (TextView) view.findViewById(R.id.questionText);
                        text.setEllipsize(TruncateAt.END);
                        text.setSingleLine();

                        if (text != null) 
                        {
                        	text.setText(question.getText());    
                        }                        
   
                }
                return view;
	      }
	}
    

    /**
     * Defines an asynchronous task for getting the questions of the project.
     */
    private class GetQuestionOperation extends AsyncTask<String, Void, String> {
        private ArrayList<Question> task_questions = null;
        private Boolean connectionProblem = false;
        private Boolean projectProblem = false;


        @Override
        protected String doInBackground(String... params) {
            RealFeedbackWrapper wrapper = RealFeedbackWrapper.getInstance(QuestionViewActivity.this);
            try {
                this.task_questions = wrapper.getQuestions();
            } catch (IOException e) {
                this.connectionProblem = true;   
            } catch (ProjectInvalidException e) {
                this.projectProblem = true;   
            }
              return null;
        }     

        @Override
        protected void onPostExecute(String result) {
        	setRefreshButton(false);
        	
                    
        }

        @Override
        protected void onPreExecute() {
        	setRefreshButton(true);
        	
        			
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}

