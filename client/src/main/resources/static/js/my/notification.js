import { accessTokenUtils } from '/js/common.js';

// 변수 선언
const noReadCount = document.getElementById('no-read-count');
const btnAllRead = document.querySelector('#all-read');
const notificationListElement = document.getElementById('notification-list');
const pageableElement = document.getElementById('pageable');
let currentPage = 1;

// 알림 제목을 눌러 조회하고 뒤로가기를 누르면 currentPage 초기화되어 1페이지로 로드된다
// 그래서 getReviewsByMemberId() 함수를 누를 때, 상태를 저장해둔다
if (!history.state) {
    // 처음 페이지 로드 시, 1페이지 화면 렌더링
    render(currentPage);
} else {
    // history 에 상태가 있으면 해당 페이지로 렌더링
    currentPage = history.state.currentPage;
    render(currentPage);
}

// 알림 목록 화면 렌더링
function render(page) {
    // Access Token 이 없으면 로그인 화면으로 이동
    accessTokenUtils.redirectLoginPage();

    // 읽지 않은 알림 개수
    countNoReadNotifications(accessTokenUtils.getMemberId()).then(response => {
        noReadCount.textContent = response.data;
    });

    // 알림 목록
    getMessagesByMemberId(accessTokenUtils.getMemberId(), page).then(response => {
        let messages = response.data;
        currentPage = page;

        // 페이지 수가 1을 초과하고 현재 페이지에 대한 리뷰 목록이 없으면 이전 페이지로 렌더링
        if (messages.data.length === 0) {
            if (page > 1) {
                currentPage -= 1;
                render(currentPage);
            }
        }

        // 현재 페이지 번호를 history 객체에 저장
        const state = { currentPage };
        history.pushState(state, '');

        // 알림 목록 렌더링
        let messageListHTML = getMessageListHTML(messages.data, currentPage);
        if (messageListHTML.length === 0) {
            let html = '';
            html += '<div class="notification-content">';
            html += '    <div id="no-content">알림 내역이 없습니다</div>';
            html += '</div>';
            notificationListElement.innerHTML = html;
        } else {
            notificationListElement.innerHTML = messageListHTML;
        }

        // 페이지네이션 렌더링
        let paginationHTML = getPaginationHTML(messages);
        pageableElement.innerHTML = paginationHTML;

        // 알림 메세지 클릭 처리
        let contents = document.querySelectorAll('.content');
        contents.forEach((content) => {
            content.addEventListener('click', (e) => {
                console.log(e.target);
                let redirectURL = e.target.getAttribute('data-notification-url');

                // 읽지 않은 알림인 경우, 데이터베이스에서 읽음 처리
                if (e.target.classList.contains('no-read-content')) {
                    let notificationId = e.target.getAttribute('data-notification-id');
                    markAsRead(notificationId);
                }

                location.href = redirectURL;
            });
        });

        console.log(contents);
    });
}

// 읽지 않은 알림 개수 API
async function countNoReadNotifications(memberId) {
    const api = 'http://localhost:8081/api/v1/notifications/no-read/count?memberId=' + memberId;
    return axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
}

// 메세지 목록 API
async function getMessagesByMemberId(memberId, page) {
    const api = 'http://localhost:8081/api/v1/notifications?memberId=' + memberId + '&page=' + page;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 알림 메세지 목록 HTML 생성
function getMessageListHTML(messages, page) {
    let html = '';

    for (let message of messages) {
        html += '<div class="notification-content">';
        html += '    <div>';
        html += '        <i class="fa-solid fa-circle fa-2xs ' + (message.read ? '' : 'no-read') + '"></i>';
        html += '        <a class="content ' + (message.read ? 'read-content' : 'no-read-content') + '" ';
        html += '           data-notification-url="' + message.url + '" ';
        html += '           data-notification-id="' + message.id + '">' + message.message + '</a>';
        html += '    </div>';
        html += '    <div>' + formatDate(message.createdAt) + '</div>';
        html += '</div>'
    }

    return html;
}

// 날짜 형식 변경 (2025-03-26T09:19:57.543247 → 2025-03-26 09:19:57)
function formatDate(current) {
    // 문자열로 처리
    return current.replace('T', ' ').split('.')[0];
}

// 페이지 버튼 HTML 생성
function getPaginationHTML(list) {
    let html = '';

    html += '<ul>';
    // 이전 버튼
    if (list.hasPrev) {
        html += '    <li>';
        html += '        <a href="javascript:;" onclick=render(' + (list.start - 1) + ')>';
        html += '            <i class="fa-solid fa-angle-left"></i>';
        html += '        </a>';
        html += '    </li>';
    }
    // 페이지 번호
    for (let i = list.start; i <= list.end; i++) {
        let active = (i == list.currentPage) ? 'active' : '';
        html += '    <li>';
        html += '        <a href="javascript:;" class="' + active + '" onclick=render(' + i + ')>' + i + '</a>';
        html += '    </li>';
    }
    // 다음 버튼
    if (list.hasNext) {
        html += '    <li>';
        html += '        <a href="javascript:;" onclick=render(' + (list.end + 1) + ')>';
        html += '            <i class="fa-solid fa-angle-right"></i>';
        html += '        </a>';
        html += '    </li>';
    }
    html += '</ul>';

    return html;
}

// 읽음 처리 API
async function markAsRead(notificationId) {
    const api =
        'http://localhost:8081/api/v1/notifications/' + notificationId + '/mark_read?memberId=' + accessTokenUtils.getMemberId();
    const response = await axios.patch(api, { read: true }, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 모두 읽음 처리
btnAllRead.addEventListener('click', () => {
    markAllAsRead().then(() => {
        location.href = '/my/notification';
    });
});

// 모두 읽음 처리 API
async function markAllAsRead() {
    const api =
        'http://localhost:8081/api/v1/notifications/mark_all_read?memberId=' + accessTokenUtils.getMemberId();
    const response = await axios.patch(api, { read: true }, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 현재 notification.js 는 모듈 상태이므로 전역 스코프에 render() 를 노출한다
// 그렇지 않으면 Uncaught ReferenceError: render is not defined 에러가 발생한다
window.render = render;