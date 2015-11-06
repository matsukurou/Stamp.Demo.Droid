package com.dartslive.stamp;

import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.Toast;

import com.stamp12cm.echosdk.StampBaseView;

public class MainView extends StampBaseView {
	
	public MainView(Context context) {
		super(context);
	}

	public MainView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void onStamp(ArrayList<NameValuePair> listParams) {
		String text = "";
		for (int i=0; i<listParams.size(); i++) {
			NameValuePair pair = listParams.get(i);
			
			if (i==0)
				text = text + pair.getName() + "=" + pair.getValue();
			else
				text = text + "&" + pair.getName() + "=" + pair.getValue();
		}
		
		Toast.makeText(getContext(), "onStamp! Use s, p, c, version parameter for auth.", Toast.LENGTH_SHORT).show();
		
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		  @Override
		  public void run() {
			  stopStampingEffect(new StampEffectListener() {

				@Override
				public void finishedStampingEffect() {
					Toast.makeText(getContext(), "Stamping animation end.", Toast.LENGTH_SHORT).show();
				}
			  });
		  }
		}, 2000);
		
	}
	
	public void onError(String errorNo, String errorMsg) {
		Toast.makeText(getContext(), "["+errorNo + "]" + errorMsg, Toast.LENGTH_SHORT).show();
	}
	
	public void onLogData(String log) {
		
	}
	
	// onMessageを通じて SDK で知らせるべきメッセージが転送されます。
	// スタンプ関連の入力エラーや FRAUD チェック(不正使用チェック)に係わるメッセージが転送されます。
	public void onMessage(String text)
	{
		Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	
}
