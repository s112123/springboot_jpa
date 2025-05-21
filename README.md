## Project Clone
#### 1) Git Bash
```
git clone https://github.com/s112123/springboot_jpa.git today_reviews_project
```

<br>
<br>

## Docker 
#### 1) Docker Desktop 실행

<br>
<br>

#### 2) Docker Container 실행
&#8209; 터미널 → 프로젝트 폴더 경로로 이동 → 아래 명령 실행 <br>
```
docker compose up --build
```
&#8209; 서버가 초기화될 때까지 기다렸다가 Server Initialization Completed. 로그가 출력되면 http://localhost:9090/ 접속 <br>
&#8209; 초기 로그인 계정은 다음과 같다 <br>
&#8209; ADMIN 계정 → (아이디) admin@test.com / (비밀번호) a123412341234 <br>
&#8209; USER 계정 → (아이디) user1@test.com / (비밀번호) a123412341234 <br>
&#8209; USER 계정 → (아이디) user2@test.com / (비밀번호) a123412341234 <br>
&#8209; USER 계정 → (아이디) user3@test.com / (비밀번호) a123412341234 <br>

<br>
<br>

#### 3) Docker Compose 종료
&#8209; Container 종료 → 터미널에서 Ctrl + C → 아래 명령 실행 <br>

```
docker compose down --rmi all
```