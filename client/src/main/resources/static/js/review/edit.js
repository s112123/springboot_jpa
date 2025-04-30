import { accessTokenUtils } from '/js/common.js';
import { reviewEditor, tempImageFileNames } from './ckeditor.js';

// 변수 선언
let btnEdit = document.getElementById('edit-review');
let btnDelete = document.getElementById('delete-review');
let btnCancel = document.getElementById('cancel-review');
let storeName = document.getElementById('storeName');
let storeAddress = document.getElementById('storeAddress');
let reviewTitle = document.getElementById('title');
let star = document.getElementById('star');
let errors = document.querySelectorAll('.error');

// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const reviewId = params.get('review_id');

// Access Token 이 없으면 로그인 페이지로 이동
accessTokenUtils.redirectLoginPage();

// CK Editor 에서 이미지를 등록할 때, multiple 기능 해제
const fileInput = document.querySelector('input[type="file"]');
fileInput.removeAttribute('multiple');

// 리뷰를 조회하여 화면에 렌더링
getReview(reviewId).then((response) => {
    // 조회된 리뷰
    let review = response.data;
    // 장소 이름
    storeName.value = review.storeName;
    // 장소 위치
    storeAddress.value = review.storeAddress;
    // 제목
    reviewTitle.value = review.title;

    // 글 내용
    // DB 에 저장된 <img> 태그의 src 값을 변경한다
    // 수정 전 → <img src="http://localhost:8081/api/v1/reviews/content-images/temp/{memberId}/{fileName}">
    // 수정 후 → <img src="http://localhost:8081/api/v1/reviews/content-images/{memberId}/{fileName}?reviewId=1">
    review.content = review.content.replace(/<img[^>]+src="([^"]+\/temp[^"]+)"/g, (match, p1) => {
        const updatedSrc = p1.replace('/temp', '') + '?reviewId=' + review.reviewId;
        return match.replace(p1, updatedSrc);
    });

    let ckEditor = document.getElementsByClassName('ck-content')[0];
    reviewEditor.setData(review.content);

    // tempImageFileNames 에 기존에 저장된 이미지의 정보 저장
    // 저장 객체 → {originalFileName: 'lenna1.png', savedFileName: '8c513a83.png'}
    let reviewImages = review.reviewImagesDetailsList;
    reviewImages.forEach((reviewImage) => {
        let imageFileName = {
            'originalFileName': reviewImage.originalFileName,
            'savedFileName': reviewImage.savedFileName
        };
        tempImageFileNames.push(imageFileName);
    });

    // 평점 색상 처리
    const reviewerStarList = document.getElementById('store-star');
    const reviewerStarItems = [...reviewerStarList.children];
    const reviewerStarScore = review.star;
    star.value = reviewerStarScore;
    reviewerStarItems.forEach((reviewerStarItem, index) => {
        if (index < reviewerStarScore) {
            reviewerStarItem.style.color = 'rgb(249, 199, 53)';
        }
    });
});

// 리뷰 조회 API
async function getReview(reviewId) {
    const response = await axios.get('http://localhost:8081/api/v1/reviews/' + reviewId);
    return response;
}

// 리뷰 수정
btnEdit.addEventListener('click', () => {
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
    //let savedImageIndex = [...imgTags].map(imgTag => parseInt(imgTag.getAttribute('data-image-index')));
    let savedImageNames = [...imgTags].map(imgTag => imgTag.getAttribute('data-image-name'));
    // 저장되어야 하는 <img> 태그
    let savedImages = tempImageFileNames.filter(image => savedImageNames.includes(image.savedFileName));
    // 삭제되어야 하는 <img> 태그
    // 만약, 기존 이미지가 변경되었으면 reviews 폴더에서 삭제해야 한다
    // 리뷰 내용에 사용될 임시 이미지는 temps 폴더에서 reviews 폴더로 이동된다
    // 리뷰 내용에 사용되지 않을 이미지는 @Scheduled 를 통해 주기적으로 temps 폴더의 파일은 비운다
    let deletedImages = tempImageFileNames.filter(image => !savedImageNames.includes(image.savedFileName));

    // 글 내용
    // DB 에 저장된 <img> 태그의 src 값을 변경한다
    // 수정 전 → <img src="http://localhost:8081/api/v1/reviews/content-images/{memberId}/{fileName}?reviewId=1">
    // 수정 후 → <img src="http://localhost:8081/api/v1/reviews/content-images/temp/{memberId}/{fileName}">
    let content = reviewEditor.getData();
    content = content.replace(/<img[^>]+src="([^"]+\/content-images\/[^"]+)"/g, (match, p1) => {
        const updatedSrc = p1.replace('/content-images', '/content-images/temp')
                             .replace('/temp/temp', '/temp')
                             .split('?')[0];
        return match.replace(p1, updatedSrc);
    })

    // 리뷰 수정
    if (confirm('리뷰를 수정하시겠습니까?')) {
        // 리뷰 수정 정보
        const formData = {
            'reviewId': reviewId,
            'writer': accessTokenUtils.getUsername(),
            'title': title.value.trim(),
            'storeName': storeName.value.trim(),
            'storeAddress': storeAddress.value.trim(),
            'content': content,
            'star': star.value,
            'savedImageForms': savedImages,
            'deletedImageForms': deletedImages
        };

        // 리뷰 수정
        updateReview(formData).then((response) => {
            location.href = '/review/view?review_id=' + reviewId;
        });
    }
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

// 리뷰 수정 API
async function updateReview(formData) {
    const api = 'http://localhost:8081/api/v1/reviews/' + reviewId;
    const response = await axios.patch(api, formData, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 장소 이름란에서 TAB 키를 누르면 장소위치가 클릭되도록 처리
storeName.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') {
        e.preventDefault();
        storeAddress.click();
    }
});

// 리뷰 삭제
btnDelete.addEventListener('click', () => {
    if (confirm('리뷰를 삭제하시겠습니까?')) {
        deleteReview().then((response) => {
            alert('리뷰가 삭제되었습니다');
            location.replace('/');
        });
    }
});

// 리뷰 삭제 API
async function deleteReview() {
    const api = 'http://localhost:8081/api/v1/reviews/' + reviewId;
    const response = await axios.delete(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 취소 버튼 클릭
btnCancel.addEventListener('click', () => {
    history.back();
});