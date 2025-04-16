import { accessTokenUtils } from '/js/common.js';

// 변수 선언
const noticeContent = document.getElementById('content');
const btnSendNotice = document.getElementById('btn-send-notice');

// 공지 발송 → 보내기 버튼 클릭
btnSendNotice.addEventListener('click', () => {
    // 유효성 검사 → AccessToken 여부
    accessTokenUtils.redirectLoginPage();
    // 공지 발송
    sendNotice();
});

// 공지 발송 → 공지 내용을 입력하고 Enter 를 누른 경우
noticeContent.addEventListener('keydown', (e) => {
    // 유효성 검사 → AccessToken 여부
    accessTokenUtils.redirectLoginPage();
    // 공지 발송
    if (e.key === 'Enter') {
        sendNotice();
    }
});

// 공지 발송
function sendNotice() {
    // 공지 메세지
    const jsonData = {
        writerId: accessTokenUtils.getMemberId(),
        content: content.value.trim()
    };

    // 공지 발송
    addNotice(jsonData).then(() => {
        alert('공지가 등록되었습니다');
        noticeContent.value = '';
        noticeContent.focus();
    });
}

// 공지 발송 API
async function addNotice(jsonData) {
    const api = 'http://localhost:8081/api/v1/notices';
    const response = await axios.post(api, jsonData, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}