import { accessTokenUtils } from '/js/common.js';

// 변수 선언
const reviewListElement = document.getElementById('my-reviews');
const pageableElement = document.getElementById('pageable');
const btnRemoveAll = document.getElementById('remove-reviews');
const allCheck = document.getElementById('all-check');
let deletedReviewIds = [];
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
    // 테이블의 머리글에 전체 체크하는 부분이 체크되어 있으면 체크 해제
    allCheck.checked = false;
    // 선택되어 저장되었던 항목 제거
    deletedReviewIds.length = 0;

    // 리뷰 목록
    getReviewsByMemberId(page).then((response) => {
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
            reviewListElement.innerHTML = '<td colspan="7">등록된 리뷰가 없습니다</td>';
        } else {
            reviewListElement.innerHTML = reviewListHTML;
        }

        // 페이지네이션 렌더링
        let paginationHTML = getPaginationHTML(reviews);
        pageableElement.innerHTML = paginationHTML;

        // 체크 박스
        let items = document.querySelectorAll('.checkItem');
        items.forEach((item) => {
            item.addEventListener('change', (e) => {
                if (e.target.checked) {
                    deletedReviewIds[deletedReviewIds.length] = e.target.value;
                } else {
                    deletedReviewIds = deletedReviewIds.filter(reviewId => reviewId != e.target.value);
                }
            });
        });
    });
}

// 리뷰 목록 조회 API
async function getReviewsByMemberId(page) {
    const api = 'http://localhost:8081/api/v1/reviews/my/' + accessTokenUtils.getMemberId() + '/pages/' + page;
    const response = await axios.get(api, {
        'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
    });
    return response;
}

// 리뷰 목록 HTML 생성
function getReviewListHTML(reviews, page) {
    let html = '';

    for (let review of reviews) {
        html += '<tr>';
        html += '    <td>';
        html += `        <input type="checkbox" class="checkItem" value="${review.reviewId}" />`;
        html += '    </td>';
        html += '    <td>';
        html += `        <a href="/review/view?review_id=${review.reviewId}">${review.title}</a>`;
        html += '    </td>';
        html += '    <td>' + review.storeName + '</td>';
        html += '    <td>' + review.goodCount + '</td>';
        html += '    <td>' + review.hits + '</td>';
        html += '    <td>' + formatDate(review.createdAt) + '</td>';
        html += '    <td>';
        html += `        <a href="javascript:;" onclick="removeReview(${review.reviewId}, ${page})">`;
        html += '            <i class="fa-solid fa-trash-can"></i>';
        html += '        </a>';
        html += '    </td>';
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

// 개별 삭제
function removeReview(reviewId, page) {
    // Access Token 이 없으면 로그인 화면으로 이동
    accessTokenUtils.redirectLoginPage();

    if (confirm('리뷰를 삭제하시겠습니까?')) {
        deleteReview(reviewId).then((response) => {
            alert('리뷰가 삭제되었습니다');
            render(page);
        });
    }
}

// 리뷰 삭제 API
async function deleteReview(reviewId) {
    const api = 'http://localhost:8081/api/v1/reviews/' + reviewId;
    const response = await axios.delete(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 선택된 리뷰 삭제
btnRemoveAll.addEventListener('click', () => {
    // Access Token 이 없으면 로그인 화면으로 이동
    accessTokenUtils.redirectLoginPage();

    // 유효성 검사 → 리뷰 선택 여부
    if (deletedReviewIds.length === 0) {
        alert('선택된 리뷰가 없습니다');
        return;
    }

    // 리뷰 삭제
    if (confirm('선택된 리뷰를 모두 삭제하시겠습니까?')) {
        deleteSelectedReviews(deletedReviewIds).then(() => {
            alert('선택된 리뷰가 모두 삭제되었습니다');
            deletedReviewIds.length = 0;
            render(currentPage);
        });
    }
});

// 전체 선택
function checkAll(target) {
    deletedReviewIds.length = 0;
    let items = document.querySelectorAll('.checkItem');
    items.forEach(item => {
        item.checked = target.checked;
        if (item.checked) {
            deletedReviewIds[deletedReviewIds.length] = item.value;
        }
    });
}

// 선택된 리뷰 삭제 API
async function deleteSelectedReviews(deletedReviewIds) {
    const api = 'http://localhost:8081/api/v1/reviews';
    const response = await axios({
        method: 'delete',
        url: api,
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        },
        data: deletedReviewIds
    });
    return response;
}

// 현재 review.js 는 모듈 상태이므로 전역 스코프에 render() 와 checkAll(), removeReview() 를 노출한다
// 그렇지 않으면 Uncaught ReferenceError: render is not defined 에러가 발생한다
window.render = render;
window.checkAll = checkAll;
window.removeReview = removeReview;