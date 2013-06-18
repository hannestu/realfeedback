package screen.realfeedback;



import java.io.IOException;
import java.util.concurrent.TimeUnit;
import wrapper.RealFeedbackWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

/**
* Shows a textfield for entering the project code and a button to continue.
*/
public class StartActivity extends Activity {
	
	

    @Override
    /**
	* Creates StartActivity. 
	* @param savedInstanceState Bundle
	*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }
    
    
    /**
	* Checks if project code exist and continues with QuestionViewActivity.
	* And shows an error dialog otherwise. 
	* @param view View
	*/
    public void onGo(View view) 
    {   	
    	EditText projectCodeET = (EditText) findViewById(R.id.enteredProjectCode);
    	String projectCode = projectCodeET.getText().toString();
    	if (projectCode.equalsIgnoreCase(""))
    	{
    		String sorryString = "Please enter an id.";
    		showDialog(this.getString(R.string.noIdTitle),sorryString);     		
    	}
    	else
    	{    	
	    	//check project code 
	       	ValidOperation vo = new ValidOperation();
	    	try 
	    	{
				vo.execute(projectCode).get(10, TimeUnit.SECONDS);
			} 
	    	catch (Exception e) 
	    	{
	    		e.printStackTrace();
				vo.connectionProblem = true;
			} 
	    	
	    	
	    	//Error handling
	    	if (vo.connectionProblem)
	    	{
	    		showDialog(this.getString(R.string.noConnectionTitle),this.getString(R.string.noConnection));    		
	    	}
	    	else 
	    	{
	    		if (vo.valid)
	    		{
	    			Intent intent = new Intent(this, QuestionViewActivity.class);
	    			//intent.putExtra("show", "0");
	    	        startActivity(intent);     			
	    		}
	    		else
	    		{
	    			String sorryString = "There is no project with id "+projectCode+". Please try again with another id.";
		    		showDialog(this.getString(R.string.noIdTitle),sorryString);            		
	    		}    		
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
				public void onClick(DialogInterface dialog, int id) {

		           }
		       });
	
		builder.setMessage(message)
		       .setTitle(title);

		AlertDialog dialog = builder.create();
		dialog.show();		
	}
    
    
    
    /**
    * Defines an asynchronous task for checking the project code.
    * @param project_id String
    */
    private class ValidOperation extends AsyncTask<String, Void, String> 
    {
        private boolean valid = false;
        private boolean connectionProblem = true;
        private ProgressDialog dialog = null;
        
        @Override
        protected String doInBackground(String... params) 
        {
            String id = params[0];
            RealFeedbackWrapper wrapper = RealFeedbackWrapper.getInstance(StartActivity.this);
            try 
            {
                this.valid = wrapper.isValid(id);
                this.connectionProblem = false;
            }
            catch (IOException e) 
            {
                this.connectionProblem = true;
                this.valid = false;
            }              
            return null;
        }     
    
        @Override
        protected void onPostExecute(String result) 
        {
        	dialog.dismiss();                    
        }

        @Override
        protected void onPreExecute() 
        {
        	dialog = new ProgressDialog(StartActivity.this);
        	dialog.setMessage("Loading...");
        	dialog.show();        			
        }

        @Override
        protected void onProgressUpdate(Void... values) 
        {
        }     
    }    
}
