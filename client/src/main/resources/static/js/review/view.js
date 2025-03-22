// 평점 색상 처리
const reviewerStarList = document.getElementById('reviewer-star');
const reviewerStarItems = [...reviewerStarList.children];
const reviewerStarScore = reviewerStarList.getAttribute('data-reviewer-star');
reviewerStarItems.forEach((reviewerStarItem, index) => {
    if (index < reviewerStarScore) {
      reviewerStarItem.style.color = 'rgb(249, 199, 53)';
    }
});

// 홈으로 이동
const btnHome = document.getElementById('go-home');
btnHome.addEventListener('click', () => {
  location.href = '/';
});

// 편집 화면으로 이동
const btnUpdate = document.getElementById('update-review');
if (btnUpdate != null) {
  btnUpdate.addEventListener('click', () => {
    const reviewId = btnUpdate.getAttribute('data-review-id');
    location.href = '/review/edit?review_id=' + reviewId;
  });
}
