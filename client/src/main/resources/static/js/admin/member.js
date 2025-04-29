import { accessTokenUtils } from '/js/common.js';

// 변수 선언
const memberListElement = document.getElementById('admin-members');
const pageableElement = document.getElementById('pageable');

// 처음 페이지 로드 시, 1페이지 화면 렌더링
render(1);

// 회원 목록 화면 렌더링
function render(page) {
    // Access Token 이 없으면 로그인 화면으로 이동
    accessTokenUtils.redirectLoginPage();

    // 회원 목록
    getMembers(page).then((response) => {
        let members = response.data;

        // 회원 목록 렌더링
        let memberListHTML = getMemberListHTML(members.data);
        memberListElement.innerHTML = memberListHTML;

        // 권한 변경 처리
        let memberRoleElements = document.getElementsByClassName('member-roles');
        Array.from(memberRoleElements).forEach((memberRoleElement) => {
            // 현재 저장된 권한
            let currentSelectedRole = memberRoleElement.value;
            // 권한이 변경되면 이벤트 발생
            memberRoleElement.addEventListener('change', (e) => {
                if (confirm(e.target.value + ' 권한으로 변경하시겠습니까?')) {
                    let memberId = memberRoleElement.getAttribute('data-member-id');
                    changeRole(memberId, e.target.value).then((response) => {
                        alert('권한이 변경되었습니다');
                    });
                } else {
                    // 변경 취소하면 기존의 권한 유지
                    memberRoleElement.value = currentSelectedRole;
                }
            });
        });

        // 페이지네이션 렌더링
        let paginationHTML = getPaginationHTML(members);
        pageableElement.innerHTML = paginationHTML;
    });
}

// 회원 목록 조회 API
async function getMembers(page) {
    const api = 'http://localhost:8081/api/v1/members/pages/' + page;
    const response = await axios.get(api, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken()
        }
    });
    return response;
}

// 회원 목록 HTML 생성
function getMemberListHTML(members) {
    let html = '';

    for (let member of members) {
        html += '<tr>';
        html += '    <td>' + member.memberId + '</td>';
        html += '    <td>' + member.email + '</td>';
        html += '    <td>' + member.username + '</td>';

        // 로그인 한 회원과 목록의 회원이 일치하면 본인 권한은 변경할 수 없도록 비활성화
        const isSelf = member.memberId === accessTokenUtils.getMemberId();
        html += '    <td>';
        html += `        <select class="member-roles" ${isSelf ? 'disabled' : ''} data-member-id="${member.memberId}">`;
        html += '            <option value="USER"' + ((member.role === "USER") ? "selected" : "") + '>USER</option>';
        html += '            <option value="ADMIN"' + ((member.role === "ADMIN") ? "selected" : "") + '>ADMIN</option>';
        html += '        </select>';
        html += '    </td>';
        html += '    <td>' + formatDate(member.createdAt) + '</td>';
        html += '    <td>';
        html += '        <a href="javascript:;" onclick="deleteMember(' + member.memberId + ')">';
        html += '            <i class="fa-solid fa-trash-can"></i>';
        html += '        </a>';
        html += '    </td>';
        html += '</tr>'
    }

    return html;
}

// 날짜 형식 변경 (2025-03-26T09:19:57.543247 → 2025-03-26 09:19:57)
function formatDate(current) {
    // 문자열로 처리
    return current.replace('T', ' ').split('.')[0];
}

// 페이지 버튼 HTML 생성
function getPaginationHTML(list) {
    let html = '';

    html += '<ul>';
    // 이전 버튼
    if (list.hasPrev) {
        html += '    <li>';
        html += '        <a href="javascript:;" onclick=render(' + (list.start - 1) + ')>';
        html += '            <i class="fa-solid fa-angle-left"></i>';
        html += '        </a>';
        html += '    </li>';
    }
    // 페이지 번호
    for (let i = list.start; i <= list.end; i++) {
        let active = (i == list.currentPage) ? 'active' : '';
        html += '    <li>';
        html += '        <a href="javascript:;" class="' + active + '" onclick=render(' + i + ')>' + i + '</a>';
        html += '    </li>';
    }
    // 다음 버튼
    if (list.hasNext) {
        html += '    <li>';
        html += '        <a href="javascript:;" onclick=render(' + (list.end + 1) + ')>';
        html += '            <i class="fa-solid fa-angle-right"></i>';
        html += '        </a>';
        html += '    </li>';
    }
    html += '</ul>';

    return html;
}

// 권한 변경 API
async function changeRole(memberId, role) {
    const api = 'http://localhost:8081/api/v1/members/' + memberId + '/roles';
    const response = await axios.patch(api, role, {
        headers: {
            'Authorization': 'Bearer ' + accessTokenUtils.getAccessToken(),
            'Content-Type': 'application/json'
        }
    });
    return response;
}

// 회원 삭제
function deleteMember(memberId) {
    if (confirm(memberId + '번 회원을 삭제하시겠습니까?\n회원의 모든 활동이 삭제되며 복구할 수 없습니다')) {
        alert('회원을 삭제하였습니다 -> API 구현필요');
    }
}

// 현재 member.js 는 모듈 상태이므로 전역 스코프에 render() 와 deleteMember() 를 노출한다
// 그렇지 않으면 Uncaught ReferenceError: renderNext is not defined 에러가 발생한다
window.render = render;
window.deleteMember = deleteMember;
