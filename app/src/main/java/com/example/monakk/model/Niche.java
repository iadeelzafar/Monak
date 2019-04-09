package com.example.monakk.model;

import com.example.monakk.R;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import java.util.Map;
import java.util.Objects;

@RealmClass
public class Niche extends RealmObject {
  public int avatar;
  public int background;
  public RealmList<StringObject> links = new RealmList<>();
  @PrimaryKey private String topic;
  private String mainLink;
  private RealmList<StringObject> interests = new RealmList<>();

  public Niche() {

  }

  public Niche(Realm realm,
      String topic,
      Map<String, String> linkTopicPair) {

    switch (topic) {
      case "Sports":
        this.avatar = R.drawable.sports;
      case "Health":
        this.avatar = R.drawable.health;
      case "World":
        this.avatar = R.drawable.world;
      case "Politics":
        this.avatar = R.drawable.politics;
      case "Science":
        this.avatar = R.drawable.science;
      case "Entertainment":
        this.avatar = R.drawable.entertainment;
      case "Comics":
        this.avatar = R.drawable.comic;
      default:
        this.avatar = R.mipmap.ic_launcher;
    }
    this.topic = topic;
    this.background = R.color.newsBackground;

    if (linkTopicPair != null) {
      for (Map.Entry<String, String> entry : linkTopicPair.entrySet()) {
        this.interests.add(StringObject.init(realm, entry.getKey()));
        this.links.add(StringObject.init(realm, entry.getValue()));
      }
    }
    this.mainLink = Objects.requireNonNull(this.links.get(0)).string;
  }

  public RealmList<StringObject> getLinks() {
    return links;
  }

  public RealmList<StringObject> getInterests() {
    return interests;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public int getAvatar() {
    switch (this.topic) {
      case "Sports":
        return R.drawable.sports;
      case "Health":
        return R.drawable.health;
      case "World":
        return R.drawable.world;
      case "Politics":
        return R.drawable.politics;
      case "Science":
        return R.drawable.science;
      case "Entertainment":
        return R.drawable.entertainment;
      case "Comics":
        return R.drawable.comic;
      default:
        return R.mipmap.ic_launcher;
    }
  }

  public int getBackground() {
    return background;
  }

  public void setBackground(int background) {
    this.background = background;
  }

  public String getMainLink() {
    return mainLink;
  }
}
