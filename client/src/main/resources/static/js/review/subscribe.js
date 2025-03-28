import { accessTokenUtils } from '/js/common.js';

// 변수 선언
let btnSubscribe = document.getElementById('btn-subscribe');

// 구독 여부에 따라 버튼 상태 변경



// 구독 버튼 클릭
btnSubscribe.addEventListener('click', (e) => {
    // Access Token 이 없으면 로그인 화면으로 이동
    if (!accessTokenUtils.getAccessToken()) {
        // 현재 URL 에서 도메인을 제거한 경로
        // http://localhost:8080/review/view?review_id=1 → /review/view?review_id=1
        let redirectUrl = window.location.pathname + window.location.search;
        location.href = '/login?redirect=' + redirectUrl;
        return;
    }

    // 구독하기
    if (confirm('작성자님을 구독하시겠습니까?')) {
        console.log('구독완료!');
        // 구독하기 버튼을 구독취소 버튼으로 변경
        toSubscribeCancelButton();
    }
});

// 구독하기 버튼을 구독취소 버튼으로 변경
function toSubscribeCancelButton() {
    btnSubscribe.classList.remove('not-subscribed');
    btnSubscribe.classList.add('subscribed');
    btnSubscribe.textContent = '구독취소';
}


/*
import {email, writer, memberNickName, writerNickName} from './good.js';

// 변수 선언
var btnSubscribe = document.getElementById('btn-subscribe');
var btnSubscribeText = '';

// 구독하기 버튼 클릭
btnSubscribe.addEventListener('click', () => {
  if (email.value.trim().length === 0) {
    alert('로그인이 필요합니다');
    return;
  }

  // 구독자 - 발행자
  var subscribe = {
    'subscriberEmail': email.value,
    'publisherEmail': writer.value
  }

  // 버튼 텍스트
  btnSubscribeText = btnSubscribe.innerText;
  console.log(btnSubscribeText);

  // 구독하기
  if (btnSubscribeText === '구독하기') {
    follow(subscribe).then(response => {
      console.log(response.data);

      // 알림 발송
      sendNotificationSubscribe(writer).then(() => {
        console.log("server push: subscribe");
        // 구독취소로 버튼 변경
        showSubscribeCancelBtn();
        return;
      });
    });
  }

  // 구독취소
  if (btnSubscribeText === '구독취소') {
    if (confirm('구독을 취소하시겠습니까?')) {
      cancelFollow(subscribe).then(() => {
        // 구독하기로 버튼 변경
        showSubscribeBtn();
      });
    }
  }
});

// 구독취소
async function cancelFollow(subscribe) {
  var response = await axios.post(`/subscribes/cancel`, subscribe);
  return response;
}

// 구독하기
async function follow(subscribe) {
  var response = await axios.post(`/subscribes`, subscribe);
  return response;
}

// 알림 발송
async function sendNotificationSubscribe(writer) {
  var notification = {
    'fromEmail': email.value,
    'toEmail': writer.value,
    'category': 'subscribe',
    'content': `${memberNickName.value}님이 ${writerNickName.innerText}님을 구독하였습니다`,
    'url': `/my/profile`
  }
  var response = await axios.post(`/notifications/publish/${writer.value}`, notification);
  return response;
}

// 구독취소 버튼
function showSubscribeCancelBtn() {
  btnSubscribe.innerText = '구독취소';
  btnSubscribe.style.backgroundColor = 'rgb(230, 230, 230)';
  btnSubscribe.style.color = '#000';
}

// 구독하기 버튼
function showSubscribeBtn() {
   btnSubscribe.innerText = '구독하기';
   btnSubscribe.style.backgroundColor = 'rgb(210, 40, 40)';
   btnSubscribe.style.color = '#fff';
}
*/