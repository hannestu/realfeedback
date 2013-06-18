package wrapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	 private static final String MYDATABASE = "db_realfeedback_questions";
	 private static final int VERSION = 1;
	 
	 
	 
	 public Database(Context connection) {
	  super(connection, MYDATABASE, null, VERSION);
	 }
	 
	 @Override
	 public void onCreate(SQLiteDatabase db) {
	  db.execSQL("CREATE TABLE IF NOT EXISTS questions(question String PRIMARY KEY);");
	  
	 }
	 
	 @Override
	 public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
	  
	 }
	 
	 public Boolean doesQuestionExist(String question){
		 //System.out.println(question);
		 SQLiteDatabase db = this.getWritableDatabase();
		 String query = "SELECT COUNT(*) FROM questions WHERE question='"+question+"'";
		 Cursor mCount= db.rawQuery(query, null);
		 mCount.moveToFirst();
		 int count= mCount.getInt(0);
		 //System.out.println(count);
		 mCount.close();
		 if (count > 0) return true;
		 return false;
	 }
	 
	 public void saveQuestion(String question){
		 SQLiteDatabase db = this.getWritableDatabase();
		 if(this.doesQuestionExist(question)) return;
		 
		 ContentValues values = new ContentValues();
		 values.put("question", question);
		 db.insert("questions", null, values);
		 db.close();
	 }
	}