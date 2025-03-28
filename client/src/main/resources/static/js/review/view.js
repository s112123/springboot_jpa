import { accessTokenUtils } from '/js/common.js';

// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const reviewId = params.get('review_id');

// 리뷰 조회
getReview(reviewId).then((response) => {
    // 조회된 리뷰
    let review = response.data;

    // 장소 이름
    const storeName = document.getElementById('store-name');
    storeName.textContent = review.storeName;

    // 장소 위치
    const storeAddress = document.getElementById('store-address');
    storeAddress.textContent = review.storeAddress;

    // 평점 색상 처리
    // 만약, REST API 가 아니라 Model 로 넘긴 경우, 다음 주석과 같이 처리한다
    // view.html 에서 <ul id="reviewer-star" th:data-reviewer-star="${review.star}"> 로 받으면 된다
    // 그리고 const reviewerStarScore = reviewerStarList.getAttribute('data-reviewer-star'); 로 값을 받으면 된다
    const reviewerStarList = document.getElementById('reviewer-star');
    const reviewerStarItems = [...reviewerStarList.children];
    const reviewerStarScore = review.star;
    reviewerStarItems.forEach((reviewerStarItem, index) => {
        if (index < reviewerStarScore) {
            reviewerStarItem.style.color = 'rgb(249, 199, 53)';
        }
    });

    // 좋아요 버튼 생성
    const storeHeart = document.getElementById('store-heart');
    if (accessTokenUtils.getMemberId() !== null && review.memberId === accessTokenUtils.getMemberId()) {
        storeHeart.style.display = 'none';
    } else {
        storeHeart.style.display = 'block';
    }

    // 작성자 프로필 이미지
    const writerProfileImage = document.getElementById('writer-profile-image');
    const writerProfileImageFileName = review.profileImageDetails.savedFileName;
    if (writerProfileImageFileName === 'default.png') {
        writerProfileImage.src = '/images/profiles/default.png';
    } else {
        writerProfileImage.src = 'http://localhost:8081/api/v1/members/profile-images/' +
                                  review.memberId + '/' + writerProfileImageFileName;
    }

    // 작성자 이름
    const writer = document.getElementById('writer-name');
    writer.textContent = review.writer;

    // 구독 버튼 생성
    const writerAction = document.getElementById('writer-action');
    if (accessTokenUtils.getMemberId() !== null && review.memberId === accessTokenUtils.getMemberId()) {
        writerAction.style.display = 'none';
    } else {
        writerAction.style.display = 'block';
    }

    // 제목
    const reviewTitle = document.getElementById('review-title');
    reviewTitle.textContent = review.title;

    // 리뷰 내용
    // 만약, REST API 가 아니라 Model 로 넘긴 경우, 다음 주석과 같이 처리한다
    // <div class="review-content" th:utext="${review.content}"></div>
    const reviewContent = document.getElementById('review-content');
    // DB 에 저장된 <img> 태그의 src 값을 변경한다
    // 수정 전 → <img src="http://localhost:8081/api/v1/reviews/content-images/temp/{memberId}/{fileName}">
    // 수정 후 → <img src="http://localhost:8081/api/v1/reviews/content-images/{memberId}/{fileName}?reviewId=1">
    review.content = review.content.replace(/<img[^>]+src="([^"]+\/temp[^"]+)"/g, (match, p1) => {
        const updatedSrc = p1.replace('/temp', '') + '?reviewId=' + review.reviewId;
        return match.replace(p1, updatedSrc);
    });
    reviewContent.innerHTML = review.content;

    // 편집 버튼
    const btnUpdate = document.getElementById('update-review');
    if (accessTokenUtils.getMemberId() !== null && review.memberId === accessTokenUtils.getMemberId()) {
        // 편집 화면으로 이동
        btnUpdate.style.display = 'block';
        btnUpdate.addEventListener('click', () => {
            location.href = '/review/edit?review_id=' + reviewId;
        });
    } else {
        btnUpdate.style.display = 'none';
    }
})
.catch((error) => {
    // 리뷰가 존재하지 않는 경우
    // 스프링부트를 활용하여 error > 4xx.html 또는 5xx.html 을 호출하도록 한다
    if (error.response && error.response.status === 404) {
        // 4xx 페이지 반환
        location.href = '/error/404';
    } else {
        // 5xx 페이지 반환
        location.href = '/error/500';
    }
});

// 리뷰 조회 API
async function getReview(reviewId) {
    const response = await axios.get('http://localhost:8081/api/v1/reviews/' + reviewId);
    return response;
}

// 홈으로 이동
const btnHome = document.getElementById('go-home');
btnHome.addEventListener('click', () => {
    location.href = '/';
});
