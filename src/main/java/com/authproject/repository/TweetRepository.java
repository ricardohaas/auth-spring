package com.authproject.repository;

import com.authproject.entities.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TweetRepository extends JpaRepository<Tweet, UUID> {

}
