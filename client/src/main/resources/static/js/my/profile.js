import { accessTokenUtils } from '/js/common.js';

// 변수 선언
let eleEmail = document.getElementById('email');
let eleUsername = document.getElementById('username');
let elePassword = document.getElementById('password');
let eleProfileImage = document.getElementById('profile-image');
let eleImageFile = document.getElementById('profile-file');
let eleImageFileName = document.getElementById('file-name');
let eleUpdatedMessage = document.getElementById('updated-message');
let btnChangePassword = document.getElementById('btn-change-pass');
let btnUpdate = document.getElementById('update-my-info');
let btnWithdrawMembership = document.getElementById('withdraw-membership');
let errors = document.querySelectorAll('.error');
let errorCapsLock = document.querySelector('.error-capslock');
let errorPassword = document.querySelector('.error-password');
let profileImageHistory = new Set();
let isDuplicatedUsername = false;

// HTML 로드
document.addEventListener('DOMContentLoaded', async () => {
    // Access Token 확인
    accessTokenUtils.redirectLoginPage();

    // 회원 정보 조회
    try {
        const response = await findMember(accessTokenUtils.getUsername());
        const body = response.data;

        // 사용자 정보
        eleEmail.value = body.email;
        eleUsername.value = body.username;

        // 프로필 이미지
        const profileImage = body.profileImage;
        const profileImageFileName = profileImage.savedFileName;
        if (profileImageFileName === 'default.png') {
            // src > main > resources > static > images > profiles > default.png
            eleProfileImage.src = '/images/profiles/default.png';
        } else {
            // uploads > profiles > memberId
            eleProfileImage.src = 'http://localhost:8081/api/v1/members/profile-images/' +
                                   accessTokenUtils.getMemberId() + '/' + profileImage.savedFileName;
        }

        // URL 에서 쿼리 스트링 추출
        const queryString = window.location.search;
        const params = new URLSearchParams(queryString);
        const isRegister = params.get('register');

        // 회원가입 후, 첫 로그인에 닉네임 변경 요구
        if (isRegister !== null && isRegister) {
            setTimeout(() => {
                alert('프로필 이미지, 닉네임 등 회원 정보를 변경하세요');
             }, 100);
        }
    } catch (error) {
        // 회원을 찾을 수 없으면 로그인 페이지로 이동하여 Access Token 을 재발급 받아야 한다
        if (error.response && error.response.data.status === 404) {
            location.href = '/login';
        };
    }
});

// 회원 정보 조회 API
async function findMember(username) {
    const response = await axios.get('http://localhost:8081/api/v1/members/' + username, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 프로필 이미지 변경
eleImageFile.addEventListener('change', (e) => {
    // 이미지 변경 시, 현재 이미지 파일 이름을 저장한다
    let imageUrl = eleProfileImage.src;
    let currentImageFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    profileImageHistory.add(currentImageFileName);

    // 선택된 이미지 파일 이름
    eleImageFileName.value = eleImageFile.value.replace(/^C:\\fakepath\\/i, '');

    // 선택된 이미지 파일 이름이 'default.png' 이면 가입시 기본 이미지 파일이름이 'default.png' 이므로 금지시킨다
    if (eleImageFileName.value === 'default.png') {
        alert('PNG 파일 이름으로 default 는 사용하실 수 없습니다');
        return;
    }

    // 선택된 이미지 파일
    let formData = new FormData();
    formData.append('memberId', accessTokenUtils.getMemberId());
    formData.append('profile-file', e.target.files[0]);

    // DB 에 등록하지 않고 서버에만 임시 파일 저장
    saveSelectedImageFile(formData).then(response => {
        // 임시 파일을 이미지로 보여준다
        eleProfileImage.src = 'http://localhost:8081/api/v1/members/profile-images/' +
                               accessTokenUtils.getMemberId() + '/' + response.data.savedFileName;
        imageUrl = eleProfileImage.src;

        // 다른 이미지 변경으로 인해 삭제될 수 있으므로 삭제 파일 목록에 넣는다
        currentImageFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        profileImageHistory.add(currentImageFileName);
    });
});

// 프로필 이미지 변경 API
async function saveSelectedImageFile(formData) {
    const response = await axios.post('http://localhost:8081/api/v1/members/profile-images', formData, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'multipart/form-data'
        }
    });
    return response;
};

// 회원 정보 수정
btnUpdate.addEventListener('click', () => {
    // 에러 메시지 모두 숨김
    errors.forEach(error => {
        error.style.display = 'none';
    });

    // 유효성 검사 → 닉네임 입력 여부
    if (eleUsername.value.trim().length < 1 || eleUsername.value.trim().length > 10) {
        eleUsername.nextElementSibling.style.display = 'block';
        eleUsername.focus();
        return false;
    }

    // 회원 정보 수정
    if (confirm("회원정보를 변경하시겠습니까?")) {
        // 프로필 이미지 변경에 사용된 모든 임시 파일 삭제
        // profileImageHistory (Set 객체) 에서 마지막 파일만 놔두고 모두 삭제
        let lastChangedImageFileName = undefined;
        if (profileImageHistory.size > 0) {
            let deletedImageFiles = [...profileImageHistory];
            lastChangedImageFileName = deletedImageFiles.pop();
            deleteImageFiles(deletedImageFiles, accessTokenUtils.getAccessToken()).then(response => {
                // Set 객체 비우기
                profileImageHistory.clear();
                // 배열 비우기
                deletedImageFiles.length = 0;
            });
        }

        // Access Token 에서 username 가져오기
        const originalUsername = accessTokenUtils.getUsername();

        // 변경 정보
        const changedUserInfo = {
            'username': eleUsername.value.trim(),
            'password': (elePassword.value.trim().length === 0) ? undefined : elePassword.value.trim(),
            'originalFileName': eleImageFileName.value.length === 0 ? undefined: eleImageFileName.value,
            'savedFileName': lastChangedImageFileName
        };

        // 회원 정보 수정 처리
        updateMember(originalUsername, changedUserInfo).then(response => {
            // 새롭게 발급받은 Access Token 을 갱신
            const newAccessToken = response.data.accessToken;
            localStorage.setItem('todayReviewsAccessToken', newAccessToken);

            // 회원 정보 변경 완료 메시지
            eleUpdatedMessage.style.display = 'block';
            setTimeout(() => {
                eleUpdatedMessage.style.display = 'none';
            }, 2000);

            // 비밀번호 입력 칸을 공백 처리
            elePassword.value = '';
        });
    }
});

// 닉네임 중복 여부
eleUsername.addEventListener('keyup', () => {
    // 에러 메시지 모두 숨김
    errors.forEach((error) => {
        error.style.display = 'none';
    });

    // Access Token 에서 username 가져오기
    const username = accessTokenUtils.getUsername();

    // 중복 체크
    if (eleUsername.value.trim() !== username) {
        isDuplicatedUsername = false;
        if (eleUsername.value.trim().length > 0) {
            isExistsUsername(eleUsername.value.trim()).then(response => {
                isDuplicatedUsername = response.data;
                if (isDuplicatedUsername) {
                    eleUsername.nextElementSibling.nextElementSibling.style.display = 'block';
                    eleUsername.focus();
                } else {
                    eleUsername.nextElementSibling.nextElementSibling.style.display = 'none';
                }
            });
        }
    }
});

// 닉네임 중복 검증 API
async function isExistsUsername(username) {
    let response = await axios.post('http://localhost:8081/api/v1/members/username/check', username, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'text/plain'
        }
    });
    return response;
}

// 비밀번호 입력 활성화
btnChangePassword.addEventListener('click', () => {
    // 비밀번호 관련 에러 메시지 모두 숨김
    errorPassword.style.display = 'none';
    errorCapsLock.style.display = 'none';

    // 비밀번호 입력 창 활성화
    elePassword.style.border = '1px solid rgb(210, 40, 40)';
    elePassword.readOnly = false;
    elePassword.focus();
});

// 비밀번호를 입력하고 다른 곳을 클릭하면 비활성화
elePassword.addEventListener('blur', () => {
    // 유효성 검사 → 비밀번호 형식 일치 여부
    if (elePassword.value.trim().length > 0) {
        let passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d).{10,}$/;
        if (!passwordRegex.test(elePassword.value.trim())) {
            errorPassword.style.display = 'block';
            return false;
        }
    }

    elePassword.style.border = '1px solid rgb(180, 180, 180)';
    elePassword.readOnly = true;
    errorPassword.style.display = 'none';
    errorCapsLock.style.display = 'none';
});

// 비밀번호를 입력하고 Enter 를 누르면 비활성화
elePassword.addEventListener('keydown', (e) => {
    // 비밀번호 입력 칸에서 CapsLock 키를 눌렀는지 여부
    if (e.getModifierState('CapsLock')) {
        // CapsLock 키가 눌렸을 때
        errorCapsLock.style.display = 'block';
    } else {
        // CapsLock 키가 눌려지지 않았을 때
        errorCapsLock.style.display = 'none';
    }

    // Enter 키를 눌렀을 때
    if (e.key === 'Enter') {
        // 유효성 검사 → 비밀번호 형식 일치 여부
        if (elePassword.value.trim().length > 0) {
            let passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d).{10,}$/;
            if (!passwordRegex.test(elePassword.value.trim())) {
                errorPassword.style.display = 'block';
                return false;
            }
        }

        elePassword.style.border = '1px solid rgb(180, 180, 180)';
        elePassword.readOnly = true;
        errorPassword.style.display = 'none';
        errorCapsLock.style.display = 'none';
    }
});

// 프로필 이미지 삭제 API
async function deleteImageFiles(imageFileNames) {
    let api = 'http://localhost:8081/api/v1/members/profile-images/' + accessTokenUtils.getMemberId();
    const response = await axios.delete(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        },
        data: imageFileNames
    });
    return response;
};

// 회원 정보 수정 API
async function updateMember(username, userInfo) {
    const response = await axios.patch('http://localhost:8081/api/v1/members/' + username, userInfo, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 회원 탈퇴
btnWithdrawMembership.addEventListener('click', () => {
    if (confirm('회원탈퇴 하시겠습니까? 모든 내역이 삭제됩니다')) {
        removeMemberShip(eleEmail.value).then(response => {
            // JWT 삭제
            accessTokenUtils.removeAccessToken();
            // 탈퇴 완료
            alert('회원 탈퇴가 완료되었습니다');
            location.replace('http://localhost:8080/');
        });
    }
});

// 회원 탈퇴 API
async function removeMemberShip(email) {
    let response = await axios.delete('http://localhost:8081/api/v1/members/' + email, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 페이지를 벗어날 때
window.addEventListener('beforeunload', (e) => {
    // 이미지 파일을 변경 중이었다면 원래 프로필 이미지는 놔두고 변경으로 인해 서버에 저장되었던 파일 삭제
    if (profileImageHistory.size > 0) {
        // 첫 번째 저장된 이미지 파일 제외한 Set 객체
        let isFirst = true;
        profileImageHistory.forEach((fileName) => {
            if (fileName !== 'default.png') {
                profileImageHistory.add(fileName);
            } else {
                if (isFirst) {
                    profileImageHistory.delete(fileName);
                    isFirst = false;
                } else {
                    profileImageHistory.add(fileName);
                }
            }
        });
        // 변경에 의해 임시로 저장된 모든 이미지 모두 삭제
        // Set 객체는 배열로 변환해야 한다 → Array.from(profileImageHistory) 또는 [...profileImageHistory]
        deleteImageFiles([...profileImageHistory]).then(response => {
            // Set 객체 비우기
            profileImageHistory.clear();
        });
    }
});


/////////////////////////////////////////
//// 변수 선언
//let btnSubscribeCancels = document.querySelectorAll('.btn-subscribe-cancel');
//let btnSubscribes = document.querySelectorAll('.btn-subscribe');
//let imageUrl = document.getElementById('image-url');
//let isChangePassword = false;
//let isValid = true;

//// 내가 구독한 사람에서 구독취소 버튼 클릭
//btnSubscribeCancels.forEach((btnSubscribeCancel) => {
//  btnSubscribeCancel.addEventListener('click', () => {
//    let publisherEmail = btnSubscribeCancel.previousElementSibling;
//
//    // 구독자 - 발행자
//    let subscribe = {
//      'subscriberEmail': email.value,
//      'publisherEmail': publisherEmail.value
//    }
//
//    // 구독취소
//    if (confirm('구독을 취소하시겠습니까?')) {
//      cancelFollow(subscribe).then(response => {
//        location.replace('/my/profile');
//      });
//    }
//  });
//});
//
//// 구독취소
//async function cancelFollow(subscribe) {
//  let response = await axios.post(`/subscribes/cancel`, subscribe);
//  return response;
//}
//
//// 나를 구독한 사람에서 구독하기 버튼 클릭
//btnSubscribes.forEach((btnSubscribe) => {
//  btnSubscribe.addEventListener('click', () => {
//    let publisherEmail = btnSubscribe.previousElementSibling;
//
//    // 구독자 - 발행자
//    let subscribe = {
//      'subscriberEmail': email.value,
//      'publisherEmail': publisherEmail.value
//    }
//
//    // 구독하기
//    follow(subscribe).then(response => {
//      location.replace('/my/profile');
//    });
//  });
//});
//
//// 구독하기
//async function follow(subscribe) {
//  let response = await axios.post(`/subscribes`, subscribe);
//  return response;
//}

