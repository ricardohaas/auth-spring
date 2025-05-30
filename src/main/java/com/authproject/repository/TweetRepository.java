package com.authproject.repository;

import com.authproject.entities.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TweetRepository extends JpaRepository<Tweet, Long> {

    Page<Tweet> findByUser_UserId(UUID userId, Pageable pageable);

}
