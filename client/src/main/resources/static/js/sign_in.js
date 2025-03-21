// 변수 선언
var email = document.getElementById('email');
var password = document.getElementById('password');
var btnSignIn = document.getElementById('btn-sign-in');
var errors = document.querySelectorAll('.error');
var errorEmail = document.getElementById('error-email');
var errorPassword = document.getElementById('error-password');

// 로그인
btnSignIn.addEventListener('click', () => {
    // 에러 메시지 모두 숨김
    errors.forEach((error) => {
        error.style.display = 'none';
    });

    // 유효성 검사 → 이메일 입력 여부
    if (email.value.trim().length === 0) {
        errorEmail.innerText = '이메일을 입력하세요';
        errorEmail.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 비밀번호
    if (password.value.trim().length === 0) {
        errorPassword.innerText = '비밀번호를 입력하세요';
        errorPassword.style.display = 'block';
        return false;
    }

    // 로그인 정보
    const formData = {
        'email': email.value.trim(),
        'password': password.value.trim()
    }

    // 로그인 처리
    login(formData).then(response => {
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        location.replace('http://localhost:8080/');
    })
    .catch(error => {
        // 인증 실패
        if (error.response.data.status === 401) {
            errorPassword.innerText = error.response.data.message;
            errorPassword.style.display = 'block';
            return;
        }
    });
});

// 로그인 API
async function login(formData) {
    const response = await axios.post('http://localhost:8081/api/v1/login', formData, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response;
}

/*
// 변수 선언
var errorGlobal = document.getElementById('error-global');
var btnFindPassword = document.getElementById('find-password');
var isValid = true;

// 로그인 버튼 클릭
btnSignIn.addEventListener('click', () => {
  var formData = {
    'email': email.value.trim(),
    'password': password.value.trim(),
    'remember': remember.checked
  };

  // 로그인 처리
  login(formData).then((response) => {
    var results = response.data;
    // 유효성 검사
    isValid = validateLogin(results);
    // 로그인 성공시
    if (isValid) {
      // 메인 페이지로 이동
      location.replace('/');
    }
  });
});

// 비밀번호 찾기 버튼 클릭
btnFindPassword.addEventListener('click', () => {
  // 이메일 입력 여부 확인
  if (email.value.trim().length === 0) {
    alert('이메일을 입력해주세요');
    return;
  }
  // 이메일 형식 여부 확인
  regex = /^[0-9a-zA-Z]([-_\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/i;
  isRegex = regex.test(email.value.trim());
  if (!isRegex) {
    alert('이메일 형식이 아닙니다');
    return;
  }

  // 임시 비밀번호 발송
  if (confirm('입력하신 이메일 주소로 임시 비밀번호를 전송하시겠습니까?')) {
    sendTempPassword(email).then(response => {
      var isExistsEmail = response.data;
      if (!isExistsEmail) {
        alert('가입되지 않은 이메일 입니다');
        return;
      }
      alert('이메일로 임시 비밀번호가 전송되었습니다');
    });
  }
});

// 임시 비밀번호 발송
async function sendTempPassword(email) {
  var response = await axios.get(`/mail/temp-password/${email.value.trim()}`);
  return response;
}

// 로그인 처리
async function login(formData) {
  var response = axios.post('/login', formData);
  return response;
}

// 유효성 검사
function validateLogin(results) {
  isValid = true;

  errors.forEach((error) => {
    error.style.display = 'none';
  });

  if (Array.isArray(results)) {
    for (var i = 0; i < results.length; i++) {
      if (results[i].field === 'email') {
        errorEmail.innerText = results[i].defaultMessage;
        errors[0].style.display = 'block';
      }
      if (results[i].field === 'password') {
        errorPassword.innerText = results[i].defaultMessage;
        errors[1].style.display = 'block';
      }
    }
    return false;
  }

  if (results.res === -1) {
    errorGlobal.innerText = results.defaultMessage;
    errorGlobal.style.display = 'block';
    return false;
  }

  return true;
}
*/