package org.digitalcampus.oppia.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.Survey;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.SurveySubmitTask;
import org.digitalcampus.oppia.task.UpdateProfileTask;
import org.digitalcampus.oppia.utils.MaterialSpinner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class BaseLineSurveyActivity extends AppActivity implements OnSharedPreferenceChangeListener, SubmitListener  {

	private RadioGroup surveyOptionsGroup;
	private RadioButton yes;
	private RadioButton no;
	private MaterialSpinner surveyOption;
	private Button submit;
	private DbHelper db;
	private ProgressDialog pDialog;
	private long userId;
	private SharedPreferences prefs;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_baseline_survey);
		db=new DbHelper(BaseLineSurveyActivity.this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
	    prefs.registerOnSharedPreferenceChangeListener(this);
		surveyOptionsGroup=(RadioGroup) findViewById(R.id.survey_options_group);
		yes=(RadioButton) findViewById(R.id.yes);
		no=(RadioButton) findViewById(R.id.no);
		surveyOption=(MaterialSpinner) findViewById(R.id.survey_option);
		submit=(Button) findViewById(R.id.submit_survey);
		userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		submit.setOnClickListener(new OnClickListener() {
			private Survey s;
			private RadioButton radioButton;

			@Override
			public void onClick(View v) {
				pDialog = new ProgressDialog(BaseLineSurveyActivity.this);
				//pDialog.setTitle("Submitting....");
				pDialog.setProgressStyle(android.R.style.Widget_DeviceDefault_Light_ProgressBar_Horizontal);
				//pDialog.setIndeterminate(false);
				pDialog.setTitle("Baseline Survey");
				pDialog.setMessage("Submitting....");
				
				pDialog.setCancelable(false);
				pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				      
				    } 
				});
				pDialog.show();

				ArrayList<Survey> survey = new ArrayList<Survey>();
				s = new Survey();
				radioButton=(RadioButton) findViewById(surveyOptionsGroup.getCheckedRadioButtonId());
				/*surveyOptionsGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch(checkedId){
						case R.id.yes:
							s.setQOneResponse("Yes");
						break;
						case R.id.no:
							s.setQOneResponse("No");
						break;
						}
					}
				});*/
				s.setQOneResponse(radioButton.getText().toString());
				s.setQTwoResponse(surveyOption.getSelectedItem().toString());
				s.setUserId(String.valueOf(userId));
				survey.add(s);
				Payload p = new Payload(survey);
				SurveySubmitTask rt = new SurveySubmitTask(BaseLineSurveyActivity.this);
				rt.setSurveyListener(BaseLineSurveyActivity.this);
				rt.execute(p);
				
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void submitComplete(Payload response) {
		pDialog.setTitle("Thank you!");
		//pDialog.setIndeterminate(false);
		pDialog.setMessage("Thank you for your feedback. ");
		pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();
		        finish();
				Intent i=new Intent(BaseLineSurveyActivity.this,WelcomeMenuActivity.class);
				startActivity(i);
		    } 
		});
		//pDialog.dismiss();
		
		/*pDialog.setButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
			
			}
		  });*/
		//pDialog.dismiss();
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
	}

}
