package com.example.monakk.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Enclosure;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;
import com.example.monakk.R;
import com.example.monakk.Util.CustomItemClickListener;
import com.example.monakk.Util.RVAdapter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BlankFragment extends Fragment {

  private static final String ARG_PARAM1 = "param1";

  @BindView(R.id.rv)
  ShimmerRecyclerView shimmerRecyclerView;

  private ArrayList<Item> mItems;

  private OnFragmentInteractionListener mListener;

  private String parse;

  public BlankFragment() {

  }


  public static BlankFragment newInstance(String param1) {
    BlankFragment fragment = new BlankFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      parse = getArguments().getString(ARG_PARAM1);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
    ButterKnife.bind(this, rootView);
    LinearLayoutManager llm = new LinearLayoutManager(getActivity());

    shimmerRecyclerView.setLayoutManager(llm);

    shimmerRecyclerView.setHasFixedSize(false);

    shimmerRecyclerView.showShimmerAdapter();
    mItems = new ArrayList<>();

    new GetRssFeed().execute(parse);

    return rootView;
  }

  private void secondaryParser() {
    Parser parser = new Parser();
    parser.execute(parse);
    parser.onFinish(new Parser.OnTaskCompleted() {

      @Override
      public void onTaskCompleted(ArrayList<Article> list) {

        for (final Article article : list) {
          Item item = new Item() {
            @Nullable
            @Override
            public String getLink() {
              return article.getLink();
            }

            @Nullable
            @Override
            public Date getPublicationDate() {
              return article.getPubDate();
            }

            @Nullable
            @Override
            public String getTitle() {
              return article.getTitle();
            }

            @Nullable
            @Override
            public String getDescription() {
              return article.getDescription();
            }

            @Nullable
            @Override
            public String getImageLink() {
              return article.getImage();
            }

            @Nullable
            @Override
            public String getAuthor() {
              return article.getAuthor();
            }

            @NonNull
            @Override
            public List<? extends Enclosure> getEnclosures() {
              return null;
            }
          };
          mItems.add(item);
        }
        initializeAdapter();
      }

      @Override
      public void onError() {

        Toast.makeText(getContext(), "Cannot get NEWS Feed. Try any other channel.",
            Toast.LENGTH_LONG).show();
      }
    });
  }

  private void initializeAdapter() {

    RVAdapter adapter = new RVAdapter(getActivity(), mItems, new CustomItemClickListener() {
      @Override
      public void onItemClick(View v, int position) {
        String url = mItems.get(position).getLink();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.newsBackground));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
      }
    });
    adapter.notifyDataSetChanged();
    if (shimmerRecyclerView != null) {
      shimmerRecyclerView.hideShimmerAdapter();
      shimmerRecyclerView.setAdapter(adapter);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }


  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  private class GetRssFeed extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
      try {
        InputStream inputStream = new URL(params[0]).openConnection().getInputStream();
        Feed feed = EarlParser.parseOrThrow(inputStream, 0);

        mItems.addAll(feed.getItems());
      } catch (Exception e) {
        Log.v("Error Parsing Data", e + "");
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (mItems.isEmpty()) {
        Toast.makeText(getContext(), "Try other approach.", Toast.LENGTH_SHORT).show();
        secondaryParser();
      } else {
        initializeAdapter();
      }
    }
  }
}
