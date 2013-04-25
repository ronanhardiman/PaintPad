package com.firstpeople.paintpad.activity;

import com.firstpeople.paintpad.utils.ImageButtonTools;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * ����Activity����Ҫ��ʾ�˱�����İ����ĵ����������˳���ť
 * @author tf
 *
 */
public class Help extends Activity implements OnClickListener{
	private ImageButton backButton = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		backButton = (ImageButton)findViewById(R.id.helpReturnImageButton);
		backButton.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.finished));
		ImageButtonTools.setButtonFocusChanged(backButton);
		backButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.helpReturnImageButton:
			this.finish();
			break;
		default:
			break;
		}
	}
}
