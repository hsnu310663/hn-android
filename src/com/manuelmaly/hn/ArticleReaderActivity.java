package com.manuelmaly.hn;

import java.net.URLEncoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;
import com.manuelmaly.hn.model.HNPost;
import com.manuelmaly.hn.util.FontHelper;
import com.manuelmaly.hn.model.HNFeed;

@EActivity(R.layout.article_activity)
public class ArticleReaderActivity extends Activity {
	
	private static final String WEB_VIEW_SAVED_STATE_KEY = "webViewSavedState";
	public static final String EXTRA_HNPOST = "HNPOST";
	public static final String EXTRA_HTMLPROVIDER_OVERRIDE = "HTMLPROVIDER_OVERRIDE";
	public static final String EXTRA_POSITION = "NEXT_POSITION";

    private static final String HTMLPROVIDER_PREFIX_VIEWTEXT = "http://viewtext.org/article?url=";
    private static final String HTMLPROVIDER_PREFIX_GOOGLE = "http://www.google.com/gwt/x?u=";
    private static final String HTMLPROVIDER_PREFIX_INSTAPAPER = "http://www.instapaper.com/text?u=";
    
	public static final int FEEDNUM = 30;
    
	// add by CCWang
    private static boolean translateFlag;
    private static final String HTMLTRANSLATE_PREFIX = "http://translate.google.com.tw/translate?sl=auto&tl=zh-TW&prev=_t&hl=zh-TW&ie=UTF-8&u=";
    // ..
    
	@ViewById(R.id.article_webview)
	WebView mWebView;

	@ViewById(R.id.actionbar)
	FrameLayout mActionbarContainer;

	@ViewById(R.id.actionbar_title_button)
	Button mActionbarTitle;

    @ViewById(R.id.actionbar_back)
    ImageView mActionbarBack;
    
    @ViewById(R.id.actionbar_share)
	ImageView mActionbarMore;

	@ViewById(R.id.actionbar_refresh)
	ImageView mActionbarRefresh;

	@ViewById(R.id.actionbar_refresh_container)
	LinearLayout mActionbarRefreshContainer;

	@ViewById(R.id.actionbar_refresh_progress)
	ProgressBar mActionbarRefreshProgress;

	@SystemService
	LayoutInflater mInflater;

	float up_x, down_x;

	int position;
	HNPost mPost;
	String mHtmlProvider;

	boolean mIsLoading;
	private Bundle mWebViewSavedState;
	private boolean multiF = false;

    @AfterViews
    @SuppressLint("SetJavaScriptEnabled")
    public void init() {
   
        mActionbarTitle.setTypeface(FontHelper.getComfortaa(this, true));
        mActionbarTitle.setText(getString(R.string.article));
        mActionbarTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
                Intent i = new Intent(ArticleReaderActivity.this, CommentsActivity_.class);
                i.putExtra(CommentsActivity.EXTRA_HNPOST, mPost);
                if (getIntent().getStringExtra(EXTRA_HTMLPROVIDER_OVERRIDE) != null)
                    i.putExtra(EXTRA_HTMLPROVIDER_OVERRIDE, getIntent().getStringExtra(EXTRA_HTMLPROVIDER_OVERRIDE));
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        mActionbarBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        
        mActionbarRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mWebView.getProgress() < 100 && mIsLoading) {
                    mWebView.stopLoading();
                    setIsLoading(false);
                } else {
                	setIsLoading(true);
                    mWebView.loadUrl(getArticleViewURL(mPost, mHtmlProvider, ArticleReaderActivity.this));
                }
            }
        });

        translateFlag = false;
        
		position = (int) getIntent().getIntExtra(EXTRA_POSITION, -1);
		if (position == -1) {
		}

        mPost = (HNPost) getIntent().getSerializableExtra(EXTRA_HNPOST);
        if (mPost != null && mPost.getURL() != null) {
            String htmlProviderOverride = getIntent().getStringExtra(EXTRA_HTMLPROVIDER_OVERRIDE);
            if (htmlProviderOverride != null)
                mHtmlProvider = htmlProviderOverride;
            else
                mHtmlProvider = Settings.getHtmlProvider(this);
            mWebView.loadUrl(getArticleViewURL(mPost, mHtmlProvider, this));
        }
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new HNReaderWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100 && mIsLoading) {
                	setIsLoading(false);
                } else if (!mIsLoading) {
                    // Most probably, user tapped on a link in the webview -
                    // let's spin the refresh icon:
                	setIsLoading(true);
                }
            }
        });
        if(mWebViewSavedState != null) {
            mWebView.restoreState(mWebViewSavedState);
        }
        
        setIsLoading(true);
    }
    //------------------------------------------------------------------------------------------------------------------
    protected void setIsLoading(boolean loading) {
    	mIsLoading = loading;
		mActionbarRefreshProgress.setVisibility(loading ? View.VISIBLE
				: View.GONE);
		mActionbarRefresh.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
    //------------- re330's codes -----------------
    @Click(R.id.actionbar_share)
    void moreClicked() {
        mActionbarMore.setSelected(true);
        LinearLayout moreContentView = (LinearLayout) mInflater.inflate(R.layout.article_more_content, null);
        moreContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        
        final PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.gray_comments_information)));
        popupWindow.setContentView(moreContentView);
        popupWindow.showAsDropDown(mActionbarMore);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                mActionbarMore.setSelected(false);
            }
        });

        Button favoriteButton = (Button) moreContentView.findViewById(R.id.article_more_content_favorite);
        favoriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.getInstance().addFavoritePost(position))   
                	Toast.makeText(ArticleReaderActivity.this, "Add to favorite",
						Toast.LENGTH_SHORT).show();
                else
                	Toast.makeText(ArticleReaderActivity.this, "Already exist in favorite",
    						Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
     
        Button shareButton = (Button) moreContentView.findViewById(R.id.article_more_content_share);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, mPost.getTitle());
                i.putExtra(Intent.EXTRA_TEXT, mPost.getURL());
                startActivity(Intent.createChooser(i, getString(R.string.share_article_url)));
                popupWindow.dismiss();
            }
        });
        
        //by CCWang, translate button
        Button translateButton = (Button) moreContentView.findViewById(R.id.article_more_content_translate);
        translateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	translateFlag = !translateFlag;
            	mWebView.loadUrl(getArticleViewURL(mPost, mHtmlProvider, ArticleReaderActivity.this));
                popupWindow.dismiss();
            }
        });
        //..

        popupWindow.update(moreContentView.getMeasuredWidth(), moreContentView.getMeasuredHeight());
    }
  //-----------------------------------------
    @SuppressWarnings("deprecation")
    public static String getArticleViewURL(HNPost post, String htmlProvider, Context c) {
        String encodedURL = URLEncoder.encode(post.getURL());
        if (htmlProvider.equals(c.getString(R.string.pref_htmlprovider_viewtext)))
            return HTMLPROVIDER_PREFIX_VIEWTEXT + encodedURL;
        else if (htmlProvider.equals(c.getString(R.string.pref_htmlprovider_google)))
            return HTMLPROVIDER_PREFIX_GOOGLE + encodedURL;
        else if (htmlProvider.equals(c.getString(R.string.pref_htmlprovider_instapaper)))
            return HTMLPROVIDER_PREFIX_INSTAPAPER + encodedURL;
        //modified by CCWang, add translate
        else
            return (translateFlag?HTMLTRANSLATE_PREFIX + encodedURL:post.getURL());
        //..
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack())
            mWebView.goBack();
        else
            super.onBackPressed();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Bundle webViewSavedState = new Bundle();
    	mWebView.saveState(webViewSavedState);
    	outState.putBundle(WEB_VIEW_SAVED_STATE_KEY, webViewSavedState);
    	super.onSaveInstanceState(outState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if(savedInstanceState != null) {
    		mWebViewSavedState = savedInstanceState.getBundle(WEB_VIEW_SAVED_STATE_KEY);
    	}
    }

    @Override
    protected void onDestroy() {
    	mWebView.loadData("", "text/html", "utf-8"); //Destroy any players (e.g. Youtube, Soundcloud) if any
    	//Calling mWebView.destroy(); would not always work according to here: http://stackoverflow.com/questions/6201615/how-do-i-stop-flash-after-leaving-a-webview?rq=1
    	
    	super.onDestroy();
    }

    private class HNReaderWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
  //------------- re330's codes -----------------
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// Log.d("ddd",String.valueOf(ev.getAction()));
		boolean result = onTouch(ev);
		if (!result)
			return super.dispatchTouchEvent(ev);
		return result;
	}

	public boolean onTouch(MotionEvent event) {
		float dx;

		if (event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN) {
			multiF = true;
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			down_x = event.getX();
			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (multiF) {
				multiF = false;
				return false;
			}

			up_x = event.getX();
			dx = down_x - up_x;
			Intent intent = new Intent(ArticleReaderActivity.this,
					MainActivity.class);
			if (Math.abs(dx) > 200) {
				if (dx > 0)
					position = (position + 1) % (FEEDNUM); // next page
				else
					position = (position + FEEDNUM - 1) % (FEEDNUM); // previous
																		// page

				intent.putExtra(MainActivity.ARTICAL_POSITION, position);
				setResult(RESULT_OK, intent);
				finish();
				return false;
			} else {
				return false;
			}
		}
		return false;
	}
	//------------------------------------------

}
