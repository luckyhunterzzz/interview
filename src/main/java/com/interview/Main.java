package com.interview;

import com.interview.console.UserConsole;
import com.interview.controller.UserController;
import com.interview.repository.UserRepository;
import com.interview.service.UserService;
import com.interview.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {

        try {
            log.info("Starting user-service application");
            UserRepository userRepository = new UserRepository();
            UserService userService = new UserService(userRepository);
            UserController userController = new UserController(userService);
            UserConsole userConsole = new UserConsole(userController);
            userConsole.start();
        } catch (Exception e) {
            log.error("Application terminated with an error", e);
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
