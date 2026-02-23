# 라이더 근접 도착 알림(“곧 도착”) 구현 가이드

현재 코드베이스는 이미 다음 기반 요소를 가지고 있습니다.

- 라이더 위치 업데이트 API: `POST /riders/{riderId}/location`
- 라이더 위치 조회 API: `GET /riders/{riderId}/location`
- SSE 기반 실시간 알림 패턴(라이더 배차 알림): `RiderDispatchNotificationService`
- 좌표 간 거리 계산 유틸: `Location.calculateDistanceInHaversineFormula`

이를 활용해 주문자에게 “배달이 얼마 안 남았음”을 알리려면 아래와 같이 설계하면 됩니다.

## 1) 알림 조건 정의

알림은 고정 거리 또는 ETA 기반으로 트리거합니다.

- **거리 기반(권장 시작점)**: 라이더-도착지 거리 <= 500m 일 때 알림
- **ETA 기반(확장)**: 지도 API 기반 남은 시간 <= 3분 일 때 알림

초기에는 운영 복잡도가 낮은 거리 기반부터 시작하고, 이후 ETA로 고도화하는 것이 안전합니다.

## 2) 이벤트 발생 지점

가장 자연스러운 트리거는 라이더 앱이 주기적으로 호출하는 위치 업데이트 시점입니다.

1. 라이더 앱이 3~5초마다 위치 전송
2. 서버가 `updateRiderLocation`에서 좌표 저장
3. 같은 트랜잭션(또는 직후)에서 배달 건 조회
4. 도착지 거리 계산 후 임계값 이하면 주문자 알림 발송

즉, 별도 스케줄러 없이도 “푸시형”으로 near-arrival 이벤트를 생성할 수 있습니다.

## 3) 중복 알림 방지(필수)

위치 업데이트가 자주 들어오기 때문에 중복 발송 방지 플래그가 필요합니다.

예시 필드:

- `Delivery.nearArrivalNotified` (boolean, default false)

발송 로직:

- 조건 충족 && `nearArrivalNotified == false` 일 때만 발송
- 발송 성공 후 `nearArrivalNotified = true`

## 4) 주문자 알림 채널

현재 프로젝트에 SSE 패턴이 이미 있으므로 동일하게 적용 가능합니다.

- 구독 API 예시: `GET /orders/{orderId}/notifications/subscribe`
- 이벤트명 예시: `delivery-near-arrival`
- payload 예시:
  - `orderId`
  - `deliveryId`
  - `riderId`
  - `remainingDistanceKm`
  - `message` (예: “라이더가 곧 도착합니다.”)

추후 모바일 푸시(Firebase/APNs)로 확장할 때도 같은 도메인 이벤트를 재사용할 수 있습니다.

## 5) 서버 처리 의사 코드

```java
public RiderLocationResponse updateRiderLocation(Long riderId, double latitude, double longitude) {
    Rider rider = riderRepository.findById(riderId)...;
    rider.updateLocation(latitude, longitude);

    List<Delivery> activeDeliveries = deliveryRepository
        .findByRiderIdAndStatusIn(riderId, List.of(DISPATCHED, PICKED_UP));

    for (Delivery delivery : activeDeliveries) {
        Location destination = delivery.getOrder().getDeliveryLocation();
        double distanceKm = rider.getLocation().calculateDistanceInHaversineFormula(destination);

        if (distanceKm <= 0.5 && !delivery.isNearArrivalNotified()) {
            customerNotificationService.notifyNearArrival(delivery, distanceKm);
            delivery.markNearArrivalNotified();
        }
    }

    return RiderLocationResponse.of(rider);
}
```

## 6) 운영 관점 체크리스트

- 위치 수집 주기: 3~5초(도심), 5~10초(배터리 절약)
- 오래된 위치 무시: `updatedAt` 기준 stale 데이터 필터링
- 상태 제한: `DELIVERED`, `CANCEL` 건은 제외
- 오차 완화: GPS 튐 방지를 위해 2회 연속 임계값 만족 시 발송(선택)
- 장애 대응: SSE 실패 시 emitter 정리 + 재연결 전략

## 7) 단계적 적용 순서

1. 거리 임계값 상수화 (`NEAR_ARRIVAL_THRESHOLD_KM = 0.5`)
2. Delivery 엔티티에 중복 발송 방지 필드 추가
3. 활성 배달 조회 repository 메서드 추가
4. 주문자용 SSE NotificationService/Controller 추가
5. `updateRiderLocation`에 near-arrival 검사/발송 연동
6. 통합 테스트(구독 후 위치 업데이트 -> 이벤트 수신) 추가

## 8) 한 줄 요약

**라이더 위치 업데이트 API를 이벤트 소스로 사용하고, 라이더-도착지 거리 임계값 + 중복 방지 플래그를 결합해 SSE로 주문자에게 “곧 도착” 알림을 보내는 구조가 가장 단순하고 안정적입니다.**
