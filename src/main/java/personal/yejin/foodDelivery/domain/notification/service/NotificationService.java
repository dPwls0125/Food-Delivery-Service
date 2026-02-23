package personal.yejin.foodDelivery.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter subscribe(Long riderId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		emitters.put(riderId, emitter);

		emitter.onCompletion(() -> emitters.remove(riderId));
		emitter.onTimeout(() -> emitters.remove(riderId));

		try {
			emitter.send(SseEmitter.event()
				.name("connect")
				.data("connected!"));
		} catch (IOException e) {
			log.error("SSE 연결 중 오류 발생: {}", e.getMessage());
			emitters.remove(riderId);
		}

		return emitter;
	}

	public void notify(Long riderId, String eventName, Object data) {
		SseEmitter emitter = emitters.get(riderId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.name(eventName)
					.data(data));
			} catch (IOException e) {
				log.error("알림 전송 중 오류 발생: {}", e.getMessage());
				emitters.remove(riderId);
			}
		}
	}
}
