import { accessTokenUtils } from '/js/common.js';

// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const reviewId = params.get('review_id');

// 변수 선언
let btnGood = document.getElementById('good');
let btnGoodCancel = document.getElementById('good-cancel');

// 좋아요 버튼 클릭 (빈 하트)
btnGood.addEventListener('click', (e) => {
    // Access Token 이 없으면 로그인 화면으로 이동
    if (!accessTokenUtils.getAccessToken()) {
        // 현재 URL 에서 도메인을 제거한 경로
        // http://localhost:8080/review/view?review_id=1 → /review/view?review_id=1
        let redirectUrl = window.location.pathname + window.location.search;
        location.href = '/login?redirect=' + redirectUrl;
        return;
    }

    // 좋아요 등록
    const formData = {
        'memberId': accessTokenUtils.getMemberId(),
        'reviewId': reviewId
    };
    saveGood(formData).then(() => {
         // 좋아요 버튼 숨기고 좋아요 취소 버튼 활성화
         btnGood.style.display = 'none';
         btnGoodCancel.style.display = 'block';
    });
});

// 좋아요 등록 API
async function saveGood(formData) {
    const api = 'http://localhost:8081/api/v1/goods';
    const response = await axios.post(api, formData, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 좋아요 여부 API
async function isGood(reviewId, memberId) {
    const api = 'http://localhost:8081/api/v1/goods/' + reviewId + '/' + memberId;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 좋아요 취소 버튼 클릭 (꽉 채운 하트)
btnGoodCancel.addEventListener('click', () => {
    // Access Token 이 없으면 로그인 화면으로 이동
    if (!accessTokenUtils.getAccessToken()) {
        let redirectUrl = window.location.pathname + window.location.search;
        location.href = '/login?redirect=' + redirectUrl;
        return;
    }

    // 좋아요 취소
    cancelGood(reviewId, accessTokenUtils.getMemberId()).then(() => {
        // 좋아요 취소 버튼 숨기고 좋아요 버튼 활성화
        btnGoodCancel.style.display = 'none';
        btnGood.style.display = 'block';
    });
});

// 좋아요 취소 API
async function cancelGood(reviewId, memberId) {
    const api = 'http://localhost:8081/api/v1/goods/' + reviewId + '/' + memberId;
    const response = await axios.delete(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

export { isGood };

/*
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
*/