package com.questionproassignment.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final Cache<String, Object> cache;

    public UserServiceImpl(@Autowired Cache<String, Object> cache) {
        this.cache = cache;
    }
    @Override
    public Object getTopStories() throws IOException, ExecutionException, InterruptedException {
        String cacheKey="top-stories";
        Object cachedData=cache.getIfPresent(cacheKey);
        if(cachedData!=null)
        {
            return cachedData;
        }
        List<Integer> topStoriesIdList = getStoryMaxId();
        List<Map<String,Object>> topTenStoriesList = new ArrayList<>();
        int nThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        for (int id : topStoriesIdList) {
            CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String storyApiUrl = "https://hacker-news.firebaseio.com/v0/item/" + id + ".json?print=pretty";
                    String responseBody = doApiCall(storyApiUrl).toString();
                    System.out.println(responseBody);
                    Map<String, Object> responseMapper=convertStringToMap(responseBody);
                    Map<String, Object> storyMapper = new LinkedHashMap<>();
                    storyMapper.put("submittedBy", responseMapper.get("by"));
                    storyMapper.put("title", responseMapper.get("title"));
                    storyMapper.put("url", responseMapper.get("url"));
                    storyMapper.put("score", responseMapper.get("score"));
                    storyMapper.put("timeOfSubmission", getDateTimeFromEpoch((Integer) responseMapper.get("time")));
                    return storyMapper;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }, executorService);
            futures.add(future);
        }
        for (CompletableFuture<Map<String, Object>> future : futures) {
            Map<String, Object> storyMapper = future.get();
            if (storyMapper != null) {
                topTenStoriesList.add(storyMapper);
            }
        }
        executorService.shutdown();
        Object response=sortTopTenStories(topTenStoriesList);
        cache.put(cacheKey,response);
        return response;
    }
    public Map<String, Object> convertStringToMap(String responseBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBody, Map.class);
    }

    @Override
    public Object getPastStories() {
        Object cachedData=cache.getIfPresent("top-stories");
        if(cachedData!=null)
        {
            return cachedData;
        }
        else
        {
            return new ArrayList<>();
        }
    }

    public List<Map<String,Object>> sortTopTenStories(List<Map<String,Object>> topTenStoriesList)
    {
        topTenStoriesList.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                int score1 = (int) stringObjectMap.get("score");
                int score2 = (int) t1.get("score");
                return score2 - score1;
            }
        });
        return topTenStoriesList;
    }
    public String getDateTimeFromEpoch(int epochTime) {
//        long epochTimeInLong=Long.parseLong(epochTime);
        Date date = new Date(epochTime * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        return sdf.format(date);
    }
    public List<Integer> getStoryMaxId() throws IOException {
        String maxIdApiUrl = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
        String responseBody = (String) doApiCall(maxIdApiUrl);
        List<Integer> topStories = Arrays.stream(responseBody.replaceAll("[\\[\\]\\s+]", "").split(","))
                .map(Integer::parseInt).sorted(Collections.reverseOrder()).collect(Collectors.toList());
        // Soring the list in descending order to g
        List<Integer> topTenStories = new ArrayList<>(topStories.subList(0, 10));
        return topTenStories;
    }
    public Object doApiCall(String apiUrl) throws IOException {
        // Create a new instance of CloseableHttpClient
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(apiUrl);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
        }
    }
    @Override
    public Object comments(int storyId) throws IOException, ExecutionException, InterruptedException {
        String storyApiUrl="https://hacker-news.firebaseio.com/v0/item/"+storyId+".json?print=pretty";
        String responseBody=(String) doApiCall(storyApiUrl);
        Map<String,Object> responseMapper=convertStringToMap(responseBody);
        List<Integer> comments= (List<Integer>) responseMapper.get("kids");
        comments.sort(Collections.reverseOrder());
        List<Integer> tenTopComments=comments.subList(0,10);
        return getComments(tenTopComments);
    }
    public Object getComments(List<Integer> tenTopComments) throws ExecutionException, InterruptedException {
        List<Map<String,Object>> topCommentList = new ArrayList<>();
        int nThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        System.out.println(tenTopComments);
        for (int commentId : tenTopComments) {
            CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String storyApiUrl = "https://hacker-news.firebaseio.com/v0/item/"+commentId+".json?print=pretty";
                    String responseBody = doApiCall(storyApiUrl).toString();
                    Map<String, Object> responseMapper=convertStringToMap(responseBody);
                    Map<String, Object> commentMapper = new HashMap<>();
                    List<Integer> subComments= (List<Integer>) responseMapper.get("kids");
                    commentMapper.put("text", responseMapper.get("text"));
                    commentMapper.put("userHackerNewsHandle", responseMapper.get("by"));
                    commentMapper.put("subCommentLength", subComments!=null ? subComments.size():0);
                    return commentMapper;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }, executorService);
            futures.add(future);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join(); // Wait for all futures to complete
        for (CompletableFuture<Map<String, Object>> future : futures) {
            Map<String, Object> commentMapper = future.get();
            if (commentMapper != null) {
                topCommentList.add(commentMapper);
            }
        }
        executorService.shutdown();
        System.out.println(sortTopComment(topCommentList));
        return sortTopComment(topCommentList);
    }
    public List<Map<String,Object>> sortTopComment(List<Map<String,Object>> topCommentList)
    {
        topCommentList.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                int subCommentLength1 = (int) stringObjectMap.get("subCommentLength");
                int subCommentLength2 = (int) t1.get("subCommentLength");
                return subCommentLength2 - subCommentLength1;
            }
        });
        return topCommentList;
    }
}
