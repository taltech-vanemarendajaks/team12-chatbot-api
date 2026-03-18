package com.example.bossbot.util;

import com.example.bossbot.user.User;
import com.example.bossbot.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create some initial demo / mock data for the app.
 * The class may be deleted if you do not want the demo data anymore
 */
@Component
@AllArgsConstructor
public class DbInitialize implements CommandLineRunner {
    UserRepository userRepository;

    @Override
    public void run(String... args) throws SQLException {
        userRepository.deleteAll(); // FIXME:: remove, if testing done, as it deletes all previous data on app restart
        List<User> users = new ArrayList<>();
        users.add(new User("Johan"));
        users.add(new User("Merily"));
        users.add(new User("Milli"));
        users.add(new User("Villi"));

        userRepository.saveAll(users);

    }
}