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

import java.util.ArrayList;
import java.util.List;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.fragments.BadgesFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
import org.digitalcampus.oppia.fragments.ScorecardFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.ImageUtils;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ActionBar.Tab;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class ScorecardActivity extends FragmentActivity implements ActionBar.TabListener ,OnSharedPreferenceChangeListener{

	public static final String TAG = ScorecardActivity.class.getSimpleName();
    public static final String TAB_TARGET = "target";
    public static final String TAB_TARGET_POINTS = "tab_points";
    public static final String TAB_TARGET_BADGES = "tab_badges";

	private ActionBar actionBar;
	private ViewPager viewPager;
	private ActivityPagerAdapter apAdapter;
	private int currentTab = 0;
	private SharedPreferences prefs;
	private Course course = null;
	private ArrayList<Course> courses;

    private String targetTabOnLoad;
	private long userId;
	private DbHelper db;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_scorecard);
		db = new DbHelper(this);
		actionBar = getActionBar();
		viewPager = (ViewPager) findViewById(R.id.activity_scorecard_pager);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		courses = new ArrayList<Course>();
		courses=db.getCourses(userId);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			this.course = (Course) bundle.getSerializable(Course.TAG);
            this.targetTabOnLoad = bundle.getString(TAB_TARGET);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		actionBar.removeAllTabs();
		List<Fragment> fragments = new ArrayList<Fragment>();
		
		Fragment fScorecard;
		if(this.course != null){
			fScorecard = ScorecardFragment.newInstance(course);
            if (course.getImageFile() != null) {
                BitmapDrawable bm = ImageUtils.LoadBMPsdcard(course.getImageFileFromRoot(), this.getResources(),
                        R.drawable.ic_launcher);
                actionBar.setIcon(bm);
            }
		} else {
			fScorecard = ScorecardFragment.newInstance();
		}
		
		fragments.add(fScorecard);
		actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_scorecard)).setTabListener(this), true);
	
		boolean scoringEnabled = prefs.getBoolean(PrefsActivity.PREF_SCORING_ENABLED, true);
		if (scoringEnabled) {
			Fragment fPoints = PointsFragment.newInstance();
			fragments.add(fPoints);
			actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_points)).setTabListener(this), false);
		}
		
		boolean badgingEnabled = prefs.getBoolean(PrefsActivity.PREF_BADGING_ENABLED, true);
		if (badgingEnabled) {
			Fragment fBadges= BadgesFragment.newInstance();
			fragments.add(fBadges);
			actionBar.addTab(actionBar.newTab().setText(this.getString(R.string.tab_title_badges)).setTabListener(this), false);
		}
		
		apAdapter = new ActivityPagerAdapter(getSupportFragmentManager(), fragments);
		viewPager.setAdapter(apAdapter);

        if ( targetTabOnLoad != null){
            if (targetTabOnLoad.equals(TAB_TARGET_POINTS) && scoringEnabled) {
                currentTab = 1;
            }
            if (targetTabOnLoad.equals(TAB_TARGET_BADGES) && badgingEnabled) {
                currentTab = scoringEnabled ? 2 : 1;
            }
        }
		viewPager.setCurrentItem(currentTab);
        actionBar.setSelectedNavigationItem(currentTab);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            public void onPageSelected(int arg0) {
                actionBar.setSelectedNavigationItem(arg0);
            }

        });
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
		this.currentTab = tab.getPosition();
		
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) { }

	public void onTabReselected(Tab tab, FragmentTransaction ft) { }
	
	
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
		}  else if (itemId == R.id.menu_settings) {
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
		} else if (itemId == R.id.menu_scorecard) {
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
				ScorecardActivity.this.startActivity(new Intent(ScorecardActivity.this, StartUpActivity.class));
				ScorecardActivity.this.finish();

			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
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
