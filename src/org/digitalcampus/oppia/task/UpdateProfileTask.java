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

package org.digitalcampus.oppia.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateProfileTask extends AsyncTask<Payload, Object, Payload> {

	public static final String TAG = UpdateProfileTask.class.getSimpleName();

	private Context ctx;
	private SharedPreferences prefs;
	private SubmitListener mStateListener;

	public UpdateProfileTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		User u = (User) payload.getData().get(0);
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);

		String url = prefs.getString(PrefsActivity.PREF_SERVER, ctx.getString(R.string.prefServerDefault))
				+ MobileLearning.UPDATE_PROFILE_PATH;
		Log.d(TAG,url);
		HttpPost httpPost = new HttpPost(url);
		try {
			// update progress dialog
			publishProgress("Updating");
			// add post params
			JSONObject json = new JSONObject();
			json.put("username", u.getPhoneNo());
			json.put("password", u.getPassword());
            json.put("email",u.getEmail());
            json.put("first_name",u.getFirstname());
            json.put("last_name",u.getLastname());
            json.put("phone_number",u.getPhoneNo());
            json.put("phone_number_two",u.getPhoneNo2());
            json.put("phone_number_three",u.getPhoneNo3());
            json.put("status",u.getStatus());
            json.put("year_group",u.getYeargroup());
            json.put("home_town",u.getHometown());
            json.put("program",u.getProgram());
            json.put("school_code",u.getSchoolCode());
            StringEntity se = new StringEntity(json.toString(),"utf8");
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            System.out.println(json.toString());
            httpPost.setEntity(se);

			// make request
			HttpResponse response = client.execute(httpPost);

			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
			String responseStr = "";
			String s = "";

			while ((s = buffer.readLine()) != null) {
				responseStr += s;
			}
			System.out.println(response.getStatusLine().getStatusCode());
			switch (response.getStatusLine().getStatusCode()){
				case 400: // unauthorised
					payload.setResult(false);
					payload.setResultResponse(responseStr);
					break;
				case 201: // reset complete
					JSONObject jsonResp = new JSONObject(responseStr);
					//u.setApiKey(jsonResp.getString("api_key"));
					try {
						u.setPoints(jsonResp.getInt("points"));
						u.setBadges(jsonResp.getInt("badges"));
					} catch (JSONException e){
						u.setPoints(0);
						u.setBadges(0);
					}
					try {
						u.setScoringEnabled(jsonResp.getBoolean("scoring"));
						u.setBadgingEnabled(jsonResp.getBoolean("badging"));
					} catch (JSONException e){
						u.setScoringEnabled(true);
						u.setBadgingEnabled(true);
					}
					/*try {
						JSONObject metadata = jsonResp.getJSONObject("metadata");
				        MetaDataUtils mu = new MetaDataUtils(ctx);
				        mu.saveMetaData(metadata, prefs);
					} catch (JSONException e) {
						e.printStackTrace();
					}*/
					u.setFirstname(jsonResp.getString("first_name"));
					u.setLastname(jsonResp.getString("last_name"));
					
					// add or update user in db
					DbHelper db = new DbHelper(ctx);
					db.addOrUpdateUserOnProfileUpdate(u);
					DatabaseManager.getInstance().closeDatabase();
					payload.setResult(true);
					payload.setResultResponse("Update complete!");
					break;
				default:
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_connection));
			}

		} catch (UnsupportedEncodingException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (ClientProtocolException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (JSONException e) {
			Mint.logException(e);
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_processing_response));
		}
		return payload;
	}

	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
			System.out.println(response.getResultResponse());
			if (mStateListener != null) {
				mStateListener.submitComplete(response);
			}
		}
	}

	public void setUpdateListener(SubmitListener srl) {
		synchronized (this) {
			mStateListener = srl;
		}
	}
}
