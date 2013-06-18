package screen.realfeedback;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import wrapper.*;

/**
* Shows an image of a cat.
*/
public class CatActivity extends Activity {
	private boolean valid = false;
	private boolean error = false;
	private boolean isWorking = false;
	private String question_id = "";
	private float scaleFactor = 1.0f;

	public static   final   int CONNECTION_PROBLEM  = 0;
	public static   final   int PROJECT_PROBLEM     = 1;
	public static   final   int MAX_SCALING     = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cat);	
		
		//Action bar
        ActionBar actionBar = this.getActionBar();
        //show upButton
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        final ImageView image = (ImageView) findViewById(R.id.catImage);
		
		//ImageView image = (ImageView) findViewById(R.id.catImage);
		image.setOnTouchListener(new View.OnTouchListener() {		 
			@Override
			public boolean onTouch(View view, MotionEvent event) 
			{					
				if (MotionEvent.ACTION_DOWN == event.getAction())
				{
					if (!isWorking)
					{
						int x_coordinate = Math.round(event.getX()/scaleFactor);		
						int y_coordinate = Math.round(event.getY()/scaleFactor);
						
						vibrate();
									
						//check coordinates
						while (!valid && !error)
						{		
							isWorking = true;
							ValidateVoteOperation vvo = new ValidateVoteOperation();
							try 
							{							
								vvo.execute(Integer.toString(x_coordinate),Integer.toString(y_coordinate),question_id).get(10,TimeUnit.SECONDS);
							}
							catch (Exception e) 
							{						
								vvo.connectionProblem = true;
							} 
								
							
							//Error handling
							if(vvo.connectionProblem == true)
							{
								error = true;
								showDialog(R.string.noConnectionTitle,R.string.noConnection); 
								isWorking = false;
								return true;
				            }
				            else if(vvo.projectProblem == true)
				            {
				            	error = true;
				            	showDialog(R.string.noProjectTitle,R.string.noProject); 
				            	isWorking = false;
				            	return true;		            	
				            }
				            else if(vvo.image_url != null){
				            	//another image was returned
				            	initCat(vvo.image_url);
				            	isWorking = false;
				            	return true;
				            }
				            else if(vvo.voteCorrect == true){
				            	//vote was correct
				            	valid = true;			            	
				            	closeActivity("Your vote was successful");	
				            	isWorking = false;
				            	return true;		            	
				            }
							
						}
					}
					isWorking = false;
					return true;
				}								
				return false;	 				
		}


    });
		
	String url = getFirstURL();
	
	if (url != null)
	{
		initCat(url);
	}
	else
	{
		showDialog(R.string.noCatTitle,R.string.noCat);
	}
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
   	* Vibrates for 300 milli seconds.
   	* @param title String
   	* @param message String
   	*/
	private void vibrate()
	{
		Vibrator vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		vib.vibrate(300);	
	}
	

	/**
   	* Shows an dialog with an error message.
   	* @param title String
   	* @param message String
   	*/
    private void showDialog(int title, int message)
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
		getMenuInflater().inflate(R.menu.cat, menu);
		return true;
	}
	
	
	/**
   	* Gets the first image url.
   	* @return imageUrl String
   	*/
	private String getFirstURL()
	{
		Intent intent = this.getIntent();	
		String url = "";

	    Bundle extras = intent.getExtras();
	    if(extras == null) 
	    {
	    	url = null;
	    	question_id = null;
	    } 
	    else 
	    {
	    	url = extras.getString("image_url");
	    	question_id = extras.getString("question_id");
	    }
	    return url;		
	}
	
	
	/**
   	* Closes the actual activity and starts QuestionViewActivity.
   	* @param text String
   	*/
	private void closeActivity(String text)
	{
		Intent intent = new Intent(this, QuestionViewActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent); 

		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		
		this.finish();			
	}
	
	
	/**
   	* Computes the scale factor of the image.
   	* @param imageWidth int
   	* @param imageHeight int
   	*/
	private Matrix computeScaleFactor(int imageWidth, int imageHeight)
	{
		//get display size
		Display display = getWindowManager().getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    int width = size.x - (size.x/5);
	    int height = size.y - (size.x/5);	 
	    
	    //scale
	    float xScale = ((float) width) / imageWidth;
	    float yScale = ((float) height) / imageHeight;
	    this.scaleFactor = (xScale <= yScale) ? xScale : yScale;
	    this.scaleFactor = (this.scaleFactor <= MAX_SCALING) ? this.scaleFactor : MAX_SCALING;
	   
	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(this.scaleFactor, this.scaleFactor);
	    
	    return matrix;	  
	}
	
	
	/**
   	* Loads the image of the cat by the url and initializes the ImageView.
   	* @param url String
   	*/
	public void initCat(String url)
	{
		ImageView view = (ImageView) this.findViewById(R.id.catImage);
		GetCatOperation gco = new GetCatOperation();
		try 
		{			
			gco.execute(url).get(10,TimeUnit.SECONDS);
			if (gco.isOK)
			{
				int width = gco.image.getWidth();
				int height = gco.image.getHeight();
				
				Matrix matrix = computeScaleFactor(width,height);	
			    Bitmap scaledBitmap = Bitmap.createBitmap(gco.image, 0, 0, width, height, matrix, true);
			    view.setImageBitmap(scaledBitmap);
				}
			
		} 
		catch (Exception e) 
		{
			showDialog(R.string.noCatTitle,R.string.noCat);
		} 		
	}
	
	
	/**
    * Defines an asynchronous task for validating a vote.
    * @param x_coordinate String (integer)
    * @param y_coordinate String (integer)
    * @param question_id String
    */
	private class ValidateVoteOperation extends AsyncTask<String, Void, String> {
        String image_url = null;
        Boolean voteCorrect = false;
        Boolean connectionProblem = false;
        Boolean projectProblem = false;
        private ProgressDialog dialog = null;
        
        @Override
        protected String doInBackground(String... params) {
            String x = params[0];
            String y = params[1];
            String id = params[2];
            RealFeedbackWrapper wrapper = RealFeedbackWrapper.getInstance(CatActivity.this);
            try 
            {
                String result = wrapper.validateVote(x, y);
                if (result != null)
                {
                    this.image_url = result;
                }
                else
                {
                    this.voteCorrect = true;
                    wrapper.setAnswered(id);
                }
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
        protected void onPostExecute(String result) {
        	dialog.dismiss();
        	
                    
        }

        @Override
        protected void onPreExecute() {
        	dialog = new ProgressDialog(CatActivity.this);
        	dialog.setMessage("Validating the tap...");
        	dialog.show();
        			
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
	
	
	/**
    * Defines an asynchronous task for receiving an image of a cat from an id.
    * @param image_url String
    */
	private class GetCatOperation extends AsyncTask<String, Void, String> {
        Boolean isOK = true;
        Bitmap image = null;
        private ProgressDialog dialog = null;
        
        @Override
        protected String doInBackground(String... params) {
            String image_url = params[0];
            
            try 
    		{		
    			  image = BitmapFactory.decodeStream((InputStream)new URL(image_url).getContent());
    			  
    			  if (image == null)
    			  {
    				  isOK = false;
    				  showDialog(R.string.noCatTitle,R.string.noCat);
    			  }    			    			  
     
    		} 
    		catch (Exception e) 
    		{
    			isOK = false;
    			showDialog(R.string.noCatTitle,R.string.noCat);
    		}     		
            return null;
        }     

        @Override
        protected void onPostExecute(String result) {
        	dialog.dismiss();
        	
                    
        }

        @Override
        protected void onPreExecute() {
        	dialog = new ProgressDialog(CatActivity.this);
        	dialog.setMessage("Loading a new cat...");
        	dialog.show();
        			
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }	

}
	


