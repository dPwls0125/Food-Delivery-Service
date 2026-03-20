# Food-Delivery-Service API 명세

> 실제 구현된 컨트롤러 기준으로 작성된 API 문서입니다.

---

## 전체 흐름 요약

```
[USER]
  주문 생성           → POST /orders
  할인 미리보기       → POST /orders/{orderId}/discounts/preview
  결제 완료           → POST /payment/{orderId}
        ↓
[OWNER]
  라이더 매칭 요청   → POST /deliveries/{orderId}/dispatch
        ↓
[System / RIDER]
  배달 상태 변경      → POST /deliveries/{deliveryId}/status
  라이더 위치 전송    → POST /riders/{riderId}/location
        ↓
[USER 실시간 알림]
  SSE 구독           → GET /orders/{orderId}/notifications/subscribe
  라이더 위치 조회   → GET /riders/{riderId}/location
  현재 루트 조회     → GET /routes/me/current-route
```

---

## 1. 주문 생성

**POST** `/orders`

**역할:** USER

**Request Body**
```json
{
  "storeId": 101,
  "orderItems": [
    { "menuId": 1, "quantity": 2 },
    { "menuId": 3, "quantity": 1 }
  ],
  "deliveryAddress": "서울시 강남구 테헤란로 123"
}
```

**Response — 201 Created**
```json
{
  "orderId": 5001,
  "orderStatus": "CREATED",
  "totalPrice": 18000
}
```

> `Location` 헤더: `/api/orders/{orderId}`

---

## 2. 할인 미리보기

**POST** `/orders/{orderId}/discounts/preview`

**역할:** USER

### 할인 규칙

| 항목          | 내용                                      |
|---------------|-------------------------------------------|
| 배민클럽 할인 | **배달비**에만 적용 (배달비의 10% 할인)    |
| 쿠폰 할인     | **음식 가격**에만 적용                    |
| 쿠폰 사용 조건 | 최소 주문금액 15,000원 이상               |
| 최대 쿠폰 할인 | 5,000원                                   |
| 쿠폰 사용 수  | 1개만 적용 가능                           |
| 쿠폰 우선순위 | 최종 금액이 최소가 되는 쿠폰 자동 선택   |

### 배달 종류별 배달비

| 배달 종류        | 코드     |
|------------------|----------|
| 한집배달         | `SINGLE` |
| 알뜰배달(묶음)   | `BUNDLE` |

**Path Variable**

| 이름      | 타입   | 설명  |
|-----------|--------|-------|
| `orderId` | Long   | 주문 ID |

**Request Body**
```json
{
  "couponId": 10,
  "useBaeminClub": true
}
```

**Response — 200 OK**
```json
{
  "orderId": 5001,
  "originalPrice": 18000,
  "discountDetails": {
    "couponDiscount": 3000,
    "baeminClubDiscount": 1500
  },
  "totalDiscountAmount": 4500,
  "finalPrice": 13500
}
```

---

## 3. 주문 결제

**POST** `/payment/{orderId}`

**역할:** USER

**Path Variable**

| 이름      | 타입 | 설명  |
|-----------|------|-------|
| `orderId` | Long | 주문 ID |

**Request Body**
```json
{
  "paymentMethod": "CARD",
  "couponId": 10,
  "useBaeminClub": true
}
```

**Response — 200 OK**
```json
{
  "orderId": 5001,
  "orderStatus": "PAID",
  "paymentStatus": "SUCCESS",
  "originalPrice": 18000,
  "discountAmount": 4500,
  "paidAmount": 13500,
  "paymentMethod": "CARD",
  "paidAt": "2026-01-05T15:10:00"
}
```

---
 
## 4. 라이더 매칭 요청 (Owner)

**POST** `/deliveries/{orderId}/dispatch`

**역할:** OWNER

**Path Variable**

| 이름      | 타입 | 설명  |
|-----------|------|-------|
| `orderId` | Long | 주문 ID |

**Request Body**
```json
{
  "deliveryType": "BUNDLE"
}
```

| `deliveryType` 값 | 의미       |
|-------------------|------------|
| `SINGLE`          | 한집배달   |
| `BUNDLE`          | 알뜰배달   |

**Response — 200 OK**
```json
{
  "orderId": 5001,
  "dispatchStatus": "REQUESTED",
  "deliveryType": "BUNDLE"
}
```

---

## 5. 배달 상태 변경 (Rider)

**POST** `/deliveries/{deliveryId}/status`

**역할:** RIDER

**Path Variable**

| 이름         | 타입 | 설명     |
|--------------|------|----------|
| `deliveryId` | Long | 배달 ID |

**Request Body**
```json
{
  "status": "PICKED_UP"
}
```

**사용 가능한 상태값 (DeliveryStatus)**

| 값           | 설명           |
|--------------|----------------|
| `PICKED_UP`  | 음식 픽업 완료 |
| `DELIVERED`  | 배달 완료      |

**Response — 200 OK**
```json
{
  "deliveryId": 1,
  "deliveryStatus": "PICKED_UP"
}
```

---

## 6. 현재 배달 루트 조회 (Rider)

**GET** `/routes/me/current-route`

**역할:** RIDER

**Response — 200 OK**
```json
{
  "stops": [
    { "sequence": 1, "type": "PICKUP",   "orderId": 5001, "address": "가게 A" },
    { "sequence": 2, "type": "PICKUP",   "orderId": 5002, "address": "가게 B" },
    { "sequence": 3, "type": "DELIVERY", "orderId": 5001, "address": "고객 A" },
    { "sequence": 4, "type": "DELIVERY", "orderId": 5002, "address": "고객 B" }
  ]
}
```

| `type` 값  | 의미         |
|------------|--------------|
| `PICKUP`   | 가게 픽업    |
| `DELIVERY` | 고객 배달    |

---

## 7. 주문 상세 조회 (Role-based)

**GET** `/orders/{orderId}`

**역할:** USER / OWNER / RIDER (역할에 따라 응답 뷰 분기 예정)

**Path Variable**

| 이름      | 타입 | 설명  |
|-----------|------|-------|
| `orderId` | Long | 주문 ID |

**Response — 200 OK**
```json
{
  "orderId": 5001,
  "store": {
    "name": "김밥천국",
    "address": "서울시 강남구 ..."
  },
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "orderStatus": "PICKED_UP",
  "items": [
    { "name": "김밥", "quantity": 2 },
    { "name": "라면", "quantity": 1 }
  ],
  "customerNote": "문 앞에 놔주세요"
}
```

> **TODO:** 역할(Role)에 따른 응답 DTO 분기 처리 필요

---

## 8. 라이더 위치 업데이트

**POST** `/riders/{riderId}/location`

**역할:** RIDER

**Path Variable**

| 이름      | 타입 | 설명     |
|-----------|------|----------|
| `riderId` | Long | 라이더 ID |

**Request Body**
```json
{
  "latitude": 37.498095,
  "longitude": 127.027610
}
```

**Response — 201 Created**
```json
{
  "riderId": 5001,
  "latitude": 37.498095,
  "longitude": 127.027610,
  "lastUpdatedAt": "2026-01-05T16:15:00"
}
```

---

## 9. 사용자의 라이더 위치 조회

**GET** `/riders/{riderId}/location`

**역할:** USER

**Path Variable**

| 이름      | 타입 | 설명     |
|-----------|------|----------|
| `riderId` | Long | 라이더 ID |

**Response — 200 OK**
```json
{
  "riderId": 5001,
  "latitude": 37.498095,
  "longitude": 127.027610,
  "lastUpdatedAt": "2026-01-05T16:15:00"
}
```

---

## 10. 실시간 알림 (SSE)

### 주문 알림 구독 (USER)

**GET** `/orders/{orderId}/notifications/subscribe`

- `Content-Type: text/event-stream`
- SSE를 통해 배달 상태 이벤트 수신

**이벤트 종류**

| 이벤트           | 설명                    |
|------------------|-------------------------|
| `ORDER_PICKED_UP` | 라이더가 음식 픽업 완료 |
| `ORDER_NEARBY`    | 라이더가 근처 도착      |
| `ORDER_DELIVERED` | 배달 완료               |

---

### 라이더 배차 알림 구독 (RIDER)

**GET** `/riders/{riderId}/notifications/subscribe`

- `Content-Type: text/event-stream`
- 새 배차 요청 발생 시 SSE 이벤트 수신

---

## 엔드포인트 요약

| 메서드 | 경로                                     | 역할   | 설명                      |
|--------|------------------------------------------|--------|---------------------------|
| POST   | `/orders`                                | USER   | 주문 생성                 |
| POST   | `/orders/{orderId}/discounts/preview`    | USER   | 할인 미리보기             |
| GET    | `/orders/{orderId}`                      | ALL    | 주문 상세 조회            |
| GET    | `/orders/{orderId}/notifications/subscribe` | USER | 주문 알림 SSE 구독      |
| POST   | `/payment/{orderId}`                     | USER   | 결제 처리                 |
| POST   | `/deliveries/{orderId}/dispatch`         | OWNER  | 라이더 매칭 요청          |
| POST   | `/deliveries/{deliveryId}/status`        | RIDER  | 배달 상태 변경            |
| GET    | `/routes/me/current-route`               | RIDER  | 현재 배달 루트 조회       |
| POST   | `/riders/{riderId}/location`             | RIDER  | 라이더 위치 업데이트      |
| GET    | `/riders/{riderId}/location`             | USER   | 라이더 위치 조회          |
| GET    | `/riders/{riderId}/notifications/subscribe` | RIDER | 배차 알림 SSE 구독     |
