import { accessTokenUtils } from '/js/common.js';

// 변수 선언
let email = document.getElementById('email');
let password = document.getElementById('password');
let registerMessage = document.getElementById('register-message');
let btnSignIn = document.getElementById('btn-sign-in');
let btnFindPassword = document.getElementById('find-password');
let errors = document.querySelectorAll('.error');
let errorEmail = document.getElementById('error-email');
let errorPassword = document.getElementById('error-password');
let errorCapsLock = document.getElementById('error-capslock');

// URL 에서 쿼리 스트링 추출
const queryString = window.location.search;
const params = new URLSearchParams(queryString);
const isRegister = params.get('register');
const redirectUrl = params.get('redirect');

// 회원가입 시, 메시지 출력
if (isRegister !== null && isRegister) {
    registerMessage.style.display = 'block';
} else {
    registerMessage.style.display = 'none';
}

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
    // 유효성 검사 → 이메일 형식 여부
    let emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (email.value.trim().includes(' ') || !emailRegex.test(email.value.trim())) {
        errorEmail.innerText = '이메일 형식이 아닙니다';
        errorEmail.style.display = 'block';
        email.focus();
        return false;
    }
    // 유효성 검사 → 비밀번호 입력 여부
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
        // localStorage.setItem('todayReviewsAccessToken', response.data.accessToken);
        accessTokenUtils.saveAccessToken(response.data.accessToken);
        if (isRegister) {
            location.replace('/my/profile?register=true');
            isRegister = false;
        }
        if (redirectUrl) {
            location.replace(redirectUrl);
            return;
        }
        location.replace('/');
    })
    .catch(error => {
        if (error.response && error.response.data.status === 401) {
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
        },
        withCredentials: true
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
    let emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (email.value.trim().includes(' ') || !emailRegex.test(email.value.trim())) {
        alert('이메일 주소 형식이 아닙니다');
        email.focus();
        return false;
    }

    // 임시 비밀번호를 메일로 발송
    if (confirm('입력하신 이메일 주소로 임시 비밀번호를 전송하시겠습니까?')) {
        sendTempPassword(email.value.trim()).then(response => {
            alert('임시 비밀번호가 전송되었습니다');
            // 실제 메일로 보낼 때는 아래 코드는 제거해야 한다
            alert('임시 비밀번호: ' + response.data);
            email.style.borderColor = 'rgb(180, 180, 180)';
            password.style.borderColor = 'rgb(210, 40, 40)';
            password.focus();
        })
        .catch (error => {
            if (error.response && error.response.data.status === 404) {
                alert('가입되지 않은 이메일 주소입니다');
                return;
            }
        });
    }
});

// 비밀번호 찾기 API
async function sendTempPassword(email) {
    const response = await axios.post('http://localhost:8081/api/v1/members/send-password', {'email': email}, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response;
}