package com.userService.user.Service;

import com.userService.user.Dtos.UserRequestDto;
import com.userService.user.Dtos.UserResponseDto;

public interface UserService {

    UserResponseDto registerUserDetails(UserRequestDto requestDto);

    UserResponseDto getUserDetails(Long userId);

}
