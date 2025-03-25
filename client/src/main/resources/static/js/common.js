// AccessToken 가져오기
function getAccessToken() {
    return localStorage.getItem('todayReviewsAccessToken');
}

// Access Token 에서 username 추출
// JWT 는 Base64 로 인코딩 되어 있으므로 Access Token 에서 디코딩하여 username 추출
// JWT 에 한글이 포함되면 한글은 UTF-8 로 인코딩된 후, Base64 로 인코딩된다
// 그래서 한글이 포함된 경우 atob() 로 Base64 를 디코딩하면 UTF-8 로 인코딩된 한글이 있다
// 그리고 UTF-8 을 다시 디코딩 해주면 된다
// 영어는 ASCII 를 사용하므로 상관없다
function getUsername() {
    let accessToken = getAccessToken();
    if (!accessToken) {
        return null;
    }
    // 페이로드 추출 → 헤더.페이로드.서명
    const payload = accessToken.split('.')[1];
    // atob() → Javascript 에 내장된 Base64 인코딩 데이터를 디코딩하는 함수
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    // 디코딩이 한글인 경우, UTF-8 로 변환
    const jsonPayload = decodeURIComponent(escape(decoded));
    return JSON.parse(jsonPayload).username;
}

// Access Token 에서 memberId 추출
function getMemberId() {
    let accessToken = getAccessToken();
    if (!accessToken) {
        return null;
    }
    const payload = accessToken.split('.')[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    const jsonPayload = decodeURIComponent(escape(decoded));
    return JSON.parse(jsonPayload).memberId;
}

// AccessToken 이 없으면 로그인 페이지로 이동
function redirectLoginPage() {
    // accessToken 변수는 최초 로그인 후에는 값이 할당이 되어 있다
    // 그래서 로그인 후, Access Token 이 필요한 작업 중 Access Token 이 지워지면 다시 로그인 페이지로 가야한다
    // 그래서 다시 accessToken 변수가 아니라 getAccessToken() 으로 확인한다
    if (!getAccessToken()) {
        location.href = '/login';
        return;
    }
}

export const accessTokenUtils = {
    getAccessToken,
    getUsername,
    getMemberId,
    redirectLoginPage
};