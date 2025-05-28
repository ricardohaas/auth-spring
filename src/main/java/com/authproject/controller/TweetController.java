package com.authproject.controller;

import com.authproject.controller.dto.CreateTweetDto;
import com.authproject.repository.TweetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TweetController {
    private final TweetRepository tweetRepository;


    public TweetController(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto createTweetDto){

    }
}
