package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.dto.UpdateProfileRequest;
import dev.yerokha.cookscorner.dto.UpdateProfileResponse;
import dev.yerokha.cookscorner.dto.User;
import dev.yerokha.cookscorner.dto.UserDto;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.exception.FollowException;
import dev.yerokha.cookscorner.exception.IdMismatchException;
import dev.yerokha.cookscorner.exception.NotFoundException;
import dev.yerokha.cookscorner.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.Integer.parseInt;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ImageService imageService;

    public UserService(UserRepository userRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("User not found"));
    }

    public User getUser(Long userId, Long userIdFromAuthToken) {
        UserEntity entity = getUserEntityById(userId);
        Boolean isFollowed = checkIfUserFollowed(userId, userIdFromAuthToken);
        return new User(
                entity.getUserId(),
                entity.getName(),
                entity.getBio(),
                entity.getProfilePicture() == null ? null : entity.getProfilePicture().getImageUrl(),
                entity.getRecipeEntities().size(),
                entity.getFollowers().size(),
                entity.getFollowing().size(),
                isFollowed
        );
    }

    private Boolean checkIfUserFollowed(Long userId, Long userIdFromAuthToken) {
        if (userIdFromAuthToken == null || userId.equals(userIdFromAuthToken)) {
            return null;
        }

        return userRepository.existsByUserIdAndFollowingUserId(userIdFromAuthToken, userId);
    }

    public void follow(Long userId, Long userIdFromAuthToken) {
        if (Objects.equals(userId, userIdFromAuthToken)) {
            throw new FollowException("You can not follow yourself");
        }

        UserEntity loggedInUser = getUserEntityById(userIdFromAuthToken);

        UserEntity followedUser = getUserEntityById(userId);

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
        UserEntity loggedInUser = getUserEntityById(userIdFromAuthToken);

        UserEntity followedUser = getUserEntityById(userId);

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

    public UserEntity getUserEntityById(Long userIdFromAuthToken) {
        return userRepository.findById(userIdFromAuthToken).orElseThrow(() ->
                new NotFoundException("User not found"));
    }

    public UpdateProfileResponse updateUser(UpdateProfileRequest request, Long userIdFromAuthToken, MultipartFile image) {
        if (!Objects.equals(request.userId(), userIdFromAuthToken)) {
            throw new IdMismatchException("User id must match");
        }

        UserEntity entity = getUserEntityById(userIdFromAuthToken);
        entity.setName(request.name());
        entity.setBio(request.bio());

        if (image != null) {
            entity.setProfilePicture(imageService.processImage(image));
        }

        userRepository.save(entity);

        return new UpdateProfileResponse(
                entity.getUserId(),
                entity.getName(),
                entity.getBio(),
                entity.getProfilePicture() == null ? null : entity.getProfilePicture().getImageUrl()
        );
    }

    public Page<UserDto> search(Map<String, String> params) {
        Pageable pageable = getPageable(params);
        String query = params.get("query");
        if (query == null || query.isEmpty()) {
            return userRepository.findAllByOrderByFollowersDesc(pageable)
                    .map(this::toDto);
        }
        return userRepository.findByNameContainingIgnoreCaseOrBioContainingIgnoreCase(query, query, pageable)
                .map(this::toDto);
    }

    public Page<UserDto> getFollowers(Long userId, Map<String, String> params) {
        Pageable pageable = getPageable(params);
        return userRepository.findByFollowingUserId(userId, pageable)
                .map(this::toDto);
    }

    public Page<UserDto> getFollowing(Long userId, Map<String, String> params) {
        Pageable pageable = getPageable(params);
        return userRepository.findByFollowersUserId(userId, pageable)
                .map(this::toDto);
    }

    private Pageable getPageable(Map<String, String> params) {
        return PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "12")));
    }

    private UserDto toDto(UserEntity entity) {
        return new UserDto(
                entity.getUserId(), entity.getName(), entity.getProfilePicture() == null ? null :
                entity.getProfilePicture().getImageUrl()
        );
    }

    @Transactional
    public void incrementViewCount(Long userId) {
        userRepository.incrementViewCount(userId);
    }
}

