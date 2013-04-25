package com.firstpeople.paintpad.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.firstpeople.paintpad.activity.R;
import com.firstpeople.paintpad.interfaces.EditTextDialogListener;
import com.firstpeople.paintpad.interfaces.OnClickOkListener;
import com.firstpeople.paintpad.utils.FileNameOk;
import com.firstpeople.paintpad.utils.ImageButtonTools;
import com.firstpeople.paintpad.utils.SDCardFiles;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SaveDialog extends Dialog implements
		android.view.View.OnClickListener {
	private EditTextDialogListener mListener;
	private ImageButton mOkButton;
	private ImageButton mCancelButton;
	private Context mContext;
	private TextView mTextView;
	private EditText mEditText;
	private ImageView mImageView;

	public SaveDialog(Context context, EditTextDialogListener listener) {
		super(context);
		mListener = listener;
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.savedialog);
		mOkButton = (ImageButton) findViewById(R.id.imageButtonSaveDialogOk);
		mCancelButton = (ImageButton) findViewById(R.id.imageButtonSaveDialogCancel);
		mTextView = (TextView) findViewById(R.id.textViewSaveDialogText);
		mEditText = (EditText) findViewById(R.id.editTextSaveImageName);
		mImageView = (ImageView) findViewById(R.id.imageViewSave);

		mOkButton.setBackgroundDrawable(mContext.getResources().getDrawable(
				R.drawable.finished));
		mOkButton.setOnClickListener(this);
		mCancelButton.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.cancle));
		mCancelButton.setOnClickListener(this);
		mEditText.setOnClickListener(this);
		mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		mImageView.setImageResource(R.drawable.savepicture);

		ImageButtonTools.setButtonFocusChanged(mOkButton);
		ImageButtonTools.setButtonFocusChanged(mCancelButton);

		setMessage("请输入要保存的文件名:");
		setTitle("留作纪念?");
		setEditText(getCurrentDateStr());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageButtonSaveDialogOk:
			String fileName = mEditText.getText().toString();
			File mFile = new File(fileName);
			// 如果文件名不合法，则需要重新输入
			if (!FileNameOk.isFileNameOk(mFile)) {
				Toast.makeText(mContext, "文件名不合法，请重新输入", Toast.LENGTH_LONG)
						.show();
				break;
			}
			// 如果文件名存在,则询问是否覆盖
			if (SDCardFiles.fileNameExists(fileName + ".png")) {
				OkCancleDialog dialog = new OkCancleDialog(mContext,
						new OnClickOkListener() {
							@Override
							public void onClickOk() {
								//如果点击确定，则覆盖
								mListener.getDialogText(mEditText.getText().toString());
								dismiss();
							}
						});
				dialog.show();
				dialog.setMessage("文件已存在\n是否覆盖？");
				break;
			}
			// 将合法的文件名传回
			mListener.getDialogText(fileName);
			dismiss();
			break;
		case R.id.imageButtonSaveDialogCancel:
			dismiss();
			break;
		case R.id.editTextSaveImageName:
			// 清空
			mEditText.selectAll();
		default:
			break;
		}
	}

	public void setMessage(String msg) {
		mTextView.setText(msg);
	}

	public void setEditText(String string) {
		mEditText.setText(string);
	}

	private String getCurrentDateStr() {
		Date mDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
		return formatter.format(mDate);
	}
}
