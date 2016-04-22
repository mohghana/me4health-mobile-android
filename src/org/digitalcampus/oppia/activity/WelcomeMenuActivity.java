package org.digitalcampus.oppia.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WelcomeMenuActivity extends AppActivity implements OnSharedPreferenceChangeListener{
	public static final String TAG = WelcomeMenuActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private TextView welcomeText;
	private TextView pointsText;
	private DbHelper db;
	private LinearLayout coursesMenu;
	private LinearLayout achievementMenu;
	private ArrayList<Course> courses;
	private long userId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_home_new);
	    db = new DbHelper(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		welcomeText=(TextView) findViewById(R.id.welcome_text);
		pointsText=(TextView) findViewById(R.id.points);
		welcomeText.setText("Good "+db.getTime()+", "+db.getUserFirstName(prefs.getString(PrefsActivity.PREF_USER_NAME, "")));
		pointsText.setText(String.valueOf(prefs.getInt(PrefsActivity.PREF_POINTS, 0))+"\n points");
		coursesMenu=(LinearLayout) findViewById(R.id.courses_menu);
		achievementMenu=(LinearLayout) findViewById(R.id.achievements_menu);
		userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		courses = new ArrayList<Course>();
		courses=db.getCourses(userId);
		coursesMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WelcomeMenuActivity.this, OppiaMobileGroupActivity.class));
			}
		});
		
		achievementMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WelcomeMenuActivity.this, ScorecardActivity.class));
				
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		UIUtils.showUserData(menu,this, null);
		MenuItem item = menu.findItem(R.id.menu_logout);
		item.setVisible(prefs.getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, true));
	    return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Log.d(TAG,"selected:" + item.getItemId());
		int itemId = item.getItemId();
		if (itemId == R.id.menu_about) {
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		} else if (itemId == R.id.menu_download) {
			startActivity(new Intent(this, TagSelectActivity.class));
			return true;
		} else if (itemId == R.id.menu_settings) {
			Intent i = new Intent(this, PrefsActivity.class);
			Bundle tb = new Bundle();
			ArrayList<Lang> langs = new ArrayList<Lang>();
			for(Course m: courses){
				langs.addAll(m.getLangs());
			}
			tb.putSerializable("langs", langs);
			i.putExtras(tb);
			startActivity(i);
			return true;
		} else if (itemId == R.id.menu_monitor) {
			startActivity(new Intent(this, MonitorActivity.class));
			return true;
		} else if (itemId == R.id.menu_update) {
			startActivity(new Intent(this, UpdateProfileActivity.class));
			return true;
		}else if (itemId == R.id.menu_scorecard) {
			startActivity(new Intent(this, ScorecardActivity.class));
			return true;
		} else if (itemId == R.id.menu_search) {
			startActivity(new Intent(this, SearchActivity.class));
			return true;
		} else if (itemId == R.id.menu_logout) {
			logout();
			return true;
		}else if (itemId == R.id.menu_userguide) {
			startActivity(new Intent(this, UserGuideActivity.class));
			return true;
		}
		return true;
	}
	
	

	private void logout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.logout);
		builder.setMessage(R.string.logout_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// wipe user prefs
				Editor editor = prefs.edit();
				editor.putString(PrefsActivity.PREF_USER_NAME, "");
				editor.putString(PrefsActivity.PREF_API_KEY, "");
				editor.putInt(PrefsActivity.PREF_BADGES, 0);
				editor.putInt(PrefsActivity.PREF_POINTS, 0);
				editor.commit();

				// restart the app
				WelcomeMenuActivity.this.startActivity(new Intent(WelcomeMenuActivity.this, StartUpActivity.class));
				WelcomeMenuActivity.this.finish();

			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}
public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		if(key.equalsIgnoreCase(PrefsActivity.PREF_SERVER)){
			Editor editor = sharedPreferences.edit();
			if(!sharedPreferences.getString(PrefsActivity.PREF_SERVER, "").endsWith("/")){
				String newServer = sharedPreferences.getString(PrefsActivity.PREF_SERVER, "").trim()+"/";
				editor.putString(PrefsActivity.PREF_SERVER, newServer);
		    	editor.commit();
			}
		}
		
		if(key.equalsIgnoreCase(PrefsActivity.PREF_POINTS)
				|| key.equalsIgnoreCase(PrefsActivity.PREF_BADGES)){
			supportInvalidateOptionsMenu();
		}

		if(key.equalsIgnoreCase(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED)){
			boolean newPref = sharedPreferences.getBoolean(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED, false);
			Log.d(TAG, "PREF_DOWNLOAD_VIA_CELLULAR_ENABLED" + newPref);
		}
	}
}
