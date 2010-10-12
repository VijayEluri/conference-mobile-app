package com.google.android.apps.iosched.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.webkit.WebView;

import com.google.android.apps.iosched.droidconuk2010.R;

public class AmiandoActivity extends WebViewActivity {

	protected void loadData(WebView webView) {

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		InputStream inputStream = getResources().openRawResource(R.raw.amiando);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}

		String html = byteArrayOutputStream.toString();

		
		html = String.format(html, metrics.widthPixels, metrics.heightPixels - getResources().getDimensionPixelSize(R.dimen.title_height));
		webView.loadDataWithBaseURL( "file:///android-asset/", html, "text/html", "utf-8", null );
		
	}

}
