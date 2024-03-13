package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.dto.User;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.exception.FollowException;
import dev.yerokha.cookscorner.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
    }

    public User getUser(Long userId) {
        UserEntity entity = getUserById(userId);
        return new User(
                entity.getName(),
                entity.getBio(),
                entity.getProfilePicture() == null ? null : entity.getProfilePicture().getImageUrl(),
                entity.getRecipes().size(),
                entity.getFollowers().size(),
                entity.getFollowing().size()
        );
    }

    public void follow(Long userId, Long userIdFromAuthToken) {
        if (Objects.equals(userId, userIdFromAuthToken)) {
            throw new FollowException("You can not follow yourself");
        }

        UserEntity loggedInUser = getUserById(userIdFromAuthToken);

        UserEntity followedUser = getUserById(userId);

        Set<UserEntity> followingList = loggedInUser.getFollowing();
        Set<UserEntity> followersList = followedUser.getFollowers();

        // Add the followed user to the following list
        followingList.add(followedUser);
        loggedInUser.setFollowing(followingList);

        // Add the current user to the followers list of the followee
        followersList.add(loggedInUser);
        followedUser.setFollowers(followersList);

        // Update both users
        userRepository.save(loggedInUser);
        userRepository.save(followedUser);

    }

    public void unfollow(Long userId, Long userIdFromAuthToken) {
        UserEntity loggedInUser = getUserById(userIdFromAuthToken);

        UserEntity followedUser = getUserById(userId);

        Set<UserEntity> followingList = loggedInUser.getFollowing();
        Set<UserEntity> followersList = followedUser.getFollowers();

        // Remove the followed user from the following list
        followingList.remove(followedUser);
        loggedInUser.setFollowing(followingList);

        // Remove the current user from the followers list of the followee
        followersList.remove(loggedInUser);
        followedUser.setFollowers(followersList);

        // Update both users
        userRepository.save(loggedInUser);
        userRepository.save(followedUser);
    }

    private UserEntity getUserById(Long userIdFromAuthToken) {
        return userRepository.findById(userIdFromAuthToken).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
    }

}

