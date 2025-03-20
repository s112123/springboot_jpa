// 변수 선언
var email = document.getElementById('email');
var nickName = document.getElementById('nickName');
var password = document.getElementById('password');
var errors = document.querySelectorAll('.error');
var btnUpdate = document.getElementById('update-my-info');
var btnChangePassword = document.getElementById('btn-change-pass');
var btnWithdrawMembership = document.getElementById('withdraw-membership');
var updatedMessage = document.getElementById('updated-message');
var profileImage = document.getElementById('profile-image');
var file = document.getElementById('profile-file');
var fileName = document.getElementById('fileName');
var imageUrl = document.getElementById('imageUrl');
var btnSubscribeCancels = document.querySelectorAll('.btn-subscribe-cancel');
var btnSubscribes = document.querySelectorAll('.btn-subscribe');
var isChangePassword = false;
var isValid = true;

// 입력 폼
var form = {
  'email': email,
  'nickName': nickName,
  'password': password,
  'imageUrl': imageUrl,
  'fileName': fileName
};

// 회원 정보 수정
btnUpdate.addEventListener('click', () => {
  // 유효성 검사
  errors.forEach(error => {
    error.style.display = 'none';
  });

  if (!validateForm(form)) {
    return;
  }

  // 닉네임 중복 여부
  checkDuplicateNickName(form).then(response => {
    var isDuplicated = response.data;
    if (isDuplicated) {
      form.nickName.nextElementSibling.nextElementSibling.style.display = 'block';
      return;
    }

    // 회원정보 수정하기
    if (confirm("회원정보를 변경하시겠습니까?")) {
      // 파일 담기
      var formData = new FormData();
      formData.append('profile-file', file.files[0]);

      // 이미지 파일 저장
      saveProfileImage(formData).then(response => {
        // 첨부 파일 정보 변경
        updateImageInfo(response);
      })
      .finally(() => {
        // 수정된 회원정보 전달
        editMember().then(response => {
          result = response.data;
          if (result === 'updated') {
            updatedMessage.style.display = 'block';
            setTimeout(() => {
              updatedMessage.style.display = 'none';
            }, 3000);
            password.value = '';
          }
        });
      });
    }
  });
});

// 프로필 이미지 파일 변경
file.addEventListener('change', (event) => {
  //fileName.value = file.value.replace(/^C:\\fakepath\\/i, '');

  // 파일 담기
  var formData = new FormData();
  formData.append('profile-file', event.target.files[0]);

  // 임시 파일 저장
  saveTempProfileImage(formData).then((response) => {
    // 첨부 파일 정보 변경
    updateImageInfo(response);
  });
});

// 비밀번호 입력 버튼 클릭
btnChangePassword.addEventListener('click', () => {
  // 비밀번호 입력 창 활성화
  password.style.border = '1px solid rgb(210, 40, 40)';
  password.readOnly = false;
  password.focus();

  // 비밀번호 입력 창 비활성화
  password.addEventListener('blur', () => {
    password.style.border = '1px solid rgb(180, 180, 180)';
    password.readOnly = true;
  });
});

// 회원탈퇴 버튼 클릭
btnWithdrawMembership.addEventListener('click', () => {
  if (confirm('회원탈퇴 하시겠습니까? 모든 내역이 삭제됩니다')) {
    removeMemberShip().then(response => {
      location.replace('/');
    });
  }
});

// 회원탈퇴
async function removeMemberShip() {
  var response = await axios.get(`/my/profile/remove`);
  return response;
}

// 유효성 검사
function validateForm(form) {
  isValid = true;
  if (form.nickName.value.trim().length === 0) {
    // 닉네임 입력 여부
    form.nickName.nextElementSibling.style.display = 'block';
    isValid = false;
  }
  return isValid;
}

// 닉네임 중복 여부
async function checkDuplicateNickName(form) {
  var response = await axios.get(`/members/check_nickname/${form.nickName.value}`);
  return response;
}

// 수정하기
async function editMember() {
  var editForm = {
    'nickName': nickName.value.trim(),
    'password': password.value.trim(),
    'imageUrl': imageUrl.value.trim(),
    'fileName': fileName.value.trim(),
  };
  var response = await axios.patch(`/members/${email.value}`, editForm);
  return response;
}

// 이미지 파일 저장
async function saveProfileImage(formData) {
  var headers = {
    'Content-Type': 'multipart/form-data'
  };
  var response = await axios.post('/members/image/save', formData, {headers: headers});
  return response;
}

// 이미지 임시 파일 저장
async function saveTempProfileImage(formData) {
  var headers = {
    'Content-Type': 'multipart/form-data'
  };
  var response = await axios.post('/members/temp_image/save', formData, {headers: headers});
  return response;
}

// 첨부 파일 정보 변경
function updateImageInfo(response) {
  // 응답받은 이미지 경로
  var $imageUrl = response.data;
  // 프로필 이미지에 변경된 이미지 표시
  profileImage.src = $imageUrl;
  // 이미지 경로
  imageUrl.value = $imageUrl;
  // 이미지 파일 이름
  fileName.value = $imageUrl;
  fileName.value = fileName.value.substring(fileName.value.lastIndexOf('/') + 1);
}

// 내가 구독한 사람에서 구독취소 버튼 클릭
btnSubscribeCancels.forEach((btnSubscribeCancel) => {
  btnSubscribeCancel.addEventListener('click', () => {
    var publisherEmail = btnSubscribeCancel.previousElementSibling;

    // 구독자 - 발행자
    var subscribe = {
      'subscriberEmail': email.value,
      'publisherEmail': publisherEmail.value
    }

    // 구독취소
    if (confirm('구독을 취소하시겠습니까?')) {
      cancelFollow(subscribe).then(response => {
        location.replace('/my/profile');
      });
    }
  });
});

// 구독취소
async function cancelFollow(subscribe) {
  var response = await axios.post(`/subscribes/cancel`, subscribe);
  return response;
}

// 나를 구독한 사람에서 구독하기 버튼 클릭
btnSubscribes.forEach((btnSubscribe) => {
  btnSubscribe.addEventListener('click', () => {
    var publisherEmail = btnSubscribe.previousElementSibling;

    // 구독자 - 발행자
    var subscribe = {
      'subscriberEmail': email.value,
      'publisherEmail': publisherEmail.value
    }

    // 구독하기
    follow(subscribe).then(response => {
      location.replace('/my/profile');
    });
  });
});

// 구독하기
async function follow(subscribe) {
  var response = await axios.post(`/subscribes`, subscribe);
  return response;
}

