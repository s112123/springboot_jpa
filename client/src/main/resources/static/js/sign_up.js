// 변수 선언
let email = document.getElementById('email');
let confirmCode = document.getElementById('confirm-code');
let password = document.getElementById('password');
let rePassword = document.getElementById('re-password');
let msgSendMail = document.getElementById('send-message');
let msgConfirmCode = document.getElementById('confirm-code-message');
let confirmCodeBox = document.getElementById('confirm-code-box');
let btnSignUp = document.getElementById('btn-sign-up');
let btnSendMail = document.getElementById('btn-send-mail');
let btnConfirmCode = document.getElementById('btn-confirm-code');
let errors = document.querySelectorAll('.error');
let errorEmail = document.getElementById('error-email');
let errorPassword = document.getElementById('error-password');
let errorRePassword = document.getElementById('error-re-password');
let isSentConfirmCode = false;
let isValidConfirmCode = false;
let isDuplicatedEmail = false;

// 이메일 입력 시
email.addEventListener("keyup", () => {
    // 이메일을 입력하고 인증 메일을 보낸 후, 다시 이메일을 입력할 수 있다
    isDuplicatedEmail = false;
    isSentConfirmCode = false;
    confirmCodeBox.style.display = 'none';
    msgSendMail.style.display = 'none';
    msgConfirmCode.style.display = 'none';

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
    let response = await axios.post('http://localhost:8081/api/v1/members/emails/check', email, {
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
    confirmCodeBox.style.display = 'none';

    // 유효성 검사 → 이메일 입력 여부
    if (email.value.trim().length === 0) {
        errorEmail.innerText = '이메일은 필수입니다';
        errorEmail.style.display = 'block';
        return false;
    }
    // 유효성 검사 → 이메일 형식 여부
    let emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email.value.trim())) {
        errorEmail.innerText = '이메일 형식이 아닙니다';
        errorEmail.style.display = 'block';
        return false;
    }

    // 인증 메일 발송
    sendConfirmCodeEmail(email.value.trim()).then(response => {
        // 인증 유무와 상관없이 버튼을 다시 누르면 새로 인증받은 번호로 인증 받아야 한다
        isValidConfirmCode = false;
        isSentConfirmCode = true;
        confirmCodeBox.style.display = 'flex';
        errorEmail.style.display = 'none';
        msgConfirmCode.style.display = 'none';
        msgSendMail.innerText = '인증 메일이 발송되었습니다';
        msgSendMail.style.display = 'block';

        // 원래는 Email 로 받지만 현재는 Email 대신 Console 로 받는다
        // 서버에서 이메일로 보내는 경우, 이 메세지창은 필요없다
        alert(response.data);
    });
});

// 인증 메일 발송 API
async function sendConfirmCodeEmail(email) {
    let response = await axios.post('http://localhost:8081/api/v1/members/codes', email, {
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
        errorEmail.innerText = '인증 코드를 입력하세요';
        errorEmail.style.display = 'block';
        return false;
    }

    // 입력한 회원 정보
    let formData = {
        'email': email.value.trim(),
        'code': confirmCode.value.trim()
    };

    // 인증 확인
    validateConfirmCode(formData).then(response => {
        isValidConfirmCode = true;
        msgConfirmCode.innerText = '인증이 완료되었습니다';
        errorEmail.style.display = 'none';
        msgConfirmCode.style.display = 'block';
        confirmCodeBox.style.display = 'none';
        confirmCode.value = '';
    })
    .catch (error => {
        errorEmail.innerText = error.response.data.message;
        errorEmail.style.display = 'block';
        confirmCode.value = '';
    });
});

// 인증 확인 API
async function validateConfirmCode(formData) {
    let response = await axios.post('http://localhost:8081/api/v1/members/codes/check', formData, {
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
    let emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
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
    let passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d).{10,}$/;
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
    let formData = {
        'email': email.value.trim(),
        'password': password.value.trim()
    };

    // 회원 가입
    addMember(formData).then(response => {
        location.replace('/login?register=true');
    })
    .catch(error => {
        // 에러 메시지 출력
        let errorList = error.response.data;
        for (let i = 0; i < errorList.length; i++) {
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
    let response = await axios.post('http://localhost:8081/api/v1/members', formData, {
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response;
}