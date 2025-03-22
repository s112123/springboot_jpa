// 변수 선언
var chatItems = document.querySelectorAll('.chat-item');
var message = document.querySelector('#message');
var btnSend = document.querySelector('#send-message');
var conversationView = document.querySelector('#conversation-view');
var isClicked = false;
var senderEmail = null;
var receiverEmail = null;
var socket = null;
var stompClient = null;
var chatRoomKey = '';

// 채팅 대상자 클릭시 (채팅방 생성 → 채팅 기록 불러오기 → 웹 소켓 연결)
chatItems.forEach((chatItem, index) => {
  chatItem.addEventListener('click', () => {
    // 기존에 연결된 WebSocket 연결 끊기
    if (stompClient !== null) {
      stompClient.disconnect();
      isClicked = false;
    }

    // 채팅 대상자 클릭
    isClicked = true;
    senderEmail = chatItem.querySelectorAll('input')[0];
    receiverEmail = chatItem.querySelectorAll('input')[1];

    // 채팅 대상자 색상 변경
    chatActive(index);

    // 채팅방 생성
    makeChatRoom(receiverEmail).then((response) => {
      chatRoomKey = response.data;

      // 채팅 목록 가져오기
      getChatMessages(receiverEmail).then((response) => {
        var messages = response.data;
        var html = '';
        conversationView.innerHTML = '';

        for (var i = 0; i < messages.length; i++) {
          if (messages[i].senderEmail === receiverEmail.value) {
            html += `<div class="receiver-message-box">`;
            html += `  <p class="receiver-message">${messages[i].message}</p>`;
/*            if (!messages[i].isRead) {
              html += `  <p class="is-read-message">1</p>`;
            }*/
            html += `</div>`;
          }

          if (messages[i].senderEmail === senderEmail.value) {
            html += `<div class="sender-message-box">`;
/*            if (!messages[i].isRead) {
              html += `  <p class="is-read-message">1</p>`;
            }*/
            html += `  <p class="sender-message">${messages[i].message}</p>`;
            html += `</div>`;
          }
        }
        conversationView.innerHTML = html;
        conversationView.scrollTop = conversationView.scrollHeight;
      });

      // 읽음 처리
      readMessage(chatRoomKey, senderEmail, receiverEmail, isClicked);
    });

    // 웹 소켓 연결
    socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, () => {
      stompClient.subscribe(`/user/${chatRoomKey}/private/message`, (response) => {
        var message = JSON.parse(response.body);
        var html = '';

        console.log(message);
        // 내가 보낸 메세지
        if (message.senderEmail === senderEmail.value) {
          html += `<div class="sender-message-box">`;
/*          if (!message.isRead) {
            html += `  <p class="is-read-message">1</p>`;
          }*/
          html += `  <p class="sender-message">${message.message}</p>`;
          html += `</div>`;
        }

        // 상대방이 보낸 메세지
        if (message.senderEmail === receiverEmail.value) {
          html += `<div class="receiver-message-box">`;
          html += `  <p class="receiver-message">${message.message}</p>`;
/*          if (!message.isRead) {
            html += `  <p class="is-read-message">1</p>`;
          }*/
          html += `</div>`;
        }

        conversationView.innerHTML += html;
        conversationView.scrollTop = conversationView.scrollHeight;

        // 읽음 처리
        readMessage(chatRoomKey, senderEmail, receiverEmail, isClicked);
      });
    });
  });
});

// 채팅 대상자 색상 변경
function chatActive(index) {
  chatItems.forEach((chatItem) => {
    chatItem.classList.remove('chat-active');
  });
  chatItems[index].classList.add('chat-active');
}

// 채팅방 생성
async function makeChatRoom(receiverEmail) {
  var response = await axios.get(`/chats/make_room/${receiverEmail.value}`);
  return response;
}

// 채팅 대화 불러오기
async function getChatMessages(receiverEmail) {
  var response = await axios.get(`/chats/${receiverEmail.value}`);
  return response;
}

// 읽음 처리
async function readMessage(chatRoomKey, senderEmail, receiverEmail, isRead) {
  var chatMessage = {
    'chatRoomKey': chatRoomKey,
    'senderEmail': senderEmail.value,
    'receiverEmail': receiverEmail.value,
    'isRead': isRead
  }

  var response = await axios.patch(`/chats/read`, chatMessage);
  return response;
}

// 메세지 입력 input 에서 Enter 키 처리
message.addEventListener('keypress', () => {
  if (event.keyCode === 13) {
    // 대화 상대 선택 유무 및 메세지 내용 입력 여부 확인
    if (!isClicked) {
      alert("채팅상대를 선택하세요");
      return;
    }

    if (message.value.trim().length === 0) {
      alert("메세지를 입력하세요");
      return;
    }

    // 채팅 메세지 보내기
    var chatMessage = {
      'senderEmail': senderEmail.value,
      'receiverEmail': receiverEmail.value,
      'message': message.value.trim()
    };
    sendChatMessage(chatMessage);
    message.value = '';
  }
});

// 보내기 버튼 클릭시, 채팅방 생성 및 메세지 발송
btnSend.addEventListener('click', () => {
  // 대화 상대 선택 유무 및 메세지 내용 입력 여부 확인
  if (!isClicked) {
    alert("채팅상대를 선택하세요");
    return;
  }

  if (message.value.trim().length === 0) {
    alert("메세지를 입력하세요");
    return;
  }

  // 채팅 메세지 보내기
  var chatMessage = {
    'senderEmail': senderEmail.value,
    'receiverEmail': receiverEmail.value,
    'message': message.value.trim()
  };
  sendChatMessage(chatMessage);
  message.value = '';
});

// 메시지 발송
async function sendChatMessage(chatMessage) {
  stompClient.send('/app/chat_service', {}, JSON.stringify(chatMessage));
}

