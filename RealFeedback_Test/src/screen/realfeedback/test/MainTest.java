package screen.realfeedback.test;

import screen.realfeedback.QuestionViewActivity;
import screen.realfeedback.StartActivity;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

//import com.example.realfeedback.*;
//import wrapper.*;
//import junit.framework.TestCase;

public class MainTest extends ActivityInstrumentationTestCase2<StartActivity> {
	private Solo solo;
	
	public MainTest(){
		super(StartActivity.class);
	}
	
	protected void setUp() throws Exception{
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	public void testButton() {
		solo.clickOnButton("Go");
		solo.goBackToActivity("StartActivity");
	}
	
	public void testTextviewFalseInput() {
		solo.enterText(0, "0");
		solo.clickOnButton("Go");
		solo.getText("There is no project with id 0. Please try again with an other id.");
		solo.goBackToActivity("StartActivity");
	}
	
	public void testTextviewCorrectInput() {
		solo.enterText(0, "57888");
		solo.clickOnButton("Go");
		solo.assertCurrentActivity("Expected to be in Question view Activity.", QuestionViewActivity.class);
		solo.goBackToActivity("StartActivity");
	}

	protected void tearDown() throws Exception{
		super.tearDown();
	}

}

