## 로그인
#### 1) 로그인 → POST /api/v1/login
&#8209; JWT 인증 필요 없음 <br>
&#8209; 유효성 검사 → 입력 여부, 이메일 형식 <br>
&#8209; 스프링 시큐리티에서 LoginFilter 를 구현하여 로그인 처리 <br>
&#8209; 로그인이 완료되면 Access Token 과 Refresh Token 발급 <br>
&#8209; Access Token 과 Refresh Token 에는 username, roles, memberId 를 페이로드로 한다 <br>
&#8209; Access Token → 유효기간은 10분으로 클라이언트에 JSON 으로 전송하여 sessionStorage 에 저장 <br>
&#8209; Refresh Token → 유효기간은 30분으로 Redis (DB 1) 에 저장하고 브라우저의 쿠키에 저장 <br>
&#8209; Redis Key → refreshToken:member:{memberId} <br>
&#8209; 로그인을 완료하면 SseEmitter 를 생성하여 메모리의 Map 객체에서 접속 상태를 관리하고 알림을 전송 <br>
&#8209; 클라이언트의 Header 영역에서 EventSource 를 연결 <br>

<br>
<br>

## SSE 관리
#### 1) 구독하기 → GET /api/v1/sse/subscribe?memberId={memberId}
&#8209; JWT 인증 필요 없음 <br>
&#8209; 로그인을 성공하면 사용자 본인 (memberId) 을 구독한다 <br>
&#8209; 본인 (memberId) 로 구독을 해야 본인에게 전송되는 알림을 받을 수 있다 <br>
&#8209; 클라이언트는 헤드 영역에서 EventSource 객체로 SSE 객체 이벤트를 리스닝한다 <br>

<br>
<br>

## 비밀번호 찾기
#### 1) 임시 비밀번호 전송 → POST /api/v1/members/send-password
&#8209; JWT 인증 필요 없음 <br>
&#8209; 로그인 페이지에서 이메일을 입력하고 비밀번호 찾기 버튼 클릭 <br>
&#8209; 비밀번호를 찾는 대신 임시 비밀번호로 데이터베이스에서 변경하여 로그인하도록 한다 <br>
&#8209; 개발 환경에서 테스트시 이메일 대신 메세지 창으로 임시 비밀번호 표시 <br>

<br>
<br>

## 로그아웃
#### 1) 로그아웃 → POST /api/v1/logout
&#8209; JWT 인증 필요 없음 <br>
&#8209; 클라이언트에서 sessionStorage 에 있는 Access Token 제거 <br>
&#8209; 서버에서 Refresh Token 쿠키 제거 <br>
&#8209; 서버에서 Redis (DB 1) 에 저장된 Refresh Token 제거 <br>
&#8209; 로그인 후, 메모리의 Map 객체에 저장된 SseEmitter 제거 <br>

<br>
<br>

## 회원 관리
#### 1) 테이블
&#8209; `member (1)-(1) profile_image` <br>

#### 2) 회원 등록 → POST /api/v1/members
&#8209; JWT 인증 필요 없음 <br>
&#8209; 로그인 페이지에서 회원가입 버튼 클릭 <br>
&#8209; 입력 값 → 이메일, 비밀번호 <br>
&#8209; 유효성 검사 → 입력 여부, 이메일 형식, 중복 이메일 <br>
&#8209; 이메일 인증 → 인증 코드를 메일로 전송하고 Redis (DB 0) 에 저장 후, 비교 <br>
&#8209; Redis Key → join:{email} <br>
&#8209; 개발 환경에서 테스트시 이메일 대신 메세지 창으로 인증코드 표시 <br>
&#8209; 비밀번호 → 영어, 숫자가 포함되고 10자리 이상
&#8209; 프로필 이미지 → 기본 이미지 <br>
&#8209; 반환 값 → 닉네임, 가입일, 정보 수정일, 프로필 이미지 파일 정보 <br>

#### 3) 이메일 중복 확인 → POST /api/v1/members/emails/check
&#8209; JWT 인증 필요 없음 <br>
&#8209; 회원 등록 중 이메일이 중복 여부 확인 <br>

#### 4) 인증 코드 발송 → POST /api/v1/members/codes
&#8209; JWT 인증 필요 없음 <br>
&#8209; 유효성 검사 → Client 에서 이메일 형식 확인 <br>
&#8209; 인증 코드를 입력된 이메일 주소로 발송하고 Redis (DB 0) 에 3분간 저장 <br>
&#8209; 인증 코드를 재발송하면 기존의 인증 코드는 Redis (DB 0) 에서 자동으로 덮어쓴다 <br>

#### 5) 인증 코드 확인 → POST /api/v1/members/codes/check
&#8209; JWT 인증 필요 없음 <br>
&#8209; 유효성 검사 → 인증 코드 유효 시간, 잘못된 인증 코드 <br>
&#8209; 인증 링크를 통해 전송된 인증 코드를 Redis (DB 0) 에 저장된 인증 코드와 비교 <br>

#### 6) 회원 조회 → GET /api/v1/members/{username}
&#8209; JWT 인증 필요 <br>
&#8209; 유효성 검사 → 회원 존재 여부 <br>
&#8209; 반환 값 → 닉네임, 가입일, 정보 수정일, 프로필 이미지 파일 정보 <br>

#### 7) 회원 목록 → GET /api/v1/members/pages/{page}
&#8209; JWT 인증 필요 <br>
&#8209; 유효성 검사 → 1보다 작은 페이지 입력 여부 <br>
&#8209; 정렬 → email 로 오름차순 <br>
&#8209; 페이지네이션 → 1페이지 당 10개 목록 <br>

#### 8) 권한 변경 → PATCH /api/v1/members/{memberId}/roles
&#8209; JWT 인증 필요 <br>
&#8209; ADMIN 권한을 가진 회원이 관리자 페이지의 회원관리에서 회원 권한을 변경할 수 있다 <br>
&#8209; 사용자 본인이 본인 권한은 변경할 수 없다 <br>

#### 9) 회원 수정 → PATCH /api/v1/members/{username}
&#8209; JWT 인증 필요 <br>
&#8209; 변경 값 → 닉네임, 비밀번호, 프로필 이미지 <br>
&#8209; 유효성 검사 → 입력 여부, 중복 닉네임 <br>
&#8209; 비밀번호 → 영어, 숫자가 포함되고 10자리 이상
&#8209; 반환 값 → 닉네임, 가입일, 정보 수정일, 프로필 이미지 파일 정보 <br>
&#8209; Access Token 에 닉네임을 포함하므로 재발급해야 한다 <br>

#### 10) 닉네임 중복 확인 → PATCH /api/v1/members/{username}
&#8209; JWT 인증 필요 <br>
&#8209; 변경 값 → 닉네임, 비밀번호, 프로필 이미지 <br>
&#8209; 유효성 검사 → 입력 여부, 중복 닉네임 <br>
&#8209; 비밀번호 → 영어, 숫자가 포함되고 10자리 이상
&#8209; 반환 값 → 닉네임, 가입일, 정보 수정일, 프로필 이미지 파일 정보 <br>
&#8209; Access Token 에 닉네임을 포함하므로 재발급해야 한다 <br>

#### 11) 회원 삭제 → DELETE /api/v1/members/{email}
&#8209; JWT 인증 필요 <br>
&#8209; 회원의 활동 내역을 모두 삭제하고 복구할 수 없다 <br>
&#8209; 회원 삭제는 사용자가 회원 정보 화면에서 회원 탈퇴하거나 관리자가 회원 목록 화면에서 삭제 가능 <br>
&#8209; 유효성 검사 → 회원 존재 여부 <br>
&#8209; 회원 삭제 시, 서버의 저장소에 저장된 프로필 파일, 리뷰 파일을 모두 삭제해야 한다 <br>

#### 12) 프로필 이미지 파일 저장 → POST /api/v1/members/profile-images
&#8209; JWT 인증 필요 <br>
&#8209; 서버 저장 경로 → 서버 저장소의 드라이브 > uploads > profiles > {memberId} <br>
&#8209; 기본 이미지 파일 → 프로젝트의 src > main > resources > static > images > profiles > default.png <br>
&#8209; 유효성 검사 → 파일 첨부 여부, 파일 확장자는 png, jpg 만 가능 <br>
&#8209; 반환 값 → 저장된 이미지 파일 정보 <br>
&#8209; 회원 가입 시, 기본 이미지로 저장되므로 PNG 파일의 이름이 default 인 파일은 저장할 수 없다 <br>
&#8209; 프로필 이미지 선택 중 임시 파일이 서버에 저장되고 변경을 하면 임시 파일은 삭제한다 <br>

#### 13) 프로필 이미지 파일 조회 → GET /api/v1/members/profile-images/{memberId}/{fileName}
&#8209; JWT 인증 필요 없음 <br>
&#8209; 이미지 파일은 글 화면에서 표시되므로 JWT 인증 없이 누구나 확인할 수 있어야 한다 <br>
&#8209; 서버 저장 경로 → 서버 저장소의 드라이브 > uploads > profiles > {memberId} <br>

#### 14) 프로필 이미지 파일 삭제 → DELETE /api/v1/members/profile-images/{memberId}
&#8209; JWT 인증 필요 <br>
&#8209; 서버 저장 경로 → 서버 저장소의 드라이브 > uploads > profiles > {memberId} <br>

<br>
<br>

## 리뷰 관리
#### 1) 테이블
&#8209; `member (1)-(*) Review (1)-(*) review_image` <br>

#### 2) 리뷰 등록 → POST /api/v1/reviews
&#8209; JWT 인증 필요 <br>
&#8209; 유효성 검사 → 장소이름, 장소위치, 제목, 내용 (글자 + 이미지 1개), 평점 <br>
&#8209; 서버 저장 경로 → 서버 저장소의 드라이브 > uploads > reviews > {memberId} > {reviewId} <br>
&#8209; 리뷰 내용의 이미지는 글을 작성 중일 때, uploads > temps > {memberId} 에 저장된다 <br>
&#8209; 리뷰를 등록하면 필요한 이미지는 reviews 폴더로 옮겨지고 필요없는 temps 폴더는 스케쥴러로 삭제 <br>
&#8209; 스케쥴러는 24시간에 1번씩 동작 → DeleteScheduler <br>
&#8209; 메인 페이지의 리뷰 목록에서는 썸네일이 보여야하므로 글에는 이미지가 반드시 1개 필요 <br>
&#8209; 리뷰 조회 페이지에서 KaKao Map API 표시를 위하여 KaKao 주소 API 로 장소 위치 입력<br>

#### 3) 임시 이미지 저장 → POST /api/v1/reviews/content-images/temp/{memberId}
&#8209; JWT 인증 필요 <br>
&#8209; 서버 저장 경로 → 서버 저장소의 드라이브 > uploads > temps > {memberId} <br>
&#8209; 글 작성 중일 때, 표시를 하기 위해 필요한 API 이다 <br>

#### 4) 임시 이미지 조회 → GET /api/v1/reviews/content-images/temp/{memberId}/{tempImageFileName}
&#8209; JWT 인증 필요 없음 <br>
&#8209; 서버 저장 경로 → 서버 저장소의 드라이브 > uploads > temps > {memberId} <br>
&#8209; 글 작성 중일 때, 표시를 하기 위해 필요한 API 이다 <br>

#### 5) 리뷰 이미지 조회 → GET /api/v1/reviews/content-images/temp/{memberId}/{tempImageFileName}
&#8209; JWT 인증 필요 없음 <br>
&#8209; 서버 저장 경로 → 서버 저장소의 드라이브 > uploads > reviews > {memberId} > {reviewId} <br>
&#8209; 작성이 완료된 리뷰를 조회할 때, 필요한 API 이다 <br>

#### 6) 리뷰 목록 → GET /api/v1/reviews/pages/{page}
&#8209; JWT 인증 필요 없음 <br>
&#8209; 메인 페이지에서 보여지는 리뷰 목록으로 로그인 사용자 및 비로그인 사용자가 모두 볼 수 있다 <br>
&#8209; 정렬 옵션 → 전체, 최신순, 평점순 <br>
&#8209; 검색했을 경우, 최신순으로 정렬하여 반환 <br>

#### 7) 로그인 후, 내 정보 페이지에서 리뷰 목록 → GET /api/v1/reviews/my/{memberId}/pages/{page}
&#8209; JWT 인증 필요 <br>
&#8209; 로그인 후, 사용자가 작성한 리뷰 목록 <br>
&#8209; 등록일, 좋아요 수, 조회 수 등을 확인할 수 있다 <br>
&#8209; 리뷰 목록에서 개별 삭제, 일괄 삭제를 할 수 있다 <br>

#### 8) 리뷰 조회 → GET /api/v1/reviews/{memberId}
&#8209; JWT 인증 필요 없음 <br>
&#8209; 리뷰 목록에서 리뷰 내용을 확인 <br>
&#8209; 장소이름, 장소위치, 평점, 작성자 이미지, 작성자 닉네임, 리뷰 제목, 리뷰 내용 표시 <br>
&#8209; 장소위치에 해당하는 곳을 KaKao Map API 를 사용하여 표시 <br>
&#8209; 좋아요, 구독하기 기능이 있으며 로그인이 필요하다 <br>
&#8209; 로그인한 사용자는 본인 리뷰에 좋아요, 구독하기 버튼이 없으며 편집 버튼이 활성화된다 <br>

#### 9) 리뷰 수정 → PATCH /api/v1/reviews/{memberId}
&#8209; JWT 인증 필요 <br>
&#8209; 로그인 한 사용자가 본인 글을 수정 <br>
&#8209; 유효성 검사 및 수정 로직은 리뷰 등록과 동일 <br>

#### 10) 리뷰 개별 삭제 → DELETE /api/v1/reviews/{memberId}
&#8209; JWT 인증 필요 <br>
&#8209; 하나의 리뷰를 삭제 <br>
&#8209; 로그인 한 사용자가 리뷰 수정 페이지 또는 내 정보의 내가 쓴 리뷰 목록에서 삭제 <br>

#### 11) 리뷰 일괄 삭제 → DELETE /api/v1/reviews
&#8209; JWT 인증 필요 <br>
&#8209; 로그인 한 사용자가 내 정보의 내가 쓴 리뷰 목록에서 선택한 리뷰를 일괄삭제 <br>

<br>
<br>

## 좋아요 관리
#### 1) 테이블

<br>
<br>

## 구독 관리
#### 1) 테이블

<br>
<br>

## 채팅 관리
#### 1) 테이블
&#8209; `member (1)-(*) chat_participant (*)-(1) chat_room ` <br>
&#8209; `member (1)-(*) chat_message ` <br>
&#8209; `chat_room (1)-(*) chat_message ` <br>
&#8209; `chat_message (1)-(*) chat_message_read ` <br>

<br>
<br>

## 공지 관리
#### 1) 테이블
&#8209; `member (1)-(*) notice` <br>

#### 2) 공지 발송 → POST /api/v1/notices
&#8209; JWT 인증 필요 <br>
&#8209; ADMIN 권한을 가진 회원이 관리자 페이지의 공지하기에서 전체 회원에게 공지를 발송할 수 있다 <br>
&#8209; 유효성 검사 → 입력 여부 <br>
&#8209; 전체 회원에게 공지를 전송하고 동시에 다른 관리자가 공지를 할 수 있으므로 비동기로 처리한다 <br>
&#8209; 공지 전송 → 메세지 저장 (RDB) + MQ (Notice Queue) → 메세지 저장 (Redis - DB 2) + SSE 에 연결 중인 회원에게 실시간 알림 <br>
&#8209; 로그인을 하지 않은 회원은 Redis (DB 2) 에 저장된 알림을 알림 내역에서 확인하고 마이 페이지의 공지 사항에서 공지 확인 <br>

#### 3) 공지 목록 → GET /api/v1/notices/pages/{page}
&#8209; JWT 인증 필요 <br>
&#8209; ADMIN 권한을 가진 회원이 관리자 페이지의 공지하기에서 공지 내역을 확인할 수 있다 <br>
&#8209; USER 권한을 가진 회원이 마이 페이지의 공지사항에서 공지 내역을 확인할 수 있다 <br>

<br>
<br>

## 알림 관리
#### 1) 좋아요 알림 → Exchange (ntf.exchange.topic) > Routing Key (ntf.like) > Queue (ntf.queue.like)
&#8209; JWT 인증 필요 없음 (로그인을 요구하는 기능이므로 알림 메세지 전송시, Access Token 검증 안하는 방향으로 결정) <br>
&#8209; 사용자가 리뷰를 조회하고 좋아요 버튼을 클릭하면 리뷰 작성자에게 알림이 전송된다 <br>
&#8209; 알림이 전송될 때, RDB 에 알림이 저장되고 알림을 수신하는 측에서 Redis 에 알림을 캐시한다 <br>
&#8209; 리뷰 작성자가 로그인 중이라면 SSE 를 통해 실시간 알림이 전송되고 그렇지 않으면 추후 로그인 시, Redis (DB 2) 확인 <br>
&#8209; Redis Key → notification:consumer:{memberId} <br>
&#8209; 12시간 마다 Redis 에 캐시된 알림 메세지가 7일 전이면 삭제한다 <br>
&#8209; Redis 에 캐시된 알림이 없으면 RDB 를 한번 더 확인하여 읽지 않은 알림이 있는지 확인한다 <br>

#### 2) 구독 알림 → Exchange (ntf.exchange.topic) > Routing Key (ntf.follow) > Queue (ntf.queue.follow)
&#8209; JWT 인증 필요 없음 (로그인을 요구하는 기능이므로 알림 메세지 전송시, Access Token 검증 안하는 방향으로 결정) <br>
&#8209; 사용자가 리뷰를 조회하고 구독 버튼을 클릭하면 리뷰 작성자에게 알림이 전송된다 <br>
&#8209; 알림이 전송될 때, RDB 에 알림이 저장되고 알림을 수신하는 측에서 Redis 에 알림을 캐시한다 <br>
&#8209; 리뷰 작성자가 로그인 중이라면 SSE 를 통해 실시간 알림이 전송되고 그렇지 않으면 추후 로그인 시, Redis (DB 2) 확인 <br>
&#8209; Redis Key → notification:consumer:{memberId} <br>
&#8209; 12시간 마다 Redis 에 캐시된 알림 메세지가 7일 전이면 삭제한다 <br>
&#8209; Redis 에 캐시된 알림이 없으면 RDB 를 한번 더 확인하여 읽지 않은 알림이 있는지 확인한다 <br>

#### 3) 새 글 알림 → Exchange (ntf.exchange.topic) > Routing Key (ntf.post) > Queue (ntf.queue.post)
&#8209; JWT 인증 필요 없음 (로그인을 요구하는 기능이므로 알림 메세지 전송시, Access Token 검증 안하는 방향으로 결정) <br>
&#8209; 사용자가 리뷰를 작성하면 구독자에게 알림이 전송된다 <br>
&#8209; 알림이 전송될 때, RDB 에 알림이 저장되고 알림을 수신하는 측에서 Redis 에 알림을 캐시한다 <br>
&#8209; 구독자가 로그인 중이라면 SSE 를 통해 실시간 알림이 전송되고 그렇지 않으면 추후 로그인 시, Redis (DB 2) 확인 <br>
&#8209; Redis Key → notification:consumer:{memberId} <br>
&#8209; 12시간 마다 Redis 에 캐시된 알림 메세지가 7일 전이면 삭제한다 <br>
&#8209; Redis 에 캐시된 알림이 없으면 RDB 를 한번 더 확인하여 읽지 않은 알림이 있는지 확인한다 <br>
&#8209; 여러 사용자가 새 글을 동시에 작성해서 많은 구독자에게 알림을 보낼 수 있기 때문에 비동기로 처리한다 <br>

#### 4) 전체 공지 알림 → Exchange (ntf.exchange.topic) > Routing Key (ntf.notice) > Queue (ntf.queue.notice)
&#8209; JWT 인증 필요 없음 (로그인을 요구하는 기능이므로 알림 메세지 전송시, Access Token 검증 안하는 방향으로 결정) <br>
&#8209; ADMIN 권한을 가진 사용자가 전체 회원에게 공지를 전송한다 <br>
&#8209; 알림이 전송될 때, RDB 에 알림이 저장되고 알림을 수신하는 측에서 Redis 에 알림을 캐시한다 <br>
&#8209; 회원이 로그인 중이라면 SSE 를 통해 실시간 알림이 전송되고 그렇지 않으면 추후 로그인 시, Redis (DB 2) 확인 <br>
&#8209; Redis Key → notification:consumer:{memberId} <br>
&#8209; 12시간 마다 Redis 에 캐시된 알림 메세지가 7일 전이면 삭제한다 <br>
&#8209; Redis 에 캐시된 알림이 없으면 RDB 를 한번 더 확인하여 읽지 않은 알림이 있는지 확인한다 <br>
&#8209; 전체 회원에게 공지를 전송하고 동시에 다른 관리자가 공지를 할 수 있으므로 비동기로 처리한다 <br>

#### 5) 채팅 메세지 알림 → Exchange (ntf.exchange.topic) > Routing Key (ntf.chat) > Queue (ntf.queue.chat)
&#8209; JWT 인증 필요 없음 (로그인을 요구하는 기능이므로 알림 메세지 전송시, Access Token 검증 안하는 방향으로 결정) <br>
&#8209; 사용자가 구독한 사람에게 채팅 메세지를 전송하면 상대에게 알림을 전송한다 <br>
&#8209; 알림이 전송될 때, RDB 에 알림이 저장되고 알림을 수신하는 측에서 Redis 에 알림을 캐시한다 <br>
&#8209; 상대방이 로그인 중이라면 SSE 를 통해 실시간 알림이 전송되고 그렇지 않으면 추후 로그인 시, Redis (DB 2) 확인 <br>
&#8209; Redis Key → notification:consumer:{memberId} <br>
&#8209; 12시간 마다 Redis 에 캐시된 알림 메세지가 7일 전이면 삭제한다 <br>
&#8209; Redis 에 캐시된 알림이 없으면 RDB 를 한번 더 확인하여 읽지 않은 알림이 있는지 확인한다 <br>

<br>
<br>

## Access Token 갱신
#### 1) Access Token 갱신 → POST /api/v1/tokens/refresh
&#8209; JWT 인증 필요 없음 <br>
&#8209; Access Token 의 유효기간 10분이 만료되면 Refresh Token 을 통해 자동 갱신한다 <br>
&#8209; Refresh Token 의 유효기간이 남아있어야 하며 Access Token 을 갱신하면 Refresh Token 도 갱신한다 <br>
&#8209; 클라이언트의 쿠키에 있는 Refresh Token 과 Redis (DB 1) 에 저장된 Refresh Token 값이 동일한지 검증한다 <br>
&#8209; Refresh Token 의 유효기간은 30분으로 만료되면 자동 갱신하지 않고 재로그인을 요구한다 <br>
