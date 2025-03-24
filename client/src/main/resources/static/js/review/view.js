// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const reviewId = params.get('review_id');

// 글 조회
getReview(reviewId).then((response) => {
    console.log(response.data);

    // 장소 이름
    const storeName = document.getElementById('store-name');
    storeName.textContent = response.data.storeName;

    // 장소 위치
    const storeAddress = document.getElementById('store-address');
    storeAddress.textContent = response.data.storeAddress;

    // 평점 색상 처리
    // 만약, REST API 가 아니라 Model 로 넘긴 경우, 다음 주석과 같이 처리한다
    // view.html 에서 <ul id="reviewer-star" th:data-reviewer-star="${review.star}"> 로 받으면 된다
    // 그리고 const reviewerStarScore = reviewerStarList.getAttribute('data-reviewer-star'); 로 값을 받으면 된다
    const reviewerStarList = document.getElementById('reviewer-star');
    const reviewerStarItems = [...reviewerStarList.children];
    const reviewerStarScore = response.data.star;
    reviewerStarItems.forEach((reviewerStarItem, index) => {
        if (index < reviewerStarScore) {
            reviewerStarItem.style.color = 'rgb(249, 199, 53)';
        }
    });

    // 글 제목
    const reviewTitle = document.getElementById('review-title');
    reviewTitle.textContent = response.data.title;

    // 글 내용
    // 만약, REST API 가 아니라 Model 로 넘긴 경우, 다음 주석과 같이 처리한다
    // <div class="review-content" th:utext="${review.content}"></div>
    const reviewContent = document.getElementById('review-content');
    reviewContent.textContent = response.data.content;
})
.catch((error) => {
    // 글이 존재하지 않는 경우
});

// 글 조회 API
async function getReview(reviewId) {
    const response = await axios.get('http://localhost:8081/api/v1/reviews/' + reviewId);
    return response;
}

// 편집 버튼
const accessToken = localStorage.getItem('todayReviewsAccessToken');
const btnUpdate = document.getElementById('update-review');
if (accessToken) {
    // 편집 화면으로 이동
    btnUpdate.style.display = 'block';
    btnUpdate.addEventListener('click', () => {
        location.href = '/review/edit?review_id=' + reviewId;
    });
} else {
    btnUpdate.style.display = 'none';
}

// 홈으로 이동
const btnHome = document.getElementById('go-home');
btnHome.addEventListener('click', () => {
    location.href = '/';
});
