package com.example.bossbot.util;

import com.example.bossbot.role.Role;
import com.example.bossbot.role.RoleName;
import com.example.bossbot.role.RoleRepository;
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
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws SQLException {
        userRepository.deleteAll(); // FIXME:: remove, if testing done, as it deletes all previous data on app restart

        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(RoleName.USER));
            roleRepository.save(new Role(RoleName.ADMIN));
        }
        List<User> users = new ArrayList<>();
        users.add(new User("Johan", "johan@someemail.com"));
        users.add(new User("Merily", "merily@someemail.com"));
        users.add(new User("Milli", "milly@someemail.com"));
        users.add(new User("Villi", "villi@someemail.com"));

        userRepository.saveAll(users);

    }
}