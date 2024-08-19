package node.connection.hyperledger.fabric;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hyperledger.fabric.sdk.Channel;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ChannelManager implements RemovalListener<String, Channel> {
    static Long defaultDuration = 1L;
    static TimeUnit defaultTimeUnit = TimeUnit.HOURS;

    Cache<String, Channel> cache;

    public ChannelManager() {
        this(defaultDuration, defaultTimeUnit, null);
    }

    public ChannelManager(RemovalListener<String, Channel> removalListener) {
        this(defaultDuration, defaultTimeUnit, removalListener);
    }

    public ChannelManager(Long duration, TimeUnit unit) {
        this(duration, unit, null);
    }

    public ChannelManager(Long duration, TimeUnit unit, RemovalListener<String, Channel> removalListener) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(duration, unit)
                .maximumSize(10);
        caffeine.removalListener(Objects.requireNonNullElse(removalListener, this));
        cache = caffeine.build();
    }

    public void put(Channel channel) {
        cache.put(channel.getName(), channel);
    }

    public Channel get(String channelName) {
        return cache.getIfPresent(channelName);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void invalidate(String channelName) {
        Channel c = get(channelName);
        if (c != null) {
            cache.invalidate(c.getName());
        }
    }

    @Override
    public void onRemoval(@Nullable String key, @Nullable Channel value, @NonNull RemovalCause cause) {
        log.debug("onRemoval key:{}, channel:{}, cause:{}", key, value ,cause);
        Channel c = get(key);
        if (c == null && value != null) {
            log.debug("channel:{} is shutdown", value.getName());
            value.shutdown(true);
        }
    }
}
