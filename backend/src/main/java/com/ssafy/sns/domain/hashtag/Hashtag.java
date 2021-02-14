package com.ssafy.sns.domain.hashtag;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long id;

    @Column(name = "hashtag_name", nullable = false, unique = true)
    private String tagName;

    @OneToMany(mappedBy = "hashtag")
    private List<FeedHashtag> feedHashtags = new ArrayList<>();

    @Builder
    public Hashtag(String tagName) {
        this.tagName = tagName;
    }

    public void addFeedHashtag(FeedHashtag feedHashtag) {
        if (feedHashtags == null) feedHashtags = new ArrayList<>();
        this.feedHashtags.add(feedHashtag);
        feedHashtag.setHashtag(this);
    }

    public void deleteFeedHashtag(FeedHashtag feedHashtag) {
        this.feedHashtags.remove(feedHashtag);
        feedHashtag.setHashtag(null);
    }
}
