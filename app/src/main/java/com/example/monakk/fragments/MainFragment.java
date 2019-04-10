package com.example.monakk.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import com.example.monakk.Activities.MainActivity;
import com.example.monakk.R;
import com.example.monakk.model.Niche;
import com.example.monakk.model.StringObject;
import com.example.monakk.yalantis.flipviewpager.adapter.BaseFlipAdapter;
import com.example.monakk.yalantis.flipviewpager.utils.FlipSettings;
import com.squareup.picasso.Picasso;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainFragment extends Fragment {

  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  @BindView(R.id.friends)
  public ListView friends;
  MainActivity myParentActivity;
  private Realm realm;

  private String mParam1;
  private String mParam2;

  private OnFragmentInteractionListener mListener;

  public MainFragment() {
  }

  public static MainFragment newInstance(String param1, String param2) {
    MainFragment fragment = new MainFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
    myParentActivity = (MainActivity) getActivity();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    Realm.init(getContext());
    RealmConfiguration realmConfiguration =
        new RealmConfiguration.Builder().name(Realm.DEFAULT_REALM_NAME)
            .schemaVersion(0)
            .deleteRealmIfMigrationNeeded()
            .build();
    realm = Realm.getInstance(realmConfiguration);
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    ButterKnife.bind(this, rootView);
    friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        myParentActivity.setUrl(realm.where(Niche.class).findAll().get(position).getMainLink());
      }
    });

    reloadList();
    return rootView;
  }

  public void reloadList() {
    FlipSettings settings = new FlipSettings.Builder().defaultPage(1).build();
    RealmResults<Niche> niches = realm.where(Niche.class).notEqualTo("avatar", 0).findAll();
    friends.setAdapter(new FriendsAdapter(getActivity(), niches, settings));
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
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

  class FriendsAdapter extends BaseFlipAdapter<Niche> {

    int PAGES = 3;
    @BindViews({
        R.id.interest_1, R.id.interest_2, R.id.interest_3, R.id.interest_4, R.id.interest_5,
        R.id.interest_6
    })
    List<Button> newsChannels;
    private int[] IDS_INTEREST = {
        R.id.interest_1, R.id.interest_2, R.id.interest_3, R.id.interest_4, R.id.interest_5,
        R.id.interest_6
    };

    public FriendsAdapter(Context context, List<Niche> items, FlipSettings settings) {
      super(context, items, settings);
      PAGES = (int) Math.ceil((float) items.size() / 2.0);
    }

    @Override
    public View getPage(final int position, View convertView, ViewGroup parent, Niche niche1,
        Niche niche2) {
      final FriendsHolder holder;

      if (convertView == null) {
        holder = new FriendsHolder();
        convertView =
            getActivity().getLayoutInflater().inflate(R.layout.friends_merge_page, parent, false);
        holder.leftAvatar = ButterKnife.findById(convertView, R.id.first);
        holder.rightAvatar = ButterKnife.findById(convertView, R.id.second);
        holder.title1 = ButterKnife.findById(convertView, R.id.title1);
        holder.title2 = ButterKnife.findById(convertView, R.id.title2);
        holder.infoPage =
            getActivity().getLayoutInflater().inflate(R.layout.friends_info, parent, false);
        for (int id : IDS_INTEREST) {
          holder.interests.add((Button) holder.infoPage.findViewById(id));
        }
        convertView.setTag(holder);
      } else {
        holder = (FriendsHolder) convertView.getTag();
      }

      switch (position) {

        case 1:

          Picasso.with(getActivity()).load(niche1.getAvatar()).into(holder.leftAvatar);
          holder.title1.setText(niche1.getTopic());
          if (niche2 != null) {
            Picasso.with(getActivity()).load(niche2.getAvatar()).into(holder.rightAvatar);
            holder.title2.setText(niche2.getTopic());
          }

          break;
        default:
          fillHolder(holder, position == 0 ? niche1 : niche2);
          holder.infoPage.setTag(holder);
          return holder.infoPage;
      }
      return convertView;
    }

    @Override
    public int getPagesCount() {
      return PAGES;
    }

    private void fillHolder(FriendsHolder holder, final Niche niche) {
      if (niche == null) {
        return;
      }
      for (Button button : holder.interests) {
        if (niche.getInterests().size() > holder.interests.indexOf(button)) {
          button.setClickable(true);
          button.setText(niche.getInterests().get(holder.interests.indexOf(button)).string);
          button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              switch (v.getId()) {
                case R.id.interest_1:
                  myParentActivity.setUrl(niche.getLinks().get(0).string);
                  break;
                case R.id.interest_2:
                  myParentActivity.setUrl(niche.getLinks().get(1).string);
                  break;
                case R.id.interest_3:
                  myParentActivity.setUrl(niche.getLinks().get(2).string);
                  break;
                case R.id.interest_4:
                  myParentActivity.setUrl(niche.getLinks().get(3).string);
                  break;
                case R.id.interest_5:
                  myParentActivity.setUrl(niche.getLinks().get(4).string);
                  break;
                case R.id.interest_6:
                  myParentActivity.setUrl(niche.getLinks().get(5).string);
                  break;
              }
            }
          });
        } else {
          button.setVisibility(View.GONE);
        }
      }
      final Iterator<StringObject> iInterests = niche.getInterests().iterator();
    }

    class FriendsHolder {
      ImageView leftAvatar;
      ImageView rightAvatar;
      TextView title1, title2;
      View infoPage;
      List<Button> interests = new ArrayList<>();
    }
  }
}
