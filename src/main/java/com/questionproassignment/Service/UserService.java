package com.questionproassignment.Service;


import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface UserService {
    public Object getTopStories() throws IOException, ExecutionException, InterruptedException;
    public Object getPastStories();
    public Object comments(int storyId) throws IOException, ExecutionException, InterruptedException;



}
