// 변수 선언
var readContents = document.querySelectorAll('.read-content');
var noReadContents = document.querySelectorAll('.no-read-content');
var btnAllRead = document.querySelector('#all-read');

// 읽은 알림 클릭
readContents.forEach((readContent) => {
  readContent.addEventListener('click', () => {
    location.href = readContent.getAttribute('data-notification-url');
  });
});

// 읽지 않은 알림 클릭
noReadContents.forEach((noReadContent) => {
  noReadContent.addEventListener('click', () => {
    var notificationId = noReadContent.getAttribute('data-notification-id');

    // DB에서 읽음 처리
    readNotification(notificationId).then(response => {
      location.href = noReadContent.getAttribute('data-notification-url');
    });
  });
});

btnAllRead.addEventListener('click', () => {
  var ids = [];

  // 읽지 않은 알림의 id 값 담기
  noReadContents.forEach((noReadContent) => {
    ids.push(noReadContent.getAttribute('data-notification-id'));
  });

  // 모두 읽음 처리
  readNotificationAll().then(response => {
    location.href = '/my/notification';
  });
});

// 읽음 처리
async function readNotification(notificationId) {
  var response = await axios.get(`/my/notification/read/${notificationId}`);
  return response;
}

// 모두 읽음 처리
async function readNotificationAll() {
  var response = await axios.get(`/my/notification/read_all`);
  return response;
}