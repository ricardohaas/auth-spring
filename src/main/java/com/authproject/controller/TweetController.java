package com.authproject.controller;

import com.authproject.controller.dto.CreateTweetDto;
import com.authproject.entities.Tweet;
import com.authproject.repository.TweetRepository;
import com.authproject.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TweetController {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;


    public TweetController(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto createTweetDto,
                                            UsernamePasswordAuthenticationToken token){
        var user  =  userRepository.findById(UUID.fromString(token.getName()));
        var tweet = new Tweet();
        tweet.setContent(createTweetDto.content());
        tweet.setUser(user.get());
        tweetRepository.save(tweet);
        return ResponseEntity.ok().build();
    }
}
