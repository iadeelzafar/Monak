package com.example.monakk.Util;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.einmalfel.earl.Item;
import com.example.monakk.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {
  Context mContext;

  CustomItemClickListener listener;
  ArrayList<Item> RssItems;

  public RVAdapter(Context mContext, ArrayList<Item> RssItems, CustomItemClickListener listener) {

    try {
      this.RssItems = RssItems;
      this.mContext = mContext;
      this.listener = listener;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }

  @Override
  public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
    return new PersonViewHolder(v);
  }

  @Override
  public void onBindViewHolder(final PersonViewHolder personViewHolder, final int i) {
    personViewHolder.titleLabel.setText(RssItems.get(i).getTitle());
    personViewHolder.cv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClick(personViewHolder.cv, i);
      }
    });
    personViewHolder.webview.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClick(personViewHolder.cv, i);
      }
    });

    personViewHolder.webview.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {

        listener.onItemClick(personViewHolder.cv, i);

        return true;
      }
    });

    personViewHolder.webview.loadDataWithBaseURL(null,
        "<html>" + RssItems.get(i).getDescription() + "</html>", "text/html", "utf-8", null);

    if (RssItems.get(i).getImageLink() != null) {
      Picasso.with(mContext).load((RssItems.get(i).getImageLink())).into(personViewHolder.Photo);
    }
  }

  @Override
  public int getItemCount() {
    return RssItems.size();
  }

  public static class PersonViewHolder extends RecyclerView.ViewHolder
      implements View.OnClickListener {
    @BindView(R.id.cv)
    CardView cv;
    @BindView(R.id.person_name)
    TextView titleLabel;
    @BindView(R.id.person_age)
    WebView webview;
    @BindView(R.id.person_photo)
    ImageView Photo;

    PersonViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      cv.setCardElevation(8);
    }

    @Override
    public void onClick(View v) {

    }
  }
}