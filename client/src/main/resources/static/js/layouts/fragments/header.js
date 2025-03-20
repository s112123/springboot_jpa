// 변수 선언
var bell = document.getElementById('bell');
var email = document.getElementById('email');

if (email !== null) {
  if (email.value !== undefined && email.value !== '') {
    var eventSource = new EventSource(`/notifications/subscribe/${email.value}`);
    eventSource.addEventListener('sse', (event) => {
      if (event.data !== 'subscribe') {
        bell.classList.add('fa-shake');
        bell.style.color = 'rgb(210, 40, 40)';
      }
    });
  }
}
