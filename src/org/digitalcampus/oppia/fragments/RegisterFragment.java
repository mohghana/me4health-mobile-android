/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.fragments;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.activity.BaseLineSurveyActivity;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.OppiaMobileGroupActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.WelcomeMenuActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterTask;
import org.digitalcampus.oppia.utils.MaterialSpinner;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.Validation;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;

public class RegisterFragment extends Fragment implements SubmitListener {


	public static final String TAG = RegisterFragment.class.getSimpleName();
	private SharedPreferences prefs;
	private EditText usernameField;
	private EditText emailField;
	private EditText passwordField;
	private EditText passwordAgainField;
	private EditText firstnameField;
	private EditText lastnameField;
	private EditText jobTitleField;
	private EditText organisationField;
	private EditText phoneNoField;
	private Button registerButton;
	private ProgressDialog pDialog;
	private MaterialSpinner statusField;
	private MaterialSpinner yeargroupField;
	private MaterialSpinner programField;
	private MaterialSpinner hometownField;
	private EditText schoolCodeField;
	private LinearLayout non_guest_field;
	private LinearLayout yeargroup_field;
	private LinearLayout schoolcode_field;
	private LinearLayout program_field;
	private LinearLayout job_org_field;
	private LinearLayout region_field;
	private MaterialSpinner genderField;
	
	public static RegisterFragment newInstance() {
		RegisterFragment myFragment = new RegisterFragment();
	    return myFragment;
	}

	public RegisterFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_register, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		usernameField = (EditText) super.getActivity().findViewById(R.id.register_form_username_field);
		emailField = (EditText) super.getActivity().findViewById(R.id.register_form_email_field);
		passwordField = (EditText) super.getActivity().findViewById(R.id.register_form_password_field);
		passwordAgainField = (EditText) super.getActivity().findViewById(R.id.register_form_password_again_field);
		firstnameField = (EditText) super.getActivity().findViewById(R.id.register_form_firstname_field);
		lastnameField = (EditText) super.getActivity().findViewById(R.id.register_form_lastname_field);
		jobTitleField = (EditText) super.getActivity().findViewById(R.id.register_form_jobtitle_field);
		organisationField = (EditText) super.getActivity().findViewById(R.id.register_form_organisation_field);
		phoneNoField = (EditText) super.getActivity().findViewById(R.id.register_form_phoneno_field);
		statusField=(MaterialSpinner) super.getActivity().findViewById(R.id.register_form_status);
		genderField=(MaterialSpinner) super.getActivity().findViewById(R.id.register_form_gender_field);
		yeargroupField=(MaterialSpinner) super.getActivity().findViewById(R.id.register_form_yeargroup);
		programField=(MaterialSpinner) super.getActivity().findViewById(R.id.register_form_program);
		hometownField=(MaterialSpinner) super.getActivity().findViewById(R.id.register_form_hometown);
		schoolCodeField=(EditText) super.getActivity().findViewById(R.id.register_form_schoolcode);
		registerButton = (Button) super.getActivity().findViewById(R.id.register_btn);
		non_guest_field=(LinearLayout) super.getActivity().findViewById(R.id.non_guest_field);
		yeargroup_field=(LinearLayout) super.getActivity().findViewById(R.id.linearlayout_yeargroup);
		schoolcode_field=(LinearLayout) super.getActivity().findViewById(R.id.linearlayout_schoolcode);
		program_field=(LinearLayout) super.getActivity().findViewById(R.id.linearlayout_program);
		job_org_field=(LinearLayout) super.getActivity().findViewById(R.id.linearlayout_job_org);
		region_field=(LinearLayout) super.getActivity().findViewById(R.id.linearlayout_region);
		registerButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				onRegisterClick(v);
			}
		});
		statusField.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(statusField.getSelectedItem().toString().equals("Guest")){
					yeargroup_field.setVisibility(View.GONE);
					schoolcode_field.setVisibility(View.GONE);
					region_field.setVisibility(View.VISIBLE);
					program_field.setVisibility(View.GONE);
				}else if(statusField.getSelectedItem().toString().equals("Tutor")){
					region_field.setVisibility(View.GONE);
					yeargroup_field.setVisibility(View.GONE);
					schoolcode_field.setVisibility(View.VISIBLE);
					program_field.setVisibility(View.GONE);
				}else if(statusField.getSelectedItem().toString().equals("Student")){
					region_field.setVisibility(View.GONE);
					yeargroup_field.setVisibility(View.VISIBLE);
					schoolcode_field.setVisibility(View.VISIBLE);
					program_field.setVisibility(View.VISIBLE);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public void submitComplete(Payload response) {
		pDialog.dismiss();
		if (response.isResult()) {
			User u = (User) response.getData().get(0);
			// set params
			Editor editor = prefs.edit();
	    	editor.putString(PrefsActivity.PREF_USER_NAME, phoneNoField.getText().toString());
	    	editor.putString(PrefsActivity.PREF_PHONE_NO, phoneNoField.getText().toString());
	    	editor.putString(PrefsActivity.PREF_API_KEY, u.getApiKey());
	    	editor.putInt(PrefsActivity.PREF_POINTS, u.getPoints());
	    	editor.putInt(PrefsActivity.PREF_BADGES, u.getBadges());
	    	editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, u.isScoringEnabled());
	    	editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, u.isBadgingEnabled());
	    	editor.commit();
	    	if(u.getStatus().equals("Guest")){
	    		startActivity(new Intent(super.getActivity(), WelcomeMenuActivity.class));
	    		super.getActivity().finish();
	    	}else{
	    		startActivity(new Intent(super.getActivity(), BaseLineSurveyActivity.class));
	    		super.getActivity().finish();
	    	}
	    	
		} else {
			try {
				JSONObject jo = new JSONObject(response.getResultResponse());
				UIUtils.showAlert(super.getActivity(),R.string.error,jo.getString("error"));
			} catch (JSONException je) {
				UIUtils.showAlert(super.getActivity(),R.string.error,response.getResultResponse());
			}
		}
	}

	public void onRegisterClick(View view) {
		// get form fields
		String username = (String) usernameField.getText().toString().trim();
		String email = (String) emailField.getText().toString();
		String password = (String) passwordField.getText().toString();
		String passwordAgain = (String) passwordAgainField.getText().toString();
		String firstname = (String) firstnameField.getText().toString();
		String lastname = (String) lastnameField.getText().toString();
		String phoneNo = (String) phoneNoField.getText().toString();
		String jobTitle = (String) jobTitleField.getText().toString();
		String organisation = (String) organisationField.getText().toString();
		String status=(String) statusField.getSelectedItem().toString();
		String gender=(String) genderField.getSelectedItem().toString();
		
		String yeargroup="";
		String program = "";
		String hometown="";
		String schoolCode="";
		if(statusField.getSelectedItem().toString().equals("Guest")){
			 yeargroup=(String) "-----";
			 program=(String) "-----";
			 hometown=(String) hometownField.getSelectedItem().toString();
			 schoolCode=(String) "-----";
		}else if(statusField.getSelectedItem().toString().equals("Tutor")){
			 yeargroup=(String) "-----";
			 program=(String) "-----";
			 hometown=(String) "-----";
			 schoolCode=(String) schoolCodeField.getText().toString();
			 schoolCode.toUpperCase(Locale.ENGLISH);
		}else{
			 yeargroup=(String) yeargroupField.getSelectedItem().toString();
			 program=(String) programField.getSelectedItem().toString();
			 hometown=(String) "-----";
			 schoolCode=(String) schoolCodeField.getText().toString();
			 schoolCode.toUpperCase(Locale.ENGLISH);
		}
		// do validation
		// check username
		/*if (username.length() == 0) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_username);
			return;
		}*/
		if(statusField.getSelectedItem().equals("")){
			UIUtils.showAlert(super.getActivity(), R.string.error, "Please select a status to proceed");
		}
		
		if (username.contains(" ")) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_username_spaces);
			return;
		}
		
		// TODO check valid email address format
		// android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
		if (email.length() == 0) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_email);
			return;
		}
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			UIUtils.showAlert(super.getActivity(),R.string.error,"Enter a valid email address");
			return;
		}
		
		// check password length
		if (password.length() < MobileLearning.PASSWORD_MIN_LENGTH) {
			UIUtils.showAlert(super.getActivity(),R.string.error,getString(R.string.error_register_password,  MobileLearning.PASSWORD_MIN_LENGTH ));
			return;
		}
		
		// check password match
		if (!password.equals(passwordAgain)) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_password_no_match);
			return;
		}
		
		// check firstname
		if (firstname.length() < 2) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_firstname);
			return;
		}

		// check lastname
		if (lastname.length() < 2) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_lastname);
			return;
		}
		if(genderField.getSelectedItem().equals("")){
			UIUtils.showAlert(super.getActivity(),R.string.error,"Select a gender");
		}
		// check phone no
		if (phoneNo.length() < 8) {
			UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_phoneno);
			return;
		}
		
		if(statusField.getSelectedItem().equals("")){
			UIUtils.showAlert(super.getActivity(), R.string.error, "Please select a status to proceed");
		}
		if(statusField.getSelectedItem().equals("Guest")){
			
			if(hometownField.getSelectedItem().equals("")){
				UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_hometown);
			}
			if(statusField.getSelectedItem().equals("")){
				UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_status);
			}
		}
		if(statusField.getSelectedItem().equals("Student")){
			if(yeargroupField.getSelectedItem().equals("")){
				UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_yeargroup);
			}
			
			if(programField.getSelectedItem().equals("")){
				UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_program);
			}
			if(statusField.getSelectedItem().equals("")){
				UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_status);
			}
		}
		if(statusField.getSelectedItem().equals("Tutor")){
			/*if(yeargroupField.getSelectedItem().equals("")){
				UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_yeargroup);
			}*/
			
			if(statusField.getSelectedItem().equals("")){
				UIUtils.showAlert(super.getActivity(),R.string.error,R.string.error_register_no_status);
			}
		}
		
		pDialog = new ProgressDialog(super.getActivity());
		pDialog.setTitle(R.string.register_alert_title);
		pDialog.setMessage(getString(R.string.register_process));
		pDialog.setCancelable(true);
		pDialog.show();

		ArrayList<Object> users = new ArrayList<Object>();
    	User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		u.setPasswordAgain(passwordAgain);
		u.setFirstname(firstname);
		u.setLastname(lastname);
		u.setEmail(email);
		u.setGender(gender);
		u.setJobTitle(jobTitle);
		u.setOrganisation(organisation);
		u.setPhoneNo(phoneNo);
		u.setStatus(status);
		u.setProgram(program);
		u.setHometown(hometown);
		u.setYeargroup(yeargroup);
		u.setSchoolCode(schoolCode);
		u.setSurveyStatus("");
		users.add(u);
		Payload p = new Payload(users);
		RegisterTask rt = new RegisterTask(super.getActivity());
		rt.setRegisterListener(this);
		rt.execute(p);
	}
}
