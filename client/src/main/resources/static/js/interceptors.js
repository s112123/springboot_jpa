import { accessTokenUtils } from '/js/common.js';

// 토큰 검증
axios.interceptors.response.use(
    response => response,
    async error => {
        const status = error.response?.status;
        switch (status) {
            case 602: // 만료된 토큰
                try {
                    // console.log('만료된 토큰');
                    const refreshResponse = await axios.post('http://localhost:8081/api/v1/tokens/refresh', {}, {
                        withCredentials: true
                    });
                    const newAccessToken = refreshResponse.data.accessToken;

                    // 새로운 Access Token 을 sessionStorage 에 저장
                    accessTokenUtils.saveAccessToken(newAccessToken);

                    // 원래 요청 다시 시도
                    const originalRequest = error.config;
                    originalRequest.headers.Authorization = 'Bearer ' + newAccessToken;
                    return axios(originalRequest);
                } catch (refreshError) {
                    // 로그아웃 처리
                    await logoutAndRedirect();
                    // error 를 원래 요청한 axios 메서드로 넘겨서 해당 메서드의 .catch(error => {}) 에서 처리
                    return Promise.reject(refreshError);
                }
            case 601: // 유효하지 않은 토큰
            case 603: // 잘못된 토큰
            case 604: // 토큰 없음
                // console.log('잘못된 토큰');
                // 로그아웃 처리
                await logoutAndRedirect();
                break;
        }

        // error 를 원래 요청한 axios 메서드로 넘겨서 해당 메서드의 .catch(error => {}) 에서 처리
        return Promise.reject(error);
    }
);

// 로그아웃 처리
async function logoutAndRedirect() {
    try {
        // 로그아웃 API 호출 (refresh 토큰 제거)
        await axios.post('http://localhost:8081/api/v1/logout');
    } catch (e) {
        console.error('error: ', e);
    }

    // localStorage 에서 Access Token 제거
    accessTokenUtils.removeAccessToken();
    // refresh 토큰 쿠키 삭제
    document.cookie = 'todayReviewsRefreshToken=; path=/; max-age=0';
    // 로그인 페이지로 이동
    window.location.href = '/login';
}