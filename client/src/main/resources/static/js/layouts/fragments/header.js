import { accessTokenUtils } from '/js/common.js';
import { showToast } from '/js/layouts/toast.js';

// Navigation
const login = document.getElementById('login');
const notification = document.getElementById('notification');
const profile = document.getElementById('profile');
const admin = document.getElementById('admin');
const logout = document.getElementById('logout');

// URL 에서 경로 추출
let urlPath = window.location.pathname;

// HTML 로드
let eventSource = null;
window.addEventListener('DOMContentLoaded', () => {
    // Access Token 여부에 따라 로그인 항목 활성화
    checkLoginStatus(accessTokenUtils.getAccessToken());

    // 로그인 시, 알림 구독
    if (accessTokenUtils.getAccessToken()) {
        // 권한 확인 후, 헤더에서 관리자 페이지를 요청하는 아이콘 표시 처리
        const roles = accessTokenUtils.getRoles();
        if (!roles.includes('ADMIN')) {
            const adminNavi = document.getElementById('admin');
            if (adminNavi) {
                adminNavi.style.display = 'none';
            }
        }

        // 알림 구독
        connectSSE()

        // 읽지 않은 알림 확인
        existsNotReadNotification(accessTokenUtils.getMemberId()).then((response) => {
            // 헤더의 알림 아이콘 모양 변경
            let bell = document.getElementById('bell');
            if (response.data) {
                // 읽지 않은 알림이 있는 경우
                bell.classList.add('fa-shake');
                bell.style.color = 'rgb(210, 40, 40)';
            } else {
                // 읽지 않은 알림이 없는 경우
                bell.classList.remove('fa-shake');
                bell.style.color = 'rgb(120, 120, 120)';
            }
        });
    }

    // 로그아웃
    logout.addEventListener('click', () => {
        // 로그아웃 처리
        logoutProcess(accessTokenUtils.getMemberId()).then(() => {
            // Access Token 삭제
            accessTokenUtils.removeAccessToken();

            // SSE 연결 제거
            eventSource.close();
            eventSource = null;

            location.replace('/');
        });
    });
})

// 페이지를 전환할 때 기존 SSE 연결 닫기
window.addEventListener('beforeunload', () => {
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }
});

// SSE 연결
function connectSSE() {
    const apiSubscribe = 'http://localhost:8081/api/v1/sse/subscribe?memberId=' + accessTokenUtils.getMemberId();
    eventSource = new EventSource(apiSubscribe);
    eventSource.addEventListener('notification', (e) => {
        if (e.data !== 'ping') {
            // 알림 메세지 내용
            showToast(e.data);

            // 헤더의 알림 아이콘 모양 변경
            let bell = document.getElementById('bell');
            bell.classList.add('fa-shake');
            bell.style.color = 'rgb(210, 40, 40)';

            // 채팅 페이지의 채팅 사용자 목록에서 메세지를 수신했을 때, 읽지 않은 메세지 표시
            getNotReadNotification(accessTokenUtils.getMemberId()).then((response) => {
                let notifications = response.data;
                if (notifications && notifications.length > 0) {
                    // 알림 메세지의 type 이 CHAT 인 경우
                    notifications.forEach(notification => {
                        if (notification.type === 'CHAT' && urlPath === '/my/chat') {
                            // 메세지를 보낸 회원의 ID
                            let publisherId = notification.publisherId;

                            // 채팅 대상 목록
                            const chatMembers = document.querySelectorAll('.chat-item');
                            chatMembers.forEach(chatMember => {
                                let memberId = chatMember.getAttribute('data-member-id');

                                // 읽지 않은 메세지 여부 표시
                                if (publisherId === Number(memberId)) {
                                    const notReadMark = chatMember.querySelector('i');
                                    console.log(notReadMark);
                                    if (notReadMark) {
                                        notReadMark.style.display = 'block';
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    });

    // 연결 오류
    eventSource.onerror = (err) => {
        //console.error('SSE 연결 오류', err);
        eventSource.close();

        // Timeout 또는 연결이 끊어졌을 때, 1초마다 재연결 시도
        setTimeout(() => {
            connectSSE();
        }, 1000);
    };
}

// Access Token 여부에 따라 로그인 항목 활성화
function checkLoginStatus() {
    if (accessTokenUtils.getAccessToken()) {
        login.style.display = 'none';
        notification.style.display = 'block';
        profile.style.display = 'block';
        admin.style.display = 'block';
        logout.style.display = 'block';
    } else {
        login.style.display = 'block';
        notification.style.display = 'none';
        profile.style.display = 'none';
        admin.style.display = 'none';
        logout.style.display = 'none';
    }
}

// 검색
let btnSearch = document.getElementById('btn-search');
let search = document.getElementById('search');
let searchKeyword = '';

// 검색어 입력란에서 검색 버튼을 누른 경우
btnSearch.addEventListener('click', () => {
    searchKeyword = search.value;
    location.href = '/?searchKeyword=' + searchKeyword;
});

// 검색어 입력란에서 Enter 를 누른 경우
search.addEventListener('keypress', () => {
    if (event.keyCode === 13) {
        searchKeyword = search.value;
        location.href = '/?searchKeyword=' + searchKeyword;
    }
});

// 읽지 않은 알림 목록 확인 API
async function getNotReadNotification(memberId) {
    const api = 'http://localhost:8081/api/v1/notifications/no-read?memberId=' + memberId;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 읽지 않은 알림이 있는지 여부 확인 API
async function existsNotReadNotification(memberId) {
    const api = 'http://localhost:8081/api/v1/notifications/no-read/exists?memberId=' + memberId;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 로그아웃 API
async function logoutProcess(memberId) {
    const api = 'http://localhost:8081/api/v1/logout';
    const response = await axios.post(api, JSON.stringify(memberId), {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}