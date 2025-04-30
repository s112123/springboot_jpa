import { accessTokenUtils } from '/js/common.js';

// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const reviewId = params.get('review_id');

// 변수 선언
let btnSubscribe = document.getElementById('btn-subscribe');
let writer = document.getElementById('writer-name');

// 구독 버튼 클릭
btnSubscribe.addEventListener('click', (e) => {
    // Access Token 이 없으면 로그인 화면으로 이동
    if (!accessTokenUtils.getAccessToken()) {
        alert('로그인이 필요합니다');
        // 현재 URL 에서 도메인을 제거한 경로
        // http://localhost:8080/review/view?review_id=1 → /review/view?review_id=1
        let redirectUrl = window.location.pathname + window.location.search;
        location.href = '/login?redirect=' + redirectUrl;
        return;
    }

    // 구독 버튼의 텍스트
    let btnSubscribeText = btnSubscribe.innerText;

    // 구독하기
    if (btnSubscribeText === '구독하기') {
        if (confirm(writer.textContent + '님을 구독하시겠습니까?')) {
            const formData = {
                'followerId': accessTokenUtils.getMemberId(),
                'username': writer.textContent
            }
            followMember(formData).then(() => {
                // 구독하기 버튼을 구독취소 버튼으로 변경
                toSubscribeCancelButton();
            });
        }
    }

    // 구독취소
    if (btnSubscribeText === '구독취소') {
        if (confirm(writer.textContent + '님을 구독 취소하시겠습니까?')) {
            unFollow(accessTokenUtils.getMemberId(), writer.textContent).then(() => {
                // 구독취소 버튼을 구독하기 버튼으로 변경
                toSubscribeButton();
            });
        }
    }
});

// 구독하기 API
async function followMember(formData) {
    const api = 'http://localhost:8081/api/v1/follows';
    const response = await axios.post(api, formData, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
}

// 구독하기 버튼을 구독취소 버튼으로 변경
function toSubscribeCancelButton() {
    btnSubscribe.classList.remove('not-subscribed');
    btnSubscribe.classList.add('subscribed');
    btnSubscribe.textContent = '구독취소';
}

// 구독취소 버튼을 구독하기 버튼으로 변경
function toSubscribeButton() {
    btnSubscribe.classList.remove('subscribed');
    btnSubscribe.classList.add('not-subscribed');
    btnSubscribe.textContent = '구독하기';
}

// 구독 여부 API
async function isFollower(followerId, followedUsername) {
    const api = 'http://localhost:8081/api/v1/follows/' + followerId + '/' + followedUsername;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 구독 취소 API
async function unFollow(followerId, followedUsername) {
    const api = 'http://localhost:8081/api/v1/follows/' + followerId + '/' + followedUsername;
    const response = await axios.delete(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

export { isFollower, toSubscribeCancelButton, toSubscribeButton };