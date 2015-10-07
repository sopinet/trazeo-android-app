package com.sopinet.trazeo.app.helpers;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sopinet.trazeo.app.CatalogActivity;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.Catalog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CatalogAdapter extends ArrayAdapter<Catalog> {

    private Context context;
    private ArrayList<Catalog> catalogs;
    private int lastPosition = -1;
    private Animation animation;

    public CatalogAdapter(Context context, int textViewResourceId,
                          ArrayList<Catalog> catalogList) {
        super(context, textViewResourceId, catalogList);
        this.context = context;
        catalogs = new ArrayList<>();
        catalogs.addAll(catalogList);
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.up_from_bottom);
    }

    private class ViewHolder {
        ImageView imageCatalog;
        TextView companyName;
        TextView name;
        TextView description;
        TextView changePoints;
        TextView moreInfo;
        TextView tvPoints;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        final Catalog catalog = catalogs.get(position);

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.catalog_list_item, parent, false);

            holder = new ViewHolder();
            holder.imageCatalog = (ImageView) convertView.findViewById(R.id.imageCatalog);
            holder.companyName = (TextView) convertView.findViewById(R.id.companyName);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.changePoints = (TextView) convertView.findViewById(R.id.changePoints);
            holder.moreInfo = (TextView) convertView.findViewById(R.id.moreInfo);
            holder.tvPoints = (TextView) convertView.findViewById(R.id.tvPoints);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(this.context).cancelRequest(holder.imageCatalog);
        Picasso.with(context)
                .load(RestClient.URL_API + catalog.url)
                .into(holder.imageCatalog);

        holder.companyName.setText(catalog.company);
        holder.name.setText(catalog.title);
        holder.description.setText(catalog.description);
        holder.tvPoints.setText(catalog.points);

        holder.changePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CatalogActivity) context).changePoints(catalog.id);
            }
        });

        holder.moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CatalogActivity) context).goLink(catalog.link);
            }
        });

      /*  if (position > lastPosition) {
            convertView.startAnimation(animation);
        }
        lastPosition = position;
*/
        return convertView;
    }

}
