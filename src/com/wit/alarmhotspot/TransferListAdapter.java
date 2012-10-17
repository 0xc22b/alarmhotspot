package com.wit.alarmhotspot;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wit.alarmhotspot.model.TransferObj;

public class TransferListAdapter extends BaseAdapter {

    private int[] mIds;
    private int[] mLayouts;
    private LayoutInflater mInflater;
    private ArrayList<TransferObj> mContent;

    public TransferListAdapter(Context context, ArrayList<TransferObj> content) {
        init(context, new int[]{R.layout.transfer_list_item_1}, 
                new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4,
                R.id.text5, R.id.text6, R.id.text7}, content);
    }
    
    public TransferListAdapter(Context context, int[] itemLayouts, int[] itemIDs, ArrayList<TransferObj> content) {
    	init(context,itemLayouts,itemIDs, content);
    }

    private void init(Context context, int[] layouts, int[] ids, ArrayList<TransferObj> content) {
    	// Cache the LayoutInflate to avoid asking for a new one each time.
    	mInflater = LayoutInflater.from(context);
    	mIds = ids;
    	mLayouts = layouts;
    	mContent = content;
    }
    
    @Override
    public int getCount() {
        return mContent.size();
    }

    @Override
    public TransferObj getItem(int position) {
        return mContent.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;
        
        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(mLayouts[0], null);
            
            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text1 = (TextView) convertView.findViewById(mIds[0]);
            holder.text2 = (TextView) convertView.findViewById(mIds[1]);
            holder.text3 = (TextView) convertView.findViewById(mIds[2]);
            holder.text4 = (TextView) convertView.findViewById(mIds[3]);
            holder.text5 = (TextView) convertView.findViewById(mIds[4]);
            holder.text6 = (TextView) convertView.findViewById(mIds[5]);
            holder.text7 = (TextView) convertView.findViewById(mIds[6]);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text1.setText(mContent.get(position).getStartDateString("dd"));
        holder.text2.setText(mContent.get(position).getStartDateString("MMM yyyy"));
        holder.text3.setText(mContent.get(position).getStartDateString("h:mmaa"));
        holder.text4.setText(mContent.get(position).getEndDateString("dd"));
        holder.text5.setText(mContent.get(position).getEndDateString("MMM yyyy"));
        holder.text6.setText(mContent.get(position).getEndDateString("h:mmaa"));
        holder.text7.setText(mContent.get(position).getTransferString());

        return convertView;
    }
    
    static class ViewHolder {
        TextView text1;
        TextView text2;
        TextView text3;
        TextView text4;
        TextView text5;
        TextView text6;
        TextView text7;
    }

}
