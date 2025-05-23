import { accessTokenUtils } from '/js/common.js';

// 변수 선언
const noticeListElement = document.getElementById('my-notices');
const pageableElement = document.getElementById('pageable');

// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const noticeId = params.get('noticeId');

// 처음 페이지 로드 시, 1페이지 화면 렌더링
render(1);

// 공지사항 목록 화면 렌더링
function render(page) {
    // Access Token 이 없으면 로그인 화면으로 이동
    accessTokenUtils.redirectLoginPage();

    // 공지사항 목록
    getNotices(page).then((response) => {
        let notices = response.data;

        // 공지사항 목록 렌더링
        let noticeListHTML = getNoticeListHTML(notices.data);
        if (noticeListHTML.length === 0) {
            noticeListElement.innerHTML = '<td colspan="4">등록된 공지사항이 없습니다</td>';
        } else {
            noticeListElement.innerHTML = noticeListHTML;
        }

        // 페이지네이션 렌더링
        let paginationHTML = getPaginationHTML(notices);
        pageableElement.innerHTML = paginationHTML;
    });
}

// 공지사항 목록 API
async function getNotices(page) {
    const api = 'http://localhost:8081/api/v1/notices/pages/' + page;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 공지사항 목록 HTML 생성
function getNoticeListHTML(notices) {
    let html = '';

    for (let notice of notices) {
        html += '<tr>';
        html += '    <td>' + notice.id + '</td>';
        if (noticeId !== null && Number(noticeId) === notice.id) {
            html += '    <td class="notice-new">' + notice.content + '</td>';
        } else {
            html += '    <td>' + notice.content + '</td>';
        }
        html += '    <td>' + notice.writer + '</td>';
        html += '    <td>' + formatDate(notice.createdAt) + '</td>';
        html += '</tr>'
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

// 현재 notice.js 는 모듈 상태이므로 전역 스코프에 render() 를 노출한다
// 그렇지 않으면 Uncaught ReferenceError: render is not defined 에러가 발생한다
window.render = render;