import { accessTokenUtils } from '/js/common.js';

// Navigation
const login = document.getElementById('login');
const notification = document.getElementById('notification');
const profile = document.getElementById('profile');
const admin = document.getElementById('admin');
const logout = document.getElementById('logout');

// HTML 로드
window.addEventListener('DOMContentLoaded', () => {
    // Access Token 여부에 따라 로그인 항목 활성화
    checkLoginStatus(accessTokenUtils.getAccessToken());
})

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

///////////////////////////////
// 변수 선언
//var bell = document.getElementById('bell');
//var email = document.getElementById('email');
//
//if (email !== null) {
//  if (email.value !== undefined && email.value !== '') {
//    var eventSource = new EventSource(`/notifications/subscribe/${email.value}`);
//    eventSource.addEventListener('sse', (event) => {
//      if (event.data !== 'subscribe') {
//        bell.classList.add('fa-shake');
//        bell.style.color = 'rgb(210, 40, 40)';
//      }
//    });
//  }
//}
