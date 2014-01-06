package com.manuelmaly.hn;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;
import com.manuelmaly.hn.model.HNFeed;
import com.manuelmaly.hn.model.HNPost;
import com.manuelmaly.hn.parser.BaseHTMLParser;
import com.manuelmaly.hn.parser.HNFeedParser;
import com.manuelmaly.hn.server.HNCredentials;
import com.manuelmaly.hn.task.HNFeedTaskLoadMore;
import com.manuelmaly.hn.task.HNFeedTaskMainFeed;
import com.manuelmaly.hn.task.HNSearch;
import com.manuelmaly.hn.task.HNVoteTask;
import com.manuelmaly.hn.task.ITaskFinishedHandler;
import com.manuelmaly.hn.util.FileUtil;
import com.manuelmaly.hn.util.FontHelper;
import com.navdrawer.SimpleSideDrawer;

@EActivity(R.layout.main)
//102522064 darkmoreTw
//102522055 re330
public class MainActivity extends BaseListActivity implements ITaskFinishedHandler<HNFeed> {
	private SharedPreferences fFeed=null;
	private JSONArray postJsonArray=null;
	float drag_Sx = 0, drag_Sy = 0;
	float drag_Ex = 0, drag_Ey = 0;
	boolean startSlide = false;
	boolean longClick = false;

    @ViewById(R.id.main_list)
    ListView mPostsList;

    @ViewById(R.id.main_root)
    LinearLayout mRootView;

    @ViewById(R.id.actionbar_title)
    TextView mActionbarTitle;

    @ViewById(R.id.actionbar_refresh)
    ImageView mActionbarRefresh;
    
    @ViewById(R.id.actionbar_refresh_container)
    LinearLayout mActionbarRefreshContainer;
    
    @ViewById(R.id.actionbar_refresh_progress)
    ProgressBar mActionbarRefreshProgress;

    @ViewById(R.id.actionbar_more)
    ImageView mActionbarMore;
    
    @SystemService
    LayoutInflater mInflater;
    
	
	TextView orderByTime;
	TextView orderByReader;
	TextView orderByComment;
	TextView mSettings;
	TextView mAbout;
	TextView mFavorite;
	
    //------------- new add -----------------
    @ViewById(R.id.main_search)
    Button main_search;
    
    @ViewById(R.id.main_search_text)
    TextView main_search_text;
    
    @ViewById(R.id.Magnifier)
    ImageView Magnifier;
    
    boolean turn =false;
    public static HNFeed favoritePosts;
    HNSearch search;
    
    private HandlerThread mThread;
    //---------------------------------------
    

    TextView mEmptyListPlaceholder;
    HNFeed mFeed;
    PostsAdapter mPostsListAdapter;
    HashSet<HNPost> mUpvotedPosts;

    String mCurrentFontSize = null;
    String mCurrentHTMLContent = null;
    int mFontSizeTitle;
    int mFontSizeDetails;
    
    // Ramesh kumar coding part for change background color using radio button
    String mCurrentColor = null;
    int mColorDetails;
    
    public static MainActivity instance;
    private static final int TASKCODE_LOAD_FEED = 10;
    private static final int TASKCODE_LOAD_MORE_POSTS = 20;
    private static final int TASKCODE_VOTE = 100;
    
    private static final int NEXTPAGE = 0;
    public static final String ARTICAL_POSITION = "NEXT_POSITION";

    private static final String LIST_STATE = "listState";
    private Parcelable mListState = null;
	
	// SlideMenu
	private SimpleSideDrawer mNav;

    @AfterViews
    public void init() {
    	instance=this;
    	initFavoritePosts();

        mFeed = new HNFeed(new ArrayList<HNPost>(), null, "");
        
        mPostsListAdapter = new PostsAdapter();
        mUpvotedPosts = new HashSet<HNPost>();
        mActionbarRefresh.setImageDrawable(getResources().getDrawable(R.drawable.refresh));
        mActionbarTitle.setTypeface(FontHelper.getComfortaa(this, true));
        
        mActionbarRefreshProgress.setVisibility(View.GONE);
        mEmptyListPlaceholder = getEmptyTextView(mRootView);
        mPostsList.setEmptyView(mEmptyListPlaceholder);
        mPostsList.setAdapter(mPostsListAdapter);

        mEmptyListPlaceholder.setTypeface(FontHelper.getComfortaa(this, true));
        	
        //------------- darkmore's codes -----------------	  
		mNav = new SimpleSideDrawer(this);
		mNav.setLeftBehindContentView(R.layout.slide_menu_drawer);

		orderByTime = (TextView) findViewById(R.id.orderByTime);
		orderByReader = (TextView) findViewById(R.id.orderByReader);
		orderByComment = (TextView) findViewById(R.id.orderByComment);
		mSettings = (TextView) findViewById(R.id.mSettings);
		mAbout = (TextView) findViewById(R.id.mAbout);
		mFavorite = (TextView) findViewById(R.id.mFavorite);

		// <!--kate's code about sort button -->
		orderByTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "orderByTime",
						Toast.LENGTH_SHORT).show();
				if(search.get_keyword() !=""){
					HNSearch.mode mode = HNSearch.mode.Time;
					search.set_mode(mode);
					new Thread(Search).start();
				}
			}
		});

		orderByReader.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "orderByReader",
						Toast.LENGTH_SHORT).show();
				if(search.get_keyword() !=""){
					HNSearch.mode mode = HNSearch.mode.Reader;
					search.set_mode(mode);
					new Thread(Search).start();	
				}
			}
		});
		
		orderByComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println(mFeed.toString());
				Toast.makeText(MainActivity.this, "orderByComment",
						Toast.LENGTH_SHORT).show();
				if(search.get_keyword() !=""){
					HNSearch.mode mode = HNSearch.mode.Comment;
					search.set_mode(mode);
					new Thread(Search).start();
				}
			}
		});
		mSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			}
		});
		mAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                startActivity(new Intent(MainActivity.this, AboutActivity_.class));
			}
		});
		mFavorite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 showFavoritePosts();
				 mNav.toggleLeftDrawer();
			}
		});
	    //------------------------------  
		  
          search = new HNSearch();
          loadIntermediateFeedFromStore();
          startFeedLoading();
          
    }
 // <!--kate's code about search button -->
    @Click(R.id.main_search)
    void main_search()  {
    	open_search();
    	if( main_search_text.getText().toString() == ""){}
    	else if(  search.get_keyword() != main_search_text.getText().toString())
    	{
    		search.set_keyword(main_search_text.getText().toString());
    		new Thread(Search).start();		
    	}
    	else{}
    	 mActionbarRefresh.setImageResource(R.drawable.refresh);
         
         mActionbarRefreshProgress.setVisibility(View.VISIBLE);
         mActionbarRefresh.setVisibility(View.GONE);
    }
    private Handler SearchThreadHandler = new Handler() {
        public void handleMessage(Message msg) {
        	switch(msg.what){
            case 0:
            	showFeed(search.get_Feed());
            	if(mCurrentHTMLContent.equals("display")){
            		
            		new Thread(getURLContent_Thread).start();      		
            	}
            break;
            default:
            break;
            }
        	mActionbarRefresh.setVisibility(View.VISIBLE);
        	mActionbarRefreshProgress.setVisibility(View.GONE);
        }
    };
    
    
    private Runnable Search = new Runnable(){
    	public void run(){
    		search.Search() ;  	   
    		SearchThreadHandler.sendEmptyMessage(0);
    	}
    };

    //------------- kevin's codes. Get the part of web content-----------------
    private Runnable getURLContent_Thread = new Runnable(){
    	
        public void run(){
        
        	List<HNPost> mPosts = mFeed.getPosts();
        	
        	HNFeedParser parser = new HNFeedParser();
     
        	for(int i=0; i<mPosts.size(); i++){
        		HNPost post = mPosts.get(i);
        		post.setContent(parser.getURLContent(post.getURL()));
        		getURLContent_ThreadHandler.sendEmptyMessage(0);
        	}               	
        }
     };
     
   //------------- kevin's codes. kevin's codes. Display the part of web content -----------------
     private Handler getURLContent_ThreadHandler = new Handler() {
    	 
         public void handleMessage(Message msg) {
         	switch(msg.what){
         		case 0:
         			showFeed(mFeed);
         			break;
         		default:
         			break;
         	}
         }
     };
 
    
    @Override
    protected void onResume() {
        super.onResume();
        
        boolean registeredUserChanged = mFeed.getUserAcquiredFor() != null && (!mFeed.getUserAcquiredFor()
                .equals(Settings.getUserName(this)));
        
        // We want to reload the feed if a new user logged in
        if (HNCredentials.isInvalidated() || registeredUserChanged) {
            showFeed(new HNFeed(new ArrayList<HNPost>(), null, ""));
            startFeedLoading();
        }

        // refresh if font size changed
        if (refreshFontSizes())
        	mPostsListAdapter.notifyDataSetChanged();
        
        //Ramesh kumar coding part for change background color using radio button
        if(refreshBackgroundColor())
        	mPostsListAdapter.notifyDataSetChanged();
        
       //------------- kevin's codes. refresh the setup when changing the setting -----------------
        if(refreshHTMLContent()){       	
        	
        	if(mCurrentHTMLContent.equals("display")){
        		
        		new Thread(getURLContent_Thread).start();      		
        	}
        	else{
        		List<HNPost> mPosts = mFeed.getPosts();
        		for(int i=0; i<mPosts.size(); i++){
        			
        			mPosts.get(i).setContent("");
        		}       		
        		showFeed(mFeed);
        	}
        	
        }
        // restore vertical scrolling position if applicable
        if (mListState != null)
            mPostsList.onRestoreInstanceState(mListState);
        
        mListState = null;
    }
    
    @Click(R.id.actionbar)
    void actionBarClicked() {
        mPostsList.smoothScrollToPosition(0);
    }

    @Click(R.id.actionbar_refresh_container)
    void refreshClicked() {
    	search.set_keyword("");
        if (HNFeedTaskMainFeed.isRunning(getApplicationContext()))
            HNFeedTaskMainFeed.stopCurrent(getApplicationContext());
        else
            startFeedLoading();
    }
    @Click(R.id.Magnifier)
    void open_search() {
    	turn = !turn;
    	search_appear(turn);
    }
    
    void search_appear(boolean turn)
    {
    	if(turn){
    	  main_search_text.setVisibility(View.VISIBLE);
    	  main_search.setVisibility(View.VISIBLE);}
    	else{
      	  main_search_text.setVisibility(View.INVISIBLE);
      	  main_search.setVisibility(View.INVISIBLE);}
    		
    }

    @Click(R.id.actionbar_more)
    void moreClicked() {
        mActionbarMore.setSelected(true);
        LinearLayout moreContentView = (LinearLayout) mInflater.inflate(R.layout.main_more_content, null);

        moreContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.red_dark_washedout)));
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

        Button settingsButton = (Button) moreContentView.findViewById(R.id.main_more_content_settings);
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                popupWindow.dismiss();
            }
        });

        Button aboutButton = (Button) moreContentView.findViewById(R.id.main_more_content_about);
        aboutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity_.class));
                popupWindow.dismiss();
            }
        });

        popupWindow.update(moreContentView.getMeasuredWidth(), moreContentView.getMeasuredHeight());
    }

    @Override
    public void onTaskFinished(int taskCode, TaskResultCode code, HNFeed result, Object tag) {
        if (taskCode == TASKCODE_LOAD_FEED) {
            if (code.equals(TaskResultCode.Success) && mPostsListAdapter != null){
                showFeed(result);}
            else if (!code.equals(TaskResultCode.Success))
                Toast.makeText(this, getString(R.string.
                        error_unable_to_retrieve_feed), Toast.LENGTH_SHORT).show();

            mActionbarRefreshProgress.setVisibility(View.GONE);
            mActionbarRefresh.setVisibility(View.VISIBLE);
        } else if (taskCode == TASKCODE_LOAD_MORE_POSTS) {
            if (!code.equals(TaskResultCode.Success))
                Toast.makeText(this, getString(R.string.
                        error_unable_to_load_more), Toast.LENGTH_SHORT).show();

            mFeed.appendLoadMoreFeed(result);
            mPostsListAdapter.notifyDataSetChanged();
 
        }

    }
    public static MainActivity getInstance()
    {
    	return instance;
    }
    private void showFeed(HNFeed feed) {
        mFeed = feed;
        mPostsListAdapter.notifyDataSetChanged();
    }

    private void loadIntermediateFeedFromStore() {
        new GetLastHNFeedTask().execute((Void)null);
        long start = System.currentTimeMillis();
        
        Log.i("", "Loading intermediate feed took ms:" + (System.currentTimeMillis() - start));
    }
    
    class GetLastHNFeedTask extends FileUtil.GetLastHNFeedTask {
		ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(MainActivity.this);
			progress.setMessage("Loading");
			progress.show();
		}

		protected void onPostExecute(HNFeed result) {
			if (progress != null && progress.isShowing())
				progress.dismiss();

			if (result != null
					&& result.getUserAcquiredFor() != null
					&& result.getUserAcquiredFor().equals(
							Settings.getUserName(App.getInstance())))
				showFeed(result);
		}
	}


    private void startFeedLoading() {
        HNFeedTaskMainFeed.startOrReattach(this, this, TASKCODE_LOAD_FEED);
        mActionbarRefresh.setImageResource(R.drawable.refresh);
        
        mActionbarRefreshProgress.setVisibility(View.VISIBLE);
        mActionbarRefresh.setVisibility(View.GONE);
    }
    
    private boolean refreshHTMLContent(){
    	
    	final String htmlContent = Settings.getHtmlContent(this);
    	if((mCurrentHTMLContent == null) || !mCurrentHTMLContent.equals(htmlContent)){
    		mCurrentHTMLContent = htmlContent;
    		if(htmlContent.equals(getString(R.string.pref_htmlcontent_display))){
    			
    			mCurrentHTMLContent = "display";
    		}else{
    			
    			mCurrentHTMLContent = "dismiss";
    		} 
    		
    		return true;
    	}  	
    	else{
    		
    		return false;
    	}
    }

    private boolean refreshFontSizes() {
        final String fontSize = Settings.getFontSize(this);
        if ((mCurrentFontSize == null) || (!mCurrentFontSize.equals(fontSize))) {
        	mCurrentFontSize = fontSize;
	        if (fontSize.equals(getString(R.string.pref_fontsize_small))) {
	            mFontSizeTitle = 15;
	            mFontSizeDetails = 11;
	        } else if (fontSize.equals(getString(R.string.pref_fontsize_normal))) {
	            mFontSizeTitle = 18;
	            mFontSizeDetails = 12;
	        } else {
	            mFontSizeTitle = 22;
	            mFontSizeDetails = 15;
	        }
	        return true;
        } else {
        	return false;
        }
    }
    
    // Ramesh kumar coding part for change background color using radio button
    
    
    private boolean refreshBackgroundColor() {
        final String bgcolor = Settings.getColor(this);
        if ((mCurrentColor == null) || (!mCurrentColor.equals(bgcolor))) {
        	mCurrentColor = bgcolor;
        	if (bgcolor.equals(getString(R.string.pref_background_color_Origin))) {
 	            mColorDetails= 0xffffeddb;
 	        } else if (bgcolor.equals(getString(R.string.pref_background_color_Red))) {
	            mColorDetails= 0xffff7f7f;
	        } else if (bgcolor.equals(getString(R.string.pref_background_color_Blue))) {
	           mColorDetails=0xff9999cc;
	        } else if (bgcolor.equals(getString(R.string.pref_background_color_Green))) {
	            mColorDetails=0xffb2ffb2;
        	} else if (bgcolor.equals(getString(R.string.pref_background_color_Yellow))) {
        		mColorDetails=0xffffffcc;
	        } else if (bgcolor.equals(getString(R.string.pref_background_color_Gray))) {
	        	mColorDetails=0xffdddddd;
    		}
	        return true;
        } else {
        	return false;
        	
        }
    }       

    private void vote(String voteURL, HNPost post) {
        HNVoteTask.start(voteURL, MainActivity.this, new VoteTaskFinishedHandler(), TASKCODE_VOTE, post);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mListState = state.getParcelable(LIST_STATE);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = mPostsList.onSaveInstanceState();
        state.putParcelable(LIST_STATE, mListState);
    }

    class VoteTaskFinishedHandler implements ITaskFinishedHandler<Boolean> {
        @Override
        public void onTaskFinished(int taskCode, com.manuelmaly.hn.task.ITaskFinishedHandler.TaskResultCode code,
            Boolean result, Object tag) {
            if (taskCode == TASKCODE_VOTE) {
                if (result != null && result.booleanValue()) {
                    Toast.makeText(MainActivity.this, R.string.vote_success, Toast.LENGTH_SHORT).show();
                    HNPost post = (HNPost) tag;
                    if (post != null)
                        mUpvotedPosts.add(post);
                } else
                    Toast.makeText(MainActivity.this, R.string.vote_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	moreClicked();
    	return false;
    }
    
    class PostsAdapter extends BaseAdapter {

        private static final int VIEWTYPE_POST = 0;
        private static final int VIEWTYPE_LOADMORE = 1;

        @Override
        public int getCount() {
            int posts = mFeed.getPosts().size();
            if (posts == 0)
                return 0;
            else
                return posts + (mFeed.isLoadedMore() ? 0 : 1);
        }

        @Override
        public HNPost getItem(int position) {
            if (getItemViewType(position) == VIEWTYPE_POST)
                return mFeed.getPosts().get(position);
            else
                return null;
        }

        @Override
        public long getItemId(int position) {
            // Item ID not needed here:
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < mFeed.getPosts().size())
                return VIEWTYPE_POST;
            else
                return VIEWTYPE_LOADMORE;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case VIEWTYPE_POST:
                    if (convertView == null) {
                        convertView = (LinearLayout) mInflater.inflate(R.layout.main_list_item, null);
                        PostViewHolder holder = new PostViewHolder();
                        holder.titleView = (TextView) convertView.findViewById(R.id.main_list_item_title);
                        holder.contentView = (TextView) convertView.findViewById(R.id.main_list_item_content);
                        holder.urlView = (TextView) convertView.findViewById(R.id.main_list_item_url);
                        holder.textContainer = (LinearLayout) convertView.findViewById(R.id.main_list_item_textcontainer);
                        holder.commentsButton = (Button) convertView.findViewById(R.id.main_list_item_comments_button);
                        holder.commentsButton.setTypeface(FontHelper.getComfortaa(MainActivity.this, false));
                        holder.commentsContainer= (LinearLayout) convertView.findViewById(R.id.main_list_item_comments_container); //ramesh
                        holder.pointsView = (TextView) convertView.findViewById(R.id.main_list_item_points);
                        holder.pointsView.setTypeface(FontHelper.getComfortaa(MainActivity.this, true));
                        convertView.setTag(holder);
                    }

                    HNPost item = getItem(position);
                    PostViewHolder holder = (PostViewHolder) convertView.getTag();
                    holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeTitle);
                    holder.titleView.setText(item.getTitle());
                    holder.contentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeDetails);
                    holder.contentView.setText(item.getContent());
                    holder.urlView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeDetails);
                    holder.urlView.setText(item.getURLDomain());
                    holder.pointsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeDetails);

                    holder.textContainer.setBackgroundColor(mColorDetails);  // Ramesh kumar coding part for change background color using radio button
                    holder.commentsContainer.setBackgroundColor(mColorDetails);
                    
                    if (item.getPoints() != BaseHTMLParser.UNDEFINED)
                        holder.pointsView.setText(item.getPoints() + "");
                    else
                        holder.pointsView.setText("-");

                    holder.commentsButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mFontSizeTitle);                 
                    if (item.getCommentsCount() != BaseHTMLParser.UNDEFINED) {
                        holder.commentsButton.setVisibility(View.VISIBLE);
                        holder.commentsButton.setText(item.getCommentsCount() + "");
                    } else
                        holder.commentsButton.setVisibility(View.INVISIBLE);
                    holder.commentsButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Intent i = new Intent(MainActivity.this, CommentsActivity_.class);
                            i.putExtra(CommentsActivity.EXTRA_HNPOST, getItem(position));
                            startActivity(i);
                        }
                    });

                holder.textContainer.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (Settings.getHtmlViewer(MainActivity.this).equals(
                            getString(R.string.pref_htmlviewer_browser)))
                            openURLInBrowser(getArticleViewURL(getItem(position)), MainActivity.this);
                        else
                        	openPostInApp(position,getItem(position), null, MainActivity.this);
                    }
                });
				holder.textContainer
						.setOnLongClickListener(new OnLongClickListener() {
							public boolean onLongClick(View v) {
								
								System.out.println("onLongClick");
								if(!startSlide){
									
									longClick = true;
									final HNPost post = getItem(position);
									AlertDialog.Builder builder = new AlertDialog.Builder(
											MainActivity.this);
		                            LongPressMenuListAdapter adapter = new LongPressMenuListAdapter(post,position);
									builder.setAdapter(adapter, adapter).show();
									return true;
								}
								return false;
							}
						});
				break;

                case VIEWTYPE_LOADMORE:
                    // I don't use the preloaded convertView here because it's
                    // only one cell
                    convertView = (FrameLayout) mInflater.inflate(R.layout.main_list_item_loadmore, null);
                    final TextView textView = (TextView) convertView.findViewById(R.id.main_list_item_loadmore_text);
                    textView.setTypeface(FontHelper.getComfortaa(MainActivity.this, true));
                    final ImageView imageView = (ImageView) convertView
                        .findViewById(R.id.main_list_item_loadmore_loadingimage);
                    if (HNFeedTaskLoadMore.isRunning(MainActivity.this, TASKCODE_LOAD_MORE_POSTS)) {
                        textView.setVisibility(View.INVISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        convertView.setClickable(false);
                    }

                    final View convertViewFinal = convertView;
                    convertView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            textView.setVisibility(View.INVISIBLE);
                            imageView.setVisibility(View.VISIBLE);
                            convertViewFinal.setClickable(false);
                            HNFeedTaskLoadMore.start(MainActivity.this, MainActivity.this, mFeed,
                                TASKCODE_LOAD_MORE_POSTS);
                        }
                    });

                    break;
                default:
                    break;
            }

            return convertView;
        }
    }

    private class LongPressMenuListAdapter implements ListAdapter, DialogInterface.OnClickListener {

    	int pos;
        HNPost mPost;
        boolean mIsLoggedIn;
        boolean mUpVotingEnabled;
        ArrayList<CharSequence> mItems;

        public LongPressMenuListAdapter(HNPost post,int position) {
        	pos=position;
            mPost = post;
            mIsLoggedIn = Settings.isUserLoggedIn(MainActivity.this);
            mUpVotingEnabled = !mIsLoggedIn
                || (mPost.getUpvoteURL(Settings.getUserName(MainActivity.this)) != null && !mUpvotedPosts
                    .contains(mPost));

            mItems = new ArrayList<CharSequence>();
            if (mUpVotingEnabled)
                mItems.add(getString(R.string.upvote));
            else
                mItems.add(getString(R.string.already_upvoted));
            	mItems.addAll(Arrays.asList(
                getString(R.string.pref_htmlprovider_original_url),
                getString(R.string.pref_htmlprovider_viewtext),
                getString(R.string.pref_htmlprovider_google),
                getString(R.string.pref_htmlprovider_instapaper),
                getString(R.string.external_browser)));
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public CharSequence getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, null);
            view.setText(getItem(position));
            if (!mUpVotingEnabled && position == 0)
                view.setTextColor(getResources().getColor(android.R.color.darker_gray));
            return view;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            if (!mUpVotingEnabled && position == 4)
                return false;
            return true;
        }

        @Override
        public void onClick(DialogInterface dialog, int item) {
            switch (item) {
                case 0:
                    if (!mIsLoggedIn)
                        Toast.makeText(MainActivity.this, R.string.please_log_in, Toast.LENGTH_LONG).show();
                    else if (mUpVotingEnabled)
                        vote(mPost.getUpvoteURL(Settings.getUserName(MainActivity.this)), mPost);
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    openPostInApp( pos , mPost, getItem(item).toString(), MainActivity.this);
                    break;
                case 5:
                    openURLInBrowser(getArticleViewURL(mPost), MainActivity.this);
                    break;
                default:
                    break;
            }
        }

    }

    private String getArticleViewURL(HNPost post) {
        return ArticleReaderActivity.getArticleViewURL(post, Settings.getHtmlProvider(this), this);
    }

    public static void openURLInBrowser(String url, Activity a) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        a.startActivity(browserIntent);
    }
  //------------- re330's codes -----------------
    public static void openPostInApp(int position, HNPost post, String overrideHtmlProvider, Activity a) {

        Intent i = new Intent(a, ArticleReaderActivity_.class);
        i.putExtra(ArticleReaderActivity.EXTRA_POSITION, position);
        i.putExtra(ArticleReaderActivity.EXTRA_HNPOST, post);
        if (overrideHtmlProvider != null)
            i.putExtra(ArticleReaderActivity.EXTRA_HTMLPROVIDER_OVERRIDE, overrideHtmlProvider);
        a.startActivityForResult(i,NEXTPAGE);
       // a.startActivity(i);
    }
    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {	
    	if(requestCode == NEXTPAGE && resultCode == RESULT_OK){
    		int position = (int) data.getIntExtra(ARTICAL_POSITION,-1);  		
    		HNPost post = mFeed.getPosts().get(position);
    		openPostInApp( position, post, null, MainActivity.this );
    	}
    }
  //------------------------------
    static class PostViewHolder {
        TextView titleView;
        TextView urlView;
        TextView pointsView;
        TextView commentsCountView;
        TextView contentView;
        LinearLayout commentsContainer;
        LinearLayout textContainer;
        Button commentsButton;
    }
    //------------- darkmore's codes -----------------
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// Log.d("ddd",String.valueOf(ev.getAction()));
		boolean result = onTouch(ev);
		if (!result)
			return super.dispatchTouchEvent(ev);
		return result;
	}
	public boolean onTouch(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (longClick){
				longClick = false;
				return false;
			}
			
			if (startSlide) {
				drag_Ex = event.getX();
				drag_Ey = event.getY();
				if (Math.abs(drag_Ey-drag_Sy)<=40&&drag_Ex - drag_Sx >= 100) {
					mNav.toggleLeftDrawer();
					return true;
				}
				startSlide = false;
			}

			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!startSlide) {
				if (Math.sqrt(Math.pow(event.getX() - drag_Sx,2)
						+ Math.pow(event.getY() - drag_Sy, 2)) >= 10.0) {
					drag_Sx = event.getX();
					drag_Sy = event.getY();
					startSlide = true;
				}
			}
			return false;

		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startSlide = false;
			drag_Sx = event.getX();
			drag_Sy = event.getY();
			return false;
		}

		return false;

	}
	private void initFavoritePosts() {
		fFeed=getSharedPreferences("DATA",0);
		String storeJSON=fFeed.getString("postData", "");
		favoritePosts=new HNFeed(new ArrayList<HNPost>(), null, "");
		if(storeJSON.length()>0){
	    	try {
				postJsonArray = new JSONArray(storeJSON);
		    	for(int n=0;n<postJsonArray.length();n++){
		    		JSONObject temp=postJsonArray.getJSONObject(n);
		    		favoritePosts.addPost(new HNPost(temp.getString("url"),temp.getString("title"),temp.getString("urlDomain"),temp.getString("author"),temp.getString("postID"),temp.getInt("commentsCount"),temp.getInt("points"),null));
		    	}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	public boolean addFavoritePost(int nPost)  {
    	fFeed=getSharedPreferences("DATA",0);
    	if(postJsonArray==null){
    		postJsonArray = new JSONArray();
    	}
    	HNPost temp=mFeed.getPosts().get(nPost);
    	if(!isPostinFavorite(temp.getURL())){
	    	//New hnPOST
	        JSONObject jsonObject = new JSONObject();
	        try {
				jsonObject.put("url", temp.getURL());
		        jsonObject.put("title", temp.getTitle());
		        jsonObject.put("urlDomain", temp.getURLDomain());
		        jsonObject.put("author", temp.getAuthor());
		        jsonObject.put("postID", temp.getPostID());
		        jsonObject.put("commentsCount", temp.getCommentsCount()); 
		        jsonObject.put("points", temp.getPoints());
		        jsonObject.put("upvoteURL", null);         
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}               
	        //put in jsonArray
	        postJsonArray.put(jsonObject);
	        fFeed.edit().putString("postData", postJsonArray.toString()).commit();
	        
	    	favoritePosts.addPost(temp); 
	    	return true;
    	}
    	return false;
    	
    }
    public  boolean isPostinFavorite(String postUrl)  {
    	for(int p=0;p<favoritePosts.getPosts().size();p++){
    		if(favoritePosts.getPosts().get(p).getURL().equals(postUrl))return true;
    	}
    	return false;
    }
    private void showFavoritePosts() {
        showFeed(favoritePosts);
    }
    //------------------------------
    



}
