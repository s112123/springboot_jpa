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