import { accessTokenUtils } from '/js/common.js';

// 변수 선언
let btnReview = document.getElementById('btn-review');
let middle = document.getElementById('reviews-middle');
let noData = document.getElementById('no-data');
let orderItems = document.querySelectorAll('.order-item');
let currentPage = 1;
let currentSort = 0;
let currentKeyword = '';

// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const keyword = params.get('searchKeyword');

// 검색 처리
if (keyword) {
    currentSort = 1;
    currentKeyword = keyword;
    search.value = keyword;
    search.focus();
}

// HTML 로드
document.addEventListener('DOMContentLoaded', () => {
    // 로그인 되어 있는 경우, 리뷰쓰기 버튼 표시
    if (accessTokenUtils.getAccessToken()) {
        if (btnReview !== null) {
            btnReview.style.display = 'block';
        } else {
            btnReview.style.display = 'none';
        }
    }

    // 글 목록에서 글을 눌러 조회하고 뒤로가기를 누르면 currentPage 와 currentSort 가 초기화된다
    // 그래서 viewReview() 함수를 누를 때, 상태를 저장해둔다
    if (!history.state) {
        // 처음 페이지 로드 시, 1페이지 화면 렌더링
        render(currentPage, currentSort, currentKeyword);
    } else {
        // history 에 상태가 있으면 해당 페이지로 렌더링
        currentPage = history.state.currentPage;
        currentSort = history.state.currentSort;
        currentKeyword = history.state.currentKeyword;
        render(currentPage, currentSort, currentKeyword);
    }

    // 리뷰 정렬
    orderItems.forEach((orderItem, index) => {
        // 정렬 버튼 클릭시 동작
        orderItem.addEventListener('click', () => {
            // 정렬 옵션에 따른 리뷰 목록 반환
            currentPage = 1;
            currentSort = index;
            if (currentSort === 0) {
                search.value = '';
            }
            // 리뷰 검색어
            currentKeyword = search.value;
            render(currentPage, currentSort, currentKeyword);
        });
    });
});

// 리뷰 목록 화면 렌더링
function render(page, sort, searchKeyword) {
    currentPage = page;
    currentSort = sort;
    currentKeyword = searchKeyword;

    // 정렬 항목 색상 처리
    for (let i = 0; i < orderItems.length; i++) {
        if (i === sort) {
            orderItems[i].style.backgroundColor = 'rgba(210, 40, 40, 0.3)';
            orderItems[i].style.color = '#000';
        } else {
            orderItems[i].style.backgroundColor = 'rgb(230, 230, 230)';
            orderItems[i].style.color = 'rgb(100, 100, 100)';
        }
    }

    // 리뷰 목록
    getReviews(currentPage, currentSort, currentKeyword).then(response => {
        let reviews = response.data;
        let reviewContents = reviews.data;
        console.log(reviews);

        // DB 에 저장된 <img> 태그의 src 값을 변경한다
        // 수정 전 → <img src="http://localhost:8081/api/v1/reviews/content-images/temp/{memberId}/{fileName}">
        // 수정 후 → <img src="http://localhost:8081/api/v1/reviews/content-images/{memberId}/{fileName}?reviewId=1">
        reviewContents.forEach((review) => {
            review.content = review.content.replace(/<img[^>]+src="([^"]+\/temp[^"]+)"/g, (match, p1) => {
                const updatedSrc = p1.replace('/temp', '') + '?reviewId=' + review.reviewId;
                return match.replace(p1, updatedSrc);
            });
        });

        // 리뷰 목록을 작성한 HTML 반환
        let reviewsHTML = getReviewsHTML(reviewContents);

        // 리뷰 목록 렌더링
        if (reviewsHTML.length === 0) {
            middle.innerHTML = '<div class="no-data" id="no-data">조회된 결과가 없습니다</div>';
        } else {
            middle.innerHTML = reviewsHTML;
        }

        // 페이지네이션 렌더링
        let pageableElement = document.getElementById('pageable');
        let paginationHTML = getPaginationHTML(reviews, currentSort);
        pageableElement.innerHTML = paginationHTML;
    });
}

// 리뷰쓰기 페이지로 이동
if (btnReview !== null) {
    btnReview.addEventListener('click', () => {
        location.href = '/review/add';
    });
}

// 리뷰 목록
async function getReviews(page, sort, searchKeyword) {
    const api = `http://localhost:8081/api/v1/reviews/pages/${page}?sort=${sort}&searchKeyword=${searchKeyword}`;
    const response = await axios.get(api);
    return response;
}

// 리뷰 목록 HTML 반환
function getReviewsHTML(reviews) {
    let html = ``;

    // 리뷰 목록
    for (let review of reviews) {
        html += `<div class="review-wrap" onclick="viewReview(${review.reviewId})">`;
        html += `  <div class="review-box">`;
        html += `    <div class="review-image">`;
        // 썸네일
        let reviewImagesDetailsList = review.reviewImagesDetailsList;
        for (let reviewImagesDetails of reviewImagesDetailsList) {
            if (reviewImagesDetails.isThumbnail) {
                let thumbFileName = reviewImagesDetails.savedFileName;
                let thumbSrc = 'http://localhost:8081/api/v1/reviews/content-images/';
                thumbSrc += review.memberId + '/' + thumbFileName + '?reviewId=' + review.reviewId;
                html += '      <img src="' + thumbSrc + '" />';
                break;
            }
        }
        html += `    </div>`;
        html += `    <div class="review-summary">`;
        html += `      <div class="review-summary-star">`;
        html += `        <ul class="store-star">`;
        // 평점
        for (let i = 0; i < 5; i++) {
            if (i < review.star) {
                html += `      <li class="star-active"><i class="fa-solid fa-star"></i></li>`;
            } else {
                html += `      <li><i class="fa-solid fa-star"></i></li>`;
            }
        }
        html += `        </ul>`;
        html += `      </div>`;
        html += `      <div class="review-summary-title">${shortTitle(review.title)}</div>`;
        html += `      <div class="review-summary-content">${shortContent(removeHTMLTag(review.content))}</div>`;
        html += `    </div>`;
        html += `  </div>`;
        html += `</div>`;
    }

    return html;
}

// 타이틀 글자 수 줄임
function shortTitle(title) {
    let result = title;
    if (title.length > 14) {
        result = title.substring(0, 14) + '...';
    }
    return result;
}

// 글 내용 글자 수 줄임
function shortContent(content) {
    let result = content;
    if (content.length > 32) {
        result = content.substring(0, 32) + '...';
    }
    return result;
}

// HTML 태그 제거
function removeHTMLTag(content) {
    let result = content.replaceAll('</p><p>', ' ');
    result = result.replace(/(<([^>]+)>)/gi, '');
    result = result.replace(/\s\s+/ig, '');
    result = result.replace(/&nbsp;/ig, '');
    result = result.replace(/ +/ig, ' ');
    result = result.replaceAll('&gt;', '>');
    result = result.replaceAll('&lt;', '<');
    result = result.replaceAll('&lt;', '<');
    result = result.replaceAll('&quot;', "");
    result = result.replaceAll('&amp;', '&');
    return result;
}

// 페이지 버튼 HTML 생성
function getPaginationHTML(list, sort, searchKeyword) {
    let html = '';

    if (list.end > 1) {
        html += '<ul>';
        // 이전 버튼
        if (list.hasPrev) {
            html += '    <li>';
            html += `        <a href="javascript:;" onclick="render(${list.start - 1}, ${sort}, ${searchKeyword})">`;
            html += '            <i class="fa-solid fa-angle-left"></i>';
            html += '        </a>';
            html += '    </li>';
        }
        // 페이지 번호
        for (let i = list.start; i <= list.end; i++) {
            let active = (i == list.currentPage) ? 'active' : '';
            html += '    <li>';
            html += `        <a href="javascript:;" class="${active}" onclick="render(${i}, ${sort}, ${searchKeyword})">${i}</a>`;
            html += '    </li>';
        }
        // 다음 버튼
        if (list.hasNext) {
            html += '    <li>';
            html += `        <a href="javascript:;" onclick="render(${list.end + 1}, ${sort}, ${searchKeyword})">`;
            html += '            <i class="fa-solid fa-angle-right"></i>';
            html += '        </a>';
            html += '    </li>';
        }
        html += '</ul>';
    }

    return html;
}

// 조회 페이지 이동
function viewReview(reviewId) {
    // 글 조회 전에 history 에 currentPage, currentSort 값을 저장해둔다
    // 이 방법 대신 sessionStorage 에 저장해도 된다
    // sessionStorage.setItem('historyState', JSON.stringify({page:2, sort:'desc'}));
    // history 객체의 pushState() 에 세 번째 인수는 다시 페이지로 돌아왔을 때, URL 에 쿼리 스트링으로 붙일 수 있다
    // 예 → history.pushState(state, '', `?page=${currentPage}&sort=${currentSort}`);
    // 다음은 쿼리 스트링으로 붙이진 않고 상태만 저장한다
    const state = { currentPage, currentSort, currentKeyword };
    history.pushState(state, '');

    // 글 조회 페이지로 이동
    location.href = "/review/view?review_id=" + reviewId;
}

/*
// pushState() 로 저장된 history 의 값을 popstate 이벤트로 꺼내올 수도 있다
window.addEventListener('popstate', (e) => {
    if (e.state) {
        currentPage = e.state.currentPage;
        currentSort = e.state.currentSort;
        render(currentPage, currentSort);
    }
});
 */

// 현재 index.js 는 모듈 상태이므로 전역 스코프에 render() 와 viewReview() 를 노출한다
// 그렇지 않으면 Uncaught ReferenceError: render is not defined 에러가 발생한다
window.render = render;
window.viewReview = viewReview;








