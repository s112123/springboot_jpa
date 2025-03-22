// 변수 선언
var email = document.getElementById('email');
var writer = document.getElementById('writer');
var writerNickName = document.getElementById('writer-name');
var memberNickName = document.getElementById('member-nickname');
var reviewTitle = document.getElementById('review-title');
var storeGood = document.getElementById('store-heart');
var good = document.getElementById('good');
var notGood = document.getElementById('not-good');

// 찜 버튼 상태
var isGood = storeGood.getAttribute('data-is-good');
if (isGood === 'true') {
  good.style.display = 'block';
  notGood.style.display = 'none';
}

// 찜하기
notGood.addEventListener('click', () => {
  var reviewId = good.getAttribute('data-review-id');

  // 찜하기 버튼을 눌렀을 때
  addGood(reviewId).then(response => {
    var responseData = response.data;

    if (responseData.message > 0) {
      // 찜 버튼 변경
      good.style.display = 'block';
      notGood.style.display = 'none';

      // 알림 푸시
      sendNotificationGood(writer).then(response => {
        console.log("server push: good");
      });
    } else {
      // 로그인이 안되어 있는 경우
      alert('로그인이 필요합니다');
      return;
    }
  });
});

// 찜하기
async function addGood(reviewId) {
  var response = await axios.get(`/goods/${reviewId}`);
  return response;
}

// 찜취소
good.addEventListener('click', () => {
  var reviewId = notGood.getAttribute('data-review-id');

  cancelGood(reviewId).then(response => {
    var responseData = response.data;

    if (responseData.message > 0) {
      notGood.style.display = 'block';
      good.style.display = 'none';
    } else {
      // 로그인이 안되어 있는 경우
      alert('찜하기는 로그인이 필요합니다');
    }
  });
});

// 찜취소
async function cancelGood(reviewId) {
  var response = await axios.get(`/goods/cancel/${reviewId}`);
  return response;
}

// 알림 발송
async function sendNotificationGood(writer) {
  var notification = {
    'fromEmail': email.value,
    'toEmail': writer.value,
    'category': 'good',
    'content': `${memberNickName.value}님이 [${reviewTitle.innerText}] 리뷰를 찜하였습니다`,
    'url': `/review/view?review_id=${good.getAttribute('data-review-id')}`
  }
  var response = await axios.post(`/notifications/publish/${writer.value}`, notification);
  return response;
}

export {email, writer, memberNickName, writerNickName}