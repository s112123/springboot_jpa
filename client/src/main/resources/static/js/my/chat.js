import { accessTokenUtils } from '/js/common.js';

// 변수 선언
let message = document.getElementById('message');
const btnSendMessage = document.getElementById('send-message');
let conversationView = document.querySelector('#conversation-view');
let receiverId = undefined;
let isSelected = false;

// Access Token 확인
accessTokenUtils.redirectLoginPage();

// 웹 소켓 연결
let socket = null;
let stompClient = null;

// 채팅 대상 목록
findFollows(accessTokenUtils.getMemberId()).then((response) => {
    let follows = response.data;

    if (follows) {
        // 목록 렌더링
        let followListElement = document.getElementById('chat-list');
        followListElement.innerHTML = getFollowListHTML(follows);

        // 채팅 대상자에게 클릭 이벤트 적용
        const receivers = document.querySelectorAll('.chat-item');
        receivers.forEach((receiver, index) => {
            // 채팅방 참여
            receiver.addEventListener('click', () => {
                // 기존에 연결된 WebSocket 연결 끊기
                if (stompClient !== null) {
                    stompClient.disconnect();
                }

                isSelected = true;
                receiverId = receiver.getAttribute('data-member-id');

                // 채팅 대상 목록에서 선택한 대상자 색상 변경
                chatActive(receivers, index);

                // 채팅방 참여
                const jsonData = {
                    to: receiverId,
                    from: accessTokenUtils.getMemberId()
                };
                joinChatRoom(jsonData).then((response) => {
                    // 메세지 목록 가져오기
                    let chatMessages = response.data;
                    conversationView.innerHTML = getChatMessagesHTML(chatMessages);
                    conversationView.scrollTop = conversationView.scrollHeight;

                    // 메세지 입력 요소 초기화
                    message.value = '';
                    message.focus();
                });

                // stompClient 연결
                socket = new SockJS('http://localhost:8081/ws');
                stompClient = Stomp.over(socket);
                stompClient.connect({ 'Authorization': accessTokenUtils.getAccessToken() }, () => {
                    // 구독
                    stompClient.subscribe(`/user/${accessTokenUtils.getMemberId()}/chat/subscribe`, (response) => {
                        console.log(response.body);

                        // 현재 받은 채팅 메세지 출력
                        let chatMessage = JSON.parse(response.body);
                        let html = '';
                        html += `<div class="receiver-message-box">`;
                        html += `  <p class="receiver-message">${chatMessage.message}</p>`;
                        html += `</div>`;

                        conversationView.innerHTML += html;
                        conversationView.scrollTop = conversationView.scrollHeight;
                    });
                }, (error) => {
                    console.error('WebSocket Connect Failed: ', error);
                    alert('Disconnected');
                    location.replace('/login?redirect=/my/chat');
                });
            });
        });
    }
});

// 내가 구독한 사람 목록 (채팅 대상 목록) API
async function findFollows(memberId) {
    const api = 'http://localhost:8081/api/v1/chats/follows?memberId=' + memberId;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 내가 구독한 사람 목록 (채팅 대상 목록) HTML 생성
function getFollowListHTML(follows) {
    let html = '';
    html += '<ul>';
    for (let follow of follows) {
        // 프로필 이미지 경로 추출
        let profileImage = follow.profileImage;
        let profileImageFileName = profileImage.savedFileName;
        let imgSrc = undefined;
        if (profileImageFileName === 'default.png') {
            // src > main > resources > static > images > profiles > default.png
            imgSrc = '/images/profiles/default.png';
        } else {
            // uploads > profiles > memberId
            imgSrc = 'http://localhost:8081/api/v1/members/profile-images/' +
                      accessTokenUtils.getMemberId() + '/' + profileImage.savedFileName;
        }

        // HTML 생성
        html += `    <li class="chat-item" data-member-id="${follow.memberId}">`;
        html += '        <img src="' + imgSrc + '">';
        html += '        <span>' + follow.username.substring(0, 9) + '</span>';

        /* 읽음 여부 표시
        if () {
            html += '        <i class="fa-solid fa-circle fa-2xs"></i>';
        }
        */

        html += '    </li>';
    }
    html += '</ul>'

    return html;
}

// 채팅 대상자 색상 변경
function chatActive(receivers, index) {
    receivers.forEach((receiver) => {
        receiver.classList.remove('chat-active');
    });
    receivers[index].classList.add('chat-active');
}

// 채팅 메세지 목록 HTML 생성
function getChatMessagesHTML(chatMessages) {
    let html = '';
    for (let chatMessage of chatMessages) {
        if (chatMessage.memberId === accessTokenUtils.getMemberId()) {
            html += `<div class="sender-message-box">`;
            html += `  <p class="sender-message">${chatMessage.message}</p>`;
            html += `</div>`;
        } else {
            html += `<div class="receiver-message-box">`;
            html += `  <p class="receiver-message">${chatMessage.message}</p>`;
            /*
            if (!messages[i].isRead) {
                html += `  <p class="is-read-message">1</p>`;
            }
            */
            html += `</div>`;
        }
    }
    return html;
}

// 채팅방 참여 및 채팅방 메세지 목록 API
async function joinChatRoom(jsonData) {
    const api = `http://localhost:8081/api/v1/chats/rooms`;
    return await axios.post(api, jsonData, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
}

// 메세지 발송 → 보내기 버튼을 누른 경우
btnSendMessage.addEventListener('click', () => {
    sendMessage();
});

// 메세지 발송 → 메세지를 입력하고 Enter 를 누른 경우
message.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        sendMessage();
    }
});

// 메세지 전송
function sendMessage() {
    // 유효성 검사 → 채팅 상대 선택 여부
    if (!isSelected) {
        alert("메세지를 보낼 대상을 선택하세요");
        return;
    }
    // 유효성 검사 → 메세지 입력 여부
    if (message.value.trim().length === 0) {
        alert("메세지를 입력하세요");
        message.focus();
        return;
    }

    // 메세지 전송
    if (receiverId !== undefined) {
        // 헤더
        const headers = {
            'Authorization': accessTokenUtils.getAccessToken()
        };

        // 페이로드
        const payload = {
            to: receiverId,
            from: accessTokenUtils.getMemberId(),
            message: message.value
        }

        // 메세지 발송
        stompClient.send(`/pub/chat/message/send`, headers, JSON.stringify(payload));

        // 보낸 메세지 화면에 표시
        let html = '';
        html += `<div class="sender-message-box">`;
        html += `  <p class="sender-message">${payload.message}</p>`;
        html += `</div>`;
        conversationView.innerHTML += html;
        conversationView.scrollTop = conversationView.scrollHeight;

        // 메세지 입력 요소 초기화
        message.value = '';
        message.focus();
    }
}

// 페이지 전환할 때 Redis 에서 채팅방 참여 캐시 삭제
// 페이지를 전환할 때 기존 SSE 연결 닫기
window.addEventListener('beforeunload', () => {
    exitChatRoom(accessTokenUtils.getMemberId())
});

// 채팅방 참여 캐시 삭제 API
async function exitChatRoom(memberId) {
    const api = 'http://localhost:8081/api/v1/chats/unjoin?memberId=' + memberId;
    const response = await axios.delete(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}