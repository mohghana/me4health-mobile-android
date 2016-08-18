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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.adapter.DownloadMediaListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.listener.DownloadMediaListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.DownloadBroadcastReceiver;
import org.digitalcampus.oppia.service.DownloadService;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class DownloadMediaActivity extends AppActivity implements DownloadMediaListener {

	public static final String TAG = DownloadMediaActivity.class.getSimpleName();

    private SharedPreferences prefs;
    private ArrayList<Media> missingMedia;
	private DownloadMediaListAdapter dmla;
    private DownloadBroadcastReceiver receiver;
	private Activity activity;
	private Course course;

	private DbHelper db;

	private Tracker t;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_media);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        db=new DbHelper(this);
        t=new Tracker(this);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			missingMedia = (ArrayList<Media>) bundle.getSerializable(DownloadMediaActivity.TAG);
			activity = (Activity) bundle.getSerializable(Activity.TAG);
			course =(Course) bundle.getSerializable(Course.TAG);
		}
        else{
            missingMedia = new ArrayList<Media>();
        }

		dmla = new DownloadMediaListAdapter(this, missingMedia);
        dmla.setOnClickListener(new DownloadMediaListener());

        ListView listView = (ListView) findViewById(R.id.missing_media_list);
		listView.setAdapter(dmla);
		
		Button downloadViaPCBtn = (Button) this.findViewById(R.id.download_media_via_pc_btn);
		downloadViaPCBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                downloadViaPC();
            }
        });

		Editor e = prefs.edit();
		e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
		e.commit();
	}
	
	@Override
	public void onResume(){
		super.onResume();
        if ((missingMedia != null) && missingMedia.size()>0) {
            //We already have loaded media (coming from orientationchange)
            dmla.notifyDataSetChanged();
        }
        receiver = new DownloadBroadcastReceiver();
        receiver.setMediaListener(this);
        IntentFilter broadcastFilter = new IntentFilter(DownloadService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);
	}

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Media> savedMissingMedia = (ArrayList<Media>) savedInstanceState.getSerializable(TAG);
        this.missingMedia.clear();
        this.missingMedia.addAll(savedMissingMedia);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(TAG, missingMedia);
    }
	
	private void downloadViaPC(){
		String filename = "mobile-learning-media.html";
		String strData = "<html>";
		strData += "<head><title>"+this.getString(R.string.download_via_pc_title)+"</title></head>";
		strData += "<body>";
		strData += "<h3>"+this.getString(R.string.download_via_pc_title)+"</h3>";
		strData += "<p>"+this.getString(R.string.download_via_pc_intro)+"</p>";
		strData += "<ul>";
		for(Object o: missingMedia){
			Media m = (Media) o;
			strData += "<li><a href='"+m.getDownloadUrl()+"'>"+m.getFilename()+"</a></li>";
		}
		strData += "</ul>";
		strData += "</body></html>";
		strData += "<p>"+this.getString(R.string.download_via_pc_final,"/digitalcampus/media/")+"</p>";
		
		File file = new File(Environment.getExternalStorageDirectory(),filename);
		try {
			FileOutputStream f = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(new FileOutputStream(file));
			out.write(strData);
			out.close();
			f.close();
			UIUtils.showAlert(this, R.string.info, this.getString(R.string.download_via_pc_message,filename));
		} catch (FileNotFoundException e) {
			Mint.logException(e);
			e.printStackTrace();
		} catch (IOException e) {
			Mint.logException(e);
			e.printStackTrace();
		}
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
		} else if (itemId == android.R.id.home) {
			this.finish();
			return true;
		} else if (itemId == R.id.menu_settings) {
			Intent i = new Intent(this, PrefsActivity.class);
			Bundle tb = new Bundle();
			ArrayList<Lang> langs = new ArrayList<Lang>();
			Lang lang = new Lang("en","English");
			langs.add(lang);
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
				DownloadMediaActivity.this.startActivity(new Intent(DownloadMediaActivity.this, StartUpActivity.class));
				DownloadMediaActivity.this.finish();
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}
    @Override
    public void onDownloadProgress(String fileUrl, int progress) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null){
            mediaFile.setProgress(progress);
            dmla.notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadFailed(String fileUrl, String message) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            mediaFile.setDownloading(false);
            mediaFile.setProgress(0);
            dmla.notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadComplete(String fileUrl) {
        Media mediaFile = findMedia(fileUrl);
        
        if (mediaFile != null){
            Toast.makeText(this,  this.getString(R.string.download_complete), Toast.LENGTH_LONG).show();
            missingMedia.remove(mediaFile);
            dmla.notifyDataSetChanged();
            Activity a=new Activity();
			a.setActType("video_download");
			a.setCompleted(true);
			a.setDigest(mediaFile.getDigest());
			JSONObject obj=new JSONObject();
			JSONArray jsonArray = new JSONArray();
			try {
				obj.put("en", mediaFile.getFilename());
				jsonArray.put(obj);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			a.setTitlesFromJSONString(jsonArray.toString());
			ArrayList<Activity> alist=new ArrayList<Activity>();
			alist.add(a);
			db.insertActivities(alist);
			JSONObject data=new JSONObject();
			try {
				data.put("digest", mediaFile.getDigest());
				data.put("type", "video_download");
    			data.put("title",mediaFile.getFilename());
    			t.saveTracker(mediaFile.getCourse().getCourseId(), mediaFile.getDigest(), data, true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private Media findMedia(String fileUrl){
        if ( missingMedia.size()>0){
            for (Media mediaFile : missingMedia){
                if (mediaFile.getDownloadUrl().equals(fileUrl)){
                    return mediaFile;
                }
            }
        }
        return null;
    }

    private class DownloadMediaListener implements ListInnerBtnOnClickListener {
        //@Override
        public void onClick(int position) {

            if(!ConnectionUtils.isOnWifi(DownloadMediaActivity.this)){
                UIUtils.showAlert(DownloadMediaActivity.this, R.string.warning, R.string.warning_wifi_required);
                return;
            }
            Log.d("media-download", "Clicked " + position);
            Media mediaToDownload = missingMedia.get(position);

            if (!mediaToDownload.isDownloading()){
                Intent mServiceIntent = new Intent(DownloadMediaActivity.this, DownloadService.class);
                mServiceIntent.putExtra(DownloadService.SERVICE_ACTION, DownloadService.ACTION_DOWNLOAD);
                mServiceIntent.putExtra(DownloadService.SERVICE_URL, mediaToDownload.getDownloadUrl());
                mServiceIntent.putExtra(DownloadService.SERVICE_DIGEST, mediaToDownload.getDigest());
                mServiceIntent.putExtra(DownloadService.SERVICE_FILENAME, mediaToDownload.getFilename());
                DownloadMediaActivity.this.startService(mServiceIntent);

                mediaToDownload.setDownloading(true);
                mediaToDownload.setProgress(0);
                dmla.notifyDataSetChanged();
            }
            else{
                Intent mServiceIntent = new Intent(DownloadMediaActivity.this, DownloadService.class);
                mServiceIntent.putExtra(DownloadService.SERVICE_ACTION, DownloadService.ACTION_CANCEL);
                mServiceIntent.putExtra(DownloadService.SERVICE_URL, mediaToDownload.getDownloadUrl());
                DownloadMediaActivity.this.startService(mServiceIntent);

                mediaToDownload.setDownloading(false);
                mediaToDownload.setProgress(0);
                dmla.notifyDataSetChanged();
            }

        }
    }

}
