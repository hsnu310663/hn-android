package com.manuelmaly.hn;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
/*import android.preference.Preference.OnPreferenceClickListener;
*/
/*public AlertDialog.Builder setSingleChoiceItems(CharSequence[] items,int checkedItem,DialogInterface.OnClickListener listener){

}*/

public class AlertWithRadioButtonActivity extends Activity{

	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.bgmain);
	    }
	    public void showDialog(View v)
	    {
	    	final CharSequence[] items={"Red","Green","Blue"};
	    	AlertDialog.Builder builder=new AlertDialog.Builder(this);
	    	builder.setTitle("Pick a Color");
	    	builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
	    	
	     	builder.setSingleChoiceItems(items,-1, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					LinearLayout ll=(LinearLayout)findViewById(R.id.button1);
					
					if("Red".equals(items[which]))
					{
					ll.setBackgroundColor(Color.RED);
					}
					else if("Green".equals(items[which]))
					{
						ll.setBackgroundColor(Color.GREEN);
						}
					else if("Blue".equals(items[which]))
					{
						ll.setBackgroundColor(Color.BLUE);
						}
					
				}
			});
	    	builder.show();
	    
	    }
	
	
}

