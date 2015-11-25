package net.carslink.navimap;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import net.carslink.activity.R;

/**
 * Created by wonghoukit on 2015/4/2 0002.
 */
public class MapListAdapter extends SimpleAdapter {

    private Context mContext;
    private List<HashMap<String, Object>> mList;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_CLEAR = 1;

    public MapListAdapter(Context context, List<HashMap<String, Object>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.mContext = context;
        this.mList = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder1 viewHolder1 = null;
        ViewHolder2 viewHolder2 = null;
        int type = getItemViewType(position);
        if (convertView == null){
            switch (type){
                case TYPE_ITEM:
                    viewHolder1 = new ViewHolder1();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.lv_item_map, null);
                    viewHolder1.title = (TextView)convertView.findViewById(R.id.title);
                    viewHolder1.info = (TextView)convertView.findViewById(R.id.info);
                    convertView.setTag(viewHolder1);
                    break;
                case TYPE_CLEAR:
                    viewHolder2 = new ViewHolder2();
                    convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, null);
                    viewHolder2.textView = (TextView)convertView.findViewById(android.R.id.text1);
                    convertView.setTag(viewHolder2);
                    break;
                default:
                    break;
            }
        } else {
            switch (type){
                case TYPE_ITEM:
                    viewHolder1 = (ViewHolder1)convertView.getTag();
                    break;
                case TYPE_CLEAR:
                    viewHolder2 = (ViewHolder2)convertView.getTag();
                    break;
                default:
                    break;
            }
        }
        switch (type){
            case TYPE_ITEM:
                String title = mList.get(position).get("itemTitle").toString();
                String info = mList.get(position).get("itemInfo").toString();
                viewHolder1.title.setText(title);
                viewHolder1.info.setText(info);
                break;
            case TYPE_CLEAR:
                viewHolder2.textView.setText("清空历史记录");
                viewHolder2.textView.setTextSize(13.0f);
                viewHolder2.textView.setTextColor(Color.GRAY);
                break;
            default:
                break;
        }

        return convertView;
    }

    class ViewHolder1 {
        TextView title;
        TextView info;
    }

    class ViewHolder2 {
        TextView textView;
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0 && position == mList.size() - 1){
            return TYPE_CLEAR;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
