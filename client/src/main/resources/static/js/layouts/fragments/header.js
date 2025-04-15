import { accessTokenUtils } from '/js/common.js';

// Navigation
const login = document.getElementById('login');
const notification = document.getElementById('notification');
const profile = document.getElementById('profile');
const admin = document.getElementById('admin');
const logout = document.getElementById('logout');

// HTML 로드
let eventSource = null;
window.addEventListener('DOMContentLoaded', () => {
    // Access Token 여부에 따라 로그인 항목 활성화
    checkLoginStatus(accessTokenUtils.getAccessToken());

    // 로그인 시, 알림 구독
    if (accessTokenUtils.getAccessToken()) {
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
            location.replace('/');
        });
    });
})

// SSE 연결
function connectSSE() {
    const apiSubscribe = 'http://localhost:8081/api/v1/sse/subscribe?memberId=' + accessTokenUtils.getMemberId();
    eventSource = new EventSource(apiSubscribe);
    eventSource.addEventListener('notification', (e) => {
        if (e.data !== 'ping') {
            // 알림 메세지 내용
            alert(e.data);

            // 헤더의 알림 아이콘 모양 변경
            let bell = document.getElementById('bell');
            bell.classList.add('fa-shake');
            bell.style.color = 'rgb(210, 40, 40)';
        }
    });

    // 연결 오류
    eventSource.onerror = (err) => {
        //console.error('SSE 연결 오류', err);
        eventSource.close();

        // 1초마다 재연결
        setTimeout(() => {
            console.log('SSE 재연결');
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

// 읽지 않은 알림 확인 API
async function existsNotReadNotification(memberId) {
    const api = 'http://localhost:8081/api/v1/notifications?memberId=' + memberId;
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