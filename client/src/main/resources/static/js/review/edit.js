import {reviewEditor} from './ckeditor.js';

// 변수 선언
var errors = document.querySelectorAll('.error');
var storeName = document.getElementById('storeName');
var storeAddress = document.getElementById('storeAddress');
var thumbnail = document.getElementById('thumbnailUrl');
var thumbnailFileName = document.getElementById('thumbnailFileName');
var star = document.getElementById('star');
var form = document.getElementById('review-edit-form');
var reviewId = document.getElementById('review_id');
var btnEdit = document.getElementById('edit-review');
var btnRemove = document.getElementById('delete-review');
var btnCancel = document.getElementById('cancel-review');
var isValid = true;

// 삭제 버튼 클릭
btnRemove.addEventListener('click', () => {
  if (confirm('리뷰를 삭제하시겠습니까?')) {
    location.replace('/review/remove?review_id=' + reviewId.value);
  }
});

// 취소 버튼 클릭
btnCancel.addEventListener('click', () => {
  history.back();
});

// 변경 버튼 클릭
btnEdit.addEventListener('click', (e) => {
  e.preventDefault();

  // 에러 표시 모두 닫기
  errors.forEach(error => {
    error.style.display = 'none';
  });

  var ckEditor = document.getElementsByClassName('ck-content')[0];
  var imgTag = ckEditor.getElementsByTagName('img')[0];

  // 입력사항 유효성 검사
  if (storeName.value.trim().length === 0) {
    // 가게 이름 입력 여부
    storeName.nextElementSibling.style.display = 'block';
    isValid = false;
  } else if (storeAddress.value.trim().length === 0) {
    // 가게 주소 입력 여부
    storeAddress.nextElementSibling.style.display = 'block';
    isValid = false;
  } else if (title.value.trim().length === 0) {
    // 제목 입력 여부
    title.nextElementSibling.style.display = 'block';
    isValid = false;
  } else if (imgTag === undefined) {
    console.log('imgTag !== undefined');
    // 글 내용에 이미지 삽입 여부
    document.getElementById('editor-error').style.display = 'block';
    isValid = false;
  } else if (imgTag !== undefined) {
    // 글 내용 입력 여부
    let pTag = ckEditor.getElementsByTagName('p');
    if (pTag.length === 0) {
      document.getElementById('editor-error').style.display = 'block';
      isValid = false;
    } else {
      let textLength = 0;

      for (let i = 0; i < pTag.length; i++) {
        textLength += pTag[i].textContent.trim().length;
      }

      if (textLength === 0) {
        document.getElementById('editor-error').style.display = 'block';
        isValid = false;
      } else if (star.value.trim().length === 0) {
        // 리뷰어 평점 선택 여부
        star.parentNode.nextElementSibling.style.display = 'block';
        isValid = false;
      } else {
        isValid = true;
      }
    }
  }

  if (isValid) {
    // 게시물에 첨부되었던 임시 파일 이름을 실제 업로드 되는 파일 이름으로 변경
    uploadFile().then((response) => {
      var res = response.data;
      var uploadFileNames = res.uploadFileNames;

      // 업로드 파일 이름을 <form> 태그에 <input> 태그로 생성
      for (var i = 0; i < uploadFileNames.length; i++) {
        var $input = document.createElement('input');
        $input.setAttribute('type', 'hidden');
        $input.setAttribute('name', 'uploadFileNames');
        $input.setAttribute('value', uploadFileNames[i]);
        form.appendChild($input);
      }

      // 리뷰 목록 페이지에서 보여줄 이미지 경로 추출
      getThumbnailUrl(imgTag, thumbnail, thumbnailFileName);

      if (res.result === 'saved') {
        // 폼 전송
        form.action = '/review/edit?review_id=' + reviewId.value;
        form.method = 'post';
        form.submit();
      }
    });
  }
});

// 게시물에 첨부되었던 임시 파일 이름을 실제 업로드 되는 파일 이름으로 변경
async function uploadFile() {
  const parser = new DOMParser();
  const reviewEditorHTML = parser.parseFromString(reviewEditor.getData(), 'text/html');

  var imgTags = reviewEditorHTML.body.querySelectorAll('img');
  var uploadFileNames = [];

  imgTags.forEach((img) => {
    // 에디터 내부의 <img>의 src 값 가져옴
    var imgSrc = img.src;
    // 에디터에서 보여지는 파일이름 추출
    // 20240215_5329008e-acfa-45e0-bb6f-f6bb410882fe.png
    var uploadFileName = imgSrc.substring(imgSrc.lastIndexOf('/') + 1);
    uploadFileNames.push(uploadFileName);
    // 에디터 내부의 <img>의 src 값을 임시 이미지가 아닌 실제 사용될 이미지 경로로 처리
    // 변경 전: http://localhost:8080/images/review/20240215_5329008e-acfa-45e0-bb6f-f6bb410882fe.png
    // 변경 후: http://localhost:8080/images/review/5329008e-acfa-45e0-bb6f-f6bb410882fe.png
    img.src = imgSrc.substring(0, imgSrc.lastIndexOf('/') + 1)
        + uploadFileName.substring(uploadFileName.indexOf("_") + 1);
  });

  reviewEditor.setData(reviewEditorHTML.body.innerHTML);

  var response = await axios.post('/reviews/upload_image', {'uploadFileNames': uploadFileNames});
  return response;
}

// 리뷰 목록 페이지에서 보여줄 이미지 경로 추출
function getThumbnailUrl(imgTag, thumbnail, thumbnailFileName) {
   if (imgTag !== null && imgTag !== undefined) {
     // 이미지 경로
     thumbnail.value = imgTag.src;
     thumbnail.value = thumbnail.value.replace(location.protocol + '//' + location.host, '');
     // 이미지 파일 이름
     thumbnailFileName.value = imgTag.src;
     thumbnailFileName.value = thumbnailFileName.value.substring(thumbnailFileName.value.lastIndexOf('/') + 1);
   } else {
     thumbnail.value = '';
     thumbnailFileName.value = '';
   }
}

// 평점 선택
const input = document.getElementById('star');
const stars = [...document.querySelector('#store-star').children];
stars.forEach((star) => {
  star.addEventListener('click', () => {
    let score = star.getAttribute('data-star');
    input.value = score;
    changeStarColor(stars, score);
  });
});

// 평점 별표 색상 변경
function changeStarColor(stars, score) {
  stars.forEach((star, index) => {
    if (index < score) {
      star.style.color = 'rgb(249, 199, 53)';
    } else {
      star.style.color = 'rgb(200, 200, 200)';
    }
  });
}

// 등록된 평점 색상 처리
const reviewerStarList = document.getElementById('store-star');
const reviewerStarItems = [...reviewerStarList.children];
const reviewerStarScore = reviewerStarList.getAttribute('data-reviewer-star');
reviewerStarItems.forEach((reviewerStarItem, index) => {
    if (index < reviewerStarScore) {
      reviewerStarItem.style.color = 'rgb(249, 199, 53)';
    }
});

