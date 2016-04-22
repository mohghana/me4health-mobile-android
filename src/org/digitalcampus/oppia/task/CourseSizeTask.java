/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

package org.digitalcampus.oppia.task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.params.CoreProtocolPNames;
import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;

import com.splunk.mint.Mint;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;


public class CourseSizeTask extends AsyncTask<String, String, String>{

	public final static String TAG = DownloadCourseTask.class.getSimpleName();
	
	private Context ctx;
	private SharedPreferences prefs;
	private String fileSize;
	private Course dm;
	private String url;
	private TextView size;
	private DownloadProgress dp;
	private HttpURLConnection c;
	
	public CourseSizeTask(Context ctx, String url,TextView size) {
		this.ctx = ctx;
		this.url=url;
		this.size=new TextView(ctx);
		this.size=size;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected String doInBackground(String... params) {
		//Payload payload = params[0];
		
		 //dm = (Course) payload.getData().get(0);
		 //dp = new DownloadProgress();
		 
		try { 
			HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);

			String newurl =  client.createUrlWithCredentials(url);
			
			Log.d(TAG,"Downloading:" + newurl);
			String v = "0";
			try {
				v = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			URL u = new URL(newurl);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestMethod("GET");
            c.setRequestProperty(CoreProtocolPNames.USER_AGENT, MobileLearning.USER_AGENT + v);
            c.setDoOutput(true);
            c.connect();
            c.setConnectTimeout(Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
					ctx.getString(R.string.prefServerTimeoutConnection))));
            c.setReadTimeout(Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_RESP,
					ctx.getString(R.string.prefServerTimeoutResponse))));
			
			int fileLength = c.getContentLength();
			
			long size=fileLength;
    		double kilobytes = 0;
    		double megabyte = 0;
    		kilobytes=(size/1024);
    		 megabyte=(kilobytes/1024);
			if(kilobytes>1000){
    			fileSize=String.format("%.2f", megabyte)+"MB";
    		}else{
    			fileSize=String.format("%.2f", kilobytes)+"KB";
    		}
			
		} catch (ClientProtocolException cpe) { 
			Mint.logException(cpe);
			//payload.setResult(false);
			//payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (SocketTimeoutException ste){
			Mint.logException(ste);
			//payload.setResult(false);
			//payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException ioe) { 
			Mint.logException(ioe);
			//payload.setResult(false);
			//payload.setResultResponse(ctx.getString(R.string.error_connection));
		}

		return fileSize;
	}
	

	@Override
	protected void onPostExecute(String results) {
		synchronized (this) {
			this.size.setText(results);
        }
	}


}
