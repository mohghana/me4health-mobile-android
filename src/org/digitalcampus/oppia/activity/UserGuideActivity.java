package org.digitalcampus.oppia.activity;

import java.util.Locale;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;

public class UserGuideActivity extends AppActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_webview);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		String url = FileUtils.getLocalizedFilePath(this,lang, "userguide.html");
		WebView wv = (WebView) findViewById(R.id.fragment_webview);
		wv.loadUrl(url);
		
	}
}
