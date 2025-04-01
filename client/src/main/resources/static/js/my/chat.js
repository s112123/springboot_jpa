import { accessTokenUtils } from '/js/common.js';

// Access Token 확인
accessTokenUtils.redirectLoginPage();

// 웹 소켓 연결
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

// 메세지 수신
stompClient.connect({
    Authorization: `Bearer ${accessTokenUtils.getAccessToken()}`
}, () => {
    stompClient.subscribe(`/user/${accessTokenUtils.getMemberId()}/chat/subscribe`, (response) => {
        console.log(response.body);
    });
}, (error) => {
    console.error('WebSocket Connect Failed: ', error);
});

// 채팅 대상 목록
findFollows(accessTokenUtils.getMemberId()).then((response) => {
    let follows = response.data;
    console.log(follows);
    if (follows) {
        // 목록 렌더링
        let followListElement = document.getElementById('chat-list');
        followListElement.innerHTML = getFollowListHTML(follows.data);

        // 채팅 대상자에게 클릭 이벤트 적용
        const receivers = document.querySelectorAll('.chat-item');
        receivers.forEach((receiver, index) => {
            receiver.addEventListener('click', () => {
                console.log('click!!');
                // 채팅 대상 목록에서 선택한 대상자 색상 변경
                chatActive(receivers, index);

                // 메세지 목록 가져오기
                let conversationView = document.querySelector('#conversation-view');
                conversationView.innerHTML = '';
            });
        });
    }
});

// 내가 구독한 사람 목록 (채팅 대상 목록) API
async function findFollows(memberId) {
    const api = 'http://localhost:8081/api/v1/follows/' + memberId + '/follow';
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
//        html += '        <i class="fa-solid fa-circle fa-2xs"></i>';
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



// 변수 선언
//var chatItems = document.querySelectorAll('.chat-item');
//var message = document.querySelector('#message');
//var btnSend = document.querySelector('#send-message');
//var conversationView = document.querySelector('#conversation-view');
//var isClicked = false;
//var senderEmail = null;
//var receiverEmail = null;
//var socket = null;
//var stompClient = null;
//var chatRoomKey = '';
//
//// 채팅 대상자 클릭시 (채팅방 생성 → 채팅 기록 불러오기 → 웹 소켓 연결)
//chatItems.forEach((chatItem, index) => {
//  chatItem.addEventListener('click', () => {
//    // 기존에 연결된 WebSocket 연결 끊기
//    if (stompClient !== null) {
//      stompClient.disconnect();
//      isClicked = false;
//    }
//
//    // 채팅 대상자 클릭
//    isClicked = true;
//    senderEmail = chatItem.querySelectorAll('input')[0];
//    receiverEmail = chatItem.querySelectorAll('input')[1];
//
//    // 채팅 대상자 색상 변경
//    chatActive(index);
//
//    // 채팅방 생성
//    makeChatRoom(receiverEmail).then((response) => {
//      chatRoomKey = response.data;
//
//      // 채팅 목록 가져오기
//      getChatMessages(receiverEmail).then((response) => {
//        var messages = response.data;
//        var html = '';
//        conversationView.innerHTML = '';
//
//        for (var i = 0; i < messages.length; i++) {
//          if (messages[i].senderEmail === receiverEmail.value) {
//            html += `<div class="receiver-message-box">`;
//            html += `  <p class="receiver-message">${messages[i].message}</p>`;
///*            if (!messages[i].isRead) {
//              html += `  <p class="is-read-message">1</p>`;
//            }*/
//            html += `</div>`;
//          }
//
//          if (messages[i].senderEmail === senderEmail.value) {
//            html += `<div class="sender-message-box">`;
///*            if (!messages[i].isRead) {
//              html += `  <p class="is-read-message">1</p>`;
//            }*/
//            html += `  <p class="sender-message">${messages[i].message}</p>`;
//            html += `</div>`;
//          }
//        }
//        conversationView.innerHTML = html;
//        conversationView.scrollTop = conversationView.scrollHeight;
//      });
//
//      // 읽음 처리
//      readMessage(chatRoomKey, senderEmail, receiverEmail, isClicked);
//    });
//
//    // 웹 소켓 연결
//    socket = new SockJS('/ws');
//    stompClient = Stomp.over(socket);
//    stompClient.debug = null;
//    stompClient.connect({}, () => {
//      stompClient.subscribe(`/user/${chatRoomKey}/private/message`, (response) => {
//        var message = JSON.parse(response.body);
//        var html = '';
//
//        console.log(message);
//        // 내가 보낸 메세지
//        if (message.senderEmail === senderEmail.value) {
//          html += `<div class="sender-message-box">`;
///*          if (!message.isRead) {
//            html += `  <p class="is-read-message">1</p>`;
//          }*/
//          html += `  <p class="sender-message">${message.message}</p>`;
//          html += `</div>`;
//        }
//
//        // 상대방이 보낸 메세지
//        if (message.senderEmail === receiverEmail.value) {
//          html += `<div class="receiver-message-box">`;
//          html += `  <p class="receiver-message">${message.message}</p>`;
///*          if (!message.isRead) {
//            html += `  <p class="is-read-message">1</p>`;
//          }*/
//          html += `</div>`;
//        }
//
//        conversationView.innerHTML += html;
//        conversationView.scrollTop = conversationView.scrollHeight;
//
//        // 읽음 처리
//        readMessage(chatRoomKey, senderEmail, receiverEmail, isClicked);
//      });
//    });
//  });
//});
//
//// 채팅 대상자 색상 변경
//function chatActive(index) {
//  chatItems.forEach((chatItem) => {
//    chatItem.classList.remove('chat-active');
//  });
//  chatItems[index].classList.add('chat-active');
//}
//
//// 채팅방 생성
//async function makeChatRoom(receiverEmail) {
//  var response = await axios.get(`/chats/make_room/${receiverEmail.value}`);
//  return response;
//}
//
//// 채팅 대화 불러오기
//async function getChatMessages(receiverEmail) {
//  var response = await axios.get(`/chats/${receiverEmail.value}`);
//  return response;
//}
//
//// 읽음 처리
//async function readMessage(chatRoomKey, senderEmail, receiverEmail, isRead) {
//  var chatMessage = {
//    'chatRoomKey': chatRoomKey,
//    'senderEmail': senderEmail.value,
//    'receiverEmail': receiverEmail.value,
//    'isRead': isRead
//  }
//
//  var response = await axios.patch(`/chats/read`, chatMessage);
//  return response;
//}
//
//// 메세지 입력 input 에서 Enter 키 처리
//message.addEventListener('keypress', () => {
//  if (event.keyCode === 13) {
//    // 대화 상대 선택 유무 및 메세지 내용 입력 여부 확인
//    if (!isClicked) {
//      alert("채팅상대를 선택하세요");
//      return;
//    }
//
//    if (message.value.trim().length === 0) {
//      alert("메세지를 입력하세요");
//      return;
//    }
//
//    // 채팅 메세지 보내기
//    var chatMessage = {
//      'senderEmail': senderEmail.value,
//      'receiverEmail': receiverEmail.value,
//      'message': message.value.trim()
//    };
//    sendChatMessage(chatMessage);
//    message.value = '';
//  }
//});
//
//// 보내기 버튼 클릭시, 채팅방 생성 및 메세지 발송
//btnSend.addEventListener('click', () => {
//  // 대화 상대 선택 유무 및 메세지 내용 입력 여부 확인
//  if (!isClicked) {
//    alert("채팅상대를 선택하세요");
//    return;
//  }
//
//  if (message.value.trim().length === 0) {
//    alert("메세지를 입력하세요");
//    return;
//  }
//
//  // 채팅 메세지 보내기
//  var chatMessage = {
//    'senderEmail': senderEmail.value,
//    'receiverEmail': receiverEmail.value,
//    'message': message.value.trim()
//  };
//  sendChatMessage(chatMessage);
//  message.value = '';
//});
//
//// 메시지 발송
//async function sendChatMessage(chatMessage) {
//  stompClient.send('/app/chat_service', {}, JSON.stringify(chatMessage));
//}

