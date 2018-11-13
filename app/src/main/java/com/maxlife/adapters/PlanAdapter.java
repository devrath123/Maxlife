package com.maxlife.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.BaseActivity;
import com.maxlife.activity.MainActivity;
import com.maxlife.data.PlanData;

import java.util.ArrayList;

/**
 * Created by TOXSL\kajal.rawal on 26/5/17.
 */

public class PlanAdapter extends BaseAdapter {
    public ArrayList<PlanData> planList;
    BaseActivity activity;
    TextView nameTV;
    ListView planLV;
    ImageView planEditIV;
    MainActivity mainActivity;

    public PlanAdapter(BaseActivity activity, ArrayList<PlanData> list, MainActivity mainActivity) {
        super();
        this.activity = activity;
        this.planList = list;
        this.mainActivity = mainActivity;
    }

    @Override
    public int getCount() {
        return planList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = newView(viewGroup);
        bindView(i, view);
        planLV = (ListView) view.findViewById(R.id.planLV);
        return view;
    }

    private void bindView( int position, View convertView) {
        nameTV = (TextView) convertView.findViewById(R.id.planNameTV);
        planEditIV = (ImageView) convertView.findViewById(R.id.planEditIV);
        PlanData data = planList.get(position);
        nameTV.setText(data.name);

        nameTV.setTag(position);
        nameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (int) view.getTag();
                ((MainActivity)mainActivity).setPlan(pos);
            }
        });
        planEditIV.setTag(position);
        planEditIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (int) view.getTag();
                ((MainActivity) mainActivity).editPlanTitle(pos);
            }
        });
    }


    private View newView(ViewGroup viewGroup) {
        return (activity.getLayoutInflater().inflate(R.layout.plan_list_item, viewGroup, false));
    }
}
