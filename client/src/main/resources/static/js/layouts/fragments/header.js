// 변수 선언
const token = localStorage.getItem('accessToken');
const login = document.getElementById('login');
const notification = document.getElementById('notification');
const profile = document.getElementById('profile');
const admin = document.getElementById('admin');
const logout = document.getElementById('logout');

// Access Token 여부에 따라 로그인 항목 활성화
function checkLoginStatus() {
    if (token) {
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
window.addEventListener('DOMContentLoaded', checkLoginStatus);


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
