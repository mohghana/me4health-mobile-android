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

package org.digitalcampus.oppia.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.PostInstallListener;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.PhoneIMEISubmitTask;
import org.digitalcampus.oppia.task.PostInstallTask;
import org.digitalcampus.oppia.task.UpgradeManagerTask;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class StartUpActivity extends Activity implements UpgradeListener, PostInstallListener, InstallCourseListener, SubmitListener{

	public final static String TAG = StartUpActivity.class.getSimpleName();
	private TextView tvProgress;
	private SharedPreferences prefs;
	private DbHelper db;
	private ArrayList<User> userInfo;
	private static int SPLASH_TIME_OUT = 3000;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.disableNetworkMonitoring();
        Mint.initAndStartSession(this, MobileLearning.MINT_API_KEY);
        setContentView(R.layout.start_up);
        tvProgress = (TextView) this.findViewById(R.id.start_up_progress);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Mint.setUserIdentifier(prefs.getString(PrefsActivity.PREF_USER_NAME, "anon"));
        db=new DbHelper(StartUpActivity.this);
   	 	userInfo=new ArrayList<User>();
   	 	userInfo=db.getUserDetails(prefs.getString(PrefsActivity.PREF_USER_NAME, "anon"));
   	 	//System.out.println(userInfo.get(0).getUsername().toString());
        UpgradeManagerTask umt = new UpgradeManagerTask(this);
		umt.setUpgradeListener(this);
		ArrayList<Object> data = new ArrayList<Object>();
 		Payload p = new Payload(data);
		umt.execute(p);
		try{
		PhoneIMEISubmitTask pit=new PhoneIMEISubmitTask(this);
		pit.setImeiSubmitListener(this);
		pit.execute(p);
		}catch(Exception e){
			e.printStackTrace();
		}
	
 		
	}
    private void updateProgress(String text){
    	if(tvProgress != null){
    		tvProgress.setText(text);
    	}
    }
	
	private void endStartUpScreen() {
        // launch new activity and close splash screen
		new Handler().postDelayed(new Runnable() {
			 
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
 
            @Override
            public void run() {
            	 
                // This method will be executed once the timer is over
                // Start your app main activity
            	if (!MobileLearning.isLoggedIn(StartUpActivity.this)) {
        			startActivity(new Intent(StartUpActivity.this, WelcomeActivity.class));
        			finish();
            	} else if(userInfo.get(0).getSurveyStatus().toString().equals("taken")){
        			startActivity(new Intent(StartUpActivity.this, WelcomeMenuActivity.class));
        			finish();
            	}else if(userInfo.get(0).getSurveyStatus().toString()!=null&&!userInfo.get(0).getSurveyStatus().equals("taken")&&userInfo.get(0).getStatus().equals("Guest")){
            		startActivity(new Intent(StartUpActivity.this, WelcomeMenuActivity.class));
            		finish();
          
        		}else if(userInfo.get(0).getSurveyStatus().toString()!=null&&!userInfo.get(0).getSurveyStatus().equals("taken")&&!userInfo.get(0).getStatus().equals("Guest")){
        			startActivity(new Intent(StartUpActivity.this, BaseLineSurveyActivity.class));
        			finish();
        		}else{
        			startActivity(new Intent(StartUpActivity.this, WelcomeActivity.class));
        			finish();
        		}
 
              
            }
        }, SPLASH_TIME_OUT);
		
    }

	private void installCourses(){
		File dir = new File(FileUtils.getDownloadPath(this));
		String[] children = dir.list();
		if (children != null) {
			ArrayList<Object> data = new ArrayList<Object>();
     		Payload payload = new Payload(data);
			InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(this);
			imTask.setInstallerListener(this);
			imTask.execute(payload);
		} else {
			endStartUpScreen();
		}
	}
	
	public void upgradeComplete(Payload p) {
		
		 // set up local dirs
		
 		if(!FileUtils.createDirs(this)){
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 			builder.setTitle(R.string.error);
 			builder.setMessage(R.string.error_sdcard);
 			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					StartUpActivity.this.finish();
 				}
 			});
 			builder.show();
 			return;
 		}
 		
		if(p.isResult()){
			Payload payload = new Payload();
			PostInstallTask piTask = new PostInstallTask(this);
			piTask.setPostInstallListener(this);
			piTask.execute(payload);
		} else {
			// now install any new courses
			this.installCourses();
		}
		
	}

	public void upgradeProgressUpdate(String s) {
		this.updateProgress(s);
	}

	public void postInstallComplete(Payload response) {
		this.installCourses();
	}

	public void downloadComplete(Payload p) {
		// do nothing
		
	}

	public void downloadProgressUpdate(DownloadProgress dp) {
		// do nothing
		
	}

	public void installComplete(Payload p) {
		if(p.getResponseData().size()>0){
			Editor e = prefs.edit();
			e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
			e.commit();
		}
		endStartUpScreen();	
	}

	public void installProgressUpdate(DownloadProgress dp) {
		this.updateProgress(dp.getMessage());
	}
	@Override
	public void submitComplete(Payload response) {
		updateProgress("IMEI added");
		
	}
}
