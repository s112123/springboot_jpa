// 변수 선언
var middle = document.getElementById('reviews-middle');
var noData = document.getElementById('no-data');
var bottom = document.getElementById('reviews-bottom');
var orderItems = document.querySelectorAll('.order-item');
var btnReview = document.getElementById('btn-review');
var search = document.querySelector('#search');
var btnSearch = document.querySelector('#btn-search');
var sortOption = 0;
var searchKeyword = '';
var page = 1;

// HTML 로드
document.addEventListener('DOMContentLoaded', () => {
  // 리뷰 목록 반환: renderReviews(0, '', 1);
  renderReviews(sortOption, searchKeyword, page);
});

// 리뷰 검색
// 검색어 입력 input 에서 Enter 키 처리
search.addEventListener('keypress', () => {
  if (event.keyCode === 13) {
    if (search.value !== '') sortOption = 1;
    renderReviews(sortOption, search.value, page);
  }
});

// 검색어 입력 후, 검색 버튼 클릭 처리
btnSearch.addEventListener('click', () => {
  if (search.value !== '') sortOption = 1;
  renderReviews(sortOption, search.value, page);
});

// 리뷰 정렬
orderItems.forEach((orderItem, index) => {
  // 정렬 버튼 클릭시 동작
  orderItem.addEventListener('click', () => {
    // 정렬 옵션에 따른 리뷰 목록 반환
    sortOption = index;
    if (sortOption === 0) {
      search.value = '';
    }
    searchKeyword = search.value;
    renderReviews(sortOption, searchKeyword, page);
  });
});

// 리뷰 등록 화면으로 이동
if (btnReview != null) {
  btnReview.addEventListener('click', () => {
    location.href = '/review/add';
  });
}

// 조회 페이지 이동
function viewReview(reviewId) {
  location.href = "/review/view?review_id=" + reviewId;
}

// 리뷰 목록을 화면에 렌더링
function renderReviews(sortOption, searchKeyword, page) {
  // 정렬 항목 색상 처리
  for (var i = 0; i < orderItems.length; i++) {
    if (i === sortOption) {
        orderItems[i].style.backgroundColor = 'rgba(210, 40, 40, 0.3)';
        orderItems[i].style.color = '#000';
    } else {
        orderItems[i].style.backgroundColor = 'rgb(230, 230, 230)';
        orderItems[i].style.color = 'rgb(100, 100, 100)';
    }
  }

  // 화면 렌더링
  getReviews(sortOption, searchKeyword, page).then(response => {
    // 리뷰 목록 렌더링
    var reviews = getReviewsHTML(response.data.reviews);
    if (reviews.length === 0) {
      middle.innerHTML = '<div class="no-data" id="no-data">검색된 결과가 없습니다</div>';
    } else {
      middle.innerHTML = reviews;
    }

    // 페이징 렌더링
    var paging = getPagingHTML(response.data);
    bottom.innerHTML = paging;
  });
}

// 리뷰 목록 (api)
async function getReviews(sortOption, searchKeyword, page) {
  //console.log(`/reviews?sort_option=${sortOption}&search=${searchKeyword}&page=${page}`);
  var response = await axios.get(`/reviews?sort_option=${sortOption}&search=${searchKeyword}&page=${page}`);
  return response;
}

// 리뷰 목록 HTML 반환
function getReviewsHTML(reviews) {
  var html = ``;

  // 리뷰 목록
  for (var review of reviews) {
    html += `<div class="review-wrap" onclick="viewReview(${review.reviewId})">`;
    html += `  <div class="review-box">`;
    html += `    <div class="review-image">`;
    html += `      <img src="${review.thumbnailUrl}" />`;
    html += `    </div>`;
    html += `    <div class="review-summary">`;
    html += `      <div class="review-summary-star">`;
    html += `        <ul class="store-star">`;
    for (var i = 0; i < 5; i++) {
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

// 페이지 번호 섹션 HTML 반환
function getPagingHTML(reviews) {
  var pageable = reviews.pageable;
  var sortOption = reviews.sortOption;
  var searchKeyword = reviews.search.trim();
  var html = ``;

  if (pageable.end > 1) {
    html += `<div class="pageable">`;
    html += `  <ul>`;
    // 이전 버튼
    if (pageable.prev) {
      html += `  <li>`;
      html += `    <a href="javascript:;" `;
      html += `       onclick="renderReviews(${sortOption}, '${searchKeyword}', ${pageable.start - 1})">`;
      html += `      <i class="fa-solid fa-angle-left"></i>`;
      html += `    </a>`;
      html += `  </li>`;
    }
    // 번호 목록
    for (var i = pageable.start; i <= pageable.end; i++) {
      html += `  <li>`;
      html += `    <a href="javascript:;"`;
      html += `       ${(pageable.page === i) ? 'class="active"' : ' '} `;
      html += `       onclick="renderReviews(${sortOption}, '${searchKeyword}', ${i})">${i}</a>`;
      html += `  </li>`;
    }
    // 다음 버튼
    if (pageable.next) {
      html += `  <li>`;
      html += `    <a href="javascript:;" `;
      html += `       onclick="renderReviews(${sortOption}, '${searchKeyword}', ${pageable.end + 1})">`;
      html += `      <i class="fa-solid fa-angle-right"></i>`;
      html += `    </a>`;
      html += `  </li>`;
      html += `</ul>`;
    }
    html += `</div>`;
  }

  return html;
}

// 정렬 버튼 기본 색상
function defaultSortButtonColor() {
  orderItems.forEach((orderItem) => {
    orderItem.classList.add('sort-deactive');
  });
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

// 타이틀 글자 수 줄임
function shortTitle(title) {
  var result = title;
  if (title.length > 14) {
    result = title.substring(0, 14) + '...';
  }
  return result;
}

// 글 내용 글자 수 줄임
function shortContent(content) {
  var result = content;
  if (content.length > 32) {
    result = content.substring(0, 32) + '...';
  }
  return result;
}








