package com.questionproassignment.Controller;
import com.questionproassignment.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


@RestController
public class UserController {

    @Autowired
    UserService userService;


    @GetMapping("/test")
    public String test() {
        return "All Ok!! Version 1.0";
    }

    @GetMapping("/top-stories")
    public Object topStories() throws IOException, ExecutionException, InterruptedException {
        return userService.getTopStories();
    }

    @GetMapping("/past-stories")
    public Object pastStories() throws IOException, ExecutionException, InterruptedException {
        return userService.getPastStories();
    }

    @GetMapping("/comments/{storyId}")
    public Object comments(@PathVariable int storyId) throws IOException, ExecutionException, InterruptedException {
        return userService.comments(storyId);
    }


}
