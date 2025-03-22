// kakao map
const container = document.getElementById('map');
const options = {
  center: new kakao.maps.LatLng(33.450701, 126.570667),
  level: 3
};

const map = new kakao.maps.Map(container, options);

// 마커이미지 생성
var imageSrc = '/upload/images/map/location.png';
// 마커 크기
var imageSize = new kakao.maps.Size(35, 45);
// 마커의 좌표와 일치시킬 이미지 안에서의 좌표 설정
// var imageOption = {offset: new kakao.maps.Point(23, 69)};
var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize);

// 주소로 좌표 검색
const geocoder = new kakao.maps.services.Geocoder();
const storeAddress = document.getElementById('store-address').innerText;

geocoder.addressSearch(storeAddress, function(result, status) {
  if (status === kakao.maps.services.Status.OK) {
    // 정상적으로 검색이 완료된 경우
    var coords = new kakao.maps.LatLng(result[0].y, result[0].x);

    // 결과값으로 받은 위치를 마커로 표시
    var marker = new kakao.maps.Marker({
        map: map,
        position: coords,
        image: markerImage
    });

    // 지도의 중심을 결과값으로 받은 위치로 이동
    map.setCenter(coords);
  }
});
