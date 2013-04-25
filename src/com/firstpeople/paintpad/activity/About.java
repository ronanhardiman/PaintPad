package com.firstpeople.paintpad.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.firstpeople.paintpad.utils.ImageButtonTools;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 关于Activity说明了软件的一些特性
 * @author tf
 *
 */
public class About extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about);
		TextView tv = (TextView) this
				.findViewById(R.id.helpActivity_tv_default);

		tv.setText(getDataFromAssets("about.txt"));

		ImageButton quitButton = (ImageButton) findViewById(R.id.imageButtonAboutQuit);
		quitButton.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.finished));
		ImageButtonTools.setButtonFocusChanged(quitButton);
		quitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 读取assets的文件内容
	 *
	 * @param filePath
	 * @return
	 */
	public String getDataFromAssets(String filePath) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(this.getAssets()
					.open(filePath)));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

}
