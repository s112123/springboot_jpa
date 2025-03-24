import {reviewEditor, tempImageFileNames} from './ckeditor.js';

// 변수 선언
let accessToken = undefined;
let username = undefined;
let btnAdd = document.getElementById('add-review');
let btnCancel = document.getElementById('cancel-review');
let title = document.getElementById('title');
let storeName = document.getElementById('storeName');
let storeAddress = document.getElementById('storeAddress');
let star = document.getElementById('star');
let errors = document.querySelectorAll('.error');

// Access Token 가져오기
accessToken = localStorage.getItem('todayReviewsAccessToken');
if (!accessToken) {
    // Access Token 이 없으면 로그인 페이지로 이동
    location.href = '/login';
} else {
    username = getUsernameFromAccessToken(accessToken);
}

// 리뷰 등록
btnAdd.addEventListener('click', (e) => {
    // 에러 표시 모두 닫기
    errors.forEach(error => {
        error.style.display = 'none';
    });

    // 유효성 검사 → 위치이름 입력 여부
//    if (storeName.value.trim().length === 0) {
//        storeName.nextElementSibling.style.display = 'block';
//        return false;
//    }
    // 유효성 검사 → 위치주소 입력 여부
//    if (storeAddress.value.trim().length === 0) {
//        storeAddress.nextElementSibling.style.display = 'block';
//        return false;
//    }
    // 유효성 검사 → 제목 입력 여부
//    if (title.value.trim().length === 0) {
//        title.nextElementSibling.style.display = 'block';
//        return false;
//    }
    // 유효성 검사 → 글 본문에 이미지가 최소 1개는 삽입되어 있는지 여부
    let ckEditor = document.getElementsByClassName('ck-content')[0];
//    let imgTag = ckEditor.getElementsByTagName('img')[0];
//    if (imgTag === undefined) {
//        document.getElementById('editor-error').style.display = 'block';
//        return false;
//    }
    // 유효성 검사 → 글 내용 입력 여부
//    let pTag = ckEditor.getElementsByTagName('p');
//    if (pTag.length === 0) {
//        document.getElementById('editor-error').style.display = 'block';
//        return false;
//    } else {
//        let textLength = 0;
//        for (let i = 0; i < pTag.length; i++) {
//            textLength += pTag[i].textContent.trim().length;
//        }
//        if (textLength === 0) {
//            document.getElementById('editor-error').style.display = 'block';
//            return false;
//        }
//    }
    // 유효성 검사 → 리뷰어 평점 입력 여부
//    if (star.value.trim().length === 0) {
//        star.parentNode.nextElementSibling.style.display = 'block';
//        return false;
//    }

    // 글에 작성된 <img> 태그
    const parser = new DOMParser();
    const reviewEditorHTML = parser.parseFromString(reviewEditor.getData(), 'text/html');
    let imgTags = reviewEditorHTML.body.querySelectorAll('img');
    // 저장되어야 하는 이미지 파일의 인덱스 배열 → <img data-image-index='1') 로 지정이 되어 있다
    let savedImageIndex = [...imgTags].map(imgTag => parseInt(imgTag.getAttribute('data-image-index')));

    // 저장되어야 하는 <img> 태그
    let savedImgTags = tempImageFileNames.filter((_, index) => savedImageIndex.includes(index));
    console.log(savedImgTags);
    // 삭제되어야 하는 <img> 태그
    let deletedImgTags = tempImageFileNames.filter((_, index) => !savedImageIndex.includes(index));
    console.log(deletedImgTags);

    // 리뷰 입력 정보
    // reviewEditor → ckeditor.js 에 선언되어 있다
    // reviewEditor.getData() → CkEditor 의 내용 추출
    const formData = {
        'writer': username,
        'title': title.value.trim(),
        'storeName': storeName.value.trim(),
        'storeAddress': storeAddress.value.trim(),
        'content': reviewEditor.getData(),
        'star': star.value,
        'reviewImages': savedImgTags
    };

    // 리뷰 등록
    addReview(formData, accessToken).then(response => {
        console.log(response.data);
    });
});

// 평점 선택
let stars = [...document.querySelector('#store-star').children];
stars.forEach((s) => {
    s.addEventListener('click', () => {
        let score = s.getAttribute('data-star');
        star.value = score;
        changeStarColor(stars, score);
    });
});

// 평점 별표 색상 변경
function changeStarColor(stars, score) {
    stars.forEach((s, index) => {
        if (index < score) {
            s.style.color = 'rgb(249, 199, 53)';
        } else {
            s.style.color = 'rgb(200, 200, 200)';
        }
    });
}

// 리뷰 등록 API
async function addReview(formData, accessToken) {
    const response = await axios.post('http://localhost:8081/api/v1/reviews', formData, {
        headers: {
            "Authorization": accessToken,
            "Content-Type": "application/json"
        }
    });
    return response;
}

// JWT 는 Base64 로 인코딩 되어 있으므로 Access Token 에서 디코딩하여 username 추출
// JWT 에 한글이 포함되면 한글은 UTF-8 로 인코딩된 후, Base64 로 인코딩된다
// 그래서 한글이 포함된 경우 atob() 로 Base64 를 디코딩하면 UTF-8 로 인코딩된 한글이 있다
// 그리고 UTF-8 을 다시 디코딩 해주면 된다
// 영어는 ASCII 를 사용하므로 상관없다
function getUsernameFromAccessToken(token) {
    // 페이로드 추출 → 헤더.페이로드.서명
    const payload = token.split('.')[1];
    // atob() → Javascript 에 내장된 Base64 인코딩 데이터를 디코딩하는 함수
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    // 디코딩이 한글인 경우, UTF-8 로 변환
    const jsonPayload = decodeURIComponent(escape(decoded));
    return JSON.parse(jsonPayload).username;
}

// 취소 버튼 클릭
btnCancel.addEventListener('click', () => {
    if (confirm('리뷰 등록을 취소하시겠습니까?')) {
        location.href = "/";
    }
});

export {accessToken, username};

/*
// 변수 선언
var thumbnail = document.getElementById('thumbnailUrl');
var thumbnailFileName = document.getElementById('thumbnailFileName');
var form = document.getElementById('review-add-form');
var isValid = true;

// 등록 버튼 클릭
btnAdd.addEventListener('click', (e) => {
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
        form.action = '/review/add';
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
*/