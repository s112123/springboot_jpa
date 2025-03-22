// 변수 선언
var myReviewsForm = document.getElementById('my-reviews-form');
var checkItems = document.querySelectorAll('.checkItem');

// 선택 삭제
function removeReviews(page) {
  if (confirm('선택된 리뷰를 모두 삭제하시겠습니까?')) {
    var total = 0;

    checkItems.forEach(checkItem => {
      total += checkItem.checked;
    });

    if (total === 0) {
      alert('선택된 리뷰가 없습니다');
      return;
    }

    myReviewsForm.action = '/my/review/remove_all?page=' + page;
    myReviewsForm.method = 'post';
    myReviewsForm.submit();
  }
}

// 개별 삭제
function removeReview(reviewId, page) {
  if (confirm('리뷰를 삭제하시겠습니까?')) {
    location.href = '/my/review/remove?review_id=' + reviewId + '&page=' + page;
  }
}

// 전체 선택
function checkAll(target) {
  var items = document.querySelectorAll('.checkItem');
  items.forEach(item => {
    item.checked = target.checked;
  });
}