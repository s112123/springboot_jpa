import { accessTokenUtils } from '/js/common.js';
import { reviewEditor, tempImageFileNames } from './ckeditor.js';

// 변수 선언
let btnAdd = document.getElementById('add-review');
let btnCancel = document.getElementById('cancel-review');
let title = document.getElementById('title');
let storeName = document.getElementById('storeName');
let storeAddress = document.getElementById('storeAddress');
let star = document.getElementById('star');
let errors = document.querySelectorAll('.error');

// Access Token 이 없으면 로그인 페이지로 이동
accessTokenUtils.redirectLoginPage();

// 리뷰 등록
btnAdd.addEventListener('click', (e) => {
    // 에러 표시 모두 닫기
    errors.forEach(error => {
        error.style.display = 'none';
    });

    // 유효성 검사 → AccessToken 여부
    accessTokenUtils.redirectLoginPage();

    // 유효성 검사 → 위치이름 입력 여부
    if (storeName.value.trim().length === 0) {
        storeName.nextElementSibling.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 위치주소 입력 여부
    if (storeAddress.value.trim().length === 0) {
        storeAddress.nextElementSibling.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 제목 입력 여부
    if (title.value.trim().length === 0) {
        title.nextElementSibling.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 글 본문에 이미지가 최소 1개는 삽입되어 있는지 여부
    let ckEditor = document.getElementsByClassName('ck-content')[0];
    let imgTag = ckEditor.getElementsByTagName('img')[0];
    if (imgTag === undefined) {
        document.getElementById('editor-error').style.display = 'block';
        return false;
    }
    // 유효성 검사 → 글 내용 입력 여부
    let pTag = ckEditor.getElementsByTagName('p');
    if (pTag.length === 0) {
        document.getElementById('editor-error').style.display = 'block';
        return false;
    } else {
        let textLength = 0;
        for (let i = 0; i < pTag.length; i++) {
            textLength += pTag[i].textContent.trim().length;
        }
        if (textLength === 0) {
            document.getElementById('editor-error').style.display = 'block';
            return false;
        }
    }
    // 유효성 검사 → 리뷰어 평점 입력 여부
    if (star.value.trim().length === 0) {
        star.parentNode.nextElementSibling.style.display = 'block';
        return false;
    }

    // 글에 작성된 <img> 태그
    const parser = new DOMParser();
    const reviewEditorHTML = parser.parseFromString(reviewEditor.getData(), 'text/html');
    let imgTags = reviewEditorHTML.body.querySelectorAll('img');
    // 저장되어야 하는 이미지 파일의 인덱스 배열 → <img data-image-index='1') 로 지정이 되어 있다
    let savedImageIndex = [...imgTags].map(imgTag => parseInt(imgTag.getAttribute('data-image-index')));
    // 저장되어야 하는 <img> 태그
    let savedImages = tempImageFileNames.filter((_, index) => savedImageIndex.includes(index));
    // 삭제되어야 하는 <img> 태그
    // 아래 deletedImages 을 서버에 전달하거나 서버에서 temp > memberId 폴더를 삭제할 수 있다
    // 하지만 @Scheduled 로 삭제해본다
    // let deletedImages = tempImageFileNames.filter((_, index) => !savedImageIndex.includes(index));

    // 리뷰 입력 정보
    // reviewEditor → ckeditor.js 에 선언되어 있다
    // reviewEditor.getData() → CkEditor 의 내용 추출
    const formData = {
        'writer': accessTokenUtils.getUsername(),
        'title': title.value.trim(),
        'storeName': storeName.value.trim(),
        'storeAddress': storeAddress.value.trim(),
        'content': reviewEditor.getData(),
        'star': star.value,
        'reviewImageForms': savedImages
    };

    // 리뷰 등록
    addReview(formData).then(response => {
        location.replace('/');
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
async function addReview(formData) {
    // 리뷰 등록
    const response = await axios.post('http://localhost:8081/api/v1/reviews', formData, {
        headers: {
            "Authorization": accessTokenUtils.getAccessToken(),
            "Content-Type": "application/json"
        }
    });
    return response;
}

// 장소이름란에서 TAB 키를 누르면 장소위치가 클릭되도록 처리
storeName.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') {
        e.preventDefault();
        storeAddress.click();
    }
});

// 취소 버튼 클릭
btnCancel.addEventListener('click', () => {
    if (confirm('리뷰 등록을 취소하시겠습니까?')) {
        location.href = "/";
    }
});