package edu.baylor.flarn.services;

import edu.baylor.flarn.models.User;
import edu.baylor.flarn.models.UserType;
import edu.baylor.flarn.repositories.UserRepository;
import edu.baylor.flarn.resources.UserRegistration;
import edu.baylor.flarn.resources.UserUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User registerUser(UserRegistration userRegistration) {
        return userRepository.save(userRegistration.toUser(passwordEncoder));
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUserByType(UserType userType) {
        return userRepository.findByUserType(userType);
    }

    public Optional<User> getUser(long Id) {
        return userRepository.findById(Id);
    }

    /**
     * Method to promote/demote or change usertype.
     *
     * @param
     * @param userUpdate
     */
    public int changeUserType(UserUpdate userUpdate) {
        AtomicInteger status = new AtomicInteger();
        Optional<User> optional = userRepository.findById(userUpdate.getId());
        optional.ifPresent(user -> {
            if (!user.getUserType().equals(userUpdate.getUserType())) {
                user.setUserType(userUpdate.getUserType());
                status.set(userRepository.setUserTypeId(user.getId(), userUpdate.getUserType()));
            }

        });
        return status.get();
    }

    public List<User> getSubscribedUsers(long Id) {
        return userRepository.findSubscribedUsers(Id);
    }

    public List<User> getUserSubscriptions(long Id) {
        return userRepository.findUserSubscriptions(Id);
    }


    // TODO: adding subscription repository
}
