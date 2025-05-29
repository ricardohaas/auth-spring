package com.authproject.controller;

import com.authproject.controller.dto.CreateTweetDto;
import com.authproject.entities.Tweet;
import com.authproject.entities.User;
import com.authproject.events.TweetCreatedEvent;
import com.authproject.repository.TweetRepository;
import com.authproject.repository.UserRepository;
import com.authproject.services.TweetEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class TweetController {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    private final TweetEventService tweetEventService;


    public TweetController(TweetRepository tweetRepository, UserRepository userRepository, TweetEventService tweetEventService) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
        this.tweetEventService = tweetEventService;
    }

    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto createTweetDto,
                                            UsernamePasswordAuthenticationToken token){
        Optional<User> user  =  userRepository.findById(UUID.fromString(token.getName()));
        if( user.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        TweetCreatedEvent tweetCreatedEvent = new TweetCreatedEvent();
        tweetCreatedEvent.setContent(createTweetDto.content());
        tweetCreatedEvent.setUserId(user.get().getUserId());

        tweetEventService.publishTweetCreatedEvent(tweetCreatedEvent);

        var tweet = new Tweet();
        tweet.setContent(createTweetDto.content());
        tweet.setUser(user.get());
        tweetRepository.save(tweet);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId,
                                            UsernamePasswordAuthenticationToken token){

        Optional<Tweet> tweet = tweetRepository.findById(tweetId);

        if( tweet.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        if(tweet.get().getUser().getUserId().equals(UUID.fromString(token.getName()))){
            tweetRepository.deleteById(tweetId);
        }
        else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/tweets/{username}")
    public ResponseEntity<?> getTweetsByUser(@PathVariable("username") String username,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "5") int size) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Tweet> tweets = tweetRepository.findByUser_UserId(user.get().getUserId(), pageable);
        return ResponseEntity.ok(tweets);
    }
}
