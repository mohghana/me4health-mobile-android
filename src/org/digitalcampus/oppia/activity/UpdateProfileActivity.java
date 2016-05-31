package org.digitalcampus.oppia.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.UpdateProfileTask;
import org.digitalcampus.oppia.utils.MaterialSpinner;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.Validation;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemSelectedListener;

public class UpdateProfileActivity extends AppActivity implements OnSharedPreferenceChangeListener, SubmitListener {

	private EditText emailField;
	private EditText firstnameField;
	private EditText lastnameField;
	private EditText phoneNoField;
	private MaterialSpinner statusField;
	private MaterialSpinner yeargroupField;
	private MaterialSpinner programField;
	private MaterialSpinner hometownField;
	private EditText schoolCodeField;
	private Button updateButton;
	private SharedPreferences prefs;
	private EditText phoneNo2Field;
	private EditText phoneNo3Field;
	private DbHelper db;
	ArrayList<User> userInfo;
	private ProgressDialog pDialog;
	private LinearLayout non_guest_field;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_update_profile);
	    db = new DbHelper(UpdateProfileActivity.this);
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        prefs.registerOnSharedPreferenceChangeListener(this);
        userInfo=new ArrayList<User>();
        userInfo=db.getUserDetails(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		emailField = (EditText) findViewById(R.id.register_form_email_field);
		phoneNo2Field = (EditText) findViewById(R.id.phone_number_two_field);
		phoneNo3Field = (EditText) findViewById(R.id.phone_number_three_field);
		firstnameField = (EditText) findViewById(R.id.register_form_firstname_field);
		lastnameField = (EditText) findViewById(R.id.register_form_lastname_field);
		phoneNoField = (EditText) findViewById(R.id.phone_number_field);
		statusField=(MaterialSpinner) findViewById(R.id.register_form_status);
		yeargroupField=(MaterialSpinner) findViewById(R.id.register_form_yeargroup);
		programField=(MaterialSpinner) findViewById(R.id.register_form_program);
		hometownField=(MaterialSpinner) findViewById(R.id.register_form_hometown);
		schoolCodeField=(EditText) findViewById(R.id.register_form_schoolcode);
		updateButton = (Button) findViewById(R.id.register_btn);
		non_guest_field=(LinearLayout) findViewById(R.id.non_guest_field);
		if(userInfo.size()>0){
			phoneNoField.setText("0"+userInfo.get(0).getUsername());
			firstnameField.setText(userInfo.get(0).getFirstname());
			lastnameField.setText(userInfo.get(0).getLastname());
			emailField.setText(userInfo.get(0).getEmail());
			phoneNo2Field.setText(userInfo.get(0).getPhoneNo2());
			phoneNo3Field.setText(userInfo.get(0).getPhoneNo3());
			schoolCodeField.setText(userInfo.get(0).getSchoolCode());
			
			
		}
		statusField.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(statusField.getSelectedItem().toString().equals("Guest")){
					non_guest_field.setVisibility(View.GONE);
				}else{
					non_guest_field.setVisibility(View.VISIBLE);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		updateButton.setOnClickListener(new OnClickListener() {
			
			private long userId;
			
			@Override
			public void onClick(View v) {
				
				userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
				String username = String.valueOf(userId); 
				String email = (String) emailField.getText().toString();
				String phoneNo2 = (String) phoneNo2Field.getText().toString();
				String phoneNo3 = (String) phoneNo3Field.getText().toString();
				String firstname = (String) firstnameField.getText().toString();
				String lastname = (String) lastnameField.getText().toString();
				String phoneNo = (String) phoneNoField.getText().toString();
				String status=(String) statusField.getSelectedItem().toString();
				String yeargroup="";
				String program = "";
				String hometown="";
				String schoolCode="";
				if(statusField.getSelectedItem().toString().equals("Guest")){
					 yeargroup=(String) "-----";
					 program=(String) "-----";
					 hometown=(String) "-----";
					 schoolCode=(String) "-----";
				}else{
					 yeargroup=(String) yeargroupField.getSelectedItem().toString();
					 program=(String) programField.getSelectedItem().toString();
					 hometown=(String) hometownField.getSelectedItem().toString();
					 schoolCode=(String) schoolCodeField.getText().toString();
				}
				
				if (email.length() == 0) {
					UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_email);
					return;
				}
				// check firstname
				if (firstname.length() < 2) {
					UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_firstname);
					return;
				}

				// check lastname
				if (lastname.length() < 2) {
					UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_lastname);
					return;
				}

				// check phone no
				if (phoneNo.length() < 8) {
					UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_phoneno);
					return;
				}
				if(!statusField.getSelectedItem().equals("Guest")){
					if(!Validation.hasSelection(yeargroupField)){
						UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_yeargroup);
					}
					if(!Validation.hasSelection(hometownField)){
						UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_hometown);
					}
					if(!Validation.hasSelection(programField)){
						UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_program);
					}
					if(!Validation.hasSelection(statusField)){
						UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,R.string.error_register_no_status);
					}
				}
				pDialog = new ProgressDialog(UpdateProfileActivity.this);
				pDialog.setTitle("Update Profile");
				pDialog.setMessage("Updating");
				pDialog.setCancelable(true);
				pDialog.show();

				ArrayList<Object> users = new ArrayList<Object>();
		    	User u = new User();
				u.setUsername(username);
				u.setPassword(userInfo.get(0).getPassword());
				u.setFirstname(firstname);
				u.setLastname(lastname);
				u.setEmail(email);
				u.setPhoneNo(phoneNo);
				u.setPhoneNo2(phoneNo2);
				u.setPhoneNo3(phoneNo3);
				u.setStatus(status);
				u.setProgram(program);
				u.setHometown(hometown);
				u.setYeargroup(yeargroup);
				u.setSchoolCode(schoolCode);
				users.add(u);
				Payload p = new Payload(users);
				UpdateProfileTask rt = new UpdateProfileTask(UpdateProfileActivity.this);
				rt.setUpdateListener(UpdateProfileActivity.this);
				rt.execute(p);
			}
		});
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
	}

	@Override
	public void submitComplete(Payload response) {
		pDialog.dismiss();
		if (response.isResult()) {
			User u = (User) response.getData().get(0);
			// set params
			System.out.print(response.getResultResponse());
			Editor editor = prefs.edit();
	    	editor.putString(PrefsActivity.PREF_USER_NAME, phoneNoField.getText().toString());
	    	editor.putString(PrefsActivity.PREF_PHONE_NO, phoneNoField.getText().toString());
	    	//editor.putString(PrefsActivity.PREF_API_KEY, u.getApiKey());
	    	//editor.putInt(PrefsActivity.PREF_POINTS, u.getPoints());
	    	//editor.putInt(PrefsActivity.PREF_BADGES, u.getBadges());
	    	//editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, u.isScoringEnabled());
	    	//editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, u.isBadgingEnabled());
	    	editor.commit();
	    	
	    	startActivity(new Intent(UpdateProfileActivity.this, WelcomeMenuActivity.class));
	    	UpdateProfileActivity.this.finish();
		} else {
			try {
				JSONObject jo = new JSONObject(response.getResultResponse());
				UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,jo.getString("error"));
			} catch (JSONException je) {
				UIUtils.showAlert(UpdateProfileActivity.this,R.string.error,response.getResultResponse());
			}
		}
		
	}

}
