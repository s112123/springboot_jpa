import { accessTokenUtils } from '/js/common.js';

// 변수 선언
const goodReviewsListElement = document.getElementById('my-good-reviews');
const pageableElement = document.getElementById('pageable');
let currentPage = 1;

// 리뷰 제목을 눌러 조회하고 뒤로가기를 누르면 currentPage 초기화되어 1페이지로 로드된다
// 그래서 getReviewsByMemberId() 함수를 누를 때, 상태를 저장해둔다
if (!history.state) {
    // 처음 페이지 로드 시, 1페이지 화면 렌더링
    render(currentPage);
} else {
    // history 에 상태가 있으면 해당 페이지로 렌더링
    currentPage = history.state.currentPage;
    render(currentPage);
}

// 리뷰 목록 화면 렌더링
function render(page) {
    // Access Token 이 없으면 로그인 화면으로 이동
    accessTokenUtils.redirectLoginPage();

    // 좋아요 누른 리뷰 목록
    getGoodReviews(accessTokenUtils.getMemberId(), page).then(response => {
        let reviews = response.data;
        currentPage = page;

        // 페이지 수가 1을 초과하고 현재 페이지에 대한 리뷰 목록이 없으면 이전 페이지로 렌더링
        if (reviews.data.length === 0) {
            if (page > 1) {
                currentPage -= 1;
                render(currentPage);
            }
        }

        // 현재 페이지 번호를 history 객체에 저장
        const state = { currentPage };
        history.pushState(state, '');

        // 리뷰 목록 렌더링
        let reviewListHTML = getReviewListHTML(reviews.data, currentPage);
        if (reviewListHTML.length === 0) {
            goodReviewsListElement.innerHTML = '<td colspan="7">좋아요를 누른 리뷰가 없습니다</td>';
        } else {
            goodReviewsListElement.innerHTML = reviewListHTML;
        }

        // 페이지네이션 렌더링
        let paginationHTML = getPaginationHTML(reviews);
        pageableElement.innerHTML = paginationHTML;
    });
}

// 좋아요 누른 리뷰 목록 API
async function getGoodReviews(memberId, page) {
    const api = 'http://localhost:8081/api/v1/goods/' + memberId + '/pages/' + page;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response
}

// 좋아요 누른 리뷰 목록 HTML 생성
function getReviewListHTML(reviews, page) {
    let html = '';

    for (let review of reviews) {
        html += '<tr>';
        html += '    <td>' + review.reviewId + '</td>';
        html += '    <td>';
        html += `        <a href="/review/view?review_id=${review.reviewId}">${review.title}</a>`;
        html += '    </td>';
        html += '    <td>' + review.storeName + '</td>';
        html += '    <td>' + review.writer + '</td>';
        html += '    <td>' + review.goodCount + '</td>';
        html += '    <td>' + review.hits + '</td>';
        html += '    <td>' + formatDate(review.createdAt) + '</td>';
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

// 현재 good.js 는 모듈 상태이므로 전역 스코프에 render() 를 노출한다
// 그렇지 않으면 Uncaught ReferenceError: render is not defined 에러가 발생한다
window.render = render;