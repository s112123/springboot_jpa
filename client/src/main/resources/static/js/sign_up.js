// 변수 선언
var email = document.getElementById('email');
var password = document.getElementById('password');
var rePassword = document.getElementById('re-password');
var errors = document.querySelectorAll('.error');
var errorEmail = document.getElementById('error-email');
var errorPassword = document.getElementById('error-password');
var errorRePassword = document.getElementById('error-re-password');
var msgSendMail = document.getElementById('send-message');
var btnSendMail = document.getElementById('btn-send-mail');
var btnSignUp = document.getElementById('btn-sign-up');
var isValid = true;
var isDuplicatedEmail = false;
var isValidEmail = false;

// 이메일 중복 검사
email.addEventListener("keyup", () => {
  var formData = {
    'email': email.value.trim()
  };

  isExistsEmail(formData).then(response => {
    var res = response.data;
    if (res.isExists) {
      errorEmail.textContent = '중복된 이메일입니다';
      errorEmail.style.display = 'block';
      isDuplicatedEmail = true;
    } else {
      errorEmail.style.display = 'none';
      isDuplicatedEmail = false;
    }
  });
});

// 이메일 중복 검사
async function isExistsEmail(formData) {
  var response = await axios.post('/members/exists_email', formData);
  return response;
}

// 인증 메일 발송 (유효성 검사: 이메일 입력 여부, 이메일 형식 확인)
btnSendMail.addEventListener('click', () => {
  var formData = {
    'email': email.value.trim()
  };

  // 인증 메일 발송
  console.log(isDuplicatedEmail);
  if (!isDuplicatedEmail) {
    sendValidEmail(formData).then(response => {
      var res = response.data;

      // 이메일이 입력되지 않은 경우
      errorEmail.textContent = res.defaultMessage;
      errorEmail.style.display = 'block';

      // 이메일 인증 메일이 발송된 경우
      if (res.send) {
        msgSendMail.innerText = res.defaultMessage;
        errorEmail.style.display = 'none';
        msgSendMail.style.display = 'block';
      }
    });
  }
});

// 이메일 인증코드 발송
async function sendValidEmail(formData) {
  var response = await axios.post('/members/send_email', formData);
  return response;
}

// 회원가입 버튼 (유효성 검사: 이메일 입력 여부, 인증 여부, 비밀번호 입력 여부, 비밀번호 일치 여부)
btnSignUp.addEventListener('click', () => {
  console.log(isDuplicatedEmail);

  var formData = {
    'email': email.value.trim(),
    'password': password.value.trim()
  };

  addMember(formData).then(response => {
    var res = response.data;

    // 유효성 검사
    isValid = validateSignUp(res);

    // 회원가입 성공시, 로그인 페이지로 이동
    if (isValid) {
      location.replace('/login?register=true');
    }
  });
});

// 회원 등록
async function addMember(formData) {
  var response = axios.post('/members', formData);
  return response;
}

// 회원가입 유효성 검사
function validateSignUp(response) {
  errors.forEach((error) => {
    error.style.display = 'none';
  });

  // 이메일 중복 여부
  if (isDuplicatedEmail) {
    errorEmail.textContent = '중복된 이메일입니다';
    errorEmail.style.display = 'block';
    return false;
  }

  // 이메일 입력 여부 및 이메일 형식 여부, 비밀번호 입력 여부
  if (Array.isArray(response)) {
    for (var i = 0; i < response.length; i++) {
      if (response[i].field === 'email') {
        errorEmail.innerText = response[i].defaultMessage;
        errorEmail.style.display = 'block';
      }
      if (response[i].field === 'password') {
        errorPassword.innerText = response[i].defaultMessage;
        errorPassword.style.display = 'block';
      }
    }
    return false;
  }

  // 비밀번호 확인 여부
  if (rePassword.value.trim().length === 0) {
    errorRePassword.innerText = "비밀번호를 확인해주세요";
    errorRePassword.style.display = 'block';
    return false;
  }

  // 비밀번호 확인 일치 여부
  if (password.value.trim() !== rePassword.value.trim()) {
    errorRePassword.innerText = "비밀번호가 일치하지 않습니다";
    errorRePassword.style.display = 'block';
    return false;
  }

  // 이메일 인증 여부
  if (response.defaultMessage !== 'success') {
    errorEmail.innerText = response.defaultMessage;
    errorEmail.style.display = 'block';
    return false;
  }

  return true;
}
