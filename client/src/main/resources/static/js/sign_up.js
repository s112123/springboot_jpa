// 변수 선언
var email = document.getElementById('email');
var confirmCode = document.getElementById('confirm-code');
var password = document.getElementById('password');
var rePassword = document.getElementById('re-password');
var msgSendMail = document.getElementById('send-message');
var confirmCodeBox = document.getElementById('confirm-code-box');
var btnSignUp = document.getElementById('btn-sign-up');
var btnSendMail = document.getElementById('btn-send-mail');
var btnConfirmCode = document.getElementById('btn-confirm-code');
var errors = document.querySelectorAll('.error');
var errorEmail = document.getElementById('error-email');
var errorPassword = document.getElementById('error-password');
var errorRePassword = document.getElementById('error-re-password');
var isSentConfirmCode = false;
var isValidConfirmCode = false;
var isDuplicatedEmail = false;

// 이메일 입력 시
email.addEventListener("keyup", () => {
    // 이메일을 입력하고 인증 메일을 보낸 후, 다시 이메일을 입력할 수 있다
    isDuplicatedEmail = false;
    isSentConfirmCode = false;
    confirmCodeBox.style.display = 'none';
    msgSendMail.style.display = 'none';

    // 에러 메시지 모두 숨김
    errors.forEach((error) => {
        error.style.display = 'none';
    });

    // 이메일 중복 검사
    if (email.value.trim().length !== 0) {
        isExistsEmail(email.value.trim()).then(response => {
            isDuplicatedEmail = response.data;
            if (isDuplicatedEmail) {
                errorEmail.textContent = '이미 존재하는 이메일입니다';
                errorEmail.style.display = 'block';
                isDuplicatedEmail = true;
            } else {
                errorEmail.style.display = 'none';
                isDuplicatedEmail = false;
            }
        });
    }
});

// 이메일 중복 검증 API
async function isExistsEmail(email) {
    var response = await axios.post('http://localhost:8081/api/v1/members/emails/check', email, {
        headers: {
            'Content-Type': 'text/plain'
        }
    });
    return response;
}

// 인증 메일 발송
btnSendMail.addEventListener('click', () => {
    // 에러 메시지 숨김
    errorEmail.style.display = 'none';

    // 유효성 검사 → 이메일
    if (email.value.trim().length === 0) {
        errorEmail.innerText = '이메일은 필수입니다';
        errorEmail.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 이메일 형식 여부
    var emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email.value.trim())) {
        errorEmail.innerText = '이메일 형식이 아닙니다';
        errorEmail.style.display = 'block';
        return false;
    }

    // 인증 메일 발송
    sendConfirmCodeEmail(email.value.trim()).then(response => {
        isSentConfirmCode = true;
        confirmCodeBox.style.display = 'flex';
        msgSendMail.innerText = '인증 메일이 발송되었습니다';
        errorEmail.style.display = 'none';
        msgSendMail.style.display = 'block';
    });
});

// 인증 메일 발송 API
async function sendConfirmCodeEmail(email) {
    var response = await axios.post('http://localhost:8081/api/v1/members/codes', email, {
        headers: {
            'Content-Type': 'text/plain'
        }
    });
    return response;
}

// 인증 확인
btnConfirmCode.addEventListener('click', () => {
    // 인증 메일이 발송되었다는 메시지 숨김
    msgSendMail.style.display = 'none';

    // 유효성 검사 → 입력 여부
    if (confirmCode.value.trim().length === 0) {
        errorEmail.innerText = '인증 코드를 입력하세요 (유효시간: 3분)';
        errorEmail.style.display = 'block';
        return false;
    }

    // 입력한 회원 정보
    var formData = {
        'email': email.value.trim(),
        'code': confirmCode.value.trim()
    };

    // 인증 확인
    validateConfirmCode(formData).then(response => {
        isValidConfirmCode = true;
        msgSendMail.innerText = '인증이 완료되었습니다';
        errorEmail.style.display = 'none';
        msgSendMail.style.display = 'block';
        confirmCodeBox.style.display = 'none';
        confirmCode.value = '';
    })
    .catch (error => {
        errorEmail.innerText = error.response.data.message;
        errorEmail.style.display = 'block';
    });
});

// 인증 확인 API
async function validateConfirmCode(formData) {
    var response = await axios.post('http://localhost:8081/api/v1/members/codes/check', formData, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 회원 가입
btnSignUp.addEventListener('click', () => {
    // 에러 메시지 모두 숨김
    errors.forEach((error) => {
        error.style.display = 'none';
    });

    // 유효성 검사 → 이메일
    if (email.value.trim().length === 0) {
        errorEmail.innerText = '이메일은 필수입니다';
        errorEmail.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 이메일 형식 여부
    var emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email.value.trim())) {
        errorEmail.innerText = '이메일 형식이 아닙니다';
        errorEmail.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 메일 인증 여부
    if (!isSentConfirmCode || !isValidConfirmCode) {
        errorEmail.innerText = '메일이 인증되지 않았습니다';
        errorEmail.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 이메일 중복 여부
    if (isDuplicatedEmail) {
        errorEmail.innerText = '이미 존재하는 이메일입니다';
        errorEmail.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 비밀번호
    if (password.value.trim().length === 0) {
        errorPassword.innerText = '비밀번호는 필수입니다';
        errorPassword.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 비밀번호 형식 일치 여부
    var passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d).{10,}$/;
    if (!passwordRegex.test(password.value.trim())) {
        errorPassword.innerText = '영어와 숫자를 포함하여 10자리 이상이어야 합니다';
        errorPassword.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 비밀번호 확인 여부
    if (rePassword.value.trim().length === 0) {
        errorRePassword.innerText = '비밀번호를 확인해주세요';
        errorRePassword.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 비밀번호 확인 일치 여부
    if (password.value.trim() !== rePassword.value.trim()) {
        errorRePassword.innerText = '비밀번호가 일치하지 않습니다';
        errorRePassword.style.display = 'block';
        return false;
    }
    // 입력한 회원 정보
    var formData = {
        'email': email.value.trim(),
        'password': password.value.trim()
    };

    // 회원 가입
    addMember(formData).then(response => {
        location.replace('/login?register=true');
    })
    .catch(error => {
        // 에러 메시지 출력
        var errorList = error.response.data;
        for (var i = 0; i < errorList.length; i++) {
            if (errorList[i].field === 'email') {
                errorEmail.innerText = errorList[i].message;
                errorEmail.style.display = 'block';
            }
            if (errorList[i].field === 'password') {
                errorPassword.innerText = errorList[i].message;
                errorPassword.style.display = 'block';
            }
        }
    });
});

// 회원 가입 API
async function addMember(formData) {
    var response = await axios.post('http://localhost:8081/api/v1/members', formData, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response;
}