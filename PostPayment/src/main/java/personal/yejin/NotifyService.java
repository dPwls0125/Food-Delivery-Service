package personal.yejin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NotifyService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30;

    public SseEmitter subscribe(Long storeId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(storeId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE emitter 완료됨. storeId={}", storeId);
            emitters.remove(storeId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE emitter 타임아웃됨. storeId={}", storeId);
            emitters.remove(storeId);
        });




        return null;
    }
}

