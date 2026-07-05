package com.userService.user.Controller;

import com.userService.user.Dtos.UserRequestDto;
import com.userService.user.Dtos.UserResponseDto;
import com.userService.user.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponseDto registerUserDetails(@RequestBody UserRequestDto requestDto) {
        LOGGER.info("[UserController] - User Registration request received");
        return userService.registerUserDetails(requestDto);
    }

    @GetMapping("/getDetails/{id}")
    public UserResponseDto getUserDetails(@PathVariable("id") Long userId) {
        LOGGER.info("[UserController] - User details fetching request Received");
        return userService.getUserDetails(userId);
    }


}
