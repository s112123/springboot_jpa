// 변수 선언
var email = document.getElementById('email');
var password = document.getElementById('password');
var btnSignIn = document.getElementById('btn-sign-in');
var btnFindPassword = document.getElementById('find-password');
var errors = document.querySelectorAll('.error');
var errorEmail = document.getElementById('error-email');
var errorPassword = document.getElementById('error-password');
var errorCapsLock = document.getElementById('error-capslock');

// 이메일을 입력하고 Enter 를 누르면 비밀번호 입력 칸으로 이동
email.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        if (password.value().length.trim > 0) {
            btnSignIn.click();
        } else {
            password.focus();
        }
    }
});

// 비밀번호를 입력하고 Enter 를 누르면 로그인 버튼 클릭
password.addEventListener('keydown', (e) => {
    // 비밀번호 입력 칸에서 CapsLock 키를 눌렀는지 여부
    if (e.getModifierState('CapsLock')) {
        // CapsLock 키가 눌렸을 때
        errorCapsLock.innerText = 'CapsLock 키가 활성화되어 있습니다';
        errorCapsLock.style.display = 'block';
    } else {
        // CapsLock 키가 눌려지지 않았을 때
        errorCapsLock.style.display = 'none';
    }

    // 비밀번호 입력 칸에서 CapsLock 키를 눌렀는지 여부
    if (e.key === 'Enter') {
        btnSignIn.click();
    }
});

// 로그인
btnSignIn.addEventListener('click', () => {
    // 에러 메시지 모두 숨김
    errors.forEach((error) => {
        if (error.id !== 'error-capslock') {
            error.style.display = 'none';
        }
    });

    // 유효성 검사 → 이메일 입력 여부
    if (email.value.trim().length === 0) {
        errorEmail.innerText = '이메일을 입력하세요';
        errorEmail.style.display = 'block';
        email.focus();
        return false;
    }
    // 유효성 검사 → 비밀번호
    if (password.value.trim().length === 0) {
        errorPassword.innerText = '비밀번호를 입력하세요';
        errorPassword.style.display = 'block';
        password.focus();
        return false;
    }

    // 로그인 정보
    const formData = {
        'email': email.value.trim(),
        'password': password.value.trim()
    }

    // 로그인 처리
    loginProcess(formData).then(response => {
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        location.replace('http://localhost:8080/');
    })
    .catch(error => {
        // 서버 연결이 되지 않은 경우 → undefined, null
        if (!error.response) {
            alert('not connected server');
            return;
        }
        // 인증 실패
        if (error.response.data.status === 401) {
            errorPassword.innerText = error.response.data.message;
            errorPassword.style.display = 'block';
            return;
        }
    });
});

// 로그인 API
async function loginProcess(formData) {
    const response = await axios.post('http://localhost:8081/api/v1/login', formData, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 비밀번호 찾기
btnFindPassword.addEventListener('click', () => {
    // 유효성 검사 → 이메일 입력 여부
    if (email.value.trim().length === 0) {
        alert('임시 비밀번호를 전송할 이메일 주소를 입력해주세요');
        email.style.borderColor = 'rgb(210, 40, 40)';
        email.focus();
        return false;
    }
    // 유효성 검사 → 이메일 형식 여부
    var emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email.value.trim())) {
        alert('이메일 주소 형식이 아닙니다');
        email.focus();
        return false;
    }

    // 임시 비밀번호를 메일로 발송
    if (confirm('입력하신 이메일 주소로 임시 비밀번호를 전송하시겠습니까?')) {
        sendTempPassword(email.value.trim()).then(response => {
            alert('임시 비밀번호가 전송되었습니다');
            email.style.borderColor = 'rgb(180, 180, 180)';
            password.style.borderColor = 'rgb(210, 40, 40)';
            password.focus();
        })
        .catch (error => {
            if (error.response.data.status === 404) {
                alert('가입되지 않은 이메일 주소입니다');
                return;
            }
        });
    }
});

// 비밀번호 찾기 API
async function sendTempPassword(email) {
    const response = await axios.post('http://localhost:8081/api/v1/members/send-password', email, {
        headers: {
            'Content-Type': 'text/plain'
        }
    });
}

/*
// 변수 선언
var errorGlobal = document.getElementById('error-global');
var isValid = true;

// 로그인 처리
async function login(formData) {
  var response = axios.post('/login', formData);
  return response;
}

*/