package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.digitalcampus.mobile.learningJHPIEGO.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.*;

import java.util.ArrayList;

public class GroupedCourseAdapter extends ArrayAdapter<Course> {
	public ArrayList<Course> tags;
	public ArrayList<Course> courses;
	public Context ctx;
	public LayoutInflater minflater;
	private DbHelper db;
	private SharedPreferences prefs;
	private long userId;
	//Constructor
	public GroupedCourseAdapter(Context ctx, ArrayList<Course> tags) { //
		super(ctx, R.layout.tag_row, tags);
		this.ctx = ctx;
		this.tags = tags;
		courses=new ArrayList<Course>();
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		db=new DbHelper(ctx);
		
	}
	 static class TagViewHolder{
	        TextView tagName;
	        TextView tagDescription;
	        ImageView tagIcon;
	    }
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		   TagViewHolder viewHolder;
	        
	        if (convertView == null) {
	            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView  = inflater.inflate(R.layout.tag_row, parent, false);
	            viewHolder = new TagViewHolder();
	            viewHolder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
	            viewHolder.tagDescription = (TextView) convertView.findViewById(R.id.tag_description);
	            viewHolder.tagIcon = (ImageView) convertView.findViewById(R.id.tag_icon);
	            convertView.setTag(viewHolder);
	        } else {
	            viewHolder = (TagViewHolder) convertView.getTag();
	        }
	        userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
	        courses=db.getCoursesByTags(userId, tags.get(position).getCourseTag());
	        viewHolder.tagName.setText(tags.get(position).getCourseTag()+" ("+courses.size()+")");
	        viewHolder.tagIcon.setImageResource(R.drawable.ic_books);

		    return convertView;
		}
	
	
}
