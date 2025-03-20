// 변수 선언
var items = document.querySelectorAll('.item');

// 메뉴 선택시, 색상 변경
items.forEach((item, index) => {
  item.addEventListener('click', (e) => {
    //e.preventDefault();
    console.log("select menu");
    //activeMenu(index);
  });
});

// 메뉴 색상 변경
function activeMenu(menuOption) {
  switch (menuOption) {
    case 0:
      // 내 프로필
      location.href = '&menu-option=0'; break;
    case 1:
      // 내가 쓴 리뷰
      location.href = '&menu-option=1'; break;
    case 2:
      // 내가 찜한 리뷰
      location.href = currentUrl + '&menu-option=2'; break;
    case 3:
      // 알림 내역
      location.href = currentUrl + '&menu-option=3'; break;
    case 4:
      // 1:1 채팅
      location.href = currentUrl + '&menu-option=4'; break;
  }
}