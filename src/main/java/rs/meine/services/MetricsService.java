package rs.meine.services;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Service for tracking plugin usage metrics
 */
public class MetricsService {
    private final JavaPlugin plugin;
    private final Logger logger;
    
    // Metrics counters
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final Map<UUID, Integer> requestsPerPlayer = new HashMap<>();
    
    /**
     * Creates a new MetricsService
     * @param plugin The plugin instance
     */
    public MetricsService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        // Start metrics reporting task
        startMetricsReporting();
    }
    
    /**
     * Starts a scheduled task to periodically log metrics
     */
    private void startMetricsReporting() {
        // Log metrics every 30 minutes (36000 ticks)
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::reportMetrics, 36000, 36000);
    }
    
    /**
     * Records a successful API request
     * @param playerUUID The UUID of the player who made the request
     */
    public void recordSuccessfulRequest(UUID playerUUID) {
        totalRequests.incrementAndGet();
        successfulRequests.incrementAndGet();
        
        requestsPerPlayer.compute(playerUUID, (uuid, count) -> count == null ? 1 : count + 1);
    }
    
    /**
     * Records a failed API request
     * @param playerUUID The UUID of the player who made the request
     */
    public void recordFailedRequest(UUID playerUUID) {
        totalRequests.incrementAndGet();
        failedRequests.incrementAndGet();
    }
    
    /**
     * Reports current metrics to the log
     */
    private void reportMetrics() {
        int total = totalRequests.get();
        int success = successfulRequests.get();
        int failed = failedRequests.get();
        int uniquePlayers = requestsPerPlayer.size();
        
        logger.info(String.format("MetricsService Report: Total requests: %d, Successful: %d, Failed: %d, Unique players: %d", 
            total, success, failed, uniquePlayers));
        
        // Reset counters after reporting
        if (total > 10000) {  // Only reset if there's a significant number to avoid losing data
            totalRequests.set(0);
            successfulRequests.set(0);
            failedRequests.set(0);
            requestsPerPlayer.clear();
        }
    }
    
    /**
     * Gets the total number of requests
     * @return The total number of requests
     */
    public int getTotalRequests() {
        return totalRequests.get();
    }
    
    /**
     * Gets the number of successful requests
     * @return The number of successful requests
     */
    public int getSuccessfulRequests() {
        return successfulRequests.get();
    }
    
    /**
     * Gets the number of failed requests
     * @return The number of failed requests
     */
    public int getFailedRequests() {
        return failedRequests.get();
    }
    
    /**
     * Gets the number of unique players who have made requests
     * @return The number of unique players
     */
    public int getUniquePlayers() {
        return requestsPerPlayer.size();
    }
} 