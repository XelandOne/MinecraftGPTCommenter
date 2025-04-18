package rs.meine.models;

import java.time.Instant;

/**
 * Class to track rate limiting information for a player
 */
public class RateLimitInfo {
    private int remainingRequests;
    private final Instant resetTime;
    
    /**
     * Creates a new rate limit info
     * @param maxRequests The maximum number of requests allowed in the time window
     * @param resetTime The time when the rate limit resets
     */
    public RateLimitInfo(int maxRequests, Instant resetTime) {
        this.remainingRequests = maxRequests;
        this.resetTime = resetTime;
    }
    
    /**
     * Gets the number of remaining requests
     * @return The number of remaining requests
     */
    public int getRemainingRequests() {
        return remainingRequests;
    }
    
    /**
     * Gets the time when the rate limit resets
     * @return The reset time
     */
    public Instant getResetTime() {
        return resetTime;
    }
    
    /**
     * Decrements the number of remaining requests
     */
    public void decrementRemainingRequests() {
        this.remainingRequests--;
    }
} 